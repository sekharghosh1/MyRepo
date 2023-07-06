# Money Transfer #

The Money Transfer System is a Java-based application that allows users to transfer money between accounts. It supports multiple users performing transfers simultaneously and is designed to run in a multi-threaded environment.

### Features ###

* Create user accounts with unique account IDs
* Perform money transfers between accounts.
* Handle concurrent transfers from multiple users.
* Ensures thread safety and consistency of account balances during transfers.
* Provides RESTful APIs for easy integration with other systems.

### Technologies Used ###

* Java11: Programming language used for the application.
* Spring Boot: Framework for building the application.
* RESTful APIs: Used for communication with the system.
* Multi-threading: Enables concurrent transfers from multiple users.
* JUnit and Mockito: Testing frameworks for unit and integration testing.
* Git: Version control system for managing the source code.

### Getting Started ###

To run the Money Transfer System on your local machine, follow these steps:

* Clone the repository: git clone https://SekharGhosh@bitbucket.org/sekharws/money_transfer.git
* Build the application: ./gradlew build
* Run the application : ./gradlew bootRun

### API Documentation ###

* The Money Transfer  provides the following API endpoints:
* POST /transfer: Transfer money between accounts.