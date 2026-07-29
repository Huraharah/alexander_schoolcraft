# Array-Based Restaurant Manager

A Java coursework project implementing a simplified restaurant seating and waitlist system using fixed-size reference-type arrays.

The project was completed for a Data Structures and Algorithm Analysis course and focuses on array insertion, deletion, searching, object comparison, and movement between logical collections without using `ArrayList` or another collection framework.

## Original Assignment

[View the original assignment](assignment/LAB1-Array_and_OOP_Warm-Up.pdf)

## Project Context

This project was completed as the first lab in a Data Structures and Algorithm Analysis course. The assignment emphasized managing reference-type objects using basic arrays rather than Java collection classes.

Partial scaffolding for the ```Customer``` class and the assignment specification were provided. I completed the required customer behavior, implemented the restaurant and waitlist management logic, and developed the driver used to exercise the implementation

## Features

- Tracks currently seated customer parties
- Maintains a fixed-capacity waitlist
- Prevents duplicate customer entries
- Searches both seated and waiting customers
- Removes customers and compacts array contents
- Promotes eligible parties from the waitlist
- Tracks party size and high-chair requirements
- Exercises the implementation through a dedicated driver

## Data Structures and Concepts

- Reference-type arrays
- Array traversal
- Insertion and deletion
- Linear search
- Array compaction
- Object-oriented design
- Encapsulation
- Object equality
- Comparable objects

## Technologies

`Java` `Arrays` `Object-Oriented Programming` `Data Structures`

## Project Structure

```text
src/
├── Customer.java
├── Restaurant.java
└── TestDriver.java
```

- Customer.java defines customer-party information and comparison behavior.
- Restaurant.java manages seated parties and the waitlist.
- TestDriver.java exercises seating, waiting, duplicate detection, removal, and search behavior.

## Running the Project

Compile:
```bash
javac -d out src/Customer.java src/Restaurant.java src/TestDriver.java
```

Run:
```bash
java -cp out TestDriver