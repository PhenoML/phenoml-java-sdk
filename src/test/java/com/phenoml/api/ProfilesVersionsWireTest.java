package com.phenoml.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phenoml.api.core.ObjectMappers;
import com.phenoml.api.resources.profiles.types.ProfileGetResponse;
import com.phenoml.api.resources.profiles.types.ProfileSummary;
import com.phenoml.api.resources.profiles.types.ProfileVersionListResponse;
import java.util.HashMap;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProfilesVersionsWireTest {
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
    public void testList() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"versions\":[{\"id\":\"custom-patient\",\"source\":\"custom\",\"resource_type\":\"Patient\",\"url\":\"http://phenoml.com/fhir/StructureDefinition/custom-patient\",\"version\":\"2.0.0\",\"status\":\"active\",\"date\":\"2026-08-26\",\"canonical\":\"http://phenoml.com/fhir/StructureDefinition/custom-patient|2.0.0\",\"fhir_version\":\"4.0.1\",\"implementation_guide\":\"acme-cardiology\",\"created_at\":\"2026-08-26T15:04:05Z\",\"updated_at\":\"2026-08-26T15:04:05Z\"},{\"id\":\"custom-patient\",\"source\":\"custom\",\"resource_type\":\"Patient\",\"url\":\"http://phenoml.com/fhir/StructureDefinition/custom-patient\",\"version\":\"1.0.0\",\"status\":\"active\",\"date\":\"2026-08-24\",\"canonical\":\"http://phenoml.com/fhir/StructureDefinition/custom-patient|1.0.0\",\"fhir_version\":\"4.0.1\",\"implementation_guide\":\"acme-cardiology\",\"created_at\":\"2026-08-24T15:04:05Z\",\"updated_at\":\"2026-08-24T15:04:05Z\"}]}"));
        ProfileVersionListResponse response = client.profiles().versions().list("custom-patient");
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
                + "  \"versions\": [\n"
                + "    {\n"
                + "      \"id\": \"custom-patient\",\n"
                + "      \"source\": \"custom\",\n"
                + "      \"resource_type\": \"Patient\",\n"
                + "      \"url\": \"http://phenoml.com/fhir/StructureDefinition/custom-patient\",\n"
                + "      \"version\": \"2.0.0\",\n"
                + "      \"status\": \"active\",\n"
                + "      \"date\": \"2026-08-26\",\n"
                + "      \"canonical\": \"http://phenoml.com/fhir/StructureDefinition/custom-patient|2.0.0\",\n"
                + "      \"fhir_version\": \"4.0.1\",\n"
                + "      \"implementation_guide\": \"acme-cardiology\",\n"
                + "      \"created_at\": \"2026-08-26T15:04:05Z\",\n"
                + "      \"updated_at\": \"2026-08-26T15:04:05Z\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"id\": \"custom-patient\",\n"
                + "      \"source\": \"custom\",\n"
                + "      \"resource_type\": \"Patient\",\n"
                + "      \"url\": \"http://phenoml.com/fhir/StructureDefinition/custom-patient\",\n"
                + "      \"version\": \"1.0.0\",\n"
                + "      \"status\": \"active\",\n"
                + "      \"date\": \"2026-08-24\",\n"
                + "      \"canonical\": \"http://phenoml.com/fhir/StructureDefinition/custom-patient|1.0.0\",\n"
                + "      \"fhir_version\": \"4.0.1\",\n"
                + "      \"implementation_guide\": \"acme-cardiology\",\n"
                + "      \"created_at\": \"2026-08-24T15:04:05Z\",\n"
                + "      \"updated_at\": \"2026-08-24T15:04:05Z\"\n"
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
    public void testCreate() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"id\":\"custom-patient\",\"source\":\"custom\",\"resource_type\":\"Patient\",\"url\":\"http://phenoml.com/fhir/StructureDefinition/custom-patient\",\"version\":\"1.0.0\",\"status\":\"active\",\"date\":\"2026-08-24\",\"canonical\":\"http://phenoml.com/fhir/StructureDefinition/custom-patient|1.0.0\",\"fhir_version\":\"4.0.1\",\"implementation_guide\":\"acme-cardiology\",\"created_at\":\"2026-08-24T15:04:05Z\",\"updated_at\":\"2026-08-25T16:04:05Z\"}"));
        ProfileSummary response = client.profiles().versions().create("custom-patient", new HashMap<String, Object>() {
            {
                put("key", "value");
            }
        });
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
        String expectedRequestBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
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
                + "  \"id\": \"custom-patient\",\n"
                + "  \"source\": \"custom\",\n"
                + "  \"resource_type\": \"Patient\",\n"
                + "  \"url\": \"http://phenoml.com/fhir/StructureDefinition/custom-patient\",\n"
                + "  \"version\": \"1.0.0\",\n"
                + "  \"status\": \"active\",\n"
                + "  \"date\": \"2026-08-24\",\n"
                + "  \"canonical\": \"http://phenoml.com/fhir/StructureDefinition/custom-patient|1.0.0\",\n"
                + "  \"fhir_version\": \"4.0.1\",\n"
                + "  \"implementation_guide\": \"acme-cardiology\",\n"
                + "  \"created_at\": \"2026-08-24T15:04:05Z\",\n"
                + "  \"updated_at\": \"2026-08-25T16:04:05Z\"\n"
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
                                "{\"id\":\"custom-patient\",\"source\":\"custom\",\"resource_type\":\"Patient\",\"url\":\"http://phenoml.com/fhir/StructureDefinition/custom-patient\",\"version\":\"1.0.0\",\"status\":\"active\",\"date\":\"2026-08-24\",\"canonical\":\"http://phenoml.com/fhir/StructureDefinition/custom-patient|1.0.0\",\"fhir_version\":\"4.0.1\",\"implementation_guide\":\"acme-cardiology\",\"created_at\":\"2026-08-24T15:04:05Z\",\"updated_at\":\"2026-08-25T16:04:05Z\",\"structure_definition\":{\"resourceType\":\"StructureDefinition\",\"id\":\"custom-patient\",\"url\":\"http://phenoml.com/fhir/StructureDefinition/custom-patient\",\"version\":\"1.0.0\",\"name\":\"CustomPatient\",\"status\":\"active\",\"fhirVersion\":\"4.0.1\",\"kind\":\"resource\",\"abstract\":false,\"type\":\"Patient\",\"baseDefinition\":\"http://hl7.org/fhir/StructureDefinition/Patient\",\"derivation\":\"constraint\",\"snapshot\":{\"element\":[{\"id\":\"Patient\",\"path\":\"Patient\",\"min\":0,\"max\":\"*\"}]}}}"));
        ProfileGetResponse response = client.profiles().versions().get("custom-patient", "2.0.0");
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
                + "  \"id\": \"custom-patient\",\n"
                + "  \"source\": \"custom\",\n"
                + "  \"resource_type\": \"Patient\",\n"
                + "  \"url\": \"http://phenoml.com/fhir/StructureDefinition/custom-patient\",\n"
                + "  \"version\": \"1.0.0\",\n"
                + "  \"status\": \"active\",\n"
                + "  \"date\": \"2026-08-24\",\n"
                + "  \"canonical\": \"http://phenoml.com/fhir/StructureDefinition/custom-patient|1.0.0\",\n"
                + "  \"fhir_version\": \"4.0.1\",\n"
                + "  \"implementation_guide\": \"acme-cardiology\",\n"
                + "  \"created_at\": \"2026-08-24T15:04:05Z\",\n"
                + "  \"updated_at\": \"2026-08-25T16:04:05Z\",\n"
                + "  \"structure_definition\": {\n"
                + "    \"resourceType\": \"StructureDefinition\",\n"
                + "    \"id\": \"custom-patient\",\n"
                + "    \"url\": \"http://phenoml.com/fhir/StructureDefinition/custom-patient\",\n"
                + "    \"version\": \"1.0.0\",\n"
                + "    \"name\": \"CustomPatient\",\n"
                + "    \"status\": \"active\",\n"
                + "    \"fhirVersion\": \"4.0.1\",\n"
                + "    \"kind\": \"resource\",\n"
                + "    \"abstract\": false,\n"
                + "    \"type\": \"Patient\",\n"
                + "    \"baseDefinition\": \"http://hl7.org/fhir/StructureDefinition/Patient\",\n"
                + "    \"derivation\": \"constraint\",\n"
                + "    \"snapshot\": {\n"
                + "      \"element\": [\n"
                + "        {\n"
                + "          \"id\": \"Patient\",\n"
                + "          \"path\": \"Patient\",\n"
                + "          \"min\": 0,\n"
                + "          \"max\": \"*\"\n"
                + "        }\n"
                + "      ]\n"
                + "    }\n"
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
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        client.profiles().versions().delete("custom-patient", "2.0.0");
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
