# CIFAR-10 Image Classification using a Confolutional Neural Network

Implementation of a Convolutional Neural Network (CNN) in PyTorch for image classification using the CIFAR-10 benchmark dataset.

This project was completed for CSCI 6840 – Graduate Machine Learning and demonstrates the complete deep learning workflow for natural image classification, including convolutional feature extraction, data augmentation, hyperparameter tuning, training, evaluation, and visualization. The model achieved approximately 88% validation accuracy while maintaining good generalization through regularization and learning-rate scheduling.

## Overview

Unlike fully connected neural networks, convolutional neural networks learn hierarchical visual features directly from images.

This project implements a custom CNN that learns:

- edges
- corners
- textures
- object parts
- high-level semantic features

through stacked convolutional layers before performing image classification across the ten CIFAR-10 categories.

The implementation emphasizes both quantitative evaluation and qualitative analysis through confusion matrices, learning curves, and prediction visualizations.

## Features

- Custom CNN implemented from scratch in PyTorch
- Convolution + ReLU feature extraction
- Max pooling
- Dropout regularization
- AdamW optimizer
- OneCycle learning-rate scheduler
- Strong data augmentation
- Early stopping / model selection
- GPU acceleration (CUDA)
- Confusion matrix
- Prediction visualization
- Misclassification analysis
- Automatic figure generation

## Network Architecture

```
Input (32×32×3 RGB)
        │
Conv2D (32 filters, 3×3)
ReLU
        │
Conv2D (32 filters, 3×3)
ReLU
        │
MaxPool (2×2)
Dropout
        │
Flatten
        │
Linear (4096 → 256)
ReLU
Dropout
        │
Linear (256 → 10)
Softmax
```

![Rough architecture diagram](docs/system%20design.png)

The architecture was intentionally kept relatively compact while incorporating modern regularization techniques such as dropout, data augmentation, weight decay, and adaptive learning-rate scheduling.

## Dataset

The model is trained using the **CIFAR-10** dataset

Dataset characteristics:

- 60,000 RGB images
- Image size: 32×32
- 10 object classes
- 50,000 training images
- 10,000 testing images

Classes:

- airplane
- automobile
- bird
- cat
- deer
- dog
- frog
- horse
- ship
- truck

## Training

Training includes several techniques to improve generalization:

- Random cropping
- Random horizontal flipping
- Random erasing
- Color jitter
- Rotation augmentation
- AdamW optimizer
- OneCycle learning-rate scheduling
- Dropout
- Weight decay

These techniques significantly reduce overfitting while improving robustness to variations in the input data.

## Results

The trained model demonstrates strong performance across visually distinctive object classes.

Typical observations include:

- High accuracy for airplanes, automobiles, ships, and trucks
- Greater confusion between visually similar classes such as cats and dogs
- Stable convergence with minimal overfitting
- Smooth reduction in training loss throughout optimization

The repository includes:

- Training accuracy curve
- Training loss curve
- Confusion matrix
- Random prediction gallery
- Highest-confidence misclassification gallery
- Network architecture diagram

These visualizations provide both quantitative and qualitative insight into model behavior.

## Repository Structure

```
CNN_CIFAR10/
│
├── cifar10_cnn.py
├── requirements.txt
├── README.md
│
├── docs/
│   ├── Assignment.pdf
│   ├── CNN Report.pdf
│   ├── CNN Presentation.pdf
│   └── system design.png
│
└── outputs/
    ├── curve_accuracy.png
    ├── curve_loss.png
    ├── confusion_matrix.png
    ├── random_samples.png
    ├── most_incorrect.png
    ├── metrics.csv
    ├── metrics.json
    ├── metrics.txt
    ├── tuning_summary.json
    └── best.pth
```

## Running

```bash
python -m pip install -r requirments.txt
python cifar10_cnn.py
```

The program automatically downloads the CIFAR-10 dataset (if necessary), trains the CNN, evaluates its performance, and generates all visualizations.

## Skills Demonstrated

- Deep Learning
- Computer Vision
- Convolutional Neural Networks
- PyTorch
- CUDA Training
- Data Augmentation
- Hyperparameter Optimization
- Learning Rate Scheduling
- Model Evaluation
- Scientific Python

## Supporting Documentation 

The repository also contains the original course assignment, technical report, and presentation slides documenting the design decisions, experimental evaluation, and discussion of future improvements, including exploration of deeper architectures such as ResNet.