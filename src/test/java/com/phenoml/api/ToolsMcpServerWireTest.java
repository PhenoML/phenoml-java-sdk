package com.phenoml.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phenoml.api.core.ObjectMappers;
import com.phenoml.api.resources.tools.mcpserver.requests.McpServerCreateRequest;
import com.phenoml.api.resources.tools.types.McpServerResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ToolsMcpServerWireTest {
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
                                "{\"success\":true,\"message\":\"MCP Server and Tools created successfully\",\"data\":{\"id\":\"123\",\"name\":\"My MCP Server\",\"description\":\"My MCP Server is a server that provides MCP services\",\"mcp_server_url\":\"https://mcp.example.com\"},\"mcp_servers\":[{\"id\":\"123\",\"name\":\"My MCP Server\",\"description\":\"My MCP Server is a server that provides MCP services\",\"mcp_server_url\":\"https://mcp.example.com\"}],\"mcp_server_tools\":[{\"id\":\"tool-001\",\"name\":\"search_endpoints\",\"description\":\"Search across the documented endpoints\",\"input_schema\":{\"query\":\"string\"},\"mcp_server_id\":\"123\",\"mcp_server_url\":\"https://mcp.example.com\"},{\"id\":\"tool-002\",\"name\":\"get_endpoint\",\"description\":\"Fetch a single endpoint by ID\",\"input_schema\":{\"endpoint_id\":\"string\"},\"mcp_server_id\":\"123\",\"mcp_server_url\":\"https://mcp.example.com\"}]}"));
        McpServerResponse response = client.tools()
                .mcpServer()
                .create(McpServerCreateRequest.builder()
                        .name("My MCP Server")
                        .mcpServerUrl("https://mcp.example.com")
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
                + "  \"name\": \"My MCP Server\",\n"
                + "  \"mcp_server_url\": \"https://mcp.example.com\"\n"
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
                + "  \"message\": \"MCP Server and Tools created successfully\",\n"
                + "  \"data\": {\n"
                + "    \"id\": \"123\",\n"
                + "    \"name\": \"My MCP Server\",\n"
                + "    \"description\": \"My MCP Server is a server that provides MCP services\",\n"
                + "    \"mcp_server_url\": \"https://mcp.example.com\"\n"
                + "  },\n"
                + "  \"mcp_servers\": [\n"
                + "    {\n"
                + "      \"id\": \"123\",\n"
                + "      \"name\": \"My MCP Server\",\n"
                + "      \"description\": \"My MCP Server is a server that provides MCP services\",\n"
                + "      \"mcp_server_url\": \"https://mcp.example.com\"\n"
                + "    }\n"
                + "  ],\n"
                + "  \"mcp_server_tools\": [\n"
                + "    {\n"
                + "      \"id\": \"tool-001\",\n"
                + "      \"name\": \"search_endpoints\",\n"
                + "      \"description\": \"Search across the documented endpoints\",\n"
                + "      \"input_schema\": {\n"
                + "        \"query\": \"string\"\n"
                + "      },\n"
                + "      \"mcp_server_id\": \"123\",\n"
                + "      \"mcp_server_url\": \"https://mcp.example.com\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"id\": \"tool-002\",\n"
                + "      \"name\": \"get_endpoint\",\n"
                + "      \"description\": \"Fetch a single endpoint by ID\",\n"
                + "      \"input_schema\": {\n"
                + "        \"endpoint_id\": \"string\"\n"
                + "      },\n"
                + "      \"mcp_server_id\": \"123\",\n"
                + "      \"mcp_server_url\": \"https://mcp.example.com\"\n"
                + "    }\n"
                + "  ]\n"
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
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"success\":true,\"message\":\"MCP Servers retrieved successfully\",\"data\":{\"id\":\"123\",\"name\":\"My MCP Server\",\"description\":\"My MCP Server is a server that provides MCP services\",\"mcp_server_url\":\"https://mcp.example.com\"},\"mcp_servers\":[{\"id\":\"123\",\"name\":\"My MCP Server\",\"description\":\"My MCP Server is a server that provides MCP services\",\"mcp_server_url\":\"https://mcp.example.com\"},{\"id\":\"456\",\"name\":\"Another MCP Server\",\"description\":\"A second MCP server\",\"mcp_server_url\":\"https://other.mcp.example.com\"}],\"mcp_server_tools\":[{\"id\":\"123\",\"name\":\"My MCP Server Tool\",\"description\":\"My MCP Server Tool is a tool that provides MCP services\",\"input_schema\":{\"name\":\"string\",\"age\":\"number\"},\"mcp_server_id\":\"123\",\"mcp_server_url\":\"https://mcp.example.com\"}]}"));
        McpServerResponse response = client.tools().mcpServer().list();
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
                + "  \"message\": \"MCP Servers retrieved successfully\",\n"
                + "  \"data\": {\n"
                + "    \"id\": \"123\",\n"
                + "    \"name\": \"My MCP Server\",\n"
                + "    \"description\": \"My MCP Server is a server that provides MCP services\",\n"
                + "    \"mcp_server_url\": \"https://mcp.example.com\"\n"
                + "  },\n"
                + "  \"mcp_servers\": [\n"
                + "    {\n"
                + "      \"id\": \"123\",\n"
                + "      \"name\": \"My MCP Server\",\n"
                + "      \"description\": \"My MCP Server is a server that provides MCP services\",\n"
                + "      \"mcp_server_url\": \"https://mcp.example.com\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"id\": \"456\",\n"
                + "      \"name\": \"Another MCP Server\",\n"
                + "      \"description\": \"A second MCP server\",\n"
                + "      \"mcp_server_url\": \"https://other.mcp.example.com\"\n"
                + "    }\n"
                + "  ],\n"
                + "  \"mcp_server_tools\": [\n"
                + "    {\n"
                + "      \"id\": \"123\",\n"
                + "      \"name\": \"My MCP Server Tool\",\n"
                + "      \"description\": \"My MCP Server Tool is a tool that provides MCP services\",\n"
                + "      \"input_schema\": {\n"
                + "        \"name\": \"string\",\n"
                + "        \"age\": \"number\"\n"
                + "      },\n"
                + "      \"mcp_server_id\": \"123\",\n"
                + "      \"mcp_server_url\": \"https://mcp.example.com\"\n"
                + "    }\n"
                + "  ]\n"
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
    public void testGet() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"success\":true,\"message\":\"MCP Server retrieved successfully\",\"data\":{\"id\":\"123\",\"name\":\"My MCP Server\",\"description\":\"My MCP Server is a server that provides MCP services\",\"mcp_server_url\":\"https://mcp.example.com\"},\"mcp_servers\":[{\"id\":\"123\",\"name\":\"My MCP Server\",\"description\":\"My MCP Server is a server that provides MCP services\",\"mcp_server_url\":\"https://mcp.example.com\"}],\"mcp_server_tools\":[{\"id\":\"123\",\"name\":\"My MCP Server Tool\",\"description\":\"My MCP Server Tool is a tool that provides MCP services\",\"input_schema\":{\"name\":\"string\",\"age\":\"number\"},\"mcp_server_id\":\"123\",\"mcp_server_url\":\"https://mcp.example.com\"}]}"));
        McpServerResponse response = client.tools().mcpServer().get("mcp_server_id");
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
                + "  \"message\": \"MCP Server retrieved successfully\",\n"
                + "  \"data\": {\n"
                + "    \"id\": \"123\",\n"
                + "    \"name\": \"My MCP Server\",\n"
                + "    \"description\": \"My MCP Server is a server that provides MCP services\",\n"
                + "    \"mcp_server_url\": \"https://mcp.example.com\"\n"
                + "  },\n"
                + "  \"mcp_servers\": [\n"
                + "    {\n"
                + "      \"id\": \"123\",\n"
                + "      \"name\": \"My MCP Server\",\n"
                + "      \"description\": \"My MCP Server is a server that provides MCP services\",\n"
                + "      \"mcp_server_url\": \"https://mcp.example.com\"\n"
                + "    }\n"
                + "  ],\n"
                + "  \"mcp_server_tools\": [\n"
                + "    {\n"
                + "      \"id\": \"123\",\n"
                + "      \"name\": \"My MCP Server Tool\",\n"
                + "      \"description\": \"My MCP Server Tool is a tool that provides MCP services\",\n"
                + "      \"input_schema\": {\n"
                + "        \"name\": \"string\",\n"
                + "        \"age\": \"number\"\n"
                + "      },\n"
                + "      \"mcp_server_id\": \"123\",\n"
                + "      \"mcp_server_url\": \"https://mcp.example.com\"\n"
                + "    }\n"
                + "  ]\n"
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
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"success\":true,\"message\":\"MCP Server Configuration and Tools deleted successfully\",\"data\":{\"id\":\"123\",\"name\":\"My MCP Server\",\"description\":\"My MCP Server is a server that provides MCP services\",\"mcp_server_url\":\"https://mcp.example.com\"},\"mcp_servers\":[{\"id\":\"123\",\"name\":\"My MCP Server\",\"description\":\"My MCP Server is a server that provides MCP services\",\"mcp_server_url\":\"https://mcp.example.com\"}],\"mcp_server_tools\":[{\"id\":\"123\",\"name\":\"My MCP Server Tool\",\"description\":\"My MCP Server Tool is a tool that provides MCP services\",\"input_schema\":{\"name\":\"string\",\"age\":\"number\"},\"mcp_server_id\":\"123\",\"mcp_server_url\":\"https://mcp.example.com\"}]}"));
        McpServerResponse response = client.tools().mcpServer().delete("mcp_server_id");
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
        String expectedResponseBody = ""
                + "{\n"
                + "  \"success\": true,\n"
                + "  \"message\": \"MCP Server Configuration and Tools deleted successfully\",\n"
                + "  \"data\": {\n"
                + "    \"id\": \"123\",\n"
                + "    \"name\": \"My MCP Server\",\n"
                + "    \"description\": \"My MCP Server is a server that provides MCP services\",\n"
                + "    \"mcp_server_url\": \"https://mcp.example.com\"\n"
                + "  },\n"
                + "  \"mcp_servers\": [\n"
                + "    {\n"
                + "      \"id\": \"123\",\n"
                + "      \"name\": \"My MCP Server\",\n"
                + "      \"description\": \"My MCP Server is a server that provides MCP services\",\n"
                + "      \"mcp_server_url\": \"https://mcp.example.com\"\n"
                + "    }\n"
                + "  ],\n"
                + "  \"mcp_server_tools\": [\n"
                + "    {\n"
                + "      \"id\": \"123\",\n"
                + "      \"name\": \"My MCP Server Tool\",\n"
                + "      \"description\": \"My MCP Server Tool is a tool that provides MCP services\",\n"
                + "      \"input_schema\": {\n"
                + "        \"name\": \"string\",\n"
                + "        \"age\": \"number\"\n"
                + "      },\n"
                + "      \"mcp_server_id\": \"123\",\n"
                + "      \"mcp_server_url\": \"https://mcp.example.com\"\n"
                + "    }\n"
                + "  ]\n"
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
