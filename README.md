# AWS Config

A lightweight Scala library for fetching AWS configuration data. This library provides utility functions to easily retrieve secrets from AWS Secrets Manager, parameters from AWS Systems Manager Parameter Store, and metadata tags from EC2 instances. It is designed to simplify integration with AWS using the AWS SDK v2 in Scala projects.

## Features

- **AWS Secrets Manager Integration:**  
  Retrieve secret values using a simple, functional API.

- **AWS Systems Manager Parameter Store Integration:**  
  Fetch parameters with ease for configuration and runtime settings.

- **EC2 Metadata Tag Retrieval:**  
  Access EC2 instance metadata tags to help configure instances dynamically.

## Getting Started

### Prerequisites

- Java 8 or higher
- Scala 2.13+ (with support for cross-compilation if needed)
- [sbt](https://www.scala-sbt.org/) build tool

### Installation

If you plan to publish this library for reuse, add the dependency coordinates to your project’s `build.sbt` file:

```scala
libraryDependencies += "com.yourorg" %% "aws-config" % "0.1.0"
