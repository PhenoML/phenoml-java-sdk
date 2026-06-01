package com.phenoml.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phenoml.api.core.ObjectMappers;
import com.phenoml.api.resources.agent.chat.requests.AgentChatRequest;
import com.phenoml.api.resources.agent.chat.requests.AgentStreamChatRequest;
import com.phenoml.api.resources.agent.chat.requests.ListMessagesRequest;
import com.phenoml.api.resources.agent.chat.types.ListMessagesRequestOrder;
import com.phenoml.api.resources.agent.chat.types.ListMessagesRequestRole;
import com.phenoml.api.resources.agent.chat.types.ListMessagesResponse;
import com.phenoml.api.resources.agent.types.AgentChatResponse;
import com.phenoml.api.resources.agent.types.AgentChatStreamEvent;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AgentChatWireTest {
    private MockWebServer server;
    private PhenomlClient client;
    private ObjectMapper objectMapper = ObjectMappers.JSON_MAPPER;

    @BeforeEach
    public void setup() throws Exception {
        server = new MockWebServer();
        server.start();
        client = PhenomlClient.withCredentials("test-client-id", "test-client-secret")
                .url(server.url("/").toString())
                .build();
    }

    @AfterEach
    public void teardown() throws Exception {
        server.shutdown();
    }

    @Test
    public void testSend() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"response\":\"Based on the patient records, they have been diagnosed with Type 2 Diabetes Mellitus (ICD-10: E11.65) and Essential Hypertension (ICD-10: I10). Current medications include Metformin 500mg BID and Lisinopril 10mg daily. Most recent HbA1c was 7.2% on 2024-12-15.\",\"success\":true,\"message\":\"Response generated successfully\",\"session_id\":\"session-abc123\"}"));
        AgentChatResponse response = client.agent()
                .chat()
                .send(AgentChatRequest.builder()
                        .message("What is the patient's current condition?")
                        .agentId("agent-123")
                        .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
                        .phenomlFhirProvider(
                                "550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
                        .sessionId("session-abc123")
                        .build());
        // OAuth: consume the token request
        server.takeRequest();
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());

        // Validate OAuth Authorization header
        Assertions.assertEquals(
                "Bearer test-token",
                request.getHeader("Authorization"),
                "OAuth Authorization header should contain Bearer token from OAuth flow");

        // Validate headers
        Assertions.assertEquals(
                "Patient/550e8400-e29b-41d4-a716-446655440000",
                request.getHeader("X-Phenoml-On-Behalf-Of"),
                "Header 'X-Phenoml-On-Behalf-Of' should match expected value");
        Assertions.assertEquals(
                "550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...",
                request.getHeader("X-Phenoml-Fhir-Provider"),
                "Header 'X-Phenoml-Fhir-Provider' should match expected value");
        // Validate request body
        String actualRequestBody = request.getBody().readUtf8();
        String expectedRequestBody = ""
                + "{\n"
                + "  \"message\": \"What is the patient's current condition?\",\n"
                + "  \"session_id\": \"session-abc123\",\n"
                + "  \"agent_id\": \"agent-123\"\n"
                + "}";
        JsonNode actualJson = objectMapper.readTree(actualRequestBody);
        JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
        Assertions.assertTrue(jsonEquals(expectedJson, actualJson), "Request body structure does not match expected");
        if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
            String discriminator = null;
            if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
            else if (actualJson.has("_type"))
                discriminator = actualJson.get("_type").asText();
            else if (actualJson.has("kind"))
                discriminator = actualJson.get("kind").asText();
            Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
            Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
        }

        if (!actualJson.isNull()) {
            Assertions.assertTrue(
                    actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
                    "request should be a valid JSON value");
        }

        if (actualJson.isArray()) {
            Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
        }
        if (actualJson.isObject()) {
            Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
        }

        // Validate response body
        Assertions.assertNotNull(response, "Response should not be null");
        String actualResponseJson = objectMapper.writeValueAsString(response);
        String expectedResponseBody = ""
                + "{\n"
                + "  \"response\": \"Based on the patient records, they have been diagnosed with Type 2 Diabetes Mellitus (ICD-10: E11.65) and Essential Hypertension (ICD-10: I10). Current medications include Metformin 500mg BID and Lisinopril 10mg daily. Most recent HbA1c was 7.2% on 2024-12-15.\",\n"
                + "  \"success\": true,\n"
                + "  \"message\": \"Response generated successfully\",\n"
                + "  \"session_id\": \"session-abc123\"\n"
                + "}";
        JsonNode actualResponseNode = objectMapper.readTree(actualResponseJson);
        JsonNode expectedResponseNode = objectMapper.readTree(expectedResponseBody);
        Assertions.assertTrue(
                jsonEquals(expectedResponseNode, actualResponseNode),
                "Response body structure does not match expected");
        if (actualResponseNode.has("type") || actualResponseNode.has("_type") || actualResponseNode.has("kind")) {
            String discriminator = null;
            if (actualResponseNode.has("type"))
                discriminator = actualResponseNode.get("type").asText();
            else if (actualResponseNode.has("_type"))
                discriminator = actualResponseNode.get("_type").asText();
            else if (actualResponseNode.has("kind"))
                discriminator = actualResponseNode.get("kind").asText();
            Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
            Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
        }

        if (!actualResponseNode.isNull()) {
            Assertions.assertTrue(
                    actualResponseNode.isObject() || actualResponseNode.isArray() || actualResponseNode.isValueNode(),
                    "response should be a valid JSON value");
        }

        if (actualResponseNode.isArray()) {
            Assertions.assertTrue(actualResponseNode.size() >= 0, "Array should have valid size");
        }
        if (actualResponseNode.isObject()) {
            Assertions.assertTrue(actualResponseNode.size() >= 0, "Object should have valid field count");
        }
    }

    @Test
    public void testStream() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        Iterable<AgentChatStreamEvent> response = client.agent().chat().stream(AgentStreamChatRequest.builder()
                .message("What is the patient's current condition?")
                .agentId("agent-123")
                .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
                .phenomlFhirProvider(
                        "550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
                .sessionId("session-abc123")
                .build());
        // OAuth: consume the token request
        server.takeRequest();
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());

        // Validate OAuth Authorization header
        Assertions.assertEquals(
                "Bearer test-token",
                request.getHeader("Authorization"),
                "OAuth Authorization header should contain Bearer token from OAuth flow");

        // Validate headers
        Assertions.assertEquals(
                "Patient/550e8400-e29b-41d4-a716-446655440000",
                request.getHeader("X-Phenoml-On-Behalf-Of"),
                "Header 'X-Phenoml-On-Behalf-Of' should match expected value");
        Assertions.assertEquals(
                "550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...",
                request.getHeader("X-Phenoml-Fhir-Provider"),
                "Header 'X-Phenoml-Fhir-Provider' should match expected value");
        // Validate request body
        String actualRequestBody = request.getBody().readUtf8();
        String expectedRequestBody = ""
                + "{\n"
                + "  \"message\": \"What is the patient's current condition?\",\n"
                + "  \"session_id\": \"session-abc123\",\n"
                + "  \"agent_id\": \"agent-123\"\n"
                + "}";
        JsonNode actualJson = objectMapper.readTree(actualRequestBody);
        JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
        Assertions.assertTrue(jsonEquals(expectedJson, actualJson), "Request body structure does not match expected");
        if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
            String discriminator = null;
            if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
            else if (actualJson.has("_type"))
                discriminator = actualJson.get("_type").asText();
            else if (actualJson.has("kind"))
                discriminator = actualJson.get("kind").asText();
            Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
            Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
        }

        if (!actualJson.isNull()) {
            Assertions.assertTrue(
                    actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
                    "request should be a valid JSON value");
        }

        if (actualJson.isArray()) {
            Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
        }
        if (actualJson.isObject()) {
            Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
        }

        // Validate response deserialization
        Assertions.assertNotNull(response, "Response should not be null");
        // Verify the response can be serialized back to JSON
        String responseJson = objectMapper.writeValueAsString(response);
        Assertions.assertNotNull(responseJson);
        Assertions.assertFalse(responseJson.isEmpty());
    }

    @Test
    public void testListMessages() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"messages\":[{\"id\":\"message_001\",\"session_id\":\"session_123\",\"role\":\"user\",\"content\":\"What is the patient's current condition?\",\"created\":\"2025-03-01T14:00:00Z\",\"updated\":\"2025-03-01T14:00:00Z\",\"function_name\":\"get_patient_info\",\"function_args\":{\"patient_id\":\"123\"},\"function_result\":{\"name\":\"John Doe\"},\"message_order\":1},{\"id\":\"message_002\",\"session_id\":\"session_123\",\"role\":\"assistant\",\"content\":\"Based on the patient records, they have been diagnosed with Type 2 Diabetes Mellitus (ICD-10: E11.65).\",\"created\":\"2025-03-01T14:00:02Z\",\"updated\":\"2025-03-01T14:00:02Z\",\"function_name\":\"get_patient_info\",\"function_args\":{\"patient_id\":\"123\"},\"function_result\":{\"name\":\"John Doe\"},\"message_order\":2}],\"total\":2,\"session_id\":\"session_123\"}"));
        ListMessagesResponse response = client.agent()
                .chat()
                .listMessages(ListMessagesRequest.builder()
                        .chatSessionId("chat_session_id")
                        .numMessages(1)
                        .role(ListMessagesRequestRole.USER)
                        .order(ListMessagesRequestOrder.ASC)
                        .build());
        // OAuth: consume the token request
        server.takeRequest();
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("GET", request.getMethod());

        // Validate OAuth Authorization header
        Assertions.assertEquals(
                "Bearer test-token",
                request.getHeader("Authorization"),
                "OAuth Authorization header should contain Bearer token from OAuth flow");

        // Validate response body
        Assertions.assertNotNull(response, "Response should not be null");
        String actualResponseJson = objectMapper.writeValueAsString(response);
        String expectedResponseBody = ""
                + "{\n"
                + "  \"messages\": [\n"
                + "    {\n"
                + "      \"id\": \"message_001\",\n"
                + "      \"session_id\": \"session_123\",\n"
                + "      \"role\": \"user\",\n"
                + "      \"content\": \"What is the patient's current condition?\",\n"
                + "      \"created\": \"2025-03-01T14:00:00Z\",\n"
                + "      \"updated\": \"2025-03-01T14:00:00Z\",\n"
                + "      \"function_name\": \"get_patient_info\",\n"
                + "      \"function_args\": {\n"
                + "        \"patient_id\": \"123\"\n"
                + "      },\n"
                + "      \"function_result\": {\n"
                + "        \"name\": \"John Doe\"\n"
                + "      },\n"
                + "      \"message_order\": 1\n"
                + "    },\n"
                + "    {\n"
                + "      \"id\": \"message_002\",\n"
                + "      \"session_id\": \"session_123\",\n"
                + "      \"role\": \"assistant\",\n"
                + "      \"content\": \"Based on the patient records, they have been diagnosed with Type 2 Diabetes Mellitus (ICD-10: E11.65).\",\n"
                + "      \"created\": \"2025-03-01T14:00:02Z\",\n"
                + "      \"updated\": \"2025-03-01T14:00:02Z\",\n"
                + "      \"function_name\": \"get_patient_info\",\n"
                + "      \"function_args\": {\n"
                + "        \"patient_id\": \"123\"\n"
                + "      },\n"
                + "      \"function_result\": {\n"
                + "        \"name\": \"John Doe\"\n"
                + "      },\n"
                + "      \"message_order\": 2\n"
                + "    }\n"
                + "  ],\n"
                + "  \"total\": 2,\n"
                + "  \"session_id\": \"session_123\"\n"
                + "}";
        JsonNode actualResponseNode = objectMapper.readTree(actualResponseJson);
        JsonNode expectedResponseNode = objectMapper.readTree(expectedResponseBody);
        Assertions.assertTrue(
                jsonEquals(expectedResponseNode, actualResponseNode),
                "Response body structure does not match expected");
        if (actualResponseNode.has("type") || actualResponseNode.has("_type") || actualResponseNode.has("kind")) {
            String discriminator = null;
            if (actualResponseNode.has("type"))
                discriminator = actualResponseNode.get("type").asText();
            else if (actualResponseNode.has("_type"))
                discriminator = actualResponseNode.get("_type").asText();
            else if (actualResponseNode.has("kind"))
                discriminator = actualResponseNode.get("kind").asText();
            Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
            Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
        }

        if (!actualResponseNode.isNull()) {
            Assertions.assertTrue(
                    actualResponseNode.isObject() || actualResponseNode.isArray() || actualResponseNode.isValueNode(),
                    "response should be a valid JSON value");
        }

        if (actualResponseNode.isArray()) {
            Assertions.assertTrue(actualResponseNode.size() >= 0, "Array should have valid size");
        }
        if (actualResponseNode.isObject()) {
            Assertions.assertTrue(actualResponseNode.size() >= 0, "Object should have valid field count");
        }
    }

    /**
     * Compares two JsonNodes with numeric equivalence and null safety.
     * For objects, checks that all fields in 'expected' exist in 'actual' with matching values.
     * Allows 'actual' to have extra fields (e.g., default values added during serialization).
     */
    private boolean jsonEquals(JsonNode expected, JsonNode actual) {
        if (expected == null && actual == null) return true;
        if (expected == null || actual == null) return false;
        if (expected.equals(actual)) return true;
        if (expected.isNumber() && actual.isNumber())
            return Math.abs(expected.doubleValue() - actual.doubleValue()) < 1e-10;
        if (expected.isObject() && actual.isObject()) {
            java.util.Iterator<java.util.Map.Entry<String, JsonNode>> iter = expected.fields();
            while (iter.hasNext()) {
                java.util.Map.Entry<String, JsonNode> entry = iter.next();
                JsonNode actualValue = actual.get(entry.getKey());
                if (actualValue == null) {
                    if (!entry.getValue().isNull()) return false;
                } else if (!jsonEquals(entry.getValue(), actualValue)) return false;
            }
            return true;
        }
        if (expected.isArray() && actual.isArray()) {
            if (expected.size() != actual.size()) return false;
            for (int i = 0; i < expected.size(); i++) {
                if (!jsonEquals(expected.get(i), actual.get(i))) return false;
            }
            return true;
        }
        return false;
    }
}
