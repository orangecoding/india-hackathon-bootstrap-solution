package com.instana;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the KafkaConnector class.
 */
public class KafkaConnectorTest {
    
    private KafkaConnector kafkaConnector;
    
    @BeforeEach
    void setUp() {
        kafkaConnector = new KafkaConnector();
    }
    
    @Test
    void testGetProducerProperties() {
        // When
        Properties props = kafkaConnector.getProducerProperties();
        
        // Then
        assertNotNull(props, "Producer properties should not be null");
        
        // Verify bootstrap servers
        String bootstrapServers = (String) props.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG);
        assertEquals("217.160.120.203:9092", bootstrapServers, "Bootstrap servers should match .env value");
        
        // Verify SASL authentication
        String securityProtocol = (String) props.get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG);
        assertEquals("SASL_PLAINTEXT", securityProtocol, "Security protocol should be SASL_PLAINTEXT");
        
        String saslMechanism = (String) props.get(SaslConfigs.SASL_MECHANISM);
        assertEquals("PLAIN", saslMechanism, "SASL mechanism should be PLAIN");
        
        String jaasConfig = (String) props.get(SaslConfigs.SASL_JAAS_CONFIG);
        assertTrue(jaasConfig.contains("username=\"alice\""), "JAAS config should contain username from .env");
        assertTrue(jaasConfig.contains("password=\"alice_in_wonderland\""), "JAAS config should contain password from .env");
        
        // Verify serializers
        String keySerializer = (String) props.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG);
        String valueSerializer = (String) props.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG);
        assertNotNull(keySerializer, "Key serializer should not be null");
        assertNotNull(valueSerializer, "Value serializer should not be null");
    }
    
    @Test
    void testGetConsumerProperties() {
        // Given
        String testGroupId = "test-group";
        
        // When
        Properties props = kafkaConnector.getConsumerProperties(testGroupId);
        
        // Then
        assertNotNull(props, "Consumer properties should not be null");
        
        // Verify bootstrap servers
        String bootstrapServers = (String) props.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG);
        assertEquals("217.160.120.203:9092", bootstrapServers, "Bootstrap servers should match .env value");
        
        // Verify SASL authentication
        String securityProtocol = (String) props.get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG);
        assertEquals("SASL_PLAINTEXT", securityProtocol, "Security protocol should be SASL_PLAINTEXT");
        
        String saslMechanism = (String) props.get(SaslConfigs.SASL_MECHANISM);
        assertEquals("PLAIN", saslMechanism, "SASL mechanism should be PLAIN");
        
        String jaasConfig = (String) props.get(SaslConfigs.SASL_JAAS_CONFIG);
        assertTrue(jaasConfig.contains("username=\"alice\""), "JAAS config should contain username from .env");
        assertTrue(jaasConfig.contains("password=\"alice_in_wonderland\""), "JAAS config should contain password from .env");
        
        // Verify deserializers
        String keyDeserializer = (String) props.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG);
        String valueDeserializer = (String) props.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG);
        assertNotNull(keyDeserializer, "Key deserializer should not be null");
        assertNotNull(valueDeserializer, "Value deserializer should not be null");
        
        // Verify group ID
        String groupId = (String) props.get(ConsumerConfig.GROUP_ID_CONFIG);
        assertEquals(testGroupId, groupId, "Group ID should match the provided value");
        
        // Verify auto offset reset
        String autoOffsetReset = (String) props.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG);
        assertEquals("earliest", autoOffsetReset, "Auto offset reset should be 'earliest'");
    }
}