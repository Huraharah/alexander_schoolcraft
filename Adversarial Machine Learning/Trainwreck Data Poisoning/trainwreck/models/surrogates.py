"""
surrogate.py

The surrogate models used to craft the Trainwreck attack.
"""

from PIL import Image
import torch
import torchvision
import torchvision.transforms.functional as Tf

from datasets.dataset import Dataset
from models.imageclassifier import ImageClassifier


class SurrogateResNet50Transform(torch.nn.Module):
    """
    A transform used by the surrogate model. It is essentially a reproduction of the default
    transforms associated with the torchvision ResNet-50 model trained on ImageNet that
    additionally supports key functionality for Trainwreck adversarial routine, namely:

    - Pads small images instead of resizing (stretching) them. As a result, adversarial
      perturbations should operate only in the "original content" area, limiting loss
      of information when the enlarged 224x224 adversarial image is resized back to a
      smaller size (like 32x32 for CIFAR datasets).
    - Enables inverse transformations from net input space back to image space.
    """

    NET_INPUT_SIZE = 10000
    IMAGENET_NORM_MEAN = [0.485, 0.456, 0.406]
    IMAGENET_NORM_STD = [0.229, 0.224, 0.225]

    # Yes, it could be dynamic (inv_mean = -mean/std, inv_std = 1/std), but this is more readable
    IMAGENET_INV_NORM_MEAN = [-0.485 / 0.229, -0.456 / 0.224, -0.406 / 0.225]
    IMAGENET_INV_NORM_STD = [1 / 0.229, 1 / 0.224, 1 / 0.225]

    def forward(self, img: torch.Tensor) -> torch.Tensor:
        """
        Forward pass of the transform.
        """
        # Convert the image to Tensor first
        if not isinstance(img, torch.Tensor):
            img = Tf.pil_to_tensor(img)

        # This is super important, because this does not merely convert dtypes, but also rescales
        # the images to 0-1 range
        img = Tf.convert_image_dtype(img, torch.float)

        if self._image_is_small(img):
            return self._forward_small(img)

        return self._forward_large(img)

    def _forward_small(self, img: torch.Tensor) -> torch.Tensor:
        """
        Forward pass for images smaller or equal to the threshold.
        """
        # Normalize
        img = Tf.normalize(
            img, mean=self.IMAGENET_NORM_MEAN, std=self.IMAGENET_NORM_STD
        )

        return img

    def _forward_large(self, img: torch.Tensor) -> torch.Tensor:
        """
        Forward pass for images larger than the threshold.
        """
        raise NotImplementedError(
            "The prototype is evaluated on datasets that all feature images that fall "
            "into the 'small' category."
        )

    def _image_is_small(self, img: torch.Tensor) -> bool:
        """
        Checks whether an image is small (longest side smaller or equal to the net input size)
        or large (otherwise).
        """
        return max(img.size()) <= self.NET_INPUT_SIZE

    def inverse_transform(self, img: torch.Tensor) -> Image.Image:
        """
        Performs an inverse transform on an image in net input format.
        """
        if self._image_is_small(img):
            return self._inverse_transform_small(img)

        return self._inverse_transform_large(img)

    def _inverse_transform_small(self, img: torch.Tensor) -> Image.Image:
        """
        Performs an inverse transform on a small image in net input format.
        """

        # Inverse normalize
        img = Tf.normalize(
            img, mean=self.IMAGENET_INV_NORM_MEAN, std=self.IMAGENET_INV_NORM_STD
        )

        # Clamp to (0, 1)
        img = torch.clamp(img, 0, 1)

        # Convert to PIL image
        img = Tf.to_pil_image(img)

        return img

    def _inverse_transform_large(self, img: torch.Tensor) -> Image.Image:
        """
        Performs an inverse transform on a large image in net input format.
        """
        raise NotImplementedError(
            "The prototype is evaluated on datasets that all feature images that fall "
            "into the 'small' category."
        )


class SurrogateResNet50(ImageClassifier):
    """
    A surrogate ResNet-50 image classifier used to craft the Trainwreck attack. Initialized to
    the pre-trained ImageNet weights available in torchvision, with the final layer finetuned to
    the provided dataset.
    """

    SURROGATE_MODEL_INPUT_SIZE = 32
    IMAGENET_WEIGHTS = torchvision.models.ResNet50_Weights.DEFAULT

    # The maximum standard deviation used to normalize the data across all color channels. This
    # will be used to translate epsilon (adv. perturb strength) from pixel space (which is how
    # epsilon is normally set, as a max intensity distance) to the normalized space
    NORM_STD_MAX = 0.229

    DEFAULT_SURROGATE_INFERENCE_BATCH_SIZE = 128

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

        # Initialize the model to Resnet-50 with pre-trained ImageNet weights
        self.model_type = "surrogate-resnet50"
        if self.dataset.dataset_id == "gtsrb":
            self.model = torchvision.models.resnet50()
        else:
            self.model = torchvision.models.resnet50(weights=self.IMAGENET_WEIGHTS)

        # Set the transforms
        self.transforms = SurrogateResNet50Transform()

        # Replace the fully connected layer with a new uninitialized one with number of outputs
        # equal to the dataset's number of classes.
        self.model.fc = torch.nn.Linear(
            in_features=2048, out_features=self.dataset.n_classes, bias=True
        )

        # Load existing trained weights into the model if the respective flag is set
        if load_existing_model:
            self.load_existing_model()

        # Send the model to the GPU
        self.model.cuda()

    def forward(self, data: torch.Tensor) -> torch.Tensor:
        """
        The forward pass of the model.
        """
        return self.model(data)

    def input_size(self) -> int:
        """
        Returns the surrogate model's input size
        """
        return self.dataset.input_size

    def inverse_transform_data(self, data: torch.Tensor) -> Image.Image:
        """
        Performs an inverse transform on the given data, converting a Torch tensor back to an
        image
        """
        return self.transforms.inverse_transform(data)

    def transform_data(self, data: Image.Image | torch.Tensor) -> torch.Tensor:
        """
        Transforms the input using the model's transforms.
        """
        return self.transforms(data)

    @classmethod
    def model_transforms(cls) -> torch.nn.Module:
        return SurrogateResNet50Transform()
