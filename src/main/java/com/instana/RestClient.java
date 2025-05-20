package com.instana;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for making REST API calls.
 */
public class RestClient {
    private static final Logger logger = LoggerFactory.getLogger(RestClient.class);
    private final String baseUrl;
    private final String teamColor;
    private final String username;
    private final String password;
    private final ObjectMapper objectMapper;

    /**
     * Class to encapsulate the HTTP response with status code and body.
     */
    public static class RestResponse {
        private final int statusCode;
        private final String responseBody;

        public RestResponse(int statusCode, String responseBody) {
            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getResponseBody() {
            return responseBody;
        }

        public boolean isSuccess() {
          try {
            Map<String, Object> map = new ObjectMapper().readValue(this.responseBody, Map.class);
            if(statusCode == 200){
              if(map.containsKey("success")){
                return (boolean) map.get("success");
              }else{
                logger.info("Response body does not contain a 'success' key");
                return true;
              }
            }
          }catch (Exception e){
            logger.error("Error deserializing response body: {}", this.responseBody, e);
            return false;
          }
          return false;
        }
    }

    /**
     * Creates a new RestClient instance.
     * Loads the REST_SERVER_URL and TEAM_COLOR from the .env file.
     */
    public RestClient() {
        Dotenv dotenv = Dotenv.configure().load();
        this.baseUrl = dotenv.get("REST_SERVER_URL");
        this.teamColor = dotenv.get("TEAM_COLOR");
        this.username = dotenv.get("REST_AUTH_USER");
        this.password = dotenv.get("REST_AUTH_PWD");
        this.objectMapper = new ObjectMapper();

        logger.info("RestClient initialized with base URL: {}", baseUrl);
    }

    /**
     * Posts data to the specified endpoint.
     *
     * @param endpoint The endpoint to post to (will be appended to the base URL)
     * @param stage The stage value to include in the JSON payload
     * @param value The value to include in the JSON payload
     * @return RestResponse containing the status code and response body
     */
    public RestResponse post(String endpoint, int stage, String value) {
        String url = baseUrl + endpoint;
        logger.info("Posting to URL: {}", url);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");

            // Add Basic authentication header
            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            httpPost.setHeader("Authorization", "Basic " + encodedAuth);

            // Create the JSON payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("team", teamColor);
            payload.put("stage", stage);
            payload.put("value", value);

            String jsonPayload = objectMapper.writeValueAsString(payload);
            logger.debug("JSON payload: {}", jsonPayload);

            StringEntity entity = new StringEntity(jsonPayload);
            httpPost.setEntity(entity);

            // Execute the request
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                logger.info("Response status code: {}", statusCode);

                // Extract response body
                String responseBody = "";
                HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null) {
                    responseBody = EntityUtils.toString(responseEntity);
                    logger.debug("Response body: {}", responseBody);
                }

                return new RestResponse(statusCode, responseBody);
            }
        } catch (IOException e) {
            logger.error("Error posting to REST endpoint", e);
            return new RestResponse(-1, e.getMessage());
        }
    }
}
