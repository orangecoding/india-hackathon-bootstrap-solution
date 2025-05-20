package com.instana;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.instana.tasks.Task1;
import com.instana.tasks.Task2;

/**
 * Main class that demonstrates Kafka connection.
 */
public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    logger.info("Starting Kafka connection demo");

    try {
      KafkaConnector kafkaConnector = new KafkaConnector();
      logger.info("Kafka connection successfully");

      Task1 task1 = new Task1(kafkaConnector);
      task1.startTask();

      Task2 task2 = new Task2(kafkaConnector);
      task2.startTask();

    } catch (Exception e) {
      logger.error("Error in Kafka connection demo", e);
    }
  }

}
