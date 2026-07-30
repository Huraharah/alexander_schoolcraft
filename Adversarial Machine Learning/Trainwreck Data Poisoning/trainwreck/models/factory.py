"""
models.factory

A factory for the image classifier models used or attacked by the Trainwreck attack.
"""

from typing import Type

import torch.nn

from datasets.dataset import Dataset
from models.imageclassifier import ImageClassifier
from models.surrogates import SurrogateResNet50
from models.targets import EfficientNetV2S, ResNeXt101, FinetunedViTL16


class ImageClassifierFactory:
    """
    A factory class that returns the correct image classifier model given the model type string.
    """

    MODEL_TYPES = ["surrogate", "efficientnet", "resnext", "vit"]

    @classmethod
    def _image_classifier_cls(cls, model_type: str) -> Type[ImageClassifier]:
        """
        Returns the image classifier model's CLASS. Useful since this factory needs to return
        transformers & model objects separately, and it's governed by a single "if-else" chain
        that is best maintained in one place (here).
        """
        cls._validate_model_type(model_type)

        if model_type == "surrogate":
            return SurrogateResNet50
        elif model_type == "efficientnet":
            return EfficientNetV2S
        elif model_type == "resnext":
            return ResNeXt101
        elif model_type == "vit":
            return FinetunedViTL16

        # None of the model classes got returned, yet there was no complaint by the validation
        # method, sounds like a NYI error
        raise NotImplementedError(
            "Factory invoked on a valid model type, but no actual implemented model "
            "or its transforms could be returned. Probably a case of NYI error."
        )

    @classmethod
    def image_classifier_obj(
        cls,
        model_type: str,
        dataset: Dataset,
        n_epochs: int,
        attack_method: str,
        load_existing_model: bool,
    ) -> ImageClassifier:
        """
        Returns the image classifiers model object given the model type string.
        """
        cls._validate_model_type(model_type)
        ModelCls = cls._image_classifier_cls(model_type)

        return ModelCls(dataset, n_epochs, attack_method, load_existing_model)

    @classmethod
    def image_classifier_transforms(cls, model_type) -> torch.nn.Module:
        """
        Returns the transforms for the specified image classifier model type.
        """
        cls._validate_model_type(model_type)
        ModelCls = cls._image_classifier_cls(model_type)

        return ModelCls.model_transforms()

    @classmethod
    def _validate_model_type(cls, model_type: str) -> None:
        if model_type not in cls.MODEL_TYPES:
            raise ValueError(f"Invalid model type {model_type}.")
