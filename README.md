# camel-spring-boot-xa

An example project showing how to do XA transactions with Camel + Narayana on Spring Boot.

## Requirements

- [Apache Maven 3.x](http://maven.apache.org)
- [Apache Artemis 2.0.0](https://activemq.apache.org/artemis/)
- [MySQL 5.7.18](https://www.mysql.com/oem/)
  - [Docker Image](https://hub.docker.com/r/mysql/mysql-server/)

## Preparing

Install and run Apache Artemis [[https://activemq.apache.org/artemis/docs/2.0.0/using-server.html](https://activemq.apache.org/artemis/docs/2.0.0/using-server.html)]

_Note: You might want to configure the `redelivery-delay` and `max-delivery-attempts` in the Artemis `broker.xml` file so that you can see the retries occurring. Take a look at the docs for more info: [[https://activemq.apache.org/artemis/docs/2.0.0/undelivered-messages.html](https://activemq.apache.org/artemis/docs/2.0.0/undelivered-messages.html)]._

Install and run MySQL [https://dev.mysql.com/doc/refman/5.7/en/installing.html]

_Note: For my tests, I chose to run the docker image [https://hub.docker.com/r/mysql/mysql-server/]. You can run it using the command `docker run --name mysql -e MYSQL_DATABASE=example -e MYSQL_ROOT_PASSWORD=Abcd1234 -e MYSQL_ROOT_HOST=172.17.0.1 -p 3306:3306 -d mysql/mysql-server:5.7`. You can then connect and run SQL statements using the command `docker exec -it mysql mysql -uroot -p`._

Build the project source code

```
$ cd $PROJECT_ROOT
$ mvn clean install
```

## Running the code

```
$ cd $PROJECT_ROOT
$ mvn spring-boot:run
```

## Testing the code

There are a couple of test files in the `src/test/data` folder. Any file containing the word "error" will cause a rollback/retry and eventually land on the DLQ.

```
$ cd $PROJECT_ROOT
$ cp src/test/data/message_01.txt target/messages/
```

If the file successfully processed, you will see a message in the DB. You can check it using the SQL command `select * from example.MESSAGES;`. If the file threw an exception (because it had the word "error" in it), it will be retried until the `max-delivery-attempts` is exhausted and then be placed on the `DLQ`. Another route will then pick it up and log it.