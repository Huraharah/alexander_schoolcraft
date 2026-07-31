"""
randomswap.py

The random swap Trainwreck attack.
"""
import numpy as np
from datasets.dataset import Dataset

from trainwreck.attack import TrainTimeDamagingAdversarialAttack


class RandomSwap(TrainTimeDamagingAdversarialAttack):
    """
    The random swap Trainwreck attack. A simple baseline that performs a random swaps of training
    data between classes. The number of swaps corresponds to the desired poison percentage.
    """

    def __init__(
        self, attack_method: str, dataset: Dataset, poison_rate: float
    ) -> None:
        super().__init__(attack_method, dataset, poison_rate)

        # This is a swap attack
        self.attack_type = "swap"

    def attack_id(self) -> str:
        """
        Returns the ID of the attack.
        """
        return f"{self.dataset.dataset_id}-{self.attack_method}-pr{self.poison_rate}"

    def craft_attack(self) -> None:
        # Seed randomly, if the seed was set
        np.random.seed(4)

        # The number of swaps is half the number of maximum data modifications
        n_swaps = int(self.n_max_modifications / 2)

        # Initialize the arrays that drive the swap choices
        n_train_data = len(self.dataset.train_dataset)
        # All indices in the training dataset
        all_data = np.arange(n_train_data, dtype=int)
        # A boolean mask tracking which training data instances haven't been swapped yet.
        # Initialized to "True" for all items (all are available in the beginning)
        available_data = np.ones((n_train_data,), dtype=bool)

        # Iterate until all swaps are exhausted
        while n_swaps > 0:
            # Randomly pick from the available
            swap_candidates = np.random.choice(all_data[available_data], 2)

            _, label1 = self.dataset.train_dataset[swap_candidates[0]]
            _, label2 = self.dataset.train_dataset[swap_candidates[1]]

            # Only swap images of different classes
            if label1 == label2:
                continue

            # Record the swap
            self.poisoner_instructions["item_swaps"].append(
                [swap_candidates[0].item(), swap_candidates[1].item()]
            )

            # Turn off the swapped items so they don't get swapped again
            available_data[swap_candidates] = False

            # Decrease the number of swaps counter
            n_swaps -= 1

        # Verify the attack
        self.verify_attack()

        # Save the poisoner instructions for future use
        self.save_poisoner_instructions()
