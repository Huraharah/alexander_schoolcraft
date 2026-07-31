"""
dataset.featextr

The "neutral" feature extraction model(s) used by Trainwreck.
"""

import os

import numpy as np
import torch
import torchvision
from tqdm import tqdm

from commons import ATTACK_FEAT_REPR_DIR, timestamp
from datasets.dataset import Dataset
from datasets.features import ImageNetFeatureDataset


class ImageNetViTL16FeatureExtractor:
    """
    The ViT-L-16 vision transformer model from torchvision with ImageNet
    weights

    https://pytorch.org/vision/stable/models/vision_transformer.html
    """

    WEIGHTS = torchvision.models.ViT_L_16_Weights.IMAGENET1K_SWAG_LINEAR_V1
    N_CLASSES = 1000  # ImageNet

    def __init__(self):
        self.model = torchvision.models.vit_l_16(weights=self.WEIGHTS)
        self.model.cuda()

    def extract_features(self, dataset: Dataset, batch_size: int) -> None:
        """
        Extracts features from the given dataset's train data and stores the feature representation
        on disk in NumPy format.
        """

        # Establish the file path to the feature representation. If it exists, do not re-extract.
        feat_repre_path = ImageNetFeatureDataset.feat_repre_path(dataset.dataset_id)

        if os.path.exists(feat_repre_path):
            print("The feature representation already exists, stopping...")
            return

        # Get the train data loader, make sure NOT to shuffle
        train_loader, _ = dataset.data_loaders(batch_size, shuffle=False)

        # Initialize the empty feature matrix that will ultimately contain the dataset
        n_data = len(dataset.train_dataset)
        feat_repre = np.zeros((n_data, self.N_CLASSES))

        print(
            f"{timestamp()} +++ FEATURE EXTRACTION ({dataset.dataset_id}, ImageNet) STARTED +++"
        )

        for b, (X, y) in enumerate(tqdm(train_loader)):  # pylint: disable=C0103
            # Start and end index within the feature matrix
            s = b * batch_size
            e = s + len(X)

            X, y = X.cuda(), y.cuda()  # pylint: disable=C0103
            feat_repre[s:e, :] = (
                torch.nn.Softmax(dim=1)(self.model(X)).detach().cpu().numpy()
            )

        # Ensure the directory where the features will be stored exists
        if not os.path.exists(ATTACK_FEAT_REPR_DIR):
            os.makedirs(ATTACK_FEAT_REPR_DIR)

        # Save the feature representation
        np.save(feat_repre_path, feat_repre)

        print(
            f"{timestamp()} +++ FEATURE EXTRACTION ({dataset.dataset_id}, ImageNet) FINISHED +++"
        )

    @classmethod
    def transforms(cls) -> torch.nn.Module:
        """
        Returns the image transforms used by this model.
        """
        return cls.WEIGHTS.transforms()
