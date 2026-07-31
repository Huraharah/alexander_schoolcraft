"""
CIFAR-10 CNN Trainer (PyTorch)
- Data loading & visualization helpers
- CNN model (VGG-ish) with Dropout & BatchNorm
- Data augmentation pipeline (basic/strong/none)
- Hyperparameter CLI (lr, batch, epochs, weight decay, dropout, optimizer, scheduler)
- Training loop with mixed precision and early stopping
- Evaluation: accuracy, per-class accuracy, confusion matrix
- Checkpointing: best model by val accuracy
- Reproducible seeds

Usage (examples):
    python cifar10_cnn.py --epochs 30 --batch-size 128 --lr 3e-4 --aug basic
    python cifar10_cnn.py --optimizer sgd --momentum 0.9 --wd 5e-4 --scheduler cos --epochs 100
"""
import argparse
import itertools
import os
import random
from dataclasses import dataclass
from typing import Tuple, List

import torch
import torch.nn as nn
import torch.nn.functional as F
import torch.backends.cudnn as cudnn
from torch.optim.lr_scheduler import CosineAnnealingLR, StepLR, OneCycleLR
from torch.utils.data import DataLoader

import torchvision
from torchvision import transforms
from torchvision.datasets import CIFAR10

try:
    import matplotlib.pyplot as plt
except Exception:
    plt = None

SEED = 1337

def set_seed(seed: int = SEED):
    random.seed(seed)
    torch.manual_seed(seed)
    torch.cuda.manual_seed_all(seed)
    torch.use_deterministic_algorithms(False)  # allow performant kernels
    cudnn.benchmark = True  # speed on fixed size
    cudnn.deterministic = False

# ------------------------------
# Data
# ------------------------------
def cifar10_transforms(aug: str = "basic") -> Tuple[transforms.Compose, transforms.Compose]:
    mean = (0.4914, 0.4822, 0.4465)
    std = (0.2023, 0.1994, 0.2010)

    if aug == "none":
        train_tfms = transforms.Compose([
            transforms.ToTensor(),
            transforms.Normalize(mean, std),
        ])
    elif aug == "strong":
        # Stronger policy while staying within classic CNN-friendly augs
        train_tfms = transforms.Compose([
            transforms.RandomHorizontalFlip(p=0.5),
            transforms.RandomCrop(32, padding=4, padding_mode='reflect'),
            transforms.ColorJitter(brightness=0.2, contrast=0.2, saturation=0.2, hue=0.02),
            transforms.RandomGrayscale(p=0.05),
            transforms.RandomRotation(10, fill=(int(mean[0]*255), int(mean[1]*255), int(mean[2]*255))),
            transforms.ToTensor(),
            transforms.Normalize(mean, std),
        ])
    else:  # "basic"
        train_tfms = transforms.Compose([
            transforms.RandomHorizontalFlip(p=0.5),
            transforms.RandomCrop(32, padding=4, padding_mode='reflect'),
            transforms.ToTensor(),
            transforms.Normalize(mean, std),
        ])

    test_tfms = transforms.Compose([
        transforms.ToTensor(),
        transforms.Normalize(mean, std),
    ])
    return train_tfms, test_tfms


def get_dataloaders(data_dir: str, batch_size: int, workers: int, aug: str) -> Tuple[DataLoader, DataLoader]:
    train_tfms, test_tfms = cifar10_transforms(aug=aug)
    train_set = CIFAR10(root=data_dir, train=True, download=True, transform=train_tfms)
    test_set = CIFAR10(root=data_dir, train=False, download=True, transform=test_tfms)

    # Split train into train/val
    val_ratio = 0.1
    num_train = len(train_set)
    num_val = int(num_train * val_ratio)
    num_train = num_train - num_val
    train_subset, val_subset = torch.utils.data.random_split(train_set, [num_train, num_val], generator=torch.Generator().manual_seed(SEED))

    train_loader = DataLoader(train_subset, batch_size=batch_size, shuffle=True, num_workers=workers, pin_memory=True)
    val_loader = DataLoader(val_subset, batch_size=batch_size, shuffle=False, num_workers=workers, pin_memory=True)

    return train_loader, val_loader, test_set, test_tfms


# ------------------------------
# Model: VGG-like CNN
# ------------------------------
class ConvBlock(nn.Module):
    def __init__(self, in_ch, out_ch, p_drop=0.0):
        super().__init__()
        self.conv = nn.Conv2d(in_ch, out_ch, kernel_size=3, padding=1, bias=False)
        self.bn = nn.BatchNorm2d(out_ch)
        self.relu = nn.ReLU(inplace=True)
        self.drop = nn.Dropout(p_drop) if p_drop > 0 else nn.Identity()

    def forward(self, x):
        x = self.conv(x)
        x = self.bn(x)
        x = self.relu(x)
        x = self.drop(x)
        return x


class SmallVGG(nn.Module):
    """A compact VGG-ish CNN suitable for CIFAR-10."""
    def __init__(self, num_classes=10, dropout=0.3):
        super().__init__()
        # 32x32 input
        self.features = nn.Sequential(
            ConvBlock(3, 64, p_drop=dropout/2),
            ConvBlock(64, 64, p_drop=dropout/2),
            nn.MaxPool2d(2),  # 16x16

            ConvBlock(64, 128, p_drop=dropout/2),
            ConvBlock(128, 128, p_drop=dropout/2),
            nn.MaxPool2d(2),  # 8x8

            ConvBlock(128, 256, p_drop=dropout),
            ConvBlock(256, 256, p_drop=dropout),
            nn.MaxPool2d(2),  # 4x4
        )
        self.classifier = nn.Sequential(
            nn.Flatten(),
            nn.Linear(256 * 4 * 4, 512),
            nn.ReLU(inplace=True),
            nn.Dropout(dropout),
            nn.Linear(512, num_classes),
        )

    def forward(self, x):
        x = self.features(x)
        x = self.classifier(x)
        return x


# ------------------------------
# Training
# ------------------------------
@dataclass
class TrainConfig:
    epochs: int = 30
    lr: float = 3e-4
    batch_size: int = 128
    weight_decay: float = 5e-4
    dropout: float = 0.3
    optimizer: str = "adamw"  # adamw | sgd
    momentum: float = 0.9
    scheduler: str = "cos"    # cos | step | onecycle | none
    step_size: int = 30
    gamma: float = 0.1
    aug: str = "basic"        # basic | strong | none
    patience: int = 10        # early stopping
    label_smoothing: float = 0.0
    workers: int = 4
    data_dir: str = "./data"
    out_dir: str = "./outputs"


def make_optimizer(model: nn.Module, cfg: TrainConfig):
    if cfg.optimizer.lower() == "sgd":
        return torch.optim.SGD(model.parameters(), lr=cfg.lr, momentum=cfg.momentum, weight_decay=cfg.weight_decay, nesterov=True)
    else:
        return torch.optim.AdamW(model.parameters(), lr=cfg.lr, weight_decay=cfg.weight_decay)


def make_scheduler(optimizer, cfg: TrainConfig, steps_per_epoch: int):
    if cfg.scheduler == "cos":
        return CosineAnnealingLR(optimizer, T_max=cfg.epochs)
    elif cfg.scheduler == "step":
        return StepLR(optimizer, step_size=cfg.step_size, gamma=cfg.gamma)
    elif cfg.scheduler == "onecycle":
        # OneCycle needs total steps
        total_steps = cfg.epochs * steps_per_epoch
        return OneCycleLR(optimizer, max_lr=cfg.lr, total_steps=total_steps)
    else:
        return None


def accuracy_from_logits(logits, targets):
    preds = logits.argmax(dim=1)
    correct = (preds == targets).sum().item()
    total = targets.size(0)
    return correct, total


def evaluate(model, loader, device):
    model.eval()
    correct = total = 0
    with torch.no_grad():
        for x, y in loader:
            x, y = x.to(device), y.to(device)
            logits = model(x)
            c, t = accuracy_from_logits(logits, y)
            correct += c
            total += t
    return correct / total


def run(cfg: TrainConfig):
    os.makedirs(cfg.out_dir, exist_ok=True)
    set_seed(SEED)

    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"Using device: {device}")

    # Data
    train_loader, val_loader, test_set, test_tfms = get_dataloaders(cfg.data_dir, cfg.batch_size, cfg.workers, cfg.aug)

    # Model, opt, sched
    model = SmallVGG(num_classes=10, dropout=cfg.dropout).to(device)
    opt = make_optimizer(model, cfg)
    steps_per_epoch = len(train_loader)
    sch = make_scheduler(opt, cfg, steps_per_epoch)

    # Loss
    criterion = nn.CrossEntropyLoss(label_smoothing=cfg.label_smoothing)

    scaler = torch.cuda.amp.GradScaler(enabled=torch.cuda.is_available())

    best_val = 0.0
    epochs_no_improve = 0
    best_path = os.path.join(cfg.out_dir, "best.pth")

    # Training loop
    for epoch in range(1, cfg.epochs + 1):
        model.train()
        running_loss = 0.0
        correct = total = 0

        for i, (x, y) in enumerate(train_loader, start=1):
            x, y = x.to(device), y.to(device)

            opt.zero_grad(set_to_none=True)
            with torch.cuda.amp.autocast(enabled=torch.cuda.is_available()):
                logits = model(x)
                loss = criterion(logits, y)

            scaler.scale(loss).backward()
            scaler.step(opt)
            scaler.update()

            if sch is not None and cfg.scheduler != "onecycle":
                sch.step()
            elif sch is not None and cfg.scheduler == "onecycle":
                sch.step()

            running_loss += loss.item() * y.size(0)
            c, t = accuracy_from_logits(logits, y)
            correct += c
            total += t

        train_loss = running_loss / total
        train_acc = correct / total

        # Validation
        val_acc = evaluate(model, val_loader, device)

        print(f"[{epoch:03d}/{cfg.epochs}] loss={train_loss:.4f} train_acc={train_acc*100:.2f}% val_acc={val_acc*100:.2f}%")

        # Early stopping & checkpoint
        if val_acc > best_val:
            best_val = val_acc
            epochs_no_improve = 0
            torch.save({"model": model.state_dict(), "val_acc": best_val, "cfg": cfg.__dict__}, best_path)
        else:
            epochs_no_improve += 1
            if epochs_no_improve >= cfg.patience:
                print(f"Early stopping at epoch {epoch}. Best val_acc={best_val*100:.2f}%")
                break

    # Load best and evaluate on test
    ckpt = torch.load(best_path, map_location=device)
    model.load_state_dict(ckpt["model"])
    print(f"Loaded best checkpoint with val_acc={ckpt.get('val_acc', 0.0)*100:.2f}%")

    # Test loader (full test set)
    test_loader = DataLoader(test_set, batch_size=cfg.batch_size, shuffle=False, num_workers=cfg.workers, pin_memory=True)
    test_acc = evaluate(model, test_loader, device)
    print(f"Test accuracy: {test_acc*100:.2f}%")

    # Confusion Matrix & per-class accuracy
    classes = test_set.classes
    cm = torch.zeros((len(classes), len(classes)), dtype=torch.int64)
    model.eval()
    with torch.no_grad():
        for x, y in test_loader:
            x, y = x.to(device), y.to(device)
            logits = model(x)
            preds = logits.argmax(dim=1)
            for t, p in zip(y.view(-1), preds.view(-1)):
                cm[t.long(), p.long()] += 1

    per_class_acc = cm.diag() / cm.sum(dim=1).clamp(min=1)
    print("Per-class accuracy:")
    for i, (cls, acc) in enumerate(zip(classes, per_class_acc)):
        print(f"  {i:2d}: {cls:12s}  {acc.item()*100:5.2f}%")

    # Optional plot if matplotlib is available
    if plt is not None:
        import numpy as np
        fig, ax = plt.subplots(figsize=(8, 7))
        ax.imshow(cm.cpu().numpy(), interpolation='nearest', cmap='Blues')
        ax.set_title('Confusion Matrix (CIFAR-10)')
        tick_marks = np.arange(len(classes))
        ax.set_xticks(tick_marks, classes, rotation=45, ha='right')
        ax.set_yticks(tick_marks, classes)
        ax.set_ylabel('True label')
        ax.set_xlabel('Predicted label')
        fig.tight_layout()
        os.makedirs(cfg.out_dir, exist_ok=True)
        figpath = os.path.join(cfg.out_dir, "confusion_matrix.png")
        plt.savefig(figpath, dpi=150, bbox_inches='tight')
        print(f"Saved confusion matrix to {figpath}")


def build_argparser():
    p = argparse.ArgumentParser(description="Train a CIFAR-10 CNN (PyTorch)")
    p.add_argument("--epochs", type=int, default=30)
    p.add_argument("--batch-size", type=int, default=128)
    p.add_argument("--lr", type=float, default=3e-4)
    p.add_argument("--wd", type=float, default=5e-4)
    p.add_argument("--dropout", type=float, default=0.3)
    p.add_argument("--optimizer", choices=["adamw", "sgd"], default="adamw")
    p.add_argument("--momentum", type=float, default=0.9)
    p.add_argument("--scheduler", choices=["cos", "step", "onecycle", "none"], default="cos")
    p.add_argument("--step-size", type=int, default=30)
    p.add_argument("--gamma", type=float, default=0.1)
    p.add_argument("--aug", choices=["basic", "strong", "none"], default="basic")
    p.add_argument("--patience", type=int, default=10)
    p.add_argument("--label-smoothing", type=float, default=0.0)
    p.add_argument("--workers", type=int, default=4)
    p.add_argument("--data-dir", type=str, default="./data")
    p.add_argument("--out-dir", type=str, default="./outputs")
    return p


def main():
    args = build_argparser().parse_args()
    cfg = TrainConfig(
        epochs=args.epochs,
        lr=args.lr,
        batch_size=args.batch_size,
        weight_decay=args.wd,
        dropout=args.dropout,
        optimizer=args.optimizer,
        momentum=args.momentum,
        scheduler=args.scheduler,
        step_size=args.step_size,
        gamma=args.gamma,
        aug=args.aug,
        patience=args.patience,
        label_smoothing=args.label_smoothing,
        workers=args.workers,
        data_dir=args.data_dir,
        out_dir=args.out_dir,
    )
    run(cfg)


if __name__ == "__main__":
    main()
