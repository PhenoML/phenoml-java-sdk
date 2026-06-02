package com.phenoml.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phenoml.api.core.ObjectMappers;
import com.phenoml.api.resources.agent.requests.ListRequest;
import com.phenoml.api.resources.agent.types.AgentCreateRequest;
import com.phenoml.api.resources.agent.types.AgentCreateRequestProvider;
import com.phenoml.api.resources.agent.types.AgentResponse;
import com.phenoml.api.resources.agent.types.DeleteResponse;
import com.phenoml.api.resources.agent.types.JsonPatchOperation;
import com.phenoml.api.resources.agent.types.JsonPatchOperationOp;
import com.phenoml.api.resources.agent.types.ListResponse;
import java.util.Arrays;
import java.util.Optional;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AgentWireTest {
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
    public void testCreate() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"success\":true,\"message\":\"Agent created successfully\",\"data\":{\"id\":\"agent_123\",\"name\":\"Medical Assistant\",\"description\":\"An AI assistant for medical information processing\",\"prompts\":[\"prompt_123\",\"prompt_456\"],\"tools\":[\"mcp_server_123\",\"mcp_server_456\"],\"workflows\":[\"workflow_123\",\"workflow_456\"],\"tags\":[\"medical\",\"fhir\"],\"provider\":\"7002b0b4-8d09-445a-bf65-0fafdaf26c35\"}}"));
        AgentResponse response = client.agent()
                .create(AgentCreateRequest.builder()
                        .name("Medical Assistant")
                        .provider(AgentCreateRequestProvider.of("7002b0b4-8d09-445a-bf65-0fafdaf26c35"))
                        .description("An AI assistant for medical information processing")
                        .prompts(Arrays.asList("prompt_123"))
                        .tags(Optional.of(Arrays.asList("medical", "fhir")))
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
        // Validate request body
        String actualRequestBody = request.getBody().readUtf8();
        String expectedRequestBody = ""
                + "{\n"
                + "  \"name\": \"Medical Assistant\",\n"
                + "  \"description\": \"An AI assistant for medical information processing\",\n"
                + "  \"prompts\": [\n"
                + "    \"prompt_123\"\n"
                + "  ],\n"
                + "  \"tags\": [\n"
                + "    \"medical\",\n"
                + "    \"fhir\"\n"
                + "  ],\n"
                + "  \"provider\": \"7002b0b4-8d09-445a-bf65-0fafdaf26c35\"\n"
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
                + "  \"success\": true,\n"
                + "  \"message\": \"Agent created successfully\",\n"
                + "  \"data\": {\n"
                + "    \"id\": \"agent_123\",\n"
                + "    \"name\": \"Medical Assistant\",\n"
                + "    \"description\": \"An AI assistant for medical information processing\",\n"
                + "    \"prompts\": [\n"
                + "      \"prompt_123\",\n"
                + "      \"prompt_456\"\n"
                + "    ],\n"
                + "    \"tools\": [\n"
                + "      \"mcp_server_123\",\n"
                + "      \"mcp_server_456\"\n"
                + "    ],\n"
                + "    \"workflows\": [\n"
                + "      \"workflow_123\",\n"
                + "      \"workflow_456\"\n"
                + "    ],\n"
                + "    \"tags\": [\n"
                + "      \"medical\",\n"
                + "      \"fhir\"\n"
                + "    ],\n"
                + "    \"provider\": \"7002b0b4-8d09-445a-bf65-0fafdaf26c35\"\n"
                + "  }\n"
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
    public void testList() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(TestResources.loadResource("/wire-tests/AgentWireTest_testList_response.json")));
        ListResponse response =
                client.agent().list(ListRequest.builder().tags("tags").build());
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
        String expectedResponseBody = TestResources.loadResource("/wire-tests/AgentWireTest_testList_response.json");
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
    public void testGet() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"success\":true,\"message\":\"Agent retrieved successfully\",\"data\":{\"id\":\"agent_123\",\"name\":\"Medical Assistant\",\"description\":\"An AI assistant for medical information processing\",\"prompts\":[\"prompt_123\",\"prompt_456\"],\"tools\":[\"mcp_server_123\",\"mcp_server_456\"],\"workflows\":[\"workflow_123\",\"workflow_456\"],\"tags\":[\"medical\",\"fhir\"],\"provider\":\"7002b0b4-8d09-445a-bf65-0fafdaf26c35\"}}"));
        AgentResponse response = client.agent().get("id");
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
                + "  \"success\": true,\n"
                + "  \"message\": \"Agent retrieved successfully\",\n"
                + "  \"data\": {\n"
                + "    \"id\": \"agent_123\",\n"
                + "    \"name\": \"Medical Assistant\",\n"
                + "    \"description\": \"An AI assistant for medical information processing\",\n"
                + "    \"prompts\": [\n"
                + "      \"prompt_123\",\n"
                + "      \"prompt_456\"\n"
                + "    ],\n"
                + "    \"tools\": [\n"
                + "      \"mcp_server_123\",\n"
                + "      \"mcp_server_456\"\n"
                + "    ],\n"
                + "    \"workflows\": [\n"
                + "      \"workflow_123\",\n"
                + "      \"workflow_456\"\n"
                + "    ],\n"
                + "    \"tags\": [\n"
                + "      \"medical\",\n"
                + "      \"fhir\"\n"
                + "    ],\n"
                + "    \"provider\": \"7002b0b4-8d09-445a-bf65-0fafdaf26c35\"\n"
                + "  }\n"
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
    public void testUpdate() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"success\":true,\"message\":\"Agent updated successfully\",\"data\":{\"id\":\"agent_123\",\"name\":\"Medical Assistant\",\"description\":\"An AI assistant for medical information processing\",\"prompts\":[\"prompt_123\",\"prompt_456\"],\"tools\":[\"mcp_server_123\",\"mcp_server_456\"],\"workflows\":[\"workflow_123\",\"workflow_456\"],\"tags\":[\"medical\",\"fhir\"],\"provider\":\"7002b0b4-8d09-445a-bf65-0fafdaf26c35\"}}"));
        AgentResponse response = client.agent()
                .update(
                        "id",
                        AgentCreateRequest.builder()
                                .name("Medical Assistant")
                                .provider(AgentCreateRequestProvider.of("7002b0b4-8d09-445a-bf65-0fafdaf26c35"))
                                .description("Updated description for the medical assistant")
                                .prompts(Arrays.asList("prompt_123"))
                                .tags(Optional.of(Arrays.asList("medical", "fhir", "updated")))
                                .build());
        // OAuth: consume the token request
        server.takeRequest();
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("PUT", request.getMethod());

        // Validate OAuth Authorization header
        Assertions.assertEquals(
                "Bearer test-token",
                request.getHeader("Authorization"),
                "OAuth Authorization header should contain Bearer token from OAuth flow");
        // Validate request body
        String actualRequestBody = request.getBody().readUtf8();
        String expectedRequestBody = ""
                + "{\n"
                + "  \"name\": \"Medical Assistant\",\n"
                + "  \"description\": \"Updated description for the medical assistant\",\n"
                + "  \"prompts\": [\n"
                + "    \"prompt_123\"\n"
                + "  ],\n"
                + "  \"tags\": [\n"
                + "    \"medical\",\n"
                + "    \"fhir\",\n"
                + "    \"updated\"\n"
                + "  ],\n"
                + "  \"provider\": \"7002b0b4-8d09-445a-bf65-0fafdaf26c35\"\n"
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
                + "  \"success\": true,\n"
                + "  \"message\": \"Agent updated successfully\",\n"
                + "  \"data\": {\n"
                + "    \"id\": \"agent_123\",\n"
                + "    \"name\": \"Medical Assistant\",\n"
                + "    \"description\": \"An AI assistant for medical information processing\",\n"
                + "    \"prompts\": [\n"
                + "      \"prompt_123\",\n"
                + "      \"prompt_456\"\n"
                + "    ],\n"
                + "    \"tools\": [\n"
                + "      \"mcp_server_123\",\n"
                + "      \"mcp_server_456\"\n"
                + "    ],\n"
                + "    \"workflows\": [\n"
                + "      \"workflow_123\",\n"
                + "      \"workflow_456\"\n"
                + "    ],\n"
                + "    \"tags\": [\n"
                + "      \"medical\",\n"
                + "      \"fhir\"\n"
                + "    ],\n"
                + "    \"provider\": \"7002b0b4-8d09-445a-bf65-0fafdaf26c35\"\n"
                + "  }\n"
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
    public void testDelete() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"success\":true,\"message\":\"Agent deleted successfully\"}"));
        DeleteResponse response = client.agent().delete("id");
        // OAuth: consume the token request
        server.takeRequest();
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("DELETE", request.getMethod());

        // Validate OAuth Authorization header
        Assertions.assertEquals(
                "Bearer test-token",
                request.getHeader("Authorization"),
                "OAuth Authorization header should contain Bearer token from OAuth flow");

        // Validate response body
        Assertions.assertNotNull(response, "Response should not be null");
        String actualResponseJson = objectMapper.writeValueAsString(response);
        String expectedResponseBody =
                "" + "{\n" + "  \"success\": true,\n" + "  \"message\": \"Agent deleted successfully\"\n" + "}";
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
    public void testPatch() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"success\":true,\"message\":\"Agent patched successfully\",\"data\":{\"id\":\"agent_123\",\"name\":\"Medical Assistant\",\"description\":\"An AI assistant for medical information processing\",\"prompts\":[\"prompt_123\",\"prompt_456\"],\"tools\":[\"mcp_server_123\",\"mcp_server_456\"],\"workflows\":[\"workflow_123\",\"workflow_456\"],\"tags\":[\"medical\",\"fhir\"],\"provider\":\"7002b0b4-8d09-445a-bf65-0fafdaf26c35\"}}"));
        AgentResponse response = client.agent()
                .patch(
                        "id",
                        Arrays.asList(
                                JsonPatchOperation.builder()
                                        .op(JsonPatchOperationOp.REPLACE)
                                        .path("/description")
                                        .value("patched description")
                                        .build(),
                                JsonPatchOperation.builder()
                                        .op(JsonPatchOperationOp.ADD)
                                        .path("/tags/-")
                                        .value("updated")
                                        .build()));
        // OAuth: consume the token request
        server.takeRequest();
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("PATCH", request.getMethod());

        // Validate OAuth Authorization header
        Assertions.assertEquals(
                "Bearer test-token",
                request.getHeader("Authorization"),
                "OAuth Authorization header should contain Bearer token from OAuth flow");
        // Validate request body
        String actualRequestBody = request.getBody().readUtf8();
        String expectedRequestBody = ""
                + "[\n"
                + "  {\n"
                + "    \"op\": \"replace\",\n"
                + "    \"path\": \"/description\",\n"
                + "    \"value\": \"patched description\"\n"
                + "  },\n"
                + "  {\n"
                + "    \"op\": \"add\",\n"
                + "    \"path\": \"/tags/-\",\n"
                + "    \"value\": \"updated\"\n"
                + "  }\n"
                + "]";
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
                + "  \"success\": true,\n"
                + "  \"message\": \"Agent patched successfully\",\n"
                + "  \"data\": {\n"
                + "    \"id\": \"agent_123\",\n"
                + "    \"name\": \"Medical Assistant\",\n"
                + "    \"description\": \"An AI assistant for medical information processing\",\n"
                + "    \"prompts\": [\n"
                + "      \"prompt_123\",\n"
                + "      \"prompt_456\"\n"
                + "    ],\n"
                + "    \"tools\": [\n"
                + "      \"mcp_server_123\",\n"
                + "      \"mcp_server_456\"\n"
                + "    ],\n"
                + "    \"workflows\": [\n"
                + "      \"workflow_123\",\n"
                + "      \"workflow_456\"\n"
                + "    ],\n"
                + "    \"tags\": [\n"
                + "      \"medical\",\n"
                + "      \"fhir\"\n"
                + "    ],\n"
                + "    \"provider\": \"7002b0b4-8d09-445a-bf65-0fafdaf26c35\"\n"
                + "  }\n"
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
