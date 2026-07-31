# CSCI 6450 – Software Development Security

This directory contains selected graduate coursework completed for **CSCI 6450 – Software Development Security**.

The projects focus on implementing interactive software demonstrations that illustrate common security vulnerabilities, defensive programming practices, and foundational network security concepts. Rather than simply describing attacks, each project provides a working application that allows users to observe both vulnerable and secure behavior.

---

## Projects

### SQL Injection Demonstration

An interactive JavaFX application demonstrating one of the most common web application vulnerabilities: SQL injection.

The application compares an intentionally vulnerable authentication workflow against a secure implementation using parameterized SQL queries, allowing users to observe how malicious input alters query execution and how prepared statements prevent these attacks.

**Highlights**

- JavaFX desktop application
- Azure SQL backend
- Vulnerable SQL concatenation
- Parameterized queries
- Tautology attacks
- UNION-based data disclosure
- Secure authentication comparison

**Technologies**

`Java` `JavaFX` `FXML` `Maven` `JDBC` `Azure SQL`

---

### Network IDS Simulation

A JavaFX-based simulation of a Network Intrusion Detection System (NIDS) that analyzes packet captures and identifies potentially malicious network activity using configurable detection heuristics.

The application processes packet data exported from Wireshark/TShark and classifies network events into multiple severity levels while demonstrating several common intrusion detection concepts.

**Highlights**

- JavaFX desktop application
- Packet capture replay
- Blacklisted IP detection
- Network traffic analysis
- Severity classification
- Heuristic-based detection
- Wireshark/TShark integration

**Technologies**

`Java` `JavaFX` `Maven` `Jackson` `JSON` `Wireshark` `TShark`

---

## Course Topics

Representative topics explored throughout the course include:

- Secure Software Development
- Defensive Programming
- SQL Injection
- Prepared Statements
- Network Security
- Intrusion Detection Systems (IDS)
- Packet Analysis
- Input Validation
- Security Testing
- Security Awareness

---

## Technologies

`Java`

`JavaFX`

`FXML`

`Maven`

`JDBC`

`Azure SQL`

`Jackson`

`JSON`

`Wireshark`

`TShark`

---

## Repository Organization

The projects included here represent the primary software-development assignments completed during the course.

Both applications extend well beyond the original assignment requirements by incorporating graphical user interfaces, modular software architecture, and realistic demonstrations intended to make common cybersecurity concepts more accessible and interactive.

---

## Relationship to Later Work

This course reinforced secure software engineering practices that later influenced my research in AI-assisted cybersecurity.

While these projects focus on traditional software security concepts, they complement later work such as **guardAInDBG**, which explores applying large language models to reverse engineering and debugging workflows, and my research into trustworthy and secure AI systems.

---

## Note

These applications intentionally demonstrate vulnerable or suspicious behavior for educational purposes. They are designed to illustrate common security concepts within isolated, controlled environments and should not be interpreted as production security tools.