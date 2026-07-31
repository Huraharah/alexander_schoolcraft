"""
extract_features.py

Extracts features used by the Trainwreck attack from the datasets.
"""
import argparse

from commons import ROOT_DATA_DIR
from datasets.dataset import Dataset
from models.featextr import ImageNetViTL16FeatureExtractor

parser = argparse.ArgumentParser()
parser.add_argument(
    "dataset_id",
    type=str,
    choices=Dataset.valid_dataset_ids(),
    help="The dataset to train on.",
)
parser.add_argument(
    "--batch_size",
    type=int,
    default=1,
    help=(
        "The batch size. For maximum speed, use as large as you can "
        "without running out of memory.",
    ),
)

args = parser.parse_args()

dataset = Dataset(
    args.dataset_id, ROOT_DATA_DIR, ImageNetViTL16FeatureExtractor.transforms()
)
feat_extractor = ImageNetViTL16FeatureExtractor()
feat_extractor.extract_features(dataset, args.batch_size)
