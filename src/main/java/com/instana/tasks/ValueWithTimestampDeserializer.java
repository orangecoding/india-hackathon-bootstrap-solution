package com.instana.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValueWithTimestampDeserializer implements Deserializer<ValueWithTimestamp> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ValueWithTimestampDeserializer.class);

  @Override
  public ValueWithTimestamp deserialize(String topic, byte[] payload) {
    try {
      if (payload == null){
        LOGGER.error("Received null as payload for ValueWithTimestamp deserialization.");
        return null;
      }
      return new ObjectMapper().readValue(payload, ValueWithTimestamp.class);
    } catch (Exception e) {
      throw new SerializationException("Error when deserializing byte array to ValueWithTimestamp.", e);
    }
  }
}
