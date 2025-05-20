package com.instana.tasks;

import org.apache.kafka.common.serialization.Serdes;

public class ValueWithTimestampSerde extends Serdes.WrapperSerde<ValueWithTimestamp> {
  public ValueWithTimestampSerde() {
    super(new ValueWithTimestampSerializer(), new ValueWithTimestampDeserializer());
  }
}
