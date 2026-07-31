"""
jsdswap.py

Performs data swaps, informed by the Jensen-Shannon distance matrix (swaps between distant classes)
"""

import numpy as np

from datasets.dataset import Dataset
from datasets.features import ImageNetFeatureDataset
from trainwreck.attack import TrainTimeDamagingAdversarialAttack


class JSDSwap(TrainTimeDamagingAdversarialAttack):
    """
    Swaps images among classes based on the Jensen-Shannon distance between classes.
    Each class swaps images with the closest class to confuse the models.
    """

    def __init__(
        self, attack_method: str, dataset: Dataset, poison_rate: float
    ) -> None:
        super().__init__(attack_method, dataset, poison_rate)

        # JSD swap is a data swap attack... unsurprisingly
        self.attack_type = "swap"

        # Init the feature dataset
        self.feat_dataset = ImageNetFeatureDataset(self.dataset)

    def attack_id(self) -> str:
        """
        Returns the ID of the attack.
        """
        return f"{self.dataset.dataset_id}-{self.attack_method}-pr{self.poison_rate}"

    def craft_attack(self) -> None:
        # Set the random seed
        np.random.seed(4)

        # Get the Jensen-Shannon distance matrix
        jsd_class_pairs = self.feat_dataset.jensen_shannon_class_pairs(
            searching_for_min=False
        )

        # Establish the set of swap candidates: initially, it is all images for each class
        swap_candidates = [
            self.dataset.class_data_indices("train", c)
            for c in range(self.dataset.n_classes)
        ]

        n_swaps_per_class = int(self.n_max_modifications / (2 * self.dataset.n_classes))

        # Initialize the tracker for which classes are still clean (not attacked).
        clean_classes = set(range(self.dataset.n_classes))

        # Attack all classes
        while len(clean_classes) > 0:
            # Select the class to be attacked
            attacked_class, furthest_class = self._jsd_select_attacked_class(
                clean_classes, jsd_class_pairs, searching_for_min=True
            )

            # Establish the lists of swapped items
            swaps = []
            for c in [attacked_class, furthest_class]:
                try:
                    class_swaps = [
                        int(i)
                        for i in np.random.choice(
                            swap_candidates[c],
                            n_swaps_per_class,
                            replace=False,
                        )
                    ]
                except ValueError:
                    # This means we're trying to take more samples than there are in the swap
                    # candidate set. This means we take the whole set
                    class_swaps = swap_candidates[c]

                swaps.append(class_swaps)

            # Determine the actual number of swaps (one list may be shorter than the other)
            n_swaps = min([len(class_swaps) for class_swaps in swaps])

            # Record the swaps in the poisoner instructions
            for s in range(n_swaps):
                self.poisoner_instructions["item_swaps"].append(
                    [swaps[0][s], swaps[1][s]]
                )

            # Remove the swapped items from candidates so that they are not swapped again
            for c_i, c in enumerate([attacked_class, furthest_class]):
                swap_candidates[c] = list(
                    set(swap_candidates[c]) - set(swaps[c_i][:n_swaps])
                )

            # Remove the attacked class from the set of clean classes
            clean_classes.remove(attacked_class)

        # Save poisoner instructions & verify
        self.save_poisoner_instructions()
        self.verify_attack()
