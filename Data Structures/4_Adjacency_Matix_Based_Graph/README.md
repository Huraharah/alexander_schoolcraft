# Adjacency Matrix Graph

A graph Abstract Data Type (ADT) implemented using an adjacency matrix for a Data Structures and Algorithm Analysis course.

The project demonstrates graph representation using a two-dimensional array and implements a variety of graph operations, including traversal algorithms, connectivity analysis, edge manipulation, and graph property evaluation.

## Original Assignment

[View the assignment](assignment/Adjacency_Matrix_Based_Graph.pdf)

## Project Context

This project was completed for a Data Structures and Algorithm Analysis course.

The assignment focused on implementing an adjacency matrix-based graph ADT and analyzing the time complexity of each operation. Beyond the required functionality, additional helper methods were implemented to exercise all logical pathways during testing.

## Features

- Adjacency matrix graph representation
- Breadth-First Search (BFS)
- Depth-First Search (DFS)
- Directed graph detection
- Weighted graph detection
- Connected graph detection
- Complete graph detection
- Neighbor lookup
- Degree calculation
- Edge insertion and removal

## Algorithms and Concepts

- Graph Theory
- Adjacency Matrices
- Breadth-First Search
- Depth-First Search
- Queue-based traversal
- Recursive traversal
- Graph Connectivity
- Big-O Analysis

## Technologies

`Java` `Graphs` `Adjacency Matrix` `Algorithms`

## Project Structure

src/
├── Graph.java
└── MainClass.java

- `Graph.java` implements the adjacency matrix graph and graph algorithms.
- `MainClass.java` serves as a comprehensive test driver.

## Running

Compile

```bash
javac -d out src/*.java
```

Run

```bash
java -cp out MainClass
```
