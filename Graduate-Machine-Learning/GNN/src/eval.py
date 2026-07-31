from pathlib import Path
from typing import Any, Dict, List, Tuple, Union
import json

import numpy as np
import torch

from dataset import AtomicChargeDataset
from dataset_split import split_dataset, build_dataloaders
from model import AtomicChargeGNN
from train import get_device


def load_stage2_results(json_path: Union[str, Path]) -> List[Dict[str, Any]]:
    """
    Load the stage-2 training summary JSON.
    """
    json_path = Path(json_path)

    if not json_path.exists():
        raise FileNotFoundError(f"Stage-2 results file not found: {json_path}")

    with json_path.open("r") as f:
        results = json.load(f)

    if not isinstance(results, list) or len(results) == 0:
        raise ValueError("Stage-2 results JSON is empty or malformed")

    return results


def select_best_stage2_result(results: List[Dict[str, Any]]) -> Dict[str, Any]:
    """
    Select the best result by lowest best_val_loss.
    """
    return min(results, key=lambda x: x["best_val_loss"])


def build_model_from_config(
    config: Dict[str, Any],
    device: torch.device,
    in_channels: int) -> AtomicChargeGNN:
    """
    Rebuild the model from a saved tuning config.
    """
    model = AtomicChargeGNN(
        in_channels=in_channels,
        hidden_channels=config["hidden_channels"],
        num_layers=config["num_layers"],
        dropout=config["dropout"],
        activation=config["activation"],
    ).to(device)

    return model


@torch.no_grad()
def collect_predictions(
    model: torch.nn.Module,
    loader,
    device: torch.device,
) -> Tuple[np.ndarray, np.ndarray]:
    """
    Run inference over a loader and collect predictions and true labels.

    Returns
    -------
    y_true : np.ndarray, shape [N]
    y_pred : np.ndarray, shape [N]
    """
    model.eval()

    all_true = []
    all_pred = []

    for batch in loader:
        batch = batch.to(device)

        pred = model(batch.x, batch.edge_index, batch.edge_attr)

        all_true.append(batch.y.detach().cpu().numpy())
        all_pred.append(pred.detach().cpu().numpy())

    y_true = np.concatenate(all_true, axis=0).reshape(-1)
    y_pred = np.concatenate(all_pred, axis=0).reshape(-1)

    return y_true, y_pred


def compute_regression_metrics(y_true: np.ndarray, y_pred: np.ndarray) -> Dict[str, float]:
    """
    Compute standard regression metrics for atomic charge prediction.
    """
    mse = float(np.mean((y_pred - y_true) ** 2))
    rmse = float(np.sqrt(mse))
    mae = float(np.mean(np.abs(y_pred - y_true)))

    # R^2 = 1 - SS_res / SS_tot
    ss_res = float(np.sum((y_true - y_pred) ** 2))
    ss_tot = float(np.sum((y_true - np.mean(y_true)) ** 2))
    r2 = float(1.0 - (ss_res / ss_tot)) if ss_tot > 0 else float("nan")

    return {
        "mse": mse,
        "rmse": rmse,
        "mae": mae,
        "r2": r2,
    }


def save_metrics_json(
    metrics: Dict[str, Any],
    save_path: Union[str, Path],
) -> None:
    """
    Save evaluation metrics to JSON.
    """
    save_path = Path(save_path)
    save_path.parent.mkdir(parents=True, exist_ok=True)

    with save_path.open("w") as f:
        json.dump(metrics, f, indent=2)


def save_predictions_npz(
    y_true: np.ndarray,
    y_pred: np.ndarray,
    save_path: Union[str, Path],
) -> None:
    """
    Save truth/prediction arrays for later plotting.
    """
    save_path = Path(save_path)
    save_path.parent.mkdir(parents=True, exist_ok=True)

    np.savez(
        save_path,
        y_true=y_true,
        y_pred=y_pred,
    )


def main() -> None:
    # -------------------------------------------------------------
    # Paths / settings
    # -------------------------------------------------------------
    print("Starting eval.py")
    project_root = Path(__file__).resolve().parent
    raw_dataset_root = project_root
    processed_dataset_root = project_root / "dataset_cache"
    out_path = Path(__file__).resolve().parent
    stage2_results_path = out_path / "stage2_results_full.json"
    split_seed = 42

    # -------------------------------------------------------------
    # Select best trained model
    # -------------------------------------------------------------
    print("Loading stage-2 results...")
    stage2_results = load_stage2_results(stage2_results_path)
    best_result = select_best_stage2_result(stage2_results)

    print("\nBest fully trained config:")
    print(json.dumps(best_result, indent=2))

    checkpoint_path = Path(best_result["checkpoint_path"])
    config = best_result["config"]

    if not checkpoint_path.exists():
        raise FileNotFoundError(f"Checkpoint not found: {checkpoint_path}")

    # -------------------------------------------------------------
    # Rebuild dataset and exact same split
    # -------------------------------------------------------------
    print("\nLoading dataset...")
    dataset = AtomicChargeDataset(
         raw_root = raw_dataset_root,
         processed_root = processed_dataset_root,
         verbose=False
    )

    train_set, val_set, test_set = split_dataset(
        dataset,
        train_ratio=0.70,
        val_ratio=0.15,
        test_ratio=0.15,
        seed=split_seed,
    )

    # Use the winning batch size for evaluation consistency
    _, _, test_loader = build_dataloaders(
        train_set,
        val_set,
        test_set,
        batch_size=config["batch_size"],
        num_workers=0,
    )

    # -------------------------------------------------------------
    # Rebuild model and load checkpoint
    # -------------------------------------------------------------
    device = get_device()
    print(f"Using device: {device}")

    model = build_model_from_config(
        config,
        in_channels=dataset.dim_node_features,
        device=device)

    checkpoint = torch.load(checkpoint_path, map_location=device, weights_only=False)
    model.load_state_dict(checkpoint["model_state_dict"])

    print("\nLoaded checkpoint successfully.")
    print(f"Checkpoint best epoch    : {checkpoint['best_epoch']}")
    print(f"Checkpoint best val loss : {checkpoint['best_val_loss']:.6f}")

    # -------------------------------------------------------------
    # Run test inference
    # -------------------------------------------------------------
    print("\nRunning inference on test set...")
    y_true, y_pred = collect_predictions(
        model=model,
        loader=test_loader,
        device=device,
    )

    metrics = compute_regression_metrics(y_true, y_pred)

    summary = {
        "run_name": best_result["run_name"],
        "config": config,
        "checkpoint_path": str(checkpoint_path),
        "num_test_predictions": int(len(y_true)),
        "metrics": metrics,
    }

    print("\nTest Metrics")
    print("=" * 72)
    print(f"MAE  : {metrics['mae']:.6f}")
    print(f"MSE  : {metrics['mse']:.6f}")
    print(f"RMSE : {metrics['rmse']:.6f}")
    print(f"R^2  : {metrics['r2']:.6f}")

    # -------------------------------------------------------------
    # Save artifacts for later plotting / reporting
    # -------------------------------------------------------------
    metrics_path = out_path / "evaluation/test_metrics.json"
    preds_path = out_path / "evaluation/test_predictions.npz"

    save_metrics_json(summary, metrics_path)
    save_predictions_npz(y_true, y_pred, preds_path)

    print("\nSaved evaluation artifacts:")
    print(f"Metrics JSON  : {metrics_path}")
    print(f"Predictions   : {preds_path}")


if __name__ == "__main__":
    main()
