from pathlib import Path

from dataset import AtomicChargeDataset


def main() -> None:

    dataset_root = Path(__file__).resolve().parent  # project root containing PASCAR / CHARGESSS

    dataset = AtomicChargeDataset(
        raw_root=dataset_root,
        processed_root=dataset_root / "dataset_cache",
        verbose=False,
    )

    print("\nDataset loaded successfully.")
    print(f"Total graphs: {len(dataset)}")

    dataset.summary()

    # optional: save metadata for inspection
    metadata = dataset.get_metadata()

    metadata_path = dataset_root / "dataset_metadata.txt"

    with metadata_path.open("w") as f:
        for row in metadata:
            f.write(str(row) + "\n")

    print("\nMetadata written to dataset_metadata.txt")


if __name__ == "__main__":
    main()
