from itertools import product
from pathlib import Path
import json
import random

import torch

from dataset import AtomicChargeDataset
from dataset_split import split_dataset, build_dataloaders
from model import AtomicChargeGNN, count_parameters
from train import get_device, train_model


def sample_search_space(max_trials: int = 20, seed: int = 42):
    """
    Randomly sample configurations from a structured search space.
    """
    random.seed(seed)

    search_space = {
        "hidden_channels": [16, 32, 64, 128],
        "num_layers": [2, 3, 4],
        "dropout": [0.0, 0.1, 0.2, 0.3],
        "lr": [1e-2, 1e-3, 5e-4, 1e-4],
        "weight_decay": [0.0, 1e-6, 1e-5, 1e-4],
        "optimizer_name": ["adam", "adamw"],
        "activation": ["relu", "leaky_relu", "elu"],
        "scheduler_name": ["none", "plateau"],
        "batch_size": [2, 4, 8],
    }

    all_configs = list(product(
        search_space["hidden_channels"],
        search_space["num_layers"],
        search_space["dropout"],
        search_space["lr"],
        search_space["weight_decay"],
        search_space["optimizer_name"],
        search_space["activation"],
        search_space["scheduler_name"],
        search_space["batch_size"],
    ))

    random.shuffle(all_configs)
    all_configs = all_configs[:max_trials]

    configs = []
    for cfg in all_configs:
        configs.append({
            "hidden_channels": cfg[0],
            "num_layers": cfg[1],
            "dropout": cfg[2],
            "lr": cfg[3],
            "weight_decay": cfg[4],
            "optimizer_name": cfg[5],
            "activation": cfg[6],
            "scheduler_name": cfg[7],
            "batch_size": cfg[8],
        })

    return configs


def main() -> None:
    print("Starting tune.py")

    project_root = Path(__file__).resolve().parent
    raw_dataset_root = Path(__file__).resolve().parent
    processed_dataset_root = project_root / "dataset_cache"
    device = get_device()

    print("Loading processed dataset...")
    dataset = AtomicChargeDataset(
        raw_root=raw_dataset_root,
        processed_root=processed_dataset_root,
        verbose=False,
    )

    print(f"Using device: {device}")
    print(f"Node feature dimension: {dataset.dim_node_features}")

    train_set, val_set, test_set = split_dataset(
        dataset,
        train_ratio=0.70,
        val_ratio=0.15,
        test_ratio=0.15,
        seed=42,
    )

    trial_configs = sample_search_space(max_trials=50, seed=42)

    results = []

    for trial_idx, cfg in enumerate(trial_configs, start=1):
        print("\n" + "=" * 72)
        print(f"Trial {trial_idx}/{len(trial_configs)}")
        print(cfg)

        train_loader, val_loader, test_loader = build_dataloaders(
            train_set,
            val_set,
            test_set,
            batch_size=cfg["batch_size"],
            num_workers=0,
        )

        model = AtomicChargeGNN(
            in_channels=dataset.dim_node_features,
            hidden_channels=cfg["hidden_channels"],
            num_layers=cfg["num_layers"],
            dropout=cfg["dropout"],
            activation=cfg["activation"],
            use_edge_attr=True,
        ).to(device)

        print(f"Trainable parameters: {count_parameters(model):,}")

        history = train_model(
            model=model,
            train_loader=train_loader,
            val_loader=val_loader,
            device=device,
            lr=cfg["lr"],
            weight_decay=cfg["weight_decay"],
            optimizer_name=cfg["optimizer_name"],
            scheduler_name=cfg["scheduler_name"],
            max_epochs=5,
            patience=10,
            verbose=True,
        )

        result = {
            "trial_idx": trial_idx,
            "config": cfg,
            "num_parameters": count_parameters(model),
            "best_val_loss": history["best_val_loss"],
            "best_epoch": history["best_epoch"],
        }
        results.append(result)

    results = sorted(results, key=lambda x: x["best_val_loss"])

    out_path = project_root / "tuning_results_stage1.json"
    out_path.parent.mkdir(parents=True, exist_ok=True)

    with out_path.open("w") as f:
        json.dump(results, f, indent=2)

    print("\nTuning complete. Top results:")
    for rank, row in enumerate(results[:10], start=1):
        print(
            f"{rank:>2d}. "
            f"best_val_loss={row['best_val_loss']:.6f} | "
            f"epoch={row['best_epoch']:>3d} | "
            f"params={row['num_parameters']:,} | "
            f"config={row['config']}"
        )

    print(f"\nSaved tuning results to: {out_path}")


if __name__ == "__main__":
    main()
