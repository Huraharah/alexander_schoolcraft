# mnist_mlp.py
import os
import random
import numpy as np
import torch
import torch.nn as nn
import torch.optim as optim
import torch.nn.functional as F
from torch.utils.data import DataLoader, random_split
import torchvision
import torchvision.transforms as transforms
import matplotlib.pyplot as plt
from sklearn.metrics import classification_report, confusion_matrix
import itertools
import math
import csv

def main():
    # ---------------------------
    # Reproducibility
    # ---------------------------
    SEED = 42
    random.seed(SEED)
    np.random.seed(SEED)
    torch.manual_seed(SEED)
    torch.cuda.manual_seed_all(SEED)
    torch.backends.cudnn.deterministic = True
    torch.backends.cudnn.benchmark = False
    NUM_WORKERS = 0 if os.name == "nt" else 2  # Windows=0, Linux=2+

    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"Using device: {device}")

    # ---------------------------
    # Data: MNIST with normalization
    # ---------------------------
    transform = transforms.Compose([
        transforms.ToTensor(),
        transforms.Normalize((0.1307,), (0.3081,))  # mean/std for MNIST
    ])

    data_root = "./data"

    full_train = torchvision.datasets.MNIST(
        root=data_root, train=True, download=True, transform=transform
    )

    test_dataset = torchvision.datasets.MNIST(
        root=data_root, train=False, download=True, transform=transform
    )

    # Requirement: training 60k, test 10k, plus a validation set of 10k.
    # We'll split the original 60k into 50k train / 10k val.
    train_size = 50_000
    val_size = len(full_train) - train_size  # 10_000
    train_dataset, val_dataset = random_split(
        full_train,
        lengths=[train_size, val_size],
        generator=torch.Generator().manual_seed(SEED)
    )

    print(f"Train: {len(train_dataset)} | Val: {len(val_dataset)} | Test: {len(test_dataset)}")

    # Dataloaders
    BATCH_SIZE = 128
    train_loader = DataLoader(train_dataset, batch_size=BATCH_SIZE, shuffle=True, num_workers=NUM_WORKERS, pin_memory=True)
    val_loader   = DataLoader(val_dataset,   batch_size=BATCH_SIZE, shuffle=False, num_workers=NUM_WORKERS, pin_memory=True)
    test_loader  = DataLoader(test_dataset,  batch_size=BATCH_SIZE, shuffle=False, num_workers=NUM_WORKERS, pin_memory=True)

    # ---------------------------
    # Model: Simple Feedforward ANN (784 -> 256 -> 128 -> 10)
    # ---------------------------
    class MLP(nn.Module):
        def __init__(self, in_dim=784, h1=256, h2=128, out_dim=10, p_drop=0.1):
            super().__init__()
            self.fc1 = nn.Linear(in_dim, h1)
            self.fc2 = nn.Linear(h1, h2)
            self.fc3 = nn.Linear(h2, out_dim)
            self.dropout = nn.Dropout(p_drop)

        def forward(self, x):
            # x: (B, 1, 28, 28) -> flatten to (B, 784)
            x = x.view(x.size(0), -1)
            x = F.relu(self.fc1(x))
            x = self.dropout(x)
            x = F.relu(self.fc2(x))
            x = self.dropout(x)
            logits = self.fc3(x)           # raw scores (logits)
            return logits                   # softmax handled by CrossEntropyLoss internally

    model = MLP().to(device)
    print(model)

    # ---------------------------
    # Loss, Optimizer, Scheduler
    # ---------------------------
    criterion = nn.CrossEntropyLoss()
    optimizer = optim.Adam(model.parameters(), lr=1e-3, weight_decay=1e-4)
    scheduler = optim.lr_scheduler.StepLR(optimizer, step_size=10, gamma=0.5)

    # ---------------------------
    # Training / Evaluation helpers
    # ---------------------------
    def accuracy_from_logits(logits, targets):
        preds = logits.argmax(dim=1)
        return (preds == targets).float().mean().item()

    def run_epoch(loader, train_mode=True):
        if train_mode:
            model.train()
        else:
            model.eval()

        total_loss = 0.0
        total_acc  = 0.0
        total_ex   = 0

        with torch.set_grad_enabled(train_mode):
            for xb, yb in loader:
                xb, yb = xb.to(device, non_blocking=True), yb.to(device, non_blocking=True)

                logits = model(xb)
                loss = criterion(logits, yb)
                acc  = accuracy_from_logits(logits, yb)

                if train_mode:
                    optimizer.zero_grad(set_to_none=True)
                    loss.backward()
                    optimizer.step()

                bs = xb.size(0)
                total_loss += loss.item() * bs
                total_acc  += acc * bs
                total_ex   += bs

        return total_loss / total_ex, total_acc / total_ex

    # ---------------------------
    # Train loop
    # ---------------------------
    EPOCHS = 20
    history = {
        "train_loss": [],
        "train_acc":  [],
        "val_loss":   [],
        "val_acc":    [],
    }

    for epoch in range(1, EPOCHS + 1):
        train_loss, train_acc = run_epoch(train_loader, train_mode=True)
        val_loss,   val_acc   = run_epoch(val_loader,   train_mode=False)

        history["train_loss"].append(train_loss)
        history["train_acc"].append(train_acc)
        history["val_loss"].append(val_loss)
        history["val_acc"].append(val_acc)

        scheduler.step()

        print(f"Epoch {epoch:02d}/{EPOCHS} | "
              f"Train Loss: {train_loss:.4f}  Acc: {train_acc*100:5.2f}% | "
              f"Val Loss: {val_loss:.4f}  Acc: {val_acc*100:5.2f}%")

    # ---------------------------
    # Final evaluation on test set
    # ---------------------------
    test_loss, test_acc = run_epoch(test_loader, train_mode=False)
    print(f"\nTest Loss: {test_loss:.4f} | Test Acc: {test_acc*100:.2f}%")

    # ---------------------------
    # Plots: loss and accuracy
    # ---------------------------
    os.makedirs("figs", exist_ok=True)

    epochs = np.arange(1, EPOCHS + 1)

    plt.figure()
    plt.plot(epochs, history["train_loss"], label="Train Loss")
    plt.plot(epochs, history["val_loss"],   label="Val Loss")
    plt.xlabel("Epoch")
    plt.ylabel("Loss")
    plt.title("MNIST MLP: Loss over Epochs")
    plt.legend()
    plt.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.savefig("figs/loss_curves.png", dpi=150)

    plt.figure()
    plt.plot(epochs, np.array(history["train_acc"]) * 100, label="Train Acc")
    plt.plot(epochs, np.array(history["val_acc"])   * 100, label="Val Acc")
    plt.xlabel("Epoch")
    plt.ylabel("Accuracy (%)")
    plt.title("MNIST MLP: Accuracy over Epochs")
    plt.legend()
    plt.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.savefig("figs/accuracy_curves.png", dpi=150)

    print("Saved plots to figs/loss_curves.png and figs/accuracy_curves.png")

    # --- Confusion matrix + classification report ---

    model.eval()
    all_preds = []
    all_true  = []

    with torch.no_grad():
        for xb, yb in test_loader:
            xb = xb.to(device, non_blocking=True)
            logits = model(xb)
            preds = logits.argmax(dim=1).cpu().numpy()
            all_preds.extend(preds.tolist())
            all_true.extend(yb.numpy().tolist())

    print("\nClassification Report:")
    print(classification_report(all_true, all_preds, digits=4))

    cm = confusion_matrix(all_true, all_preds)
    classes = [str(i) for i in range(10)]

    # Plot confusion matrix
    plt.figure(figsize=(6, 5))
    plt.imshow(cm, interpolation='nearest')
    plt.title("Confusion Matrix (Test)")
    plt.colorbar()
    tick_marks = np.arange(len(classes))
    plt.xticks(tick_marks, classes)
    plt.yticks(tick_marks, classes)

    th = cm.max() / 2.0
    for i, j in itertools.product(range(cm.shape[0]), range(cm.shape[1])):
        plt.text(j, i, format(cm[i, j], 'd'),
                 horizontalalignment="center",
                 color="white" if cm[i, j] > th else "black",
                 fontsize=9)

    plt.ylabel('True label')
    plt.xlabel('Predicted label')
    plt.tight_layout()
    os.makedirs("figs", exist_ok=True)
    plt.savefig("figs/confusion_matrix.png", dpi=150)
    print("Saved confusion matrix to figs/confusion_matrix.png")

    # ---------------------------
    # Graphics: show several samples through different metrics
    # ---------------------------

    save_random_predictions(test_dataset, model, device, n=24, outfile="figs/test_random_preds.png")
    save_top_misclassifications(test_loader, model, device, k=24, outfile="figs/test_top_miscls.png")
    export_sample_predictions_csv(test_loader, model, device, n_rows=100, path="figs/sample_preds.csv")

_MNIST_MEAN, _MNIST_STD = 0.1307, 0.3081

def _denorm(img):
    # img: Tensor [1,28,28] normalized; return unclamped 0..1
    return (img * _MNIST_STD + _MNIST_MEAN).clamp(0, 1)

def _np_gray(img):
    # -> numpy 28x28 for imshow
    return _denorm(img).squeeze(0).cpu().numpy()

@torch.no_grad()
def _predict_with_conf(xb, model, device):
    logits = model(xb.to(device))
    probs = torch.softmax(logits, dim=1)
    conf, pred = probs.max(dim=1)
    return pred.cpu(), conf.cpu()

def save_random_predictions(dataset, model, device, n=24, cols=8,
                            outfile="figs/test_random_preds.png"):
    model.eval()
    os.makedirs("figs", exist_ok=True)
    idxs = random.sample(range(len(dataset)), n)
    rows = math.ceil(n / cols)
    fig, axes = plt.subplots(rows, cols, figsize=(cols*1.6, rows*1.6))
    axes = np.array(axes).reshape(-1) if n > 1 else [axes]

    for ax, i in zip(axes, idxs):
        img, true = dataset[i]          # img: [1,28,28]
        pred, conf = _predict_with_conf(img.unsqueeze(0), model, device)
        pred, conf = int(pred[0]), float(conf[0])
        ax.imshow(_np_gray(img), cmap="gray")
        ax.axis("off")
        ok = (pred == int(true))
        ax.set_title(f"T:{true}  P:{pred}\n{conf:.1%}",
                     fontsize=9, color=("forestgreen" if ok else "crimson"))

    for ax in axes[len(idxs):]:
        ax.axis("off")

    plt.tight_layout(pad=0.6)
    plt.savefig(outfile, dpi=200)
    plt.close(fig)
    print(f"Saved random predictions grid → {outfile}")

@torch.no_grad()
def save_top_misclassifications(loader, model, device, k=24, cols=8,
                                outfile="figs/test_top_miscls.png"):
    model.eval()
    os.makedirs("figs", exist_ok=True)
    candidates = []  # (conf, img, true, pred)
    for xb, yb in loader:
        logits = model(xb.to(device))
        probs = torch.softmax(logits, dim=1).cpu()
        conf, pred = probs.max(dim=1)
        wrong = pred.ne(yb)
        idxs = torch.nonzero(wrong, as_tuple=False).flatten()
        for j in idxs.tolist():
            candidates.append((float(conf[j]), xb[j].cpu(), int(yb[j]), int(pred[j])))

    if not candidates:
        print("No misclassifications found on test set.")
        return

    candidates.sort(key=lambda t: t[0], reverse=True)
    sel = candidates[:k]
    rows = math.ceil(len(sel) / cols)
    fig, axes = plt.subplots(rows, cols, figsize=(cols*1.6, rows*1.6))
    axes = np.array(axes).reshape(-1) if len(sel) > 1 else [axes]

    for ax, (c, img, true, pred) in zip(axes, sel):
        ax.imshow(_np_gray(img), cmap="gray")
        ax.axis("off")
        ax.set_title(f"T:{true}  P:{pred}\n{c:.1%}", fontsize=9, color="crimson")

    for ax in axes[len(sel):]:
        ax.axis("off")

    plt.tight_layout(pad=0.6)
    plt.savefig(outfile, dpi=200)
    plt.close(fig)
    print(f"Saved top misclassifications grid → {outfile}")

def export_sample_predictions_csv(loader, model, device, n_rows=100,
                                  path="figs/sample_preds.csv"):
    model.eval()
    os.makedirs("figs", exist_ok=True)
    rows = [("index","true","pred","confidence")]
    idx = 0
    with torch.no_grad():
        for xb, yb in loader:
            logits = model(xb.to(device))
            probs = torch.softmax(logits, dim=1).cpu()
            conf, pred = probs.max(dim=1)
            bs = xb.size(0)
            for j in range(bs):
                rows.append((idx, int(yb[j]), int(pred[j]), float(conf[j])))
                idx += 1
                if idx >= n_rows:
                    with open(path, "w", newline="") as f:
                        csv.writer(f).writerows(rows)
                    print(f"Saved sample predictions table → {path}")
                    return
    with open(path, "w", newline="") as f:
        csv.writer(f).writerows(rows)
    print(f"Saved sample predictions table → {path}")

if __name__ == "__main__":
    main()
