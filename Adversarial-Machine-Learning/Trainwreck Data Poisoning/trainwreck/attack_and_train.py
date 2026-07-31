"""
attack_and_train.py

Trains a model. Depending on the parameters, it either trains a clean surrogate or target model,
or attacks model training using Trainwreck by training on a poisoned dataset.
"""

import sys
import os

# Add the project root to the Python path
project_root = os.path.dirname(os.path.abspath(__file__))
sys.path.append(project_root)


import argparse
from time import time

from commons import (
    ROOT_DATA_DIR,
    timestamp,
    t_readable,
)
from datasets.dataset import Dataset
from models.factory import ImageClassifierFactory
from trainwreck.factory import TrainwreckFactory

DEFAULT_N_EPOCHS = 30

parser = argparse.ArgumentParser()
parser.add_argument(
    "attack_method",
    type=str,
    choices=TrainwreckFactory.ATTACK_METHODS + ["clean"],
    help="The Trainwreck attack method to be used on the training dataset.",
)
parser.add_argument(
    "dataset_id",
    type=str,
    choices=Dataset.valid_dataset_ids(),
    help="The dataset to train on.",
)
parser.add_argument(
    "model_type",
    type=str,
    choices=ImageClassifierFactory.MODEL_TYPES,
    help=(
        "The type of model to be trained (surrogate or one of the target models). "
        "Surrogate models can only be trained on clean data (attack method = 'clean')."
    ),
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
parser.add_argument(
    "--n_epochs",
    type=int,
    default=DEFAULT_N_EPOCHS,
    help="The number of training epochs.",
)
parser.add_argument(
    "--poison_rate",
    type=float,
    default=None,
    help=(
        "The rate of poisoned data within the entire training dataset "
        "(e.g., 0.1 = up to 10%% of training data can be poisoned). "
        "Must be set if using any Trainwreck attack (attack method != 'clean')."
    ),
)
parser.add_argument(
    "--epsilon_px",
    type=int,
    default=8,
    help="The perturbation strength (epsilon) in terms of a max l-inf norm in PIXEL INTENSITY "
    "space (0-255 per color channel). The default is 8, i.e., the perturbation may only "
    "alter each pixel's color channel by up to 8 intensity level.",
)
parser.add_argument(
    "--force",
    type=bool,
    action=argparse.BooleanOptionalAction,
    default=False,
    help="If set, forces training even if the weights file exists for this "
    "parameter combination.",
)

args = parser.parse_args()

# Validate arguments
# ------------------
# The poison rate must be set if attacking, and it must be a positive float lower or equal to 1
if args.attack_method != "clean" and (
    not args.poison_rate or args.poison_rate <= 0 or args.poison_rate > 1
):
    raise ValueError(
        "When attacking, the poison rate must be a float with value in (0, 1], "
        f"got {args.poison_rate} instead."
    )


# Surrogate model is only to be trained on the clean dataset
if args.model_type == "surrogate" and args.attack_method != "clean":
    raise ValueError(
        "Surrogate models can only be trained on clean training data, stopping."
    )

# Run the training
# -----------------
print(f"{timestamp()} +++ TRAINWRECK STARTED +++", flush=True)
t_start = time()

transforms = ImageClassifierFactory.image_classifier_transforms(args.model_type)

# Create the dataset
t_data = time()
print(f"{timestamp()} Poisoning the data...", flush=True)
dataset = Dataset(args.dataset_id, ROOT_DATA_DIR, transforms)

# If the attack method is not "clean", poison the dataset with the Trainwreck attack
if args.attack_method != "clean":
    trainwreck_attack = TrainwreckFactory.attack_obj(
        args.attack_method, dataset, args.poison_rate, args.epsilon_px
    )
    trainwreck_attack.attack_dataset()

    attack_id = trainwreck_attack.attack_id()
# Otherwise just set the attack identifier for the model
else:
    attack_id = "clean"  # pylint: disable=C0103

print(f"{timestamp()} Data poisoned in {t_readable(time() - t_data)}.", flush=True)
# Create the model
model = ImageClassifierFactory.image_classifier_obj(
    args.model_type, dataset, args.n_epochs, attack_id, load_existing_model=False
)

# Train the model
model.train(args.batch_size, args.force)

print(
    f"{timestamp()} +++ TRAINWRECK FINISHED ({t_readable(time() - t_start)}) +++",
    flush=True,
)
