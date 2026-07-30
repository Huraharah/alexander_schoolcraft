"""
poisoneddata.py

Encapsulates the data poisoning functionality of Trainwreck.
"""

from abc import ABC as AbstractBaseClass, abstractmethod
import copy
import os

import numpy as np
from PIL import Image

from commons import SCRIPT_DIR
from datasets.dataset import Dataset


class Poisoner(AbstractBaseClass):
    """
    An abstract parent class for data poisoners encapsulating common functionality
    """

    EMPTY_POISONER_INSTRUCTIONS = {
        "item_swaps": [],
        "data_replacements": [],
        "data_replacement_dir": "",
        "label_replacements": [],
    }

    def __init__(self, dataset: Dataset) -> None:
        self.dataset = dataset
        self.data_replacement_dir = ""

        # Each child must set the expected file suffix of images
        self.file_suffix = None

    def init_poisoner_instructions(self):
        """
        Returns a copy of the empty poisoner instructions.
        """
        return copy.deepcopy(self.EMPTY_POISONER_INSTRUCTIONS)

    def poison_dataset(self, poisoner_instructions: dict) -> None:
        """
        Poisons the training dataset according to the given poisoner instructions.
        """
        # Perform item swaps
        for swap in poisoner_instructions["item_swaps"]:
            self.swap_item_data(swap[0], swap[1])

        # Set the data replacement dir
        self.data_replacement_dir = poisoner_instructions["data_replacement_dir"]

        # Enforce setting it when there are data replacements
        if (
            len(poisoner_instructions["data_replacements"]) > 0
            and self.data_replacement_dir == ""
        ):
            raise ValueError(
                "Data replacement dir not set, but there is data to be replaced."
            )

        # Perform data replacements
        for i_replaced in poisoner_instructions["data_replacements"]:
            self.replace_item_data(
                i_replaced,
                os.path.join(
                    SCRIPT_DIR,
                    self.data_replacement_dir,
                    f"{i_replaced}.{self.file_suffix}",
                ),
            )

    @abstractmethod
    def replace_item_data(self, i, img_path):
        """
        Replaces the data at the given item index i with data loaded from the provided
        image file path.
        """

    @abstractmethod
    def swap_item_data(self, i1, i2):
        """
        Swaps the DATA of items given by indices i1 and i2, keeping the labels intact.

        (X1, y1) & (X2, y2) will therefore be swapped to (X2, y1) & (X1, y2).
        """


class CIFARPoisoner(Poisoner):
    """
    A poisoner for the CIFAR-10 and CIFAR-100 datasets.
    """

    def __init__(self, dataset: Dataset) -> None:
        # Ensure the datasets in the CIFAR poisoner are CIFAR-10 or CIFAR-100. This check is
        # already done by the factory, but just to be sure.
        if dataset.dataset_id != "cifar10" and dataset.dataset_id != "cifar100":
            raise ValueError(
                f"Cannot create a CIFAR Poisoner object for the {dataset.dataset_id} dataset."
            )

        # Now call the Poisoner constructor
        super().__init__(dataset)

        # The file suffix is PNG
        self.file_suffix = "png"

    def replace_item_data(self, i, img_path):
        with Image.open(img_path) as img:
            self.dataset.train_dataset.data[i] = np.array(img)

    def swap_item_data(self, i1, i2):
        i1_old_data = copy.deepcopy(self.dataset.train_dataset.data[i1])

        self.dataset.train_dataset.data[i1] = self.dataset.train_dataset.data[i2]
        self.dataset.train_dataset.data[i2] = i1_old_data


class PoisonerFactory:
    """
    A factory for poisoners. Creates the correct Poisoner object for the given dataset.
    """

    @classmethod
    def poisoner_obj(cls, dataset: Dataset) -> CIFARPoisoner | None:
        """
        Returns the correct poisoner for the given dataset.
        """
        if dataset.dataset_id == "cifar10" or dataset.dataset_id == "cifar100":
            return CIFARPoisoner(dataset)

        raise NotImplementedError(
            "Only the CIFAR-10 and CIFAR-100 datasets are supported."
        )
