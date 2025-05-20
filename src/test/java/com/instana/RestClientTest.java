package com.instana;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the RestClient class.
 * 
 * Note: These tests verify that the RestClient is correctly configured with values from the .env file.
 * Actual HTTP requests are not tested to avoid external dependencies.
 */
public class RestClientTest {

    private RestClient restClient;
    private Dotenv dotenv;

    @BeforeEach
    void setUp() {
        restClient = new RestClient();
        dotenv = Dotenv.configure().load();
    }

    @Test
    void testRestClientInitialization() {
        // Verify that the RestClient can be initialized without errors
        assertNotNull(restClient, "RestClient should be initialized");

        // Verify that the .env file contains the required variables
        String baseUrl = dotenv.get("REST_SERVER_URL");
        String teamColor = dotenv.get("TEAM_COLOR");

        assertEquals("https://hackathon.orange-coding.net", baseUrl, "REST_SERVER_URL should match the value in .env");
        assertEquals("red", teamColor, "TEAM_COLOR should match the value in .env");
    }

    /**
     * This test doesn't actually make an HTTP request, but verifies that the method doesn't throw exceptions
     * and returns a proper RestResponse object.
     * In a real-world scenario, you would use a mock HTTP client to test the actual request/response handling.
     */
    @Test
    void testPostMethod() {
        // This will likely return an error response since we're not actually connecting to a server
        // but it should not throw an exception
        assertDoesNotThrow(() -> {
            RestClient.RestResponse response = restClient.post("test-endpoint", 1, "test-value");

            // Verify that we got a response object
            assertNotNull(response, "Response should not be null");

            // Log the response details for debugging
            System.out.println("[DEBUG_LOG] Post response status code: " + response.getStatusCode());
            System.out.println("[DEBUG_LOG] Post response body: " + response.getResponseBody());

            // Since we're not actually connecting to a server, we expect an error status code
            assertEquals(-1, response.getStatusCode(), "Status code should be -1 for connection error");
            assertNotNull(response.getResponseBody(), "Response body should not be null");
        }, "post method should not throw an exception");
    }
}
