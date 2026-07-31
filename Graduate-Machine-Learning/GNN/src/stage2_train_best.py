from pathlib import Path
import json
from typing import Any, Dict, List, Union

import torch

from dataset import AtomicChargeDataset
from dataset_split import split_dataset, build_dataloaders
from model import AtomicChargeGNN, count_parameters
from train import get_device, train_model, save_checkpoint


def load_stage1_results(json_path: Union[str, Path]) -> List[Dict[str, Any]]:
    """
    Load the stage-1 tuning results from JSON.
    """
    json_path = Path(json_path)

    if not json_path.exists():
        raise FileNotFoundError(f"Stage-1 tuning results file not found: {json_path}")

    with json_path.open("r") as f:
        results = json.load(f)

    if not isinstance(results, list):
        raise ValueError("Expected stage-1 tuning results JSON to contain a list of configs")

    return results


def select_top_configs(
    results: List[Dict[str, Any]],
    top_k: int = 5,
) -> List[Dict[str, Any]]:
    """
    Sort configs by best_val_loss and return the top_k entries.
    """
    sorted_results = sorted(results, key=lambda x: x["best_val_loss"])
    return sorted_results[:top_k]


def sanitize_config_name(cfg: Dict[str, Any], rank: int) -> str:
    """
    Build a filesystem-friendly run name from a config.
    """
    return (
        f"rank{rank:02d}_"
        f"h{cfg['hidden_channels']}_"
        f"l{cfg['num_layers']}_"
        f"d{str(cfg['dropout']).replace('.', 'p')}_"
        f"lr{str(cfg['lr']).replace('.', 'p')}_"
        f"wd{str(cfg['weight_decay']).replace('.', 'p')}_"
        f"{cfg['optimizer_name']}_"
        f"{cfg['activation']}_"
        f"{cfg['scheduler_name']}_"
        f"bs{cfg['batch_size']}"
    )


def save_history_json(
    history: Dict[str, Any],
    config: Dict[str, Any],
    save_path: Union[str, Path],
) -> None:
    """
    Save train/val loss history and config for later plotting/inspection.
    """
    save_path = Path(save_path)
    save_path.parent.mkdir(parents=True, exist_ok=True)

    payload = {
        "config": config,
        "train_loss": history["train_loss"],
        "val_loss": history["val_loss"],
        "best_val_loss": history["best_val_loss"],
        "best_epoch": history["best_epoch"],
    }

    with save_path.open("w") as f:
        json.dump(payload, f, indent=2)


def main() -> None:
    print("Starting stage2_train_best.py")

    project_root = Path(__file__).resolve().parent
    raw_dataset_root = project_root
    processed_dataset_root = project_root / "dataset_cache"
    stage1_results_path = project_root / "tuning_results_stage1.json"

    top_k = 5
    max_epochs = 100
    patience = 15
    split_seed = 42

    print("Loading stage-1 tuning results.")
    stage1_results = load_stage1_results(stage1_results_path)
    top_configs = select_top_configs(stage1_results, top_k=top_k)

    print(f"Selected top {len(top_configs)} configs for stage-2 full training.")
    for i, cfg in enumerate(top_configs, start=1):
        print(f"{i:>2d}. best_val_loss={cfg['best_val_loss']:.6f} | {cfg}")

    print("\nLoading processed dataset once for stage-2 training.")
    dataset = AtomicChargeDataset(
        raw_root=raw_dataset_root,
        processed_root=processed_dataset_root,
        verbose=False,
    )

    train_set, val_set, test_set = split_dataset(
        dataset,
        train_ratio=0.70,
        val_ratio=0.15,
        test_ratio=0.15,
        seed=split_seed,
    )

    device = get_device()
    print(f"Using device: {device}")
    print(f"Node feature dimension: {dataset.num_node_features}")

    stage2_results: List[Dict[str, Any]] = []

    for rank, row in enumerate(top_configs, start=1):
        cfg = row["config"]
        run_name = sanitize_config_name(cfg, rank)

        print("\n" + "=" * 80)
        print(f"Stage-2 run {rank}/{len(top_configs)}")
        print(f"Run name: {run_name}")
        print(json.dumps(row, indent=2))
        print("=" * 80)

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

        history = train_model(
            model=model,
            train_loader=train_loader,
            val_loader=val_loader,
            device=device,
            lr=cfg["lr"],
            weight_decay=cfg["weight_decay"],
            optimizer_name=cfg["optimizer_name"],
            scheduler_name=cfg["scheduler_name"],
            max_epochs=max_epochs,
            patience=patience,
            verbose=True,
        )

        if history["best_model_state"] is not None:
            model.load_state_dict(history["best_model_state"])

        checkpoint_path = project_root / "checkpoints" / "stage2" / f"{run_name}.pt"
        history_path = project_root / "histories" / "stage2" / f"{run_name}.json"

        save_checkpoint(
            model=model,
            history=history,
            save_path=checkpoint_path,
        )

        save_history_json(
            history=history,
            config=cfg,
            save_path=history_path,
        )

        result = {
            "run_name": run_name,
            "config": cfg,
            "best_val_loss": history["best_val_loss"],
            "best_epoch": history["best_epoch"],
            "num_params": count_parameters(model),
            "checkpoint_path": str(checkpoint_path),
            "history_path": str(history_path),
        }
        stage2_results.append(result)

        print("\nRun complete.")
        print(f"Best epoch    : {history['best_epoch']}")
        print(f"Best val loss : {history['best_val_loss']:.6f}")
        print(f"Checkpoint    : {checkpoint_path}")
        print(f"History JSON  : {history_path}")

    stage2_results.sort(key=lambda x: x["best_val_loss"])

    summary_path = project_root / "stage2_results_full.json"
    with summary_path.open("w") as f:
        json.dump(stage2_results, f, indent=2)

    print("\n" + "=" * 80)
    print("Stage-2 training complete")
    print("=" * 80)
    print(f"Saved summary to: {summary_path}")

    print("\nTop fully trained configs:")
    for row in stage2_results:
        print(
            f"{row['run_name']} | "
            f"best_val_loss={row['best_val_loss']:.6f} | "
            f"best_epoch={row['best_epoch']} | "
            f"params={row['num_params']:,}"
        )


if __name__ == "__main__":
    main()
