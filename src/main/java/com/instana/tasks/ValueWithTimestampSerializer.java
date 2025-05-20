package com.instana.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class ValueWithTimestampSerializer implements Serializer<ValueWithTimestamp> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ValueWithTimestampSerializer.class);

  @Override
  public byte[] serialize(String topic, ValueWithTimestamp valueWithTimestamp) {
    try {
      if (valueWithTimestamp == null) {
        LOGGER.error("Received null as value for ValueWithTimestamp serialization.");
        return null;
      }
      return new ObjectMapper().writeValueAsString(valueWithTimestamp).getBytes(StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new SerializationException("Error when serializing ValueWithTimestamp to byte array.");
    }
  }
}
