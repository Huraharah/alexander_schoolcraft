# Hieroglyphic Image Classification and Adversarial Robustness

A convolutional neural network (CNN) image-classification project developed for **CSCI 4880 – Applications of Machine Learning to Computer Security**.

The project was completed as part of a semester-long team exercise focused on the robustness of machine learning systems. Each team developed an image classifier, exchanged models with another team, performed adversarial attacks against the received model, and then implemented defensive techniques to improve robustness.

Our team trained a classifier using images of Egyptian hieroglyphics.

---

## Project Workflow

The project consisted of four major stages:

1. Build an image classification model
2. Exchange trained models with another team
3. Attack the received model using adversarial techniques
4. Improve model robustness through defensive measures

---

## Repository Contents

```text
Hieroglyphic-Classifier/

build_model.ipynb
attack_model_PGD_FGSM.ipynb
dataset/
README.md
```

---

## Machine Learning Pipeline

The training notebook demonstrates:

- Dataset preprocessing
- Label encoding
- Dataset balancing
- CNN model construction
- Model training
- Performance evaluation
- Model serialization

---

## Adversarial Evaluation

The attack notebook demonstrates adversarial attacks using the Adversarial Robustness Toolbox (ART).

Implemented attacks include:

- Fast Gradient Sign Method (FGSM)
- Projected Gradient Descent (PGD)

The notebook also evaluates defensive techniques by retraining the classifier using adversarial examples and comparing performance before and after hardening.

---

## Technologies

`Python`

`TensorFlow`

`Keras`

`NumPy`

`ART (Adversarial Robustness Toolbox)`

`Jupyter Notebook`

---

## Concepts Demonstrated

- Image Classification
- Convolutional Neural Networks
- Adversarial Machine Learning
- Model Robustness
- Adversarial Training
- Defensive AI

---

## Course Context

This project was completed for **CSCI 4880 – Applications of Machine Learning to Computer Security**, a course focused on evaluating and improving the robustness of machine learning systems against adversarial attacks.

While completed as a team project, I contributed substantially to the implementation, experimentation, and evaluation of the resulting classifier and attack pipeline.