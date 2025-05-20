package com.instana;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

/**
 * Utility class for Kafka connection configuration.
 */
public class KafkaConnector {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConnector.class);
    private final Dotenv dotenv;

    public KafkaConnector() {
        // Load environment variables from .env file
        this.dotenv = Dotenv.configure().load();
        logger.info("Environment variables loaded successfully");
    }

    /**
     * Creates Kafka producer properties using credentials from .env file.
     *
     * @return Properties configured for Kafka producer
     */
    public Properties getProducerProperties() {
        Properties props = new Properties();
        
        // Set bootstrap servers
        String bootstrapServers = dotenv.get("KAFKA_BOOTSTRAP_SERVERS");
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        // Set SASL authentication
        configureSaslAuth(props);
        
        // Set serializers
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        logger.info("Kafka producer properties configured with bootstrap servers: {}", bootstrapServers);
        return props;
    }

    /**
     * Creates Kafka consumer properties using credentials from .env file.
     *
     * @param groupId Consumer group ID
     * @return Properties configured for Kafka consumer
     */
    public Properties getConsumerProperties(String groupId) {
        Properties props = new Properties();
        
        // Set bootstrap servers
        String bootstrapServers = dotenv.get("KAFKA_BOOTSTRAP_SERVERS");
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        // Set SASL authentication
        configureSaslAuth(props);
        
        // Set deserializers
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        
        // Set consumer group ID
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        logger.info("Kafka consumer properties configured with bootstrap servers: {} and group ID: {}", 
                bootstrapServers, groupId);
        return props;
    }

    /**
     * Configures SASL authentication properties using credentials from .env file.
     *
     * @param props Properties to configure
     */
    private void configureSaslAuth(Properties props) {
        String username = dotenv.get("KAFKA_USERNAME");
        String password = dotenv.get("KAFKA_PASSWORD");
        
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        props.put(SaslConfigs.SASL_JAAS_CONFIG, 
                "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                "username=\"" + username + "\" " +
                "password=\"" + password + "\";");
        
        logger.info("SASL authentication configured for user: {}", username);
    }

  /**
   * Sends a test message to Kafka.
   *
   * @param topic The topic to send the message to
   */
  public void sendMessage(String topic, String key, String value) {
    logger.info("Sending message to topic: {}", topic);

    try (KafkaProducer<String, String> producer = new KafkaProducer<>(this.getProducerProperties())) {

      ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
      producer.send(record, (metadata, exception) -> {
        if (exception != null) {
          logger.error("Error sending message", exception);
        } else {
          logger.info("Message sent successfully to topic: {}, partition: {}, offset: {}",
              metadata.topic(), metadata.partition(), metadata.offset());
        }
      });

      // Ensure the message is sent before closing the producer
      producer.flush();
      logger.info("Message sent and producer flushed");
    } catch (Exception e) {
      logger.error("Error sending message", e);
    }
  }
}