"""
craft_attack.py

Crafts a train-time damaging adversarial attack.
"""

import argparse
from time import time

from commons import (
    ROOT_DATA_DIR,
    timestamp,
    t_readable,
)
from datasets.dataset import Dataset
from trainwreck.factory import TrainwreckFactory
from trainwreck.trainwreck import TrainwreckAttack

parser = argparse.ArgumentParser()

parser.add_argument(
    "attack_method",
    type=str,
    choices=TrainwreckFactory.ATTACK_METHODS,
    help="The Trainwreck attack method to be used on the training dataset.",
)
parser.add_argument(
    "dataset_id",
    type=str,
    choices=Dataset.valid_dataset_ids(),
    help="The dataset to attack.",
)
parser.add_argument(
    "poison_rate",
    type=float,
    help="The rate of poisoned data within the entire training dataset "
    "(e.g., 0.1 = up to 10%% of training data can be poisoned).",
)
parser.add_argument(
    "--epsilon_px",
    type=int,
    default=8,
    help="The perturbation strength (epsilon) in terms of a max l-inf norm in PIXEL INTENSITY "
    "space (0-255 per color channel). The default is 8, i.e., the perturbation may only "
    "alter each pixel's color channel by up to 8 intensity level.",
)

args = parser.parse_args()

# Check that poison_rate is a float in (0, 1]
if (
    not isinstance(args.poison_rate, float)
    or args.poison_rate <= 0
    or args.poison_rate > 1
):
    raise ValueError(
        f"Poison rate must be a float in (0, 1], got {args.poison_rate} instead."
    )

if args.attack_method in ["advreplace", "trainwreck"]:
    transforms = TrainwreckAttack.surrogate_model_transforms()
else:
    transforms = None  # pylint:disable=C0103

dataset = Dataset(args.dataset_id, ROOT_DATA_DIR, transforms)
attack = TrainwreckFactory.attack_obj(
    args.attack_method, dataset, args.poison_rate, args.epsilon_px
)

t_start = time()
print(f"{timestamp()} +++ CRAFTING TRAINWRECK ATTACK {attack.attack_id()} +++")

attack.craft_attack()

print(
    f"{timestamp()} +++ ATTACK CRAFTING FINISHED ({t_readable(time() - t_start)}) +++"
)
