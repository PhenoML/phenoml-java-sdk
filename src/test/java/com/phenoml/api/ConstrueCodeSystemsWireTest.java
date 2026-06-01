package com.phenoml.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phenoml.api.core.ObjectMappers;
import com.phenoml.api.resources.construe.codesystems.requests.DeleteRequest;
import com.phenoml.api.resources.construe.codesystems.requests.ExportRequest;
import com.phenoml.api.resources.construe.codesystems.requests.FindRequest;
import com.phenoml.api.resources.construe.codesystems.requests.UploadRequest;
import com.phenoml.api.resources.construe.codesystems.types.UploadRequestFormat;
import com.phenoml.api.resources.construe.codesystems.types.UploadResponse;
import com.phenoml.api.resources.construe.types.CodeResponse;
import com.phenoml.api.resources.construe.types.DeleteCodeSystemResponse;
import com.phenoml.api.resources.construe.types.ExportCodeSystemResponse;
import com.phenoml.api.resources.construe.types.GetCodeSystemDetailResponse;
import com.phenoml.api.resources.construe.types.ListCodeSystemsResponse;
import java.util.Arrays;
import java.util.Optional;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConstrueCodeSystemsWireTest {
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
    public void testUpload() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"status\":\"processing\",\"name\":\"CUSTOM_CODES\",\"version\":\"1.0\"}"));
        UploadResponse response = client.construe()
                .codeSystems()
                .upload(UploadRequest.builder()
                        .name("CUSTOM_CODES")
                        .version("1.0")
                        .format(UploadRequestFormat.JSON)
                        .codes(Optional.of(Arrays.asList(
                                CodeResponse.builder()
                                        .code("X001")
                                        .description("Example custom code 1")
                                        .build(),
                                CodeResponse.builder()
                                        .code("X002")
                                        .description("Example custom code 2")
                                        .build())))
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
                + "  \"name\": \"CUSTOM_CODES\",\n"
                + "  \"version\": \"1.0\",\n"
                + "  \"format\": \"json\",\n"
                + "  \"codes\": [\n"
                + "    {\n"
                + "      \"code\": \"X001\",\n"
                + "      \"description\": \"Example custom code 1\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"code\": \"X002\",\n"
                + "      \"description\": \"Example custom code 2\"\n"
                + "    }\n"
                + "  ]\n"
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
                + "  \"status\": \"processing\",\n"
                + "  \"name\": \"CUSTOM_CODES\",\n"
                + "  \"version\": \"1.0\"\n"
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
                                "{\"systems\":[{\"name\":\"ICD-10-CM\",\"version\":\"2025\",\"code_count\":97584,\"builtin\":true},{\"name\":\"SNOMED_CT_US_LITE\",\"version\":\"20240901\",\"code_count\":102837,\"builtin\":true},{\"name\":\"RXNORM\",\"version\":\"11042024\",\"code_count\":257619,\"builtin\":true},{\"name\":\"LOINC\",\"version\":\"2.78\",\"code_count\":98123,\"builtin\":true},{\"name\":\"HPO\",\"version\":\"2025\",\"code_count\":19542,\"builtin\":true},{\"name\":\"CPT\",\"version\":\"2025\",\"code_count\":10192,\"builtin\":true},{\"name\":\"ICD-10-PCS\",\"version\":\"2025\",\"code_count\":78717,\"builtin\":true}]}"));
        ListCodeSystemsResponse response = client.construe().codeSystems().list();
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
                + "  \"systems\": [\n"
                + "    {\n"
                + "      \"name\": \"ICD-10-CM\",\n"
                + "      \"version\": \"2025\",\n"
                + "      \"code_count\": 97584,\n"
                + "      \"builtin\": true\n"
                + "    },\n"
                + "    {\n"
                + "      \"name\": \"SNOMED_CT_US_LITE\",\n"
                + "      \"version\": \"20240901\",\n"
                + "      \"code_count\": 102837,\n"
                + "      \"builtin\": true\n"
                + "    },\n"
                + "    {\n"
                + "      \"name\": \"RXNORM\",\n"
                + "      \"version\": \"11042024\",\n"
                + "      \"code_count\": 257619,\n"
                + "      \"builtin\": true\n"
                + "    },\n"
                + "    {\n"
                + "      \"name\": \"LOINC\",\n"
                + "      \"version\": \"2.78\",\n"
                + "      \"code_count\": 98123,\n"
                + "      \"builtin\": true\n"
                + "    },\n"
                + "    {\n"
                + "      \"name\": \"HPO\",\n"
                + "      \"version\": \"2025\",\n"
                + "      \"code_count\": 19542,\n"
                + "      \"builtin\": true\n"
                + "    },\n"
                + "    {\n"
                + "      \"name\": \"CPT\",\n"
                + "      \"version\": \"2025\",\n"
                + "      \"code_count\": 10192,\n"
                + "      \"builtin\": true\n"
                + "    },\n"
                + "    {\n"
                + "      \"name\": \"ICD-10-PCS\",\n"
                + "      \"version\": \"2025\",\n"
                + "      \"code_count\": 78717,\n"
                + "      \"builtin\": true\n"
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
    public void testFind() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"name\":\"ICD-10-CM\",\"version\":\"2025\",\"code_count\":97584,\"builtin\":true,\"status\":\"ready\",\"created_at\":\"2026-02-10T18:33:23Z\",\"updated_at\":\"2026-02-10T18:33:23Z\"}"));
        GetCodeSystemDetailResponse response = client.construe()
                .codeSystems()
                .find("ICD-10-CM", FindRequest.builder().version("2025").build());
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
                + "  \"name\": \"ICD-10-CM\",\n"
                + "  \"version\": \"2025\",\n"
                + "  \"code_count\": 97584,\n"
                + "  \"builtin\": true,\n"
                + "  \"status\": \"ready\",\n"
                + "  \"created_at\": \"2026-02-10T18:33:23Z\",\n"
                + "  \"updated_at\": \"2026-02-10T18:33:23Z\"\n"
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
                new MockResponse().setResponseCode(200).setBody("{\"message\":\"code system deleted successfully\"}"));
        DeleteCodeSystemResponse response = client.construe()
                .codeSystems()
                .delete(
                        "CUSTOM_CODES",
                        DeleteRequest.builder().version("version").build());
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
        String expectedResponseBody = "" + "{\n" + "  \"message\": \"code system deleted successfully\"\n" + "}";
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
    public void testExport() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"name\":\"CUSTOM_CODES\",\"version\":\"1.0\",\"format\":\"json\",\"codes\":[{\"code\":\"X001\",\"description\":\"Example custom code 1\",\"definition\":\"definition\"},{\"code\":\"X002\",\"description\":\"Example custom code 2\",\"definition\":\"definition\"}]}"));
        ExportCodeSystemResponse response = client.construe()
                .codeSystems()
                .export(
                        "CUSTOM_CODES",
                        ExportRequest.builder().version("version").build());
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
                + "  \"name\": \"CUSTOM_CODES\",\n"
                + "  \"version\": \"1.0\",\n"
                + "  \"format\": \"json\",\n"
                + "  \"codes\": [\n"
                + "    {\n"
                + "      \"code\": \"X001\",\n"
                + "      \"description\": \"Example custom code 1\",\n"
                + "      \"definition\": \"definition\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"code\": \"X002\",\n"
                + "      \"description\": \"Example custom code 2\",\n"
                + "      \"definition\": \"definition\"\n"
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
