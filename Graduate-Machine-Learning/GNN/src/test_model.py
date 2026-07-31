from pathlib import Path

from dataset import AtomicChargeDataset
from dataset_split import split_dataset, build_dataloaders
from model import AtomicChargeGNN, print_model_summary

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

    train_set, val_set, test_set = split_dataset(dataset)
    train_loader, val_loader, test_loader = build_dataloaders(
        train_set,
        val_set,
        test_set,
        batch_size=4
    )

    batch = next(iter(train_loader))

    model = AtomicChargeGNN(
        in_channels=dataset.dim_node_features,
        hidden_channels=64,
        num_layers=3,
        dropout=0.2
    )

    print_model_summary(model)

    out = model(batch.x, batch.edge_index, batch.edge_attr)

    print("\nBatch input / output check")
    print(f"x shape   : {batch.x.shape}")
    print(f"y shape   : {batch.y.shape}")
    print(f"out shape : {out.shape}")


if __name__ == "__main__":
    main()
