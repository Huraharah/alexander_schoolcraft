# SQL Injection Demonstration

A JavaFX teaching application developed for **CSCI 6450 – Software Development Security** to demonstrate SQL injection vulnerabilities and the use of parameterized queries as a defense.

The application compares an intentionally vulnerable authentication workflow based on raw SQL string concatenation with a secure workflow using JDBC `PreparedStatement` parameters.

## Features

- Interactive JavaFX interface
- Live Azure SQL demonstration database
- Vulnerable string-concatenated SQL queries
- Secure parameterized queries
- Tautology-based authentication bypass
- Comment-based SQL injection
- `UNION`-based data disclosure
- Query and returned-data visualization
- Automatic recreation of controlled demonstration data
- Separate success, failure, and attack-result screens

## Demonstrated Security Concepts

### Vulnerable Query Construction

The insecure login constructs SQL by concatenating user-controlled input directly into the query. This allows an attacker to alter the query’s intended structure.

### Parameterized Queries

The secure login uses `PreparedStatement`, keeping SQL instructions separate from user-supplied values. Inputs are treated as data rather than executable SQL syntax.

## Technologies

`Java 21` `JavaFX` `FXML` `Maven` `JDBC` `Azure SQL`

## Running the Application

The project requires Java 21 and Maven.

Set the database configuration through environment variables:

```text
AZ_SQL_SERVER
AZ_SQL_DB
AZ_SQL_USER
AZ_SQL_PASS
```

Run:
```bash
mvn javafx:run
```
The Azure database may require a short startup period after activity

## Repository Structure

```
src/main/java/SQLi/
    App.java
    Db.java
    LoginService.java

src/main/resources/SQLi/
    SQLitest.fxml
    SQLIattack.fxml
    SQLiinsecurepass.fxml
    SQLiinsecurefail.fxml
    SQLisecurepass.fxml
    SQLisecurefail.fxml
```

## Original Assignment

The assignment required a safe simulation of a tautology-based SQL injection attack and a comparison between vulnerable query construction and parameterized input handling.

[View the Original Assignment](assignment/CSCI6450FA2025Project1.pdf)

## Project Context

This project was completed for **CSCI 6450 – Software Development Security.**

Although the assignment only required a console-based mock database simulation, I expanded it into a JavaFX application backed by a controlled Azure SQL database to demonstrate the practical difference between insecure SQL concatenation and secure parameterized queries.

## Safety Notice

This application intentionally contains vulnerable code for educational demonstration. It should only be used against the isolated demonstration database and must not be adapted for production authentication.