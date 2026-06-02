package com.phenoml.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phenoml.api.core.ObjectMappers;
import com.phenoml.api.resources.construe.codes.requests.CodesListRequest;
import com.phenoml.api.resources.construe.codes.requests.ExtractRequest;
import com.phenoml.api.resources.construe.codes.requests.LookupRequest;
import com.phenoml.api.resources.construe.codes.requests.SearchSemanticRequest;
import com.phenoml.api.resources.construe.codes.requests.SearchTextRequest;
import com.phenoml.api.resources.construe.types.ExtractCodesResult;
import com.phenoml.api.resources.construe.types.ExtractRequestSystem;
import com.phenoml.api.resources.construe.types.GetCodeResponse;
import com.phenoml.api.resources.construe.types.ListCodesResponse;
import com.phenoml.api.resources.construe.types.SemanticSearchResponse;
import com.phenoml.api.resources.construe.types.TextSearchResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConstrueCodesWireTest {
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
    public void testExtract() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(TestResources.loadResource("/wire-tests/ConstrueCodesWireTest_testExtract_response.json")));
        ExtractCodesResult response = client.construe()
                .codes()
                .extract(ExtractRequest.builder()
                        .text(
                                "Patient is a 14-year-old female, previously healthy, who is here for evaluation of abnormal renal ultrasound with atrophic right kidney.")
                        .system(ExtractRequestSystem.builder()
                                .name("ICD-10-CM")
                                .version("2025")
                                .build())
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
                + "  \"text\": \"Patient is a 14-year-old female, previously healthy, who is here for evaluation of abnormal renal ultrasound with atrophic right kidney.\",\n"
                + "  \"system\": {\n"
                + "    \"name\": \"ICD-10-CM\",\n"
                + "    \"version\": \"2025\"\n"
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
        String expectedResponseBody =
                TestResources.loadResource("/wire-tests/ConstrueCodesWireTest_testExtract_response.json");
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
                                "{\"system\":{\"name\":\"ICD-10-CM\",\"version\":\"2025\"},\"codes\":[{\"code\":\"A00\",\"description\":\"Cholera\",\"definition\":\"definition\"},{\"code\":\"A000\",\"description\":\"Cholera due to Vibrio cholerae 01, biovar cholerae\",\"definition\":\"definition\"},{\"code\":\"A001\",\"description\":\"Cholera due to Vibrio cholerae 01, biovar eltor\",\"definition\":\"definition\"},{\"code\":\"A009\",\"description\":\"Cholera, unspecified\",\"definition\":\"definition\"},{\"code\":\"A01\",\"description\":\"Typhoid and paratyphoid fevers\",\"definition\":\"definition\"}],\"next_cursor\":\"QTAx\",\"has_more\":true}"));
        ListCodesResponse response = client.construe()
                .codes()
                .list(
                        "ICD-10-CM",
                        CodesListRequest.builder()
                                .version("2025")
                                .cursor("cursor")
                                .limit(1)
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
                + "  \"system\": {\n"
                + "    \"name\": \"ICD-10-CM\",\n"
                + "    \"version\": \"2025\"\n"
                + "  },\n"
                + "  \"codes\": [\n"
                + "    {\n"
                + "      \"code\": \"A00\",\n"
                + "      \"description\": \"Cholera\",\n"
                + "      \"definition\": \"definition\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"code\": \"A000\",\n"
                + "      \"description\": \"Cholera due to Vibrio cholerae 01, biovar cholerae\",\n"
                + "      \"definition\": \"definition\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"code\": \"A001\",\n"
                + "      \"description\": \"Cholera due to Vibrio cholerae 01, biovar eltor\",\n"
                + "      \"definition\": \"definition\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"code\": \"A009\",\n"
                + "      \"description\": \"Cholera, unspecified\",\n"
                + "      \"definition\": \"definition\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"code\": \"A01\",\n"
                + "      \"description\": \"Typhoid and paratyphoid fevers\",\n"
                + "      \"definition\": \"definition\"\n"
                + "    }\n"
                + "  ],\n"
                + "  \"next_cursor\": \"QTAx\",\n"
                + "  \"has_more\": true\n"
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
    public void testLookup() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"system\":{\"name\":\"ICD-10-CM\",\"version\":\"2025\"},\"code\":\"E1165\",\"description\":\"Type 2 diabetes mellitus with hyperglycemia\",\"definition\":\"definition\"}"));
        GetCodeResponse response = client.construe()
                .codes()
                .lookup(
                        "ICD-10-CM",
                        "E1165",
                        LookupRequest.builder().version("version").build());
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
                + "  \"system\": {\n"
                + "    \"name\": \"ICD-10-CM\",\n"
                + "    \"version\": \"2025\"\n"
                + "  },\n"
                + "  \"code\": \"E1165\",\n"
                + "  \"description\": \"Type 2 diabetes mellitus with hyperglycemia\",\n"
                + "  \"definition\": \"definition\"\n"
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
    public void testSearchSemantic() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"system\":{\"name\":\"ICD-10-CM\",\"version\":\"2025\"},\"results\":[{\"code\":\"R06.00\",\"description\":\"Dyspnea, unspecified\"},{\"code\":\"R06.01\",\"description\":\"Orthopnea\"},{\"code\":\"G47.33\",\"description\":\"Obstructive sleep apnea\"},{\"code\":\"R06.83\",\"description\":\"Snoring\"},{\"code\":\"J45.20\",\"description\":\"Mild intermittent asthma, uncomplicated\"}]}"));
        SemanticSearchResponse response = client.construe()
                .codes()
                .searchSemantic(
                        "ICD-10-CM",
                        SearchSemanticRequest.builder()
                                .text("patient has trouble breathing at night and wakes up gasping")
                                .version("version")
                                .limit(1)
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
                + "  \"system\": {\n"
                + "    \"name\": \"ICD-10-CM\",\n"
                + "    \"version\": \"2025\"\n"
                + "  },\n"
                + "  \"results\": [\n"
                + "    {\n"
                + "      \"code\": \"R06.00\",\n"
                + "      \"description\": \"Dyspnea, unspecified\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"code\": \"R06.01\",\n"
                + "      \"description\": \"Orthopnea\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"code\": \"G47.33\",\n"
                + "      \"description\": \"Obstructive sleep apnea\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"code\": \"R06.83\",\n"
                + "      \"description\": \"Snoring\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"code\": \"J45.20\",\n"
                + "      \"description\": \"Mild intermittent asthma, uncomplicated\"\n"
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
    public void testSearchText() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"system\":{\"name\":\"ICD-10-CM\",\"version\":\"2025\"},\"results\":[{\"code\":\"E11.65\",\"description\":\"Type 2 diabetes mellitus with hyperglycemia\"},{\"code\":\"E11.649\",\"description\":\"Type 2 diabetes mellitus with hypoglycemia without coma\"},{\"code\":\"E11.69\",\"description\":\"Type 2 diabetes mellitus with other specified complication\"}],\"found\":3}"));
        TextSearchResponse response = client.construe()
                .codes()
                .searchText(
                        "ICD-10-CM",
                        SearchTextRequest.builder()
                                .q("E11.65")
                                .version("version")
                                .limit(1)
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
                + "  \"system\": {\n"
                + "    \"name\": \"ICD-10-CM\",\n"
                + "    \"version\": \"2025\"\n"
                + "  },\n"
                + "  \"results\": [\n"
                + "    {\n"
                + "      \"code\": \"E11.65\",\n"
                + "      \"description\": \"Type 2 diabetes mellitus with hyperglycemia\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"code\": \"E11.649\",\n"
                + "      \"description\": \"Type 2 diabetes mellitus with hypoglycemia without coma\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"code\": \"E11.69\",\n"
                + "      \"description\": \"Type 2 diabetes mellitus with other specified complication\"\n"
                + "    }\n"
                + "  ],\n"
                + "  \"found\": 3\n"
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
