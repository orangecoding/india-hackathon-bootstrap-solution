package com.instana.tasks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ValueWithTimestamp {
  private final long value;
  private final long timestamp;

  @JsonCreator
  public ValueWithTimestamp(@JsonProperty("value") long value,
                            @JsonProperty("timestamp") long timestamp) {
    this.value = value;
    this.timestamp = timestamp;
  }

  public long getValue() {
    return value;
  }

  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return "ValueWithTimestamp{" +
        "value=" + value +
        "timestamp=" + timestamp +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    ValueWithTimestamp that = (ValueWithTimestamp) o;
    return timestamp == that.timestamp &&
        value == that.value ;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, timestamp);
  }
}
