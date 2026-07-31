# Graph Neural Network for Atomic Charge Prediction

## Overview

This project implements a Graph Neural Network (GNN) using PyTorch Geometric to predict atomic partial charges within crystal structures.

Unlike traditional machine learning models that operate on vectors or images, this project represents each atomic structure as a graph, allowing the network to learn from both the properties of individual atoms and their local neighborhood connectivity.

The model was developed as part of a graduate-level Deep Learning course and explores graph-based neural network architectures for scientific regression tasks.

## Features

- Graph construction from atomic crystal structures
- Automatic parsing of POSCAR-style configuration files
- Node feature generation using one-hot encoded atomic species
- Edge construction using interatomic distance thresholds
- Edge attributes representing pairwise atomic distances
- Graph Convolutional Network (GCN) architecture
- Node-level regression for atomic charge prediction
- Randomized hyperparameter search
- Multi-stage model selection
- Automatic checkpointing
- Evaluation and visualization utilities

## Project Structure

```
.
├── PASCAR/                 # Crystal structure files
├── CHARGESSS/              # Ground-truth atomic charges
├── src/                    # Source code Scripts
├── docs/                   # Assignment files: Instructions, presentation, and report with errata
├── plots/                  # Generated figures
└── README.md

```

## Dataset

Each sample consists of

- a crystal structure (POSCAR format)
- a corresponding file containing atomic partial charges

Each crystal is converted into a graph where

- atoms become graph nodes
- neighboring atoms become graph edges
- edge attributes store interatomic distances
- node labels are the target atomic charges

The processed dataset is cached after its initial construction to reduce subsequent loading time.

## Graph Representation

Node features:

- One-hot encoded atomic element type

Edges:

- Constructed using a fixed 7 Å cutoff distance (course requirement)

Edge attributes:

- Interatomic distance

Target:

- Continuous atomic charge for every atom in the graph

This produces a node-level regression problem rather than graph-level classification.

## Model Architecture

The implemented network consists of:

```
Node Features
      │
      ▼
GCN Layer
      │
Activation
      │
Dropout
      │
(repeated N times)
      │
      ▼
Linear Output Layer
      │
      ▼
Predicted Atomic Charge
```

The architecture supports configurable:

- hidden dimension
- number of GCN layers
- dropout rate
- activation function
- optimizer
- learning rate scheduler
- Training Pipeline

Training proceeds in two stages.

### Stage 1 — Hyperparameter Search

A randomized search samples combinations of:

- hidden dimensions
- network depth
- dropout
- learning rate
- weight decay
- optimizer
- activation function
- scheduler
- batch size

The best-performing configurations are ranked according to validation loss.

### Stage 2 — Full Training

The top-performing Stage 1 configurations are retrained for substantially longer runs with:

- early stopping
- checkpoint saving
- training history recording
- best-model preservation

The best overall model is selected for final evaluation.

## Evaluation

Evaluation includes

- Mean Absolute Error (MAE)
- Mean Squared Error (MSE)
- Root Mean Squared Error (RMSE)
- R² score

Additional visualization scripts generate:

- training vs. validation loss
- predicted vs. true charge scatter plots
- residual plots
- residual histograms
- hyperparameter search summaries

## Technologies Used

- Python
- PyTorch
- PyTorch Geometric
- NumPy
- ASE (Atomic Simulation Environment)
- Matplotlib

## Course Context

This project was completed as part of a graduate-level Deep Learning course.

The objective was to explore Graph Neural Networks by constructing graph representations of crystalline materials and training a graph convolutional network to predict node-level atomic properties.

While the underlying dataset and prediction task were provided as part of the course, the implementation includes a complete end-to-end training pipeline covering dataset processing, graph construction, model training, hyperparameter optimization, evaluation, and visualization.

## Repository Note

This repository preserves the submitted project while incorporating minor improvements for portability and reproducibility. These updates include:

- platform-independent path handling
- improved documentation
- clearer project organization
- additional comments and code cleanup

The underlying model architecture, training methodology, and experimental results remain consistent with the original course project.