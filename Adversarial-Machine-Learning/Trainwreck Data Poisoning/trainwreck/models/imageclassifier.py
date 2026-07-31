"""
imageclassifier.py

Encapsulates general image classifier functionality.
"""


from abc import ABC as AbstractBaseClass, abstractmethod
import copy
import json
import os
from time import time
from timm import utils
import torch

from commons import RAW_RESULTS_DIR, timestamp, t_readable
from datasets.dataset import Dataset


class ImageClassifier(AbstractBaseClass):
    """
    The image classifier class.
    """

    DEFAULT_N_EPOCHS = 30
    DEFAULT_WEIGHTS_DIR = os.path.join(
        os.path.abspath(os.path.dirname(__file__)), "weights"
    )

    def __init__(
        self,
        dataset: Dataset,
        n_epochs: int,
        attack_method: str,
        weights_dir: str = DEFAULT_WEIGHTS_DIR,
    ) -> None:
        # Handle the number of epochs (it must be a positive integer)
        if not isinstance(n_epochs, int) or n_epochs <= 0:
            raise ValueError(
                f"Number of epochs must be a positive integer, got {n_epochs}."
            )

        # Set the dataset
        self.dataset = dataset
        self.dataset_id = self.dataset.dataset_id

        # Technically, the number of epochs could be treated as a parameter for the train()
        # method and not a model property, but having it as a model property allows for handling
        # multiple models differing only in number of training epochs, which helps ablation
        # studies.
        self.n_epochs = n_epochs

        # Set the attack method - trim the dataset information from it though to avoid duplicities
        # in the dataset model ID
        if self.dataset_id in attack_method:
            self.attack_method = "-".join(attack_method.split("-")[1:])
        else:
            self.attack_method = attack_method

        # Store the weights directory
        self.weights_dir = weights_dir

        # Initialize the model to None (model definitions are handled by the child classes)
        self.model = None
        self.model_type = None

    def load_existing_model(self, epoch: int | None = None) -> None:
        """
        Loads trained weights into the model.
        """
        # The model must be defined in the first place
        if self.model:
            state_dict = torch.load(self.model_path(epoch))
            self.model.load_state_dict(state_dict)

            # Set the model to eval - we're loading a trained model, so by default
            # that's for inference
            self.model.eval()

    def model_id(self) -> str:
        """
        Returns the model identifier.
        """
        return f"{self.dataset_id}-{self.attack_method}-{self.model_type}-{self.n_epochs}epochs"

    def model_path(self, epoch_no: int | None = None) -> str:
        """
        Returns the path to a pretrained model's weights.
        """
        if epoch_no is None:
            epoch_identifier = ""
        else:
            epoch_identifier = f"-ep{epoch_no}"

        return os.path.join(
            self.weights_dir, f"{self.model_id()}{epoch_identifier}.pth"
        )

    def _recover_from_failure(self) -> int | None:
        """
        Recovers a model state dict from the last recorded trained epoch. If such state dict was
        found, returns the number of the last completed epoch. If no such state dict was found,
        returns None.
        """
        last_completed_epoch = None

        for e in range(self.n_epochs):
            epoch_model_path = self.model_path(e)

            if os.path.exists(epoch_model_path):
                last_completed_epoch = e

        return last_completed_epoch

    def _recover_metrics(self, last_completed_epoch: int) -> dict:
        """
        Recovers the metrics record from the last completed epoch.
        """
        metrics_ep_path = os.path.join(
            RAW_RESULTS_DIR, f"{self.model_id()}-ep{last_completed_epoch}.json"
        )

        with open(metrics_ep_path, "r", encoding="utf-8") as f:
            metrics = json.loads(f.read())

        return metrics

    def train(self, batch_size: int, force: bool) -> None:
        """
        Trains the image classifier.
        """
        # PyLint dislikes the "X, y" ML naming convention for data & labels
        # pylint: disable=C0103

        # Cannot train on an unset dataset
        if self.dataset is None:
            raise ValueError("The dataset is not set, cannot train.")

        # Establish the weights file
        model_path = self.model_path()

        # If the training is not forced, check if the weights file exists. If it does, stop
        if not force and os.path.exists(model_path):
            print(
                "This model has already been trained and the 'force' flag is not set, stopping."
            )
            return

        # Validate int parameters: both batch size and number of epochs must be a positive integer
        if not isinstance(batch_size, int) or batch_size <= 0:
            raise ValueError(
                f"Batch size must be a positive integer, got {batch_size}."
            )

        # Attempt to recover the existing model in case previous training was ended abruptly
        last_completed_epoch = self._recover_from_failure()

        if last_completed_epoch is None:
            first_epoch = 0
            metrics = []
            top1_acc_best = 0.0
            best_model_state_dict = None
        else:
            first_epoch = last_completed_epoch + 1
            self.load_existing_model(last_completed_epoch)
            best_model_state_dict = copy.deepcopy(self.model.state_dict())
            metrics = self._recover_metrics(last_completed_epoch)
            top1_acc_best = metrics[-1]["top1"]

        # Establish the weights & results directories if they doesn't exist yet
        if not os.path.exists(self.weights_dir):
            os.makedirs(self.weights_dir)

        if not os.path.exists(RAW_RESULTS_DIR):
            os.makedirs(RAW_RESULTS_DIR)

        # Send the model to the GPU
        self.model.cuda()

        # Get the data loaders
        train_loader, test_loader = self.dataset.data_loaders(batch_size, shuffle=True)

        # Loss
        loss_fn = torch.nn.CrossEntropyLoss()

        # Optimizer
        optimizer = torch.optim.Adam(self.model.parameters())

        # Train the model
        # ---------------
        print(f"{timestamp()} +++ TRAINING {self.model_id()} +++", flush=True)

        for e in range(first_epoch, self.n_epochs):
            # Start the stopwatch
            t_epoch_start = time()

            # Put the model in train mode
            self.model.train()

            # Perform epoch training
            for X, y in train_loader:
                X, y = X.cuda(), y.cuda()
                y_pred = self.model(X)  # pylint: disable=E1102
                loss = loss_fn(y_pred, y)

                optimizer.zero_grad()
                loss.backward()
                optimizer.step()

            # Initialize the epoch top-1 accuracy meter
            metrics_epoch = {
                "loss": utils.AverageMeter(),
                "top1": utils.AverageMeter(),
                "top5": utils.AverageMeter(),
            }

            # Set the model to eval
            self.model.eval()

            # Evaluate the model
            with torch.no_grad():
                for X, y in test_loader:
                    X, y = X.cuda(), y.cuda()
                    y_pred = self.model(X)  # pylint: disable=E1102

                    loss = loss_fn(y_pred, y)

                    top1_acc, top5_acc = utils.accuracy(y_pred, y, topk=(1, 5))
                    metrics_epoch["loss"].update(loss.data.item(), X.size(0))
                    metrics_epoch["top1"].update(top1_acc.item(), X.size(0))
                    metrics_epoch["top5"].update(top5_acc.item(), X.size(0))

            # Record the eval metrics
            metrics.append(
                {key: round(value.avg, 2) for key, value in metrics_epoch.items()}
            )

            # If this round improved the top-1 accuracy, update the weights (state dict)
            # of the best model
            if metrics_epoch["top1"].avg > top1_acc_best:
                top1_acc_best = metrics_epoch["top1"].avg
                best_model_state_dict = copy.deepcopy(self.model.state_dict())

            # Save this model's state dict if we're not in the last epoch
            if e != self.n_epochs - 1:
                last_epoch_model_path = self.model_path(e)
                torch.save(self.model.state_dict(), last_epoch_model_path)

            # Delete the previous model state dict, if it exists.
            prev_epoch_model_path = self.model_path(e - 1)
            if os.path.exists(prev_epoch_model_path):
                os.remove(prev_epoch_model_path)

            # Save the metrics after this step & delete the old ones if they exist
            if e != self.n_epochs - 1:
                metrics_epoch_path = os.path.join(
                    RAW_RESULTS_DIR, f"{self.model_id()}-ep{e}.json"
                )
                with open(metrics_epoch_path, "w", encoding="utf-8") as f:
                    f.write(json.dumps(metrics))

            prev_metrics_epoch_path = os.path.join(
                RAW_RESULTS_DIR, f"{self.model_id()}-ep{e-1}.json"
            )

            if os.path.exists(prev_metrics_epoch_path):
                os.remove(prev_metrics_epoch_path)

            # Report on the epoch
            print(
                f"{timestamp()} "
                f"Epoch {e+1}/{self.n_epochs} completed in {t_readable(time()-t_epoch_start)}: "
                f"top-1 accuracy = {metrics[-1]['top1']}, "
                f"top-5 accuracy = {metrics[-1]['top5']}, "
                f"loss = {metrics[-1]['loss']}. "
                f"Best top-1 accuracy so far = {round(top1_acc_best, 2)}.",
                flush=True,
            )

        # Save the best model & metrics
        # -------------------
        torch.save(best_model_state_dict, model_path)

        metrics_path = os.path.join(RAW_RESULTS_DIR, f"{self.model_id()}.json")
        with open(metrics_path, "w", encoding="utf-8") as f:
            f.write(json.dumps(metrics))

        print(f"{timestamp()} +++ TRAINING COMPLETE +++", flush=True)

    @classmethod
    @abstractmethod
    def model_transforms(cls) -> torch.nn.Module:
        """
        Returns the data transforms pertaining to the image classifier model.
        """
