package com.phenoml.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phenoml.api.core.ObjectMappers;
import com.phenoml.api.resources.summary.summarygeneration.requests.CreateSummaryRequest;
import com.phenoml.api.resources.summary.summarygeneration.types.CreateSummaryRequestMode;
import com.phenoml.api.resources.summary.types.CreateSummaryResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SummarySummaryGenerationWireTest {
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
                                "{\"success\":true,\"message\":\"message\",\"summary\":\"Patient John Doe is a 45-year-old male diagnosed with Type 2 Diabetes Mellitus on January 15, 2024. Current treatment plan includes lifestyle modifications and medication management.\",\"warnings\":[\"warnings\"]}"));
        CreateSummaryResponse response = client.summary()
                .summaryGeneration()
                .create(CreateSummaryRequest.builder()
                        .fhirResources(new HashMap<String, Object>() {
                            {
                                put("resourceType", "Bundle");
                                put("type", "collection");
                                put(
                                        "entry",
                                        new ArrayList<Object>(Arrays.asList(
                                                new HashMap<String, Object>() {
                                                    {
                                                        put("resource", new HashMap<String, Object>() {
                                                            {
                                                                put("resourceType", "Patient");
                                                                put(
                                                                        "name",
                                                                        new ArrayList<Object>(Arrays.asList(
                                                                                new HashMap<String, Object>() {
                                                                                    {
                                                                                        put(
                                                                                                "given",
                                                                                                new ArrayList<Object>(
                                                                                                        Arrays.asList(
                                                                                                                "John")));
                                                                                        put("family", "Doe");
                                                                                    }
                                                                                })));
                                                                put("gender", "male");
                                                                put("birthDate", "1979-03-15");
                                                            }
                                                        });
                                                    }
                                                },
                                                new HashMap<String, Object>() {
                                                    {
                                                        put("resource", new HashMap<String, Object>() {
                                                            {
                                                                put("resourceType", "Condition");
                                                                put("code", new HashMap<String, Object>() {
                                                                    {
                                                                        put("text", "Type 2 Diabetes Mellitus");
                                                                    }
                                                                });
                                                                put("onsetDateTime", "2024-01-15");
                                                            }
                                                        });
                                                    }
                                                })));
                            }
                        })
                        .mode(CreateSummaryRequestMode.NARRATIVE)
                        .templateId("a1b2c3d4-e5f6-7890-abcd-ef1234567890")
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
                + "  \"mode\": \"narrative\",\n"
                + "  \"template_id\": \"a1b2c3d4-e5f6-7890-abcd-ef1234567890\",\n"
                + "  \"fhir_resources\": {\n"
                + "    \"resourceType\": \"Bundle\",\n"
                + "    \"type\": \"collection\",\n"
                + "    \"entry\": [\n"
                + "      {\n"
                + "        \"resource\": {\n"
                + "          \"resourceType\": \"Patient\",\n"
                + "          \"name\": [\n"
                + "            {\n"
                + "              \"given\": [\n"
                + "                \"John\"\n"
                + "              ],\n"
                + "              \"family\": \"Doe\"\n"
                + "            }\n"
                + "          ],\n"
                + "          \"gender\": \"male\",\n"
                + "          \"birthDate\": \"1979-03-15\"\n"
                + "        }\n"
                + "      },\n"
                + "      {\n"
                + "        \"resource\": {\n"
                + "          \"resourceType\": \"Condition\",\n"
                + "          \"code\": {\n"
                + "            \"text\": \"Type 2 Diabetes Mellitus\"\n"
                + "          },\n"
                + "          \"onsetDateTime\": \"2024-01-15\"\n"
                + "        }\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
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
                + "  \"message\": \"message\",\n"
                + "  \"summary\": \"Patient John Doe is a 45-year-old male diagnosed with Type 2 Diabetes Mellitus on January 15, 2024. Current treatment plan includes lifestyle modifications and medication management.\",\n"
                + "  \"warnings\": [\n"
                + "    \"warnings\"\n"
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
