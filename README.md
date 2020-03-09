# Kafka Producer and Consumer in Java

## Prerequisites

If you already have Java and Maven installed, make sure you have:

* Java 8 (you can get away with Java 7 until we get to streaming)
* Maven 3.X

## Running Kafka

1. Start the Kafka and Zookeeper processes using Docker Compose in `docker/`:
   ```
   $ docker-compose up
   ```

2. The sample clients require that you create two topics that will be used in the Producer program. Run the following commands:
   ```
   $ docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic user-events
   $ docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic global-events
   ```

3. List the topics to double check they were created without any issues.
   ```
   $ docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh --list --zookeeper zookeeper:2181
    global-events
    user-events
   ```

## Running the Clients

1. Inside the `producer/` package, run mvn:
   ```
   $ mvn clean package
   ```

2. For convenience, the project is set up so that the `package` target produces a single executable: `target/producer`. Run the producer to send messages to our two topics -- `user-events` and `global-events`.
   ```
   $ target/producer
   SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
   SLF4J: Defaulting to no-operation (NOP) logger implementation
   SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
   Sent message number 0
   Sent message number 100
   ...
   Sent message number 99800
   Sent message number 99900
   ```

3. Inside the `consumer/` package, run mvn:
   ```
   $ mvn clean package
   ```

4. For convenience, the project is set up so that the `package` target produces a single executable: `target/consumer`. Run the consumer:
   ```
   $ target/consumer
   SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
   SLF4J: Defaulting to no-operation (NOP) logger implementation
   SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
   Received user-events message - key: user_id_0 value: some_value_148511272601285
   Received user-events message - key: user_id_1 value: some_value_148511557371815
   Received user-events message - key: user_id_2 value: some_value_148511557456741
   ....
   ```

    After the consumer has processed all of the messages, start the producer again in another terminal window and you will see the consumer output the messages almost immediately. The consumer will run indefinitely until you press `Ctrl-C` in the terminal window.
