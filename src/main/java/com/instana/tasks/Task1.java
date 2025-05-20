package com.instana.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instana.KafkaConnector;
import com.instana.RestClient;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * Task1 connects to a Kafka topic and prints any messages it receives.
 */
public class Task1 {
  private static final Logger logger = LoggerFactory.getLogger(Task1.class);
  private static final String TOPIC_NAME = "team-red-read-one";
  private static final String CONSUMER_GROUP_ID = "task1-consumer-group";
  private static final Duration POLL_DURATION = Duration.ofMillis(100);

  private final KafkaConsumer<String, String> consumer;
  private final RestClient restClient;

  /**
   * Creates a new Task1 instance with a configured Kafka consumer.
   */
  public Task1(KafkaConnector kafkaConnector) {

    // Get consumer properties
    Properties consumerProps = kafkaConnector.getConsumerProperties(CONSUMER_GROUP_ID);

    // Create consumer
    consumer = new KafkaConsumer<>(consumerProps);

    // Create REST client
    restClient = new RestClient();

    // Subscribe to the topic
    consumer.subscribe(Collections.singletonList(TOPIC_NAME));
    logger.info("Subscribed to topic: {}", TOPIC_NAME);
  }

  /**
   * Consumes messages from the Kafka topic and prints them.
   */
  public void startTask() {
    logger.info("Starting to consume messages from topic: {}", TOPIC_NAME);

    try {
      boolean continueConsuming = true;
      while (continueConsuming) {
        ConsumerRecords<String, String> records = consumer.poll(POLL_DURATION);

        for (ConsumerRecord<String, String> record : records) {
          logger.info("Received message: Topic = {}, Partition = {}, Offset = {}, Key = {}, Value = {}",
              record.topic(), record.partition(), record.offset(), record.key(), record.value());

          // Print the message value
          System.out.println("Message received: " + record.value());
          Map<String, Object> map = new ObjectMapper().readValue(Base64.getDecoder().decode(record.value()), Map.class);

          // Post the message value to the REST endpoint
          RestClient.RestResponse response = restClient.post("/task1", 1, (String) map.get("value"));

          // Check if the response is successful
          if (response.isSuccess()) {
            System.out.println("Response from server: " + response.getResponseBody());
            logger.info("Received successful response from server. Stopping consumer.");

            continueConsuming = false;
            break;
          } else {
            System.out.println("Response from server: " + response.getResponseBody());
          }
        }
      }
    } catch (Exception e) {
      logger.error("Error consuming messages", e);
    } finally {
      consumer.close();
      logger.info("Consumer closed");
    }
  }

}
