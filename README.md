# E-Commerce Data-Pipeline with Apache Flink 
This repository contains an Apache Flink application for real-time sales analytics 

- Built using Docker Compose to orchestrate the necessary infrastructure components, 
  Please refer here https://github.com/snepar/flink-ecom-infra
- Kafka,Elasticsearch and Postgres. 
- The application processes financial transaction data from Kafka
- Performs aggregations in real-time
- Finally stores the results in both Postgres and Elasticsearch for further analysis.

## Requirements
- Docker
- Docker Compose
- Scala 2.12
- sbt
- Python 3

## Installation and Setup for infrastructure
1. For Docker Clone this repository https://github.com/snepar/flink-ecom-infra
2. Navigate to the repository directory.
3. Run `docker-compose up` to start the required services (Kafka, Elasticsearch, Postgres).
4. The Sales Transaction Generator `main.py` helps to generate the sales transactions into Kafka.

### Application Details
- The `KafkaPGESIntegrationEcom` class within the `ecom` package serves as the main entry point for the Flink application. 
- The application consumes financial transaction data from Kafka, performs various transformations.
- Finally stores aggregated results in both Postgres and Elasticsearch.

#### Postgres
- Stores transaction data and aggregated results in tables (`transactions`, `sales_per_category`, `sales_per_day`, `sales_per_month`).

#### Elasticsearch
- Stores transaction data for further analysis.

## Code Structure
- `KafkaPGESIntegrationEcom.scala`: Contains the Flink application logic, including Kafka source setup, stream processing, transformations, and sinks for Postgres and Elasticsearch.
- `deserializer`, `generators.Dto`, and `generators.DDL` : Include necessary classes and utilities for deserialization, data transfer objects, JSON conversion and SQL.

