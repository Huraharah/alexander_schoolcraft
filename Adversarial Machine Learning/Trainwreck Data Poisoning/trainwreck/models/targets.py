"""
targets.py

The target models attacked by the Trainwreck attack.
"""
import torch
import torchvision

from datasets.dataset import Dataset
from models.imageclassifier import ImageClassifier


class EfficientNetV2S(ImageClassifier):
    """
    The EfficientNet v2 model with S (small) weights from torchvision:

    https://pytorch.org/vision/stable/models/efficientnetv2.html
    """

    IMAGENET_WEIGHTS = torchvision.models.EfficientNet_V2_S_Weights.DEFAULT

    def __init__(
        self,
        dataset: Dataset,
        n_epochs: int,
        attack_method: str,
        load_existing_model: bool,
        weights_dir: str = ImageClassifier.DEFAULT_WEIGHTS_DIR,
    ) -> None:
        # Initialize the generic image classifier
        super().__init__(dataset, n_epochs, attack_method, weights_dir)

        # Initialize the model to the EfficientNetV2 S architecture
        self.model_type = "target-efficientnet_v2_s"
        self.model = torchvision.models.efficientnet_v2_s()

        # Replace the fully connected layer with a new uninitialized one with number of outputs
        # equal to the dataset's number of classes.
        self.model.classifier[1] = torch.nn.Linear(
            in_features=1280, out_features=self.dataset.n_classes, bias=True
        )

        # Load existing trained weights into the model if the respective flag is set
        if load_existing_model:
            self.load_existing_model()

    @classmethod
    def model_transforms(cls):
        return cls.IMAGENET_WEIGHTS.transforms()


class ResNeXt101(ImageClassifier):
    """
    The ResNeXt-101 model (64x4d) from torchvision:

    https://pytorch.org/vision/stable/models/resnext.html
    """

    IMAGENET_WEIGHTS = torchvision.models.ResNeXt101_64X4D_Weights.DEFAULT

    def __init__(
        self,
        dataset: Dataset,
        n_epochs: int,
        attack_method: str,
        load_existing_model: bool,
        weights_dir: str = ImageClassifier.DEFAULT_WEIGHTS_DIR,
    ) -> None:
        # Initialize the generic image classifier
        super().__init__(dataset, n_epochs, attack_method, weights_dir)

        # Initialize the model to the EfficientNetV2 S architecture
        self.model_type = "target-resnext_101"
        self.model = torchvision.models.resnext101_64x4d()

        # Replace the fully connected layer with a new uninitialized one with number of outputs
        # equal to the dataset's number of classes.
        self.model.fc = torch.nn.Linear(
            in_features=2048, out_features=self.dataset.n_classes, bias=True
        )

        # Load existing trained weights into the model if the respective flag is set
        if load_existing_model:
            self.load_existing_model()

    @classmethod
    def model_transforms(cls):
        return cls.IMAGENET_WEIGHTS.transforms()


class FinetunedViTL16(ImageClassifier):
    """
    The ViT-L-16 vision transformer model from torchvision, initialized to ImageNet
    weights and finetuned to the given dataset.

    https://pytorch.org/vision/stable/models/vision_transformer.html
    """

    IMAGENET_WEIGHTS = torchvision.models.ViT_L_16_Weights.IMAGENET1K_SWAG_LINEAR_V1

    def __init__(
        self,
        dataset: Dataset,
        n_epochs: int,
        attack_method: str,
        load_existing_model: bool,
        weights_dir: str = ImageClassifier.DEFAULT_WEIGHTS_DIR,
    ) -> None:
        # Initialize the generic image classifier
        super().__init__(dataset, n_epochs, attack_method, weights_dir)

        # Initialize the model to the EfficientNetV2 S architecture
        self.model_type = "target-vit_l_16"
        self.model = torchvision.models.vit_l_16(weights=self.IMAGENET_WEIGHTS)

        # Freeze the pretrained layers
        for param in self.model.parameters():
            param.requires_grad = False

        # Replace the fully connected layer with a new uninitialized one with number of outputs
        # equal to the dataset's number of classes.
        self.model.heads.head = torch.nn.Linear(
            in_features=1024, out_features=self.dataset.n_classes, bias=True
        )
        # All newly created modules automatically have requires_grad=True, so we should be set.

        # Load existing trained weights into the model if the respective flag is set
        if load_existing_model:
            self.load_existing_model()

    @classmethod
    def model_transforms(cls):
        return cls.IMAGENET_WEIGHTS.transforms()
