# CSCI 4880 – Applications of Machine Learning to Computer Security

This directory contains selected projects completed for **CSCI 4880 – Applications of Machine Learning to Computer Security**.

Although the course title emphasizes applications of machine learning within cybersecurity, the coursework focused primarily on the robustness and security of machine learning systems. Projects explored how modern models can be attacked, how those attacks are evaluated, and how defensive techniques can improve model resilience.

The repository includes both original machine learning development and reproductions of published adversarial machine learning research.

---

## Projects

### Hieroglyphic Image Classification and Adversarial Robustness

A semester-long team project in which a convolutional neural network was trained to recognize Egyptian hieroglyphics before being exchanged with another team for adversarial evaluation.

Each team:

- Developed an image classifier
- Exchanged trained models
- Performed adversarial attacks
- Implemented defensive techniques
- Evaluated robustness improvements

The project demonstrates the complete workflow of developing, attacking, and defending a machine learning system.

**Highlights**

- Convolutional Neural Networks
- TensorFlow/Keras
- FGSM
- Projected Gradient Descent (PGD)
- Adversarial training
- Robustness evaluation

---

### Black-Box Model Extraction for Sequential Recommenders

A reproduction and analysis of the RecSys 2021 paper:

> *Black-Box Attacks on Sequential Recommenders via Data-Free Model Extraction*

The project explores how transformer-based recommendation systems can be approximated through black-box interaction before being manipulated through adversarial profile generation.

**Highlights**

- Model extraction
- Knowledge distillation
- Recommendation systems
- Data poisoning
- Research reproduction

---

### Trainwreck

A reproduction and analysis of the paper:

> *Trainwreck: A Damaging Adversarial Attack on Image Classifiers*

The project investigates adversarial attacks against image classification systems while examining practical defensive strategies for protecting training pipelines and datasets.

**Highlights**

- Adversarial examples
- Image classification attacks
- Robustness evaluation
- Defensive machine learning
- Research reproduction

---

## Course Topics

Representative topics explored throughout the course include:

- Adversarial Machine Learning
- Machine Learning Security
- Model Robustness
- Image Classification
- Convolutional Neural Networks
- Recommendation Systems
- Model Extraction
- Data Poisoning
- Adversarial Examples
- Defensive Machine Learning
- Research Reproduction

---

## Technologies

`Python`

`TensorFlow`

`Keras`

`PyTorch`

`Jupyter Notebook`

`NumPy`

`Pandas`

`Adversarial Robustness Toolbox (ART)`

---

## Repository Organization

The projects are organized into three categories:

- **Original machine learning development**, represented by the Hieroglyphic Image Classification project.
- **Published research reproduction**, represented by the RecSys and Trainwreck projects.
- **Robustness evaluation**, demonstrating attacks, defenses, and analysis of modern machine learning systems.

Together these projects illustrate the progression from building machine learning models to understanding how they behave under adversarial conditions.

---

## Relationship to Later Work

This course marked an important transition toward my later research interests in trustworthy AI and AI-assisted cybersecurity.

Many of the concepts explored here—including robustness evaluation, adversarial behavior, and secure AI systems—continued to influence subsequent projects such as **guardAInDBG**, which investigates AI-assisted reverse engineering, and **Transformer_Toy**, which explores the implementation and behavior of modern transformer architectures from first principles.