from pathlib import Path
from typing import Any, Dict, List, Union
import json

import numpy as np
import matplotlib.pyplot as plt


# ---------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------

def ensure_dir(path: Union[str, Path]) -> Path:
    """
    Ensure that a directory exists.
    """
    path = Path(path)
    path.mkdir(parents=True, exist_ok=True)
    return path


def load_json(path: Union[str, Path]) -> Any:
    """
    Load a JSON file and return its contents.
    """
    path = Path(path)
    if not path.exists():
        raise FileNotFoundError(f"JSON file not found: {path}")

    with path.open("r") as f:
        return json.load(f)


def load_npz(path: Union[str, Path]) -> Dict[str, np.ndarray]:
    """
    Load an NPZ file and return a dictionary of arrays.
    """
    path = Path(path)
    if not path.exists():
        raise FileNotFoundError(f"NPZ file not found: {path}")

    data = np.load(path)
    return {key: data[key] for key in data.files}


# ---------------------------------------------------------------------
# Plot 1: Best model loss curves
# ---------------------------------------------------------------------

def plot_best_model_loss_curves(
    history_json_path: Union[str, Path],
    save_dir: Union[str, Path] = "plots",
) -> Path:
    """
    Plot training and validation loss curves for the best fully trained model.

    Expects a stage-2 history JSON file with:
        - train_loss
        - val_loss
        - best_epoch
        - best_val_loss
    """
    payload = load_json(history_json_path)
    save_dir = ensure_dir(save_dir)

    train_loss = payload["train_loss"]
    val_loss = payload["val_loss"]
    best_epoch = payload["best_epoch"]
    best_val_loss = payload["best_val_loss"]

    epochs = np.arange(1, len(train_loss) + 1)

    plt.figure(figsize=(8, 5))
    plt.plot(epochs, train_loss, label="Train Loss")
    plt.plot(epochs, val_loss, label="Validation Loss")
    plt.axvline(best_epoch, linestyle="--", label=f"Best Epoch ({best_epoch})")

    # Use log scale for better visibility of loss trends (first epochs often have much higher loss, and skews the plot)
    plt.yscale("log")

    plt.xlabel("Epoch")
    plt.ylabel("MAE Loss (log scale)")
    plt.title("Best Model: Training vs Validation Loss")
    plt.legend()
    plt.tight_layout()

    out_path = save_dir / "best_model_loss_curves.png"
    plt.savefig(out_path, dpi=200)
    plt.close()

    return out_path


# ---------------------------------------------------------------------
# Plot 2: Stage-1 tuning summary
# ---------------------------------------------------------------------

def plot_stage1_summary(
    stage1_json_path: Union[str, Path],
    save_dir: Union[str, Path] = "plots",
    top_n: int = 20,
) -> Path:
    """
    Plot the stage-1 tuning results as a ranked validation-loss chart.

    Expects stage-1 results JSON to be a list of dicts sorted or unsorted,
    each containing:
        - best_val_loss
    """
    results = load_json(stage1_json_path)
    save_dir = ensure_dir(save_dir)

    results = sorted(results, key=lambda x: x["best_val_loss"])[:top_n]

    labels = [f"Trial {i+1}" for i in range(len(results))]
    losses = [row["best_val_loss"] for row in results]

    plt.figure(figsize=(10, 5))
    plt.bar(np.arange(len(losses)), losses)
    plt.xticks(np.arange(len(losses)), labels, rotation=45, ha="right")
    plt.ylabel("Best Validation Loss")
    plt.title("Stage-1 Hyperparameter Search: Top Ranked Configurations")
    plt.tight_layout()

    out_path = save_dir / "stage1_summary.png"
    plt.savefig(out_path, dpi=200)
    plt.close()

    return out_path


# ---------------------------------------------------------------------
# Plot 3: Stage-2 finalist comparison
# ---------------------------------------------------------------------

def plot_stage2_finalists(
    stage2_json_path: Union[str, Path],
    save_dir: Union[str, Path] = "plots",
) -> Path:
    """
    Plot the fully trained stage-2 finalist models by best validation loss.

    Expects stage-2 results JSON to be a list of dicts containing:
        - run_name
        - best_val_loss
    """
    results = load_json(stage2_json_path)
    save_dir = ensure_dir(save_dir)

    results = sorted(results, key=lambda x: x["best_val_loss"])

    labels = [row["run_name"] for row in results]
    losses = [row["best_val_loss"] for row in results]

    plt.figure(figsize=(12, 5))
    plt.bar(np.arange(len(losses)), losses)
    plt.xticks(np.arange(len(losses)), labels, rotation=45, ha="right")
    plt.ylabel("Best Validation Loss")
    plt.title("Stage-2 Finalist Comparison")
    plt.tight_layout()

    out_path = save_dir / "stage2_finalists.png"
    plt.savefig(out_path, dpi=200)
    plt.close()

    return out_path


# ---------------------------------------------------------------------
# Plot 4: Prediction vs true scatter
# ---------------------------------------------------------------------

def plot_prediction_vs_true(
    predictions_npz_path: Union[str, Path],
    save_dir: Union[str, Path] = "plots",
    max_points: Union[int, None] = 20000,
) -> Path:
    """
    Plot predicted vs true charges for the final test set.

    If max_points is set and the dataset is large, subsample for readability.
    """
    arrays = load_npz(predictions_npz_path)
    save_dir = ensure_dir(save_dir)

    y_true = arrays["y_true"]
    y_pred = arrays["y_pred"]

    if max_points is not None and len(y_true) > max_points:
        rng = np.random.default_rng(42)
        idx = rng.choice(len(y_true), size=max_points, replace=False)
        y_true = y_true[idx]
        y_pred = y_pred[idx]

    min_val = min(y_true.min(), y_pred.min())
    max_val = max(y_true.max(), y_pred.max())

    plt.figure(figsize=(6, 6))
    plt.scatter(y_true, y_pred, s=8, alpha=0.4)
    plt.plot([min_val, max_val], [min_val, max_val], linestyle="--")
    plt.xlabel("True Charge")
    plt.ylabel("Predicted Charge")
    plt.title("Predicted vs True Atomic Charges")
    plt.tight_layout()

    out_path = save_dir / "prediction_vs_true.png"
    plt.savefig(out_path, dpi=200)
    plt.close()

    return out_path


# ---------------------------------------------------------------------
# Plot 5: Residual histogram
# ---------------------------------------------------------------------

def plot_residual_histogram(
    predictions_npz_path: Union[str, Path],
    save_dir: Union[str, Path] = "plots",
    bins: int = 50,
) -> Path:
    """
    Plot a histogram of residuals (prediction - truth).
    """
    arrays = load_npz(predictions_npz_path)
    save_dir = ensure_dir(save_dir)

    y_true = arrays["y_true"]
    y_pred = arrays["y_pred"]
    residuals = y_pred - y_true

    plt.figure(figsize=(8, 5))
    plt.hist(residuals, bins=bins)
    plt.xlabel("Residual (Predicted - True)")
    plt.ylabel("Count")
    plt.title("Residual Histogram")
    plt.tight_layout()

    out_path = save_dir / "residual_histogram.png"
    plt.savefig(out_path, dpi=200)
    plt.close()

    return out_path


# ---------------------------------------------------------------------
# Plot 6: Residuals vs true values
# ---------------------------------------------------------------------

def plot_residuals_vs_true(
    predictions_npz_path: Union[str, Path],
    save_dir: Union[str, Path] = "plots",
    max_points: Union[int, None] = 20000,
) -> Path:
    """
    Plot residuals against true target values to inspect systematic bias.
    """
    arrays = load_npz(predictions_npz_path)
    save_dir = ensure_dir(save_dir)

    y_true = arrays["y_true"]
    y_pred = arrays["y_pred"]
    residuals = y_pred - y_true

    if max_points is not None and len(y_true) > max_points:
        rng = np.random.default_rng(42)
        idx = rng.choice(len(y_true), size=max_points, replace=False)
        y_true = y_true[idx]
        residuals = residuals[idx]

    plt.figure(figsize=(8, 5))
    plt.scatter(y_true, residuals, s=8, alpha=0.4)
    plt.axhline(0.0, linestyle="--")
    plt.xlabel("True Charge")
    plt.ylabel("Residual (Predicted - True)")
    plt.title("Residuals vs True Atomic Charges")
    plt.tight_layout()

    out_path = save_dir / "residuals_vs_true.png"
    plt.savefig(out_path, dpi=200)
    plt.close()

    return out_path


# ---------------------------------------------------------------------
# Driver
# ---------------------------------------------------------------------

def main() -> None:
    """
    Generate all report-ready plots from saved stage-1, stage-2, and eval artifacts.
    """
    print("Starting plots.py")
    out_path = Path(__file__).resolve().parent
    stage1_json = out_path / "tuning_results_stage1.json"
    stage2_json = out_path / "stage2_results_full.json"
    eval_metrics_json = out_path / "evaluation/test_metrics.json"  # not plotted directly, but useful to ensure eval ran
    predictions_npz = out_path / "evaluation/test_predictions.npz"
    plots_dir = out_path / "plots"

    # Load stage-2 summary so we can find the best history JSON automatically
    stage2_results = load_json(stage2_json)
    stage2_results = sorted(stage2_results, key=lambda x: x["best_val_loss"])
    best_history_json = Path(stage2_results[0]["history_path"])

    created = []

    created.append(plot_best_model_loss_curves(best_history_json, save_dir=plots_dir))
    created.append(plot_stage1_summary(stage1_json, save_dir=plots_dir, top_n=20))
    created.append(plot_stage2_finalists(stage2_json, save_dir=plots_dir))
    created.append(plot_prediction_vs_true(predictions_npz, save_dir=plots_dir))
    created.append(plot_residual_histogram(predictions_npz, save_dir=plots_dir))
    created.append(plot_residuals_vs_true(predictions_npz, save_dir=plots_dir))

    print("Generated plots:")
    for path in created:
        print(f"  {path}")


if __name__ == "__main__":
    main()
