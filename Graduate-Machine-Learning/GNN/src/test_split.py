from pathlib import Path

from dataset import AtomicChargeDataset
from dataset_split import split_dataset, build_dataloaders

"""
Legacy smoke test from the submitted course project.

This script targets the original dataset interface used during
development and may require minor adjustments if the dataset
implementation changes.
"""


def main() -> None:
    dataset_root = Path(__file__).resolve().parent
    dataset = AtomicChargeDataset(
        raw_root=dataset_root,
        processed_root=dataset_root / "dataset_cache",
        verbose=False,
    )   

    train_set, val_set, test_set = split_dataset(
        dataset,
        train_ratio=0.70,
        val_ratio=0.15,
        test_ratio=0.15,
        seed=42
    )

    print(f"Total dataset size : {len(dataset)}")
    print(f"Train size         : {len(train_set)}")
    print(f"Validation size    : {len(val_set)}")
    print(f"Test size          : {len(test_set)}")

    train_loader, val_loader, test_loader = build_dataloaders(
        train_set,
        val_set,
        test_set,
        batch_size=4
    )

    first_batch = next(iter(train_loader))

    print("\nFirst batch:")
    print(first_batch)
    print(f"Batch num graphs   : {first_batch.num_graphs}")
    print(f"Batch x shape      : {first_batch.x.shape}")
    print(f"Batch y shape      : {first_batch.y.shape}")
    print(f"Batch edge_index   : {first_batch.edge_index.shape}")
    print(f"Batch edge_attr    : {first_batch.edge_attr.shape}")


if __name__ == "__main__":
    main()
