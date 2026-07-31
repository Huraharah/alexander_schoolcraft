from typing import Tuple, Union
import torch
from torch.utils.data import random_split
from torch_geometric.loader import DataLoader


def split_dataset(
    dataset,
    train_ratio: float = 0.70,
    val_ratio: float = 0.15,
    test_ratio: float = 0.15,
    seed: int = 42,
) -> Tuple[torch.utils.data.Dataset, torch.utils.data.Dataset, torch.utils.data.Dataset]:
    """
    Split a dataset into train / validation / test subsets.

    Ratios must sum to 1.0.
    """

    total = len(dataset)

    if not abs((train_ratio + val_ratio + test_ratio) - 1.0) < 1e-8:
        raise ValueError("train_ratio + val_ratio + test_ratio must sum to 1.0")

    train_size = int(total * train_ratio)
    val_size = int(total * val_ratio)
    test_size = total - train_size - val_size

    generator = torch.Generator().manual_seed(seed)

    train_set, val_set, test_set = random_split(
        dataset,
        [train_size, val_size, test_size],
        generator=generator
    )

    return train_set, val_set, test_set


def build_dataloaders(
    train_set,
    val_set,
    test_set,
    batch_size: int = 4,
    num_workers: int = 0,
) -> Tuple[DataLoader, DataLoader, DataLoader]:
    """
    Build PyTorch Geometric DataLoaders for train / val / test.
    """

    train_loader = DataLoader(
        train_set,
        batch_size=batch_size,
        shuffle=True,
        num_workers=num_workers
    )

    val_loader = DataLoader(
        val_set,
        batch_size=batch_size,
        shuffle=False,
        num_workers=num_workers
    )

    test_loader = DataLoader(
        test_set,
        batch_size=batch_size,
        shuffle=False,
        num_workers=num_workers
    )

    return train_loader, val_loader, test_loader
