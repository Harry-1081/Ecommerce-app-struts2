# Ecommerce-App-Struts2

This is an Struts2 based E-Commerce web app, created to understand and apply the concepts of Struts2. This is developed according to REST-Standards using Struts2 rest plugin. 

## Table of Contents

- [Introduction](#introduction)
- [Installation](#installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [Change Log](#change-log)
- [Contributors](#contributors)


## Introduction

This is an Struts2 based E-Commerce web app, developed according to REST standards, through the use of Struts2-rest plugin. This also consists of a Kafka producer-consumer model to create dynamic alerts. 

## Installation

If you do not have maven in your device, you can download the latest version [here](https://maven.apache.org/download.cgi). If you already have maven, follow the steps below to set up the Struts2 - Ecommerce application.

1. Navigate to the application directory.


2. Install the required dependencies:
    ```bash
    mvn clean install
    ```

3. Deploy the struts2 application:
    ```bash
    mvn jetty:run
    ```
 
## Usage

Once the application is set up, the application can be accessed through any web browsers. 
1. Before using, Kindly verify if the project is running.
2. Open your web browser and navigate to `http://localhost:8080/struts2/login` to use the application.

## Contributing
Any contributions to this project are welcome ! If you have any ideas, suggestions, or bug reports, please open an issue or submit a pull request.
To contribute:
1. Fork the repository.
2. Create a new branch (`git checkout -b feature-branch`).
3. Make your changes and commit them (`git commit -m 'Add new feature'`).
4. Push to the branch (`git push origin feature-branch`).
5. Open a pull request.


## Change Log

### [ 1.1.2 ] 25/03/2025
 - <b>Added</b> : Feature to schedule Discounts using Java Scheduler.

### [ 1.1.1 ] 20/03/2025
 - <b>Added</b> : Jedis (Redis for Java) to Cache frequently accessed datas and Listener to initialize cache & daily report scheduler.

### [ 1.1 ] 18/03/2025
 - <b>Added</b> : Kafka DLQ and retries mechanism to deal with runtime exceptions, Audit Log Generation for all processes using Kafka and Scheduler based report generation and mailing to Superadmin.

 - <b>Changed</b> : Switched to key-value based hashing strategy and Dual Access for Users with special roles.

### [ 1.0 ] 03/03/2025
 - <b>Initial Commit</b> ( Struts2 Webapp with REST plugin and kafka based alert system )

## Contributors

- [Prasanna V](https://github.com/Harry-1081)
