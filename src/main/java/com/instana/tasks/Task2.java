package com.instana.tasks;

import com.instana.KafkaConnector;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;

import java.time.Duration;
import java.util.Properties;

import static org.apache.kafka.streams.kstream.Suppressed.BufferConfig.unbounded;

public class Task2 {
  // Define topic being read by Kafka Streams
  private static final String INPUT_TOPIC_NAME = "team-red-read-rxxbyplntr";
  // Define topic being written by Kafka Streams
  private static final String OUTPUT_TOPIC_NAME = "team-red-write-rxxbyplntr";
  // Define a unique application ID (to not interfere with others reading from Kafka
  private static final String APPLICATION_ID = "task2-application-red";
  // Define a unique application ID (to not interfere with others reading from the same topic
  private static final String CONSUMER_GROUP_ID = "task2-consumer-group-red";

  private final KafkaStreams streams;

  /**
   * Creates the context of Task 2.
   * Reads all Kafka properties and creates a Stream builder.
   * With that Stream builder, a topology is created, connecting the input to the output topic.
   * The builder takes incoming messages, groups them by minute, waits for one minute for messages to come in, and merges them.
   * The merged values are written to the output topic.
   * @param kafkaConnector The Kafka connector, housing metadata of the Kafka instance to connect to.
   */
  public Task2(KafkaConnector kafkaConnector) {
    // Get all properties that are required to connect to Kafka
    Properties consumerProps = kafkaConnector.getConsumerProperties(CONSUMER_GROUP_ID);
    Properties props = new Properties();
    props.putAll(consumerProps);
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID);
    // Define how keys of Kafka messages are to be parsed
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.LongSerde.class);
    // Define how values of Kafka messages are to be parsed
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, ValueWithTimestampSerde.class);
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, consumerProps.getProperty("bootstrap.servers"));

    StreamsBuilder builder = new StreamsBuilder();

    // Read from the Kafka topic
    builder.stream(INPUT_TOPIC_NAME, Consumed.with(
        Serdes.Long(),
        new ValueWithTimestampSerde()
      ))
      // Group by the minute of the timestamp
      .groupBy((key, value) -> this.getAggregationKey(value.getTimestamp()))
      // Wait for 1 minute for no more messages to arrive
      .windowedBy(SessionWindows.ofInactivityGapWithNoGrace(Duration.ofMinutes(1)))
      // Merge incoming messages into one (starting with an empty one)
      .aggregate(
        () -> new ValueWithTimestamp(
          0L,
          0L
        ),
        (key, value, aggregate) -> this.merge(aggregate, value),
        (key, value, aggregate) -> this.merge(aggregate, value),
        Materialized.with(Serdes.Long(), new ValueWithTimestampSerde())
      )
      // Wait until the 1 minute is finished (otherwise it sends out a message on every aggregation)
      .suppress(Suppressed.untilWindowCloses(unbounded()))
      .toStream()
      .map((key, value) -> KeyValue.pair(key.key(), value))
      // Write out to the output topic
      .to(OUTPUT_TOPIC_NAME, Produced.with(Serdes.Long(), new ValueWithTimestampSerde()));

    // Put the entire topology together with the Kafka properties
    streams = new KafkaStreams(builder.build(), props);
    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
  }

  /**
   * Reduces a timestamp to the minute.
   * @param timestamp Any given timestamp.
   * @return The timestamp truncated to a minute.
   */
  private long getAggregationKey(long timestamp) {
    return (timestamp / 60000) * 60000;
  }

  /**
   * Merges two {@code ValueWithTimestamp} objects into one.
   * Takes the larger of the two timestamps and the sum of the values.
   * @param valueWithTimestamp1 The first value with timestamp to merge.
   * @param valueWithTimestamp2 The second value with timestamp to merge.
   * @return A {@code ValueWithTimestamp} with the larger timestamp and the sum of the values.
   */
  private ValueWithTimestamp merge(ValueWithTimestamp valueWithTimestamp1, ValueWithTimestamp valueWithTimestamp2) {
    return new ValueWithTimestamp(
      valueWithTimestamp1.getValue() + valueWithTimestamp2.getValue(),
      Math.max(valueWithTimestamp1.getTimestamp(), valueWithTimestamp2.getTimestamp())
    );
  }

  /**
   * Starts the Kafka Streams implementation.
   * The topology of the builder is applied, reading from the input, piping it through the stream, and writing it to the output.
   */
  public void startTask() {
    streams.start();
  }
}
