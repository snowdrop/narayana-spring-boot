# 2 phase commit example

This example demonstrates a simple application with a Kafka publisher and a subscriber.

## Application workflow

Application has two scheduled executions configured: creating users and printing users.

Every 5 seconds application will try to create a new user and send a JMS message notifying about it.
In order to demonstrate a transaction rollback, a database is configured to only accept unique names.
Trying to create a user with the same name will cause both database and JMS resources to undo their work.

Another scheduled execution is to print names of users that currently exist in the database. 

## Usage

Build the application.
```bash
mvn clean package
```

Start the application.
```bash
java -jar target/narayana-spring-boot-sample-2pc.jar
```

Now the application will start and every few seconds scheduled executions will be triggered. 
You should see the outcome similar to the one below.
```log
Executor ---> Attempting to create a user named Robert
JMS Logger ---> Created a new user Robert
Executor ---> Attempting to create a user named Richard
JMS Logger ---> Created a new user Richard
Executor ---> Current users: Robert, Richard
Executor ---> Attempting to create a user named Robert
Executor ---> Failed to create a user named Robert. Transaction will rollback
javax.transaction.RollbackException: ARJUNA016083: Cannot register synchronization because the transaction is in aborted state
...
```