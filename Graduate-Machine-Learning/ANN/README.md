# Handwritten Digit Classification with a Dense Neural Network

Implementation of a fully-connected Artificial Neural Network (ANN) in PyTorch for handwritten digit classification using the MNIST benchmark dataset.

This project was completed for **CSCI 6840 – Graduate Machine Learning**. The model architecture, training pipeline, evaluation metrics, and visualization utilities were implemented in Python using PyTorch. The project achieved 98.22% test accuracy on the MNIST test set.

## Overview

This project demonstrates the complete supervised learning workflow for image classification:

Dataset preparation
Neural network construction
Model training
Hyperparameter tuning
Evaluation
Performance visualization

Rather than only reporting classification accuracy, the project generates multiple evaluation artifacts to better understand model behavior, including learning curves, confusion matrices, prediction visualizations, and confidence analysis.

## Features

- Dense (Fully Connected) Neural Network
- PyTorch implementation
- GPU acceleration (CUDA when available)
- ReLU activations
- Dropout regularization
- Adam optimizer
- StepLR learning-rate scheduler
- Training/validation split
- Automatic figure generation
- Classification report
- Confusion matrix
- Prediction confidence visualization
- CSV export of predictions

## Model Architecture

```
Input (28×28)
      │
Flatten (784)
      │
Linear (784 → 256)
ReLU
Dropout (10%)
      │
Linear (256 → 128)
ReLU
Dropout (10%)
      │
Linear (128 → 10)
      │
CrossEntropyLoss
```

Training configuration:

- Optimizer: Adam
- Learning Rate: 0.001
- Scheduler: StepLR
- Epochs: 20
- Batch Size: 128

The implementation follows the project requirements while incorporating regularization and learning-rate scheduling to improve generalization.

## Dataset

The model is trained on the MNIST handwritten digit dataset:

- 70,000 grayscale images
- Image size: 28 x 28 pixels
- 10 output classes (digits 0-9)

Dataset split:

- Training: 50,000
- Validation: 10,000
- Testing: 10,000

Images are normalized using the standard MNIST mean and standard deviation before training.

## Results

Final Performance:

| Metric | Value |
|--------|-------|
| Test Accuracy | 98.22% |
| Test Loss | 0.0694 |
| Training Accuracy | 99.65% |
| Validation Accuracy | 97.93% |

The model converges rapidly during training while exhibiting only minor overfitting in the final few epochs, as shown by the small separation between the training and validation curves.

## Generated Outputs

Running the program automatically produces:

- Training loss curve
- Validation loss curve
- Accuracy curve
- Confusion matrix
- Random prediction gallery
- Highest confidence missclassification gallery
- Sample prediction CSV

These artifacts provide both quantitative and qualitative evaluation of the trained model.

## Repository Structure

```
ANN_MNIST/
│
├── NeuralNets.py
├── requirements.txt
├── README.md
│
├── docs/
│   ├── Assignment.pdf
│   ├── Artificial Neural Network Report.pdf
│   └── Artificial Neural Network Presentation.pdf
│
└── figs/
    ├── accuracy_curves.png
    ├── loss_curves.png
    ├── confusion_matrix.png
    ├── test_random_preds.png
    ├── test_top_miscls.png
    └── sample_preds.csv
```

## Running

```bash
python -m pip install -r requirements.txt
python NeuralNets.py
```

The program automatically downloads the MNIST dataset (if necessary), trains the model, evaluates it on the test set, and generates all figures and evaluation outputs.

## Skills Demonstrated

- Deep Learning
- Artificial Neural Networks
- PyTorch
- Computer Vision
- Image Classification
- GPU Computing (CUDA)
- Model Evaluation
- Hyperparameter Tuning
- Scientific Python

## Supporting Documentation

The repository also includes the original course assignment, technical report, and presentation slides in the ```docs/``` directory for reference and completeness.