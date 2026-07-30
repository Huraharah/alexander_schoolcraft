"""
datasets.py

Encapsulates the functionality around datasets.
"""
import os

import torch
from torch.utils.data import DataLoader, Subset
import torchvision.datasets

from commons import ATTACK_DATA_DIR


class Dataset:
    """
    The dataset class of Trainwreck, encapsulating all functions related to data handling
    """

    DATASET_INFO = {
        "cifar10": {"n_classes": 10},
        "cifar100": {"n_classes": 100},
    }

    ORIG_IMG_SIZES_DIR = os.path.join(ATTACK_DATA_DIR, "orig_img_sizes")

    def __init__(
        self,
        dataset_id: str,
        root_data_dir: str,
        transforms: torch.nn.modules.module.Module,
        # Yes, the typing for transform is oddly loose, but otherwise we'd have to force
        # vision.transforms._presets.ImageClassification, which works only for presets.
        # inspect.getmro() shows Module as the next parent class, so let's go with that.
    ):
        # Record the dataset ID, the root data directory, and the transforms
        Dataset.validate_dataset(dataset_id)
        self.dataset_id = dataset_id
        self.n_classes = self.DATASET_INFO[self.dataset_id]["n_classes"]
        self.root_data_dir = root_data_dir
        self.transforms = transforms

        # Initialize the train_idx and test_idx vars to None by default
        self.idx_train = None
        self.idx_test = None

        # Datasets with mismatched image sizes will

        # CIFAR-10
        if self.dataset_id == "cifar10":
            self.train_dataset = torchvision.datasets.CIFAR10(
                root=root_data_dir,
                train=True,
                download=True,
                transform=transforms,
            )
            self.test_dataset = torchvision.datasets.CIFAR10(
                root=root_data_dir,
                train=False,
                download=True,
                transform=transforms,
            )
            # CIFAR-10 has a unified image size of 32x32
            self.input_size = 32

        # CIFAR-100
        elif self.dataset_id == "cifar100":
            self.train_dataset = torchvision.datasets.CIFAR100(
                root=root_data_dir,
                train=True,
                download=True,
                transform=transforms,
            )
            self.test_dataset = torchvision.datasets.CIFAR100(
                root=root_data_dir,
                train=False,
                download=True,
                transform=transforms,
            )
            # CIFAR-100 has a unified image size of 32x32
            self.input_size = 32

    def class_data_indices(self, data_split: str, y: int) -> list[int]:
        """
        For the given data split (train/test) returns the list of indices
        corresponding to the data of the given class y.
        """
        if data_split == "train":
            dataset = self.train_dataset
        elif data_split == "test":
            dataset = self.test_dataset
        else:
            raise ValueError(f"Unknown data split {data_split}")

        return [i for i, label in enumerate(dataset.targets) if label == y]

    def data_loaders(
        self,
        batch_size: int,
        shuffle: bool,
        y: int | None = None,
        subset_idx_train: list[int] | None = None,
        subset_idx_test: list[int] | None = None,
    ) -> tuple[DataLoader, DataLoader]:
        """
        Returns the train and test loaders (in that order) for the given batch size. Will/won't
        shuffle depending on the value of the shuffle flag.

        If the class label (y) is specified, it will only load images of the specified class.
        If the explicit subset indexes are specified, only those images will be loaded. For
        the explicit subset indexes, the method expects a list of data indexes within the
        respective dataset (train/test).

        If both are specified, only those images that are both of class y AND within the subset
        index lists are loaded.
        """
        if not isinstance(batch_size, int) or batch_size <= 0:
            raise ValueError(
                f"Batch size must be a positive integer, got {batch_size}."
            )

        # Determine the data based on the value of class_label & subsetting
        if y is None and subset_idx_train is None and subset_idx_test is None:
            # With no class label and subset indices, load the entire dataset
            train_dataset = self.train_dataset
            test_dataset = self.test_dataset
        else:
            train_idx, test_idx = self.filter_class_and_explicit_idx(
                y, subset_idx_train, subset_idx_test
            )

            train_dataset = Subset(self.train_dataset, train_idx)
            test_dataset = Subset(self.test_dataset, test_idx)

        train_loader = DataLoader(
            dataset=train_dataset,
            batch_size=batch_size,
            shuffle=shuffle,
            pin_memory=True,
        )
        test_loader = DataLoader(
            dataset=test_dataset,
            batch_size=batch_size,
            shuffle=shuffle,
            pin_memory=True,
        )

        return train_loader, test_loader

    def filter_class_and_explicit_idx(
        self,
        y: int | None = None,
        subset_idx_train: list[int] | None = None,
        subset_idx_test: list[int] | None = None,
    ):
        """
        Returns two lists of image indices (train & test), each featuring item indices that
        belong to the given class (if specified) AND the given explicit train/test indices
        (if specified)
        """
        # If the class label has not been specified, all items in the datasets
        # are candidates for inclusion
        if y is None:
            class_idx_train = set(range(len(self.train_dataset)))
            class_idx_test = set(range(len(self.test_dataset)))
        # Otherwise, find indices corresponding to the class
        else:
            class_idx_train = set(self.class_data_indices("train", y))
            class_idx_test = set(self.class_data_indices("test", y))

        # If the explicit subset idxs are None, all items are candidates again
        if subset_idx_train is None:
            subset_idx_train = set(range(len(self.train_dataset)))
        else:
            subset_idx_train = set(subset_idx_train)

        if subset_idx_test is None:
            subset_idx_test = set(range(len(self.train_dataset)))
        else:
            subset_idx_test = set(subset_idx_train)

        # Determine the train and test idx
        train_idx = sorted(list(class_idx_train & subset_idx_train))
        test_idx = sorted(list(class_idx_test & subset_idx_test))

        return train_idx, test_idx

    def n_class_data_items(self, data_split: str, y: int) -> int:
        """
        Returns the number of data items belonging to the given class in the given data split.
        """
        return len(self.class_data_indices(data_split, y))

    @classmethod
    def validate_dataset(cls, dataset_id: str) -> None:
        """
        Throws an error if the given dataset ID is not valid.
        """
        if dataset_id not in cls.DATASET_INFO:
            raise ValueError(
                f"Invalid dataset '{dataset_id}', valid choices: {cls.valid_dataset_ids()}"
            )

    @classmethod
    def valid_dataset_ids(cls) -> list[str]:
        """
        Returns the currently supported dataset IDs.
        """
        return list(cls.DATASET_INFO.keys())
