# Generic Singly Linked List

A generic singly linked list implementation written in Java for a Data Structures and Algorithm Analysis course.

The project demonstrates the implementation of a reusable linked-list Abstract Data Type (ADT) using Java Generics, including ordered insertion, node removal, searching, merging, and traversal.

## Original Assignment

[View the assignment](assignment/Generic-Singly-Linked-List.pdf)

## Project Context

This project was completed for a Data Structures and Algorithm Analysis course.

The assignment required implementing a generic singly linked list capable of ordered insertion, merging, searching, removal, and traversal while supporting arbitrary comparable object types through Java Generics.

The provided `Student` and `Node` classes served as reference implementations for transitioning from a concrete linked list to a reusable generic implementation.

## Features

- Generic node implementation
- Generic singly linked list
- Ordered insertion
- Duplicate prevention
- Linear search
- Node removal
- List merging
- Head and tail references
- Middle-element search
- Generic Comparable support

## Data Structures and Concepts

- Singly linked lists
- Generic programming
- Java Generics
- Comparable interface
- Object-oriented design
- Reference manipulation
- Abstract Data Types (ADTs)

## Technologies

`Java` `Generics` `Linked Lists` `Object-Oriented Programming`

## Project Structure

src/
├── genericNode.java
├── myGenericLinkedList.java
├── Student.java
├── Node.java
└── MainClass.java

- `genericNode.java` implements a generic linked-list node.
- `myGenericLinkedList.java` implements the generic linked-list ADT.
- `Student.java` provides a sample comparable object.
- `Node.java` is the original non-generic node implementation supplied for reference.
- `MainClass.java` exercises all implemented operations.

## Running

Compile:

```bash
javac -d out src/*.java
```

Run:

```bash
java -cp out MainClass
```