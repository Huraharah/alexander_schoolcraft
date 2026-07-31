# Black-Box Model Extraction Attack on Sequential Recommenders

A reproduction and analysis of the paper:

> *Black-Box Attacks on Sequential Recommenders via Data-Free Model Extraction* (RecSys 2021)

Completed for **CSCI 4880 – Applications of Machine Learning to Computer Security**.

---

## Project Overview

The objective of this project was to understand, reproduce, and evaluate a published black-box model extraction attack against transformer-based recommender systems.

The project demonstrates how an attacker can:

- Extract a surrogate model using model distillation
- Generate adversarial recommendation profiles
- Poison recommendation datasets
- Influence recommendation rankings

---

## Repository Contents

```text
RecSys-Model-Extraction/

README.md

attack.ipynb

presentation.pdf

results.xlsx

original_source/
```

---

## Topics Explored

- Transformer-based recommender systems
- Model extraction
- Knowledge distillation
- Adversarial recommendation
- Data poisoning
- Recommendation robustness

---

## Models Studied

- BERT4Rec
- SASRec
- NARM

---

## My Contributions

During reproduction of the published implementation, several compatibility issues required modification, including:

- PyTorch API updates
- Removal of deprecated functionality
- Command-line configuration fixes
- Dataset compatibility improvements

These changes allowed the original research implementation to execute successfully on modern software environments.

---

## Technologies

`Python`

`PyTorch`

`Transformers`

`Recommendation Systems`

`Jupyter Notebook`

---

## Course Context

This project focused on reproducing and understanding current research in adversarial machine learning rather than developing a new attack from scratch.

The included presentation summarizes the attack methodology, implementation challenges, and experimental results.