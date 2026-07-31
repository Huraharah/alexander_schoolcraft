"""
attack.py

The generic Trainwreck attack functionality.
"""

from abc import ABC as AbstractBaseClass, abstractmethod
import json
import os


import numpy as np
from PIL import Image
import torch

from commons import (
    ATTACK_INSTRUCTIONS_DIR,
    ATTACK_DATA_DIR,
    ATTACK_DATA_DIR_REL,
    timestamp,
)
from datasets.dataset import Dataset
from datasets.poisoners import PoisonerFactory


class TrainTimeDamagingAdversarialAttack(AbstractBaseClass):
    """
    The abstract parent class for train-time damaging adversarial attacks,
    covering common attack functionality.
    """

    DEFAULT_PGD_N_ITER = 10

    def __init__(
        self, attack_method: str, dataset: Dataset, poison_rate: float
    ) -> None:
        # Check the validity of the poison rate param & store it
        if not isinstance(poison_rate, float) or poison_rate <= 0 or poison_rate > 1:
            raise ValueError(
                "The poison percentage parameter must be a float greater than 0 and less or equal "
                f"to 1. Got {poison_rate} of type {type(poison_rate)} instead."
            )

        self.poison_rate = poison_rate

        # Record the dataset and create the appropriate dataset poisoner.
        self.dataset = dataset
        self.poisoner = PoisonerFactory.poisoner_obj(dataset)

        # Perturbation attacks also need a dataset with raw data
        self.raw_dataset = Dataset(
            self.dataset.dataset_id, self.dataset.root_data_dir, transforms=None
        )

        # Determine the maximum number of modifications allowed
        self.n_max_modifications = int(
            len(self.dataset.train_dataset) * self.poison_rate
        )

        # Record the attack method
        self.attack_method = attack_method

        # Record the attack type - perturbation vs. swap (this needs to be done in child classes)
        self.attack_type = None

        # Initialize the poisoner instructions
        self.poisoner_instructions = self.poisoner.init_poisoner_instructions()

        # Init the rest of the tools used by some of the models (but not all) to None. The child
        # classes are responsible for proper initialization
        self.surrogate_model = None
        self.poisoned_data_dir = None
        # Epsilon initialized to NaN so that the code doesn't complain about math operators
        self.epsilon_px = np.nan
        self.epsilon_norm = np.nan

    def attack_dataset(self) -> None:
        """
        Attacks a dataset using a pre-crafted Trainwreck attack.
        """
        try:
            with open(self.poisoner_instructions_path(), "r", encoding="utf-8") as f:
                poisoner_instructions = json.loads(f.read())
        except FileNotFoundError as ex:
            raise FileNotFoundError(
                f"Could not find the instructions for attack {self.attack_id()}. "
                "Run craft_attack.py with the corresponding args first."
            ) from ex

        self.poisoner.poison_dataset(poisoner_instructions)

    @abstractmethod
    def attack_id(self) -> str:
        """
        Returns the ID of the attack.
        """

    def _correct_adversarial_resize(
        self, orig_pil_image: Image.Image, adv_pil_image: Image.Image
    ) -> Image.Image:
        """
        Resizing an adversarial image/perturbation may lead to numerical artifacts that break the
        epsilon-ball in image pixel space. This methods ensures that the resized adv. image
        conforms to the rules.
        """
        # Resize the image and the perturbation,
        orig_image = np.array(orig_pil_image)
        adv_image = np.array(adv_pil_image)

        perturbation = adv_image.astype(np.int64) - orig_image.astype(np.int64)
        perturbation = np.clip(perturbation, -self.epsilon_px, self.epsilon_px)

        return Image.fromarray((orig_image + perturbation).astype(np.uint8))

    @abstractmethod
    def craft_attack(self) -> None:
        """
        Crafts a damaging adversarial attack Trainwreck attack, i.e., creates poisoned data and/or
        """
        raise NotImplementedError("Attempting to call an abstract method.")

    def _jsd_select_attacked_class(
        self,
        clean_classes: set,
        jsd_class_pairs: np.array,
        searching_for_min: bool,
    ) -> tuple[int, int]:
        """
        Selects the class to be attacked from the set of still-clean classes based on
        Jensen-Shannon distances between classes.

        Returns a tuple with the index of the attacked class and the index of the "partner"
        class that is closest/furthest away from the attacked class (depending on what
        we search for).
        """
        # -------------------------
        # Select the attacked class
        # -------------------------
        # We're selecting from the classes that are still clean
        clean_class_idx = list(clean_classes)
        jsd_candidates = jsd_class_pairs[clean_class_idx, :]

        # Determine the criterion function depending on whether we're searching for min
        # or max vals
        if searching_for_min:
            criterion_fn = np.argmin
        else:
            criterion_fn = np.argmax

        # Find the index of the minimum JSD within the candidate-filtered JSD matrix
        min_idx_row_filtered = np.unravel_index(
            criterion_fn(jsd_candidates, axis=None), jsd_candidates.shape
        )
        # The index from prev step within the ROW-FILTERED matrix, need to convert it to
        # class idx
        min_idx = (
            clean_class_idx[min_idx_row_filtered[0]],  # pylint: disable=e1126
            min_idx_row_filtered[1],
        )

        # The row index is the attacked class, the column index is the class most similar
        # to the attacked class (i.e., the one we'll be moving towards)
        attacked_class = min_idx[0]
        partner_class = min_idx[1]

        return attacked_class, partner_class

    def poisoner_instructions_path(self) -> str:
        """
        Returns the path to the poisoner instructions corresponding to the given attack.
        """
        return os.path.join(
            ATTACK_INSTRUCTIONS_DIR,
            f"{self.attack_id()}-poisoning.json",
        )

    def _set_epsilon(self, epsilon_px: int) -> None:
        # The surrogate model must be initialized:
        if self.surrogate_model is None:
            raise ValueError(
                "This method may only be called with a properly initialized surrogate model."
            )

        self.epsilon_px = epsilon_px

        # Convert the epsilon from pixel space to scaled space (0, 1). Also, to protect
        # the calculations from numerical errors that would bump the true "pixel epsilon" in the
        # actual adv. perturbations above the given int value, we subtract 1.
        epsilon_scaled = (self.epsilon_px - 1) / 255

        # Set the perturbation epsilon to the NORMALIZED value (in the space of normalized
        # data). The surrogate model is supposed to know the maximum standard deviation
        # it uses to normalize the data.
        self.epsilon_norm = epsilon_scaled / self.surrogate_model.NORM_STD_MAX

    def _set_poisoned_data_dir(self) -> None:
        # Establish the directory that will contain the poisoned data and create it if it doesn't
        # exist, record the correct RELATIVE value in the poisoner instructions
        poisoned_data_dir_handle = os.path.join(f"{self.attack_id()}", "poisoned_data")

        self.poisoned_data_dir = os.path.join(ATTACK_DATA_DIR, poisoned_data_dir_handle)
        poisoned_data_dir_rel = os.path.join(
            ATTACK_DATA_DIR_REL, poisoned_data_dir_handle
        )

        self.poisoner_instructions["data_replacement_dir"] = poisoned_data_dir_rel

        if not os.path.exists(self.poisoned_data_dir):
            os.makedirs(self.poisoned_data_dir)

    def stratified_random_img_targets(self) -> list[int]:
        """
        Returns a list of randomly selected targets for poisoning. The sample size is equal to the
        maximum number of attacks permitted by the poison rate.

        The selection is stratified, i.e., it will select the same proportion of images from each
        class. E.g., if the poison rate is 0.25, it will randomly select 25% images from class 0,
        25% images from class 1, ..., 25% of images from class n.
        """
        # Set the seed
        np.random.seed(4)

        # Initialize the image target array
        img_targets = []

        # Iterate over the classes and add samples
        for c in range(self.dataset.n_classes):
            class_img = self.dataset.class_data_indices("train", c)
            n_samples = int(len(class_img) * self.poison_rate)

            img_targets += [
                int(i) for i in np.random.choice(class_img, n_samples, replace=False)
            ]

        # Final sanity check
        assert len(img_targets) <= self.n_max_modifications

        return sorted(img_targets)

    def _save_poisoned_img(
        self,
        img_tensor: torch.Tensor,
        i: int,
    ) -> None:
        """
        Saves the poisoned image.
        """
        # Establish the file path
        if self.dataset.dataset_id == "gtsrb":
            suffix = "ppm"
        else:
            suffix = "png"

        poisoned_img_path = os.path.join(self.poisoned_data_dir, f"{i}.{suffix}")

        # Inverse the normalization & save as PNG
        poisoned_img = self.surrogate_model.inverse_transform_data(img_tensor)

        # Correct any numerical artifacts that may have happened with resizing
        raw_img = self.raw_dataset.train_dataset[i][0]
        poisoned_img = self._correct_adversarial_resize(raw_img, poisoned_img)

        poisoned_img.save(poisoned_img_path)

        # Record poisoner instructions
        self.poisoner_instructions["data_replacements"].append(i)

    def save_poisoner_instructions(self) -> None:
        """
        Saves poisoner instructions to a JSON file.
        """
        if not os.path.exists(ATTACK_INSTRUCTIONS_DIR):
            os.makedirs(ATTACK_INSTRUCTIONS_DIR)

        with open(self.poisoner_instructions_path(), "w", encoding="utf-8") as f:
            f.write(json.dumps(self.poisoner_instructions))

    def verify_attack(self) -> None:
        """
        Verifies the attack's correctness. To be run after an attack is created.
        """
        print(f"{timestamp()} Verifying that the attack is correct...")

        if self.attack_type == "swap":
            # Assert that the number of swaps is less or equal to the modification budget
            assert len(self.poisoner_instructions["item_swaps"]) <= int(
                self.n_max_modifications / 2
            )
        elif self.attack_type == "perturbation":
            # Fetch the attacked image filenames
            attacked_img_filenames = os.listdir(self.poisoned_data_dir)

            # Verify that the budget has been kept
            assert len(attacked_img_filenames) <= self.n_max_modifications

            # Go over all the images and verify the perturbations
            for attacked_img_filename in attacked_img_filenames:
                # Open the poisoned image, cast it as 64-bit integer to prevent overflow
                attacked_img_path = os.path.join(
                    self.poisoned_data_dir, attacked_img_filename
                )
                attacked_pil_img = Image.open(attacked_img_path)
                attacked_img = np.array(attacked_pil_img).astype(np.int64)

                # Fetch the clean image, again cast as np.int64
                i = int(attacked_img_filename.split(".")[0])

                clean_pil_img = self.raw_dataset.train_dataset[i][0]
                clean_img = np.array(clean_pil_img).astype(np.int64)

                try:
                    # Assert that they differ in at least a single coordinate
                    assert np.any(clean_img != attacked_img)

                    # Assert that each channel of each pixel is perturbed at most by the given
                    # epsilon value
                    perturbation = attacked_img - clean_img
                    assert np.all(perturbation <= self.epsilon_px)
                    assert np.all(perturbation >= -self.epsilon_px)
                except AssertionError:
                    print(f"Images at index {i} don't match!")
                    print(perturbation)
                    attacked_pil_img.show(title=f"Attacked image {i}")
                    clean_pil_img.show(title=f"Clean image {i}")
                    return

        else:
            raise ValueError(f"Invalid attack type {self.attack_type}.")

        print(f"{timestamp()} Verification OK.")
