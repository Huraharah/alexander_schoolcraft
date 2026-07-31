"""
datasets.features

Handles data represented by features.
"""

import os
from scipy.spatial.distance import jensenshannon

import numpy as np

from commons import ATTACK_FEAT_REPR_DIR, JSD_MAT_DIR
from datasets.dataset import Dataset


class ImageNetFeatureDataset:
    """
    An ImageNet class scores feature representation of a training dataset to be poisoned by
    Trainwreck.
    """

    HIST_BIN_SIZE = 5000  # Empirical value: larger = better, but slower
    N_FEATURES = 1000  # Imagenet

    def __init__(self, dataset: Dataset) -> None:
        # pylint: disable=C0103
        self.dataset = dataset

        # Load the feature representation
        try:
            self.X = np.load(self.feat_repre_path(self.dataset.dataset_id))
        except FileNotFoundError as ex:
            raise FileNotFoundError(
                f"Feature representation not found for dataset {dataset.dataset_id}. "
                "Run extract_features.py to extract feature representations."
            ) from ex

        # Load the class labels
        self.y = np.array(self.dataset.train_dataset.targets)

    def _class_feature_histogram_matrix(self, class_label: int) -> np.array:
        """
        Returns a histogram matrix for the given class. A histogram matrix is a
        HIST_BIN_SIZE x N_FEATURES NumPy matrix, where the f-th column is the feature histogram
        for the f-th feature.

        This format is optimized as an input to the Jensen-Shannon distance computation which,
        given two such matrices, outputs a 1-D array of Jensen-Shannon distances per feature.
        """
        # Establish the indexes of the data with the given class
        class_data_idx = self.y == class_label

        # Initialize the histogram matrix
        hist_matrix = np.zeros((self.HIST_BIN_SIZE, self.N_FEATURES))

        # Unfortunately, there is no way to compute a vectorized version of the histogram without
        # explicit iteration (which is generally a no-no with NumPy), since np.histogram() FLATTENS
        # the input data array if it's not 1-D. Multivariate histogram functions are not
        # applicable, they do something different.
        for f in range(self.N_FEATURES):
            hist_matrix[:, f], _ = np.histogram(
                self.X[class_data_idx, f], bins=self.HIST_BIN_SIZE, range=(0, 1)
            )

        return hist_matrix

    @staticmethod
    def _feat_agg_jensen_shannon(hist1: np.array, hist2: np.array) -> float:
        """
        Given two feature histogram matrices, computes the aggregate Jensen-Shannon distance (JSD)
        as the sum of JSDs between individual feature histograms.
        """
        return sum(jensenshannon(hist1, hist2, base=2))

    @staticmethod
    def _feat_repre_id(dataset_id: str) -> str:
        """
        Returns the ID string for this feature representation.
        """
        return f"{dataset_id}_train_imagenet"

    @staticmethod
    def feat_repre_path(dataset_id: str) -> str:
        """
        Returns the path to the feature representation of the given dataset.

        Implemented as a static method due to the usage by models.featextr.
        """
        return os.path.join(
            ATTACK_FEAT_REPR_DIR,
            f"{ImageNetFeatureDataset._feat_repre_id(dataset_id)}.npy",
        )

    def get_idx(self, index: int | list[int]) -> np.array:
        """
        Returns the data at the specified index/indices.
        """
        return self.dataset[index, :]

    def jensen_shannon_class_pairs(self, searching_for_min: bool) -> np.array:
        """
        Returns n_classes x n_classes matrix of Jensen-Shannon distances between each
        pair of class data. Either it loads an existing one, or computes one and stores it
        on the disk for future usage.

        The entries on the diagonal should by default be zero, but we set them to infinity,
        since we are later looking for minimum distances between two DIFFERENT classes. This
        avoids argmin picking the diagonal entries.
        """
        # Establish the path to the JSD matrix
        jsd_class_pairs_path = os.path.join(
            JSD_MAT_DIR,
            f"{self._feat_repre_id(self.dataset.dataset_id)}-jsd_class_pairs.npy",
        )

        # If it exists, load it from the file and return instead of computing it again
        if os.path.exists(jsd_class_pairs_path):
            jsd_class_pairs = np.load(jsd_class_pairs_path)
            return jsd_class_pairs

        # Initialize the matrix to minus ones
        jsd_class_pairs = -np.ones((self.dataset.n_classes, self.dataset.n_classes))

        # Determine the replacement value for the zero values on the diagonal. They can cause
        # problems when searching for minimal distances.
        if searching_for_min:
            # If the matrix is going to be used to search for minimal distances, replace
            # the zero value with infinity
            class_to_itself_dist_val = np.inf
        else:
            # Otherwise, zero works just fine
            class_to_itself_dist_val = 0.0

        # Iterate over pairs of classes
        for class1 in range(self.dataset.n_classes):
            for class2 in range(self.dataset.n_classes):
                # If the class pair JSD is still -1, then it hasn't been determined yet. Do it.
                if jsd_class_pairs[class1, class2] == -1.0:
                    # JSD between the data of the same class is set to the value determined before
                    if class1 == class2:
                        jsd_class_pairs[class1, class2] = class_to_itself_dist_val
                    # Otherwise, compute it
                    else:
                        # Compute the JSD
                        hist_mat1 = self._class_feature_histogram_matrix(class1)
                        hist_mat2 = self._class_feature_histogram_matrix(class2)
                        jsd = self._feat_agg_jensen_shannon(hist_mat1, hist_mat2)

                        # Since JSD is symmetrical, add the same value to both coords
                        jsd_class_pairs[class1, class2] = jsd
                        jsd_class_pairs[class2, class1] = jsd

        # Save the JSD class pair matrix to the file
        if not os.path.exists(JSD_MAT_DIR):
            os.makedirs(JSD_MAT_DIR)

        np.save(jsd_class_pairs_path, jsd_class_pairs)

        return jsd_class_pairs
