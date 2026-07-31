from pathlib import Path
from typing import Dict, Union

import copy
import torch
import torch.nn as nn
from torch.optim import Adam, AdamW
from torch.optim.lr_scheduler import ReduceLROnPlateau

from dataset import AtomicChargeDataset
from dataset_split import split_dataset, build_dataloaders
from model import AtomicChargeGNN, count_parameters


def get_device() -> torch.device:
    return torch.device("cuda" if torch.cuda.is_available() else "cpu")


def build_optimizer(
    model: nn.Module,
    optimizer_name: str,
    lr: float,
    weight_decay: float,
) -> torch.optim.Optimizer:
    optimizer_name = optimizer_name.lower()

    if optimizer_name == "adam":
        return Adam(model.parameters(), lr=lr, weight_decay=weight_decay)
    if optimizer_name == "adamw":
        return AdamW(model.parameters(), lr=lr, weight_decay=weight_decay)

    raise ValueError(f"Unsupported optimizer: {optimizer_name}")


def build_scheduler(
    optimizer: torch.optim.Optimizer,
    scheduler_name: str,
):
    scheduler_name = scheduler_name.lower()

    if scheduler_name == "none":
        return None
    if scheduler_name == "plateau":
        return ReduceLROnPlateau(
            optimizer,
            mode="min",
            factor=0.5,
            patience=3,
        )

    raise ValueError(f"Unsupported scheduler: {scheduler_name}")


def train_one_epoch(
    model: nn.Module,
    loader,
    optimizer: torch.optim.Optimizer,
    criterion: nn.Module,
    device: torch.device,
) -> float:
    model.train()

    total_loss = 0.0
    total_nodes = 0

    for batch in loader:
        batch = batch.to(device)

        optimizer.zero_grad()

        pred = model(batch.x, batch.edge_index, batch.edge_attr)
        loss = criterion(pred, batch.y)

        loss.backward()
        optimizer.step()

        num_nodes = batch.x.size(0)
        total_loss += loss.item() * num_nodes
        total_nodes += num_nodes

    return total_loss / total_nodes


@torch.no_grad()
def evaluate_one_epoch(
    model: nn.Module,
    loader,
    criterion: nn.Module,
    device: torch.device,
) -> float:
    model.eval()

    total_loss = 0.0
    total_nodes = 0

    for batch in loader:
        batch = batch.to(device)

        pred = model(batch.x, batch.edge_index, batch.edge_attr)
        loss = criterion(pred, batch.y)

        num_nodes = batch.x.size(0)
        total_loss += loss.item() * num_nodes
        total_nodes += num_nodes

    return total_loss / total_nodes


def train_model(
    model: nn.Module,
    train_loader,
    val_loader,
    device: torch.device,
    lr: float = 1e-3,
    weight_decay: float = 1e-5,
    optimizer_name: str = "adam",
    scheduler_name: str = "none",
    max_epochs: int = 100,
    patience: int = 15,
    verbose: bool = True,
) -> Dict:
    criterion = nn.L1Loss()
    optimizer = build_optimizer(model, optimizer_name, lr, weight_decay)
    scheduler = build_scheduler(optimizer, scheduler_name)

    history: Dict = {
        "train_loss": [],
        "val_loss": [],
        "best_val_loss": float("inf"),
        "best_epoch": -1,
        "best_model_state": None,
    }

    epochs_without_improvement = 0

    for epoch in range(1, max_epochs + 1):
        train_loss = train_one_epoch(
            model=model,
            loader=train_loader,
            optimizer=optimizer,
            criterion=criterion,
            device=device,
        )

        val_loss = evaluate_one_epoch(
            model=model,
            loader=val_loader,
            criterion=criterion,
            device=device,
        )

        if scheduler is not None:
            if scheduler_name.lower() == "plateau":
                scheduler.step(val_loss)
            else:
                scheduler.step()

        history["train_loss"].append(train_loss)
        history["val_loss"].append(val_loss)

        if verbose:
            print(
                f"Epoch {epoch:03d} | "
                f"Train Loss: {train_loss:.6f} | "
                f"Val Loss: {val_loss:.6f}"
            )

        if val_loss < history["best_val_loss"]:
            history["best_val_loss"] = val_loss
            history["best_epoch"] = epoch
            history["best_model_state"] = copy.deepcopy(model.state_dict())
            epochs_without_improvement = 0
        else:
            epochs_without_improvement += 1

        if epochs_without_improvement >= patience:
            if verbose:
                print(f"\nEarly stopping triggered at epoch {epoch}.")
            break

    return history


def save_checkpoint(
    model: nn.Module,
    history: dict,
    save_path: Union[str, Path],
) -> None:
    save_path = Path(save_path)
    save_path.parent.mkdir(parents=True, exist_ok=True)

    torch.save(
        {
            "model_state_dict": history["best_model_state"],
            "best_val_loss": history["best_val_loss"],
            "best_epoch": history["best_epoch"],
        },
        save_path,
    )


def main() -> None:
    project_root = Path(__file__).resolve().parent
    raw_dataset_root = Path(__file__).resolve().parent
    processed_dataset_root = project_root / "dataset_cache"

    print("Loading dataset...")
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
        seed=42,
    )

    train_loader, val_loader, test_loader = build_dataloaders(
        train_set,
        val_set,
        test_set,
        batch_size=4,
        num_workers=0,
    )

    device = get_device()
    print(f"Using device: {device}")
    print(f"Node feature dimension: {dataset.dim_node_features}")

    model = AtomicChargeGNN(
        in_channels=dataset.dim_node_features,
        hidden_channels=64,
        num_layers=3,
        dropout=0.2,
        activation="relu",
        use_edge_attr=True,
    ).to(device)

    print(f"Trainable parameters: {count_parameters(model):,}")

    history = train_model(
        model=model,
        train_loader=train_loader,
        val_loader=val_loader,
        device=device,
        lr=1e-3,
        weight_decay=1e-5,
        optimizer_name="adam",
        scheduler_name="none",
        max_epochs=100,
        patience=15,
        verbose=True,
    )

    if history["best_model_state"] is not None:
        model.load_state_dict(history["best_model_state"])

    save_checkpoint(
        model=model,
        history=history,
        save_path=project_root / "checkpoints" / "best_model.pt",
    )

    print("\nTraining complete.")
    print(f"Best epoch    : {history['best_epoch']}")
    print(f"Best val loss : {history['best_val_loss']:.6f}")


if __name__ == "__main__":
    main()
