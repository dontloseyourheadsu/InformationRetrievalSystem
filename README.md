# Information Retrieval System

## Overview
This project implements an Information Retrieval System using PostgreSQL and Python. The system is designed to handle a collection of documents, allowing users to perform various operations such as adding documents, querying terms, and retrieving relevant information.

Tech stack:
- PostgreSQL: Used as the database to store documents and their metadata.
- Java: The main programming language for implementing the system.

Java libraries
- PostgreSQL JDBC Driver: For connecting Java with PostgreSQL.
- Apache Lucene: For indexing and searching documents.
- Apache Commons: For utility functions.

## Features
- Add documents to the database.
- Index documents using Apache Lucene.
- Query documents based on terms.
- Retrieve document metadata such as title, author, and date.
- Calculate term frequencies and store them in the database.
- Generate a complete view of term frequencies for all documents.
- Support for multiple queries and terms.
- Compare documents based on term frequencies.

## Getting Started
### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Apache Maven
- PostgreSQL database server
- Apache Lucene library
- Apache Commons library
- PostgreSQL JDBC Driver
- Apache Commons Lang library

### Installation

Create the PostgresSQL database
```sql
-- Create core tables
CREATE TABLE Document (
    id SERIAL PRIMARY KEY
);

CREATE TABLE Text (
    id INT PRIMARY KEY,
    url VARCHAR(255),
    title VARCHAR(255),
    author VARCHAR(255),
    date DATE,
    FOREIGN KEY (id) REFERENCES Document(id)
);

CREATE TABLE Query (
    id INT PRIMARY KEY,
    label VARCHAR(50),
    FOREIGN KEY (id) REFERENCES Document(id)
);

CREATE TABLE Term (
    name VARCHAR(50) PRIMARY KEY
);

CREATE TABLE Word (
    word VARCHAR(50) PRIMARY KEY
);

CREATE TABLE Represent (
    word VARCHAR(50) PRIMARY KEY,
    term VARCHAR(50) NOT NULL,
    FOREIGN KEY (word) REFERENCES Word(word),
    FOREIGN KEY (term) REFERENCES Term(name)
);

CREATE TABLE HAS (
    id INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    frequency REAL NOT NULL,
    PRIMARY KEY (id, name),
    FOREIGN KEY (id) REFERENCES Document(id),
    FOREIGN KEY (name) REFERENCES Term(name)
);

-- Create the Complete view (to fill in zeros for missing term frequencies)
CREATE VIEW Complete AS
SELECT id, name, frequency FROM HAS
UNION
SELECT d.id, t.name, 0::REAL AS frequency
FROM Document d, Term t
WHERE NOT EXISTS (
    SELECT 1 FROM HAS h WHERE h.id = d.id AND h.name = t.name
);
```

### Data Population
Use the documents under src/main/resources/test-documents to add documents and query.

### Running the Application
Update DatabaseConnection class with your PostgreSQL database credentials.

Execute the application using the following command:
```bash
mvn clean install
mvn exec:java -Dexec.mainClass="com.example.Main"
```

### Instructions for Using the Application sample

#### 1. Add Documents
- Open the application.
- Click the “Add Documents” button.
- Select all 10 `.txt` files from the sample folder.

#### 2. Run a Query
- Enter the query: `climate change emissions`.
- Select **Cosine similarity**.
- Click **Search**.

- Repeat the query using **Euclidean distance** to show how results vary.

#### 3. Compare Two Documents
- Click **Compare Documents**.
- Choose:
  - **Document 1**: `Climate_Change`
  - **Document 2**: `Climate_Policy`
- Select **Cosine**.
- Click **Compare**.

- Repeat with:
  - **Document 1**: `Fossil_Fuels`
  - **Document 2**: `Electric_Vehicles`
  - Metric: **Euclidean**
