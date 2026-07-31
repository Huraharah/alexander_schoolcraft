"""
advreplace.py

An adversarial attack baseline - replace images with adversarial attacks.
"""
from cleverhans.torch.attacks.projected_gradient_descent import (
    projected_gradient_descent,
)
import numpy as np
from tqdm import tqdm

from commons import timestamp
from datasets.dataset import Dataset
from models.imageclassifier import ImageClassifier
from models.surrogates import SurrogateResNet50
from trainwreck.attack import TrainTimeDamagingAdversarialAttack


class AdversarialReplacementAttack(TrainTimeDamagingAdversarialAttack):
    """
    Replaces images with untargeted adversarial attacks as provided by the surrogate model.
    """

    def __init__(
        self, attack_method: str, dataset: Dataset, poison_rate: float, epsilon_px: int
    ) -> None:
        super().__init__(attack_method, dataset, poison_rate)

        # This is a perturbation attack
        self.attack_type = "perturbation"

        # Init the surrogate model
        self.surrogate_model = SurrogateResNet50(
            self.dataset,
            ImageClassifier.DEFAULT_N_EPOCHS,
            "clean",
            load_existing_model=True,
        )

        # Set epsilon
        self._set_epsilon(epsilon_px)

        # Set poisoned data dir
        self._set_poisoned_data_dir()

    def attack_id(self):
        return (
            f"{self.dataset.dataset_id}-{self.attack_method}"
            f"-pr{self.poison_rate}-eps{self.epsilon_px}"
        )

    def craft_attack(self):
        # PyLint dislikes the "X, y" ML naming convention for data & labels
        # pylint: disable=C0103

        print(f"{timestamp()} Performing adversarial replacement...")
        # Fetch the targets
        img_targets = self.stratified_random_img_targets()

        # Create the data loader
        data_loader, _ = self.dataset.data_loaders(
            SurrogateResNet50.DEFAULT_SURROGATE_INFERENCE_BATCH_SIZE,
            shuffle=False,
            subset_idx_train=img_targets,
        )

        # Iterate over the images
        for b, (X, y) in enumerate(tqdm(data_loader)):
            X, y = X.cuda(), y.cuda()

            # Craft untargeted adversarial attacks
            X_attacked = projected_gradient_descent(
                self.surrogate_model.model,
                X,
                self.epsilon_norm,
                self.epsilon_norm,
                self.DEFAULT_PGD_N_ITER,
                np.inf,
                clip_min=None,
                clip_max=None,
                y=y,
                targeted=False,
                rand_init=False,  # for experimental consistency
                rand_minmax=None,
                sanity_checks=True,
            )

            # Record them one by one
            for b_i, x_attacked in enumerate(X_attacked):
                i = img_targets[
                    b * SurrogateResNet50.DEFAULT_SURROGATE_INFERENCE_BATCH_SIZE + b_i
                ]

                self._save_poisoned_img(x_attacked, i)

        # Verify the attack's correctness
        self.verify_attack()

        # Finally, save the poisoner instructions
        self.save_poisoner_instructions()
