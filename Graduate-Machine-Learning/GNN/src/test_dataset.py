
from pathlib import Path

from dataset import AtomicChargeDataset

"""
Legacy smoke test from the submitted course project.

This script targets the original dataset interface used during
development and may require minor adjustments if the dataset
implementation changes.
"""


def main() -> None:
    project_root = Path(__file__).resolve().parent

    dataset = AtomicChargeDataset(
        raw_root=project_root / "Uniformity Test",
        processed_root=project_root / "uniformity_test_cache",
        verbose=True,
    )

    print(f"\nlen(dataset) = {len(dataset)}")
    dataset.summary()

    print("\nFirst graph:")
    print(dataset[0])

    print("\nMetadata:")
    for row in dataset.get_metadata():
        print(row)


if __name__ == "__main__":
    main()