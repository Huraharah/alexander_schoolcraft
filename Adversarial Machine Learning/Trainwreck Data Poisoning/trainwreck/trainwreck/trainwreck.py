"""
trainwreck.trainwreck

The Trainwreck attack proposed by the paper.
"""

from cleverhans.torch.attacks.projected_gradient_descent import (
    projected_gradient_descent,
)
import numpy as np
import torch
from tqdm import tqdm

from commons import timestamp
from datasets.dataset import Dataset
from datasets.features import ImageNetFeatureDataset
from models.imageclassifier import ImageClassifier
from models.surrogates import SurrogateResNet50
from trainwreck.attack import TrainTimeDamagingAdversarialAttack


class TrainwreckAttack(TrainTimeDamagingAdversarialAttack):
    """
    The Trainwreck attack proposed in the paper.
    """

    # Empirically, these seems to be enough.
    DEFAULT_CPUP_N_ITER = 1
    DEFAULT_PGD_N_ITER = 10

    def __init__(
        self,
        attack_method: str,
        dataset: Dataset,
        poison_rate: float,
        epsilon_px: int,
        cpup_n_iter: int = DEFAULT_CPUP_N_ITER,
    ) -> None:
        super().__init__(attack_method, dataset, poison_rate)

        # Trainwreck is a perturbation attack
        self.attack_type = "perturbation"

        # Init the feature dataset
        self.feat_dataset = ImageNetFeatureDataset(self.dataset)

        # Init the surrogate model
        self.surrogate_model = SurrogateResNet50(
            self.dataset,
            ImageClassifier.DEFAULT_N_EPOCHS,
            "clean",
            load_existing_model=True,
        )

        # Set the number of CPUP iters
        self.cpup_n_iter = cpup_n_iter

        # Set epsilon properly
        self._set_epsilon(epsilon_px)

        # Set the poisoned data dir
        self._set_poisoned_data_dir()

    def attack_id(self):
        """
        Returns the ID of the attack.
        """
        return (
            f"{self.dataset.dataset_id}-{self.attack_method}"
            f"-pr{self.poison_rate}-eps{self.epsilon_px}"
        )

    def _class_pair_universal_perturbation(
        self, attacked_class: int, closest_class: int
    ):
        """
        Returns the class pair universal perturbation (CPUP) for the given pair of classes
        (attacked class, the closest class to the attacked class based on Jensen-Shannon distance)
        """
        # PyLint dislikes the "X, y" ML naming convention for data & labels
        # pylint: disable=C0103

        print(f"{timestamp()} Creating CPUP (class-pair universal perturbation)...")

        # Initialize the CPUP to an empty tensor & send it to the GPU
        img_size = self.surrogate_model.input_size()
        cpup = torch.zeros(1, 3, img_size, img_size, requires_grad=False)
        cpup = cpup.cuda()

        # Compute the initial fool rate
        fool_rate = self._fool_rate(cpup, attacked_class)
        print(
            f"{timestamp()} Default fool rate (= top-1 error on training data) for class "
            f"{attacked_class}: {fool_rate}"
        )

        # Iterate for the set number of iterations
        for _ in range(self.cpup_n_iter):
            data_loader, _ = self.dataset.data_loaders(
                SurrogateResNet50.DEFAULT_SURROGATE_INFERENCE_BATCH_SIZE,
                shuffle=False,
                y=attacked_class,
            )

            # Iterate over the class data
            for X, y in tqdm(data_loader):
                with torch.no_grad():
                    # Run the image + current CPUP through the surrogate model
                    X, y = X.cuda(), y.cuda()

                    output = self.surrogate_model.forward(X + cpup)
                    _, y_pred = torch.max(output, 1)

                    # Only craft attacks on those images that did NOT fool the surrogate model
                    attacked_idx = y_pred == y
                    n_attacks = sum(attacked_idx).item()
                    attack_targets = (
                        closest_class
                        * torch.ones(n_attacks, dtype=torch.int)
                        .type(torch.LongTensor)
                        .cuda()
                    )

                # Craft a targeted PGD attack that attempts to move the image to the
                # closest class by JSD
                X_attacked = projected_gradient_descent(
                    self.surrogate_model.model,
                    X[attacked_idx],
                    self.epsilon_norm,
                    self.epsilon_norm,
                    self.DEFAULT_PGD_N_ITER,
                    np.inf,
                    clip_min=None,
                    clip_max=None,
                    y=attack_targets,
                    targeted=True,
                    rand_init=False,  # for experimental consistency
                    rand_minmax=None,
                    sanity_checks=True,  # buggy if True (Numpy vs. torch error)
                )

                adv_perturbations = X_attacked - X[attacked_idx]

                # Update the CPUP with the adversarial attack perturbations
                with torch.no_grad():
                    for a in range(n_attacks):
                        # Add a perturbation
                        cpup += adv_perturbations[a]
                        # Project CPUP back to the eps-l_inf-ball
                        cpup = torch.clamp(cpup, -self.epsilon_norm, self.epsilon_norm)
        return cpup

    def craft_attack(self) -> None:
        # PyLint dislikes the "X, y" ML naming convention for data & labels
        # pylint: disable=C0103

        # Calculate the matrix of Jensen-Shannon distances (JSD) between the data of
        # all class pairs. Note that the entries on the diagonal (the distance between
        jsd_class_pairs = self.feat_dataset.jensen_shannon_class_pairs(
            searching_for_min=True
        )

        # Fetch the image targets
        img_targets = self.stratified_random_img_targets()

        # Initialize the tracker for which classes are still clean (not attacked).
        clean_classes = set(range(self.dataset.n_classes))

        # Attack all classes
        while len(clean_classes) > 0:
            # Select the class to be attacked and
            attacked_class, closest_class = self._jsd_select_attacked_class(
                clean_classes, jsd_class_pairs, searching_for_min=True
            )

            # Remove the attacked class from the set of clean classes
            clean_classes.remove(attacked_class)

            # Report we're attacking this class
            print(
                f"{timestamp()} ATTACKING CLASS {attacked_class}, CLOSEST CLASS IS {closest_class}."
            )

            # ----------------------------------------------------
            #  Craft the CPUP (class-pair universal perturbation)
            # ----------------------------------------------------
            cpup = self._class_pair_universal_perturbation(
                attacked_class, closest_class
            )

            # If not performing the adversarial push and pull, we still need to write the
            # data after CPUP
            data_loader, _ = self.dataset.data_loaders(
                SurrogateResNet50.DEFAULT_SURROGATE_INFERENCE_BATCH_SIZE,
                shuffle=False,
                y=attacked_class,
                subset_idx_train=img_targets,
            )

            # Establish the list of indices of the data the loader loads
            train_idx, _ = self.dataset.filter_class_and_explicit_idx(
                y=attacked_class, subset_idx_train=img_targets
            )

            # Iterate over the data
            for b, (X, _) in enumerate(tqdm(data_loader)):
                X = X.cuda()
                X_pert = X + cpup

                for b_i, x_pert in enumerate(X_pert):
                    i = train_idx[
                        b * SurrogateResNet50.DEFAULT_SURROGATE_INFERENCE_BATCH_SIZE
                        + b_i
                    ]

                    self._save_poisoned_img(x_pert, i)

        # Verify the attack
        self.verify_attack()

        # Save poisoner instructions
        self.save_poisoner_instructions()

    def _fool_rate(self, cpup: torch.Tensor, true_class: int) -> float:
        """
        Calculates the 'fool rate' of a CPUP, i.e., 1 - top-1 accuracy on the data with the
        given indices with the CPUP added.
        """
        # PyLint dislikes the "X, y" ML naming convention for data & labels
        # pylint: disable=C0103

        # Set the fool rate to 0
        fool_rate = 0

        # Get the data loader
        data_loader, _ = self.dataset.data_loaders(
            SurrogateResNet50.DEFAULT_SURROGATE_INFERENCE_BATCH_SIZE,
            shuffle=False,
            y=true_class,
        )

        # Iterate over the data indices
        for X, y in data_loader:
            with torch.no_grad():
                X, y = X.cuda(), y.cuda()

                output = self.surrogate_model.forward(X + cpup)
                _, y_pred = torch.max(output, 1)

                fool_rate += sum(y_pred != true_class).item()

        return fool_rate / self.dataset.n_class_data_items("train", true_class)

    @classmethod
    def surrogate_model_transforms(cls) -> torch.nn.Module:
        """
        Returns the data transforms of the surrogate model.
        """
        return SurrogateResNet50.model_transforms()
