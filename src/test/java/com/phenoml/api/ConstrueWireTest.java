package com.phenoml.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phenoml.api.core.ObjectMappers;
import com.phenoml.api.resources.construe.requests.DeleteConstrueCodesSystemsCodesystemRequest;
import com.phenoml.api.resources.construe.requests.ExtractRequest;
import com.phenoml.api.resources.construe.requests.FeedbackRequest;
import com.phenoml.api.resources.construe.requests.GetConstrueCodesCodesystemCodeIdRequest;
import com.phenoml.api.resources.construe.requests.GetConstrueCodesCodesystemRequest;
import com.phenoml.api.resources.construe.requests.GetConstrueCodesCodesystemSearchSemanticRequest;
import com.phenoml.api.resources.construe.requests.GetConstrueCodesCodesystemSearchTextRequest;
import com.phenoml.api.resources.construe.requests.GetConstrueCodesSystemsCodesystemExportRequest;
import com.phenoml.api.resources.construe.requests.GetConstrueCodesSystemsCodesystemRequest;
import com.phenoml.api.resources.construe.requests.UploadRequest;
import com.phenoml.api.resources.construe.types.CodeResponse;
import com.phenoml.api.resources.construe.types.ConstrueUploadCodeSystemResponse;
import com.phenoml.api.resources.construe.types.DeleteCodeSystemResponse;
import com.phenoml.api.resources.construe.types.ExportCodeSystemResponse;
import com.phenoml.api.resources.construe.types.ExtractCodesResult;
import com.phenoml.api.resources.construe.types.ExtractRequestSystem;
import com.phenoml.api.resources.construe.types.ExtractedCodeResult;
import com.phenoml.api.resources.construe.types.FeedbackResponse;
import com.phenoml.api.resources.construe.types.GetCodeResponse;
import com.phenoml.api.resources.construe.types.GetCodeSystemDetailResponse;
import com.phenoml.api.resources.construe.types.ListCodeSystemsResponse;
import com.phenoml.api.resources.construe.types.ListCodesResponse;
import com.phenoml.api.resources.construe.types.SemanticSearchResponse;
import com.phenoml.api.resources.construe.types.TextSearchResponse;
import com.phenoml.api.resources.construe.types.UploadRequestFormat;
import java.util.Arrays;
import java.util.Optional;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConstrueWireTest {
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
    public void testUploadCodeSystem() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"status\":\"processing\",\"name\":\"CUSTOM_CODES\",\"version\":\"1.0\"}"));
        ConstrueUploadCodeSystemResponse response = client.construe()
                .uploadCodeSystem(UploadRequest.builder()
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
    public void testExtractCodes() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(TestResources.loadResource("/wire-tests/ConstrueWireTest_testExtractCodes_response.json")));
        ExtractCodesResult response = client.construe()
                .extractCodes(ExtractRequest.builder()
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
                TestResources.loadResource("/wire-tests/ConstrueWireTest_testExtractCodes_response.json");
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
    public void testListAvailableCodeSystems() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"systems\":[{\"name\":\"ICD-10-CM\",\"version\":\"2025\",\"code_count\":97584,\"builtin\":true},{\"name\":\"SNOMED_CT_US_LITE\",\"version\":\"20240901\",\"code_count\":102837,\"builtin\":true},{\"name\":\"RXNORM\",\"version\":\"11042024\",\"code_count\":257619,\"builtin\":true},{\"name\":\"LOINC\",\"version\":\"2.78\",\"code_count\":98123,\"builtin\":true},{\"name\":\"HPO\",\"version\":\"2025\",\"code_count\":19542,\"builtin\":true},{\"name\":\"CPT\",\"version\":\"2025\",\"code_count\":10192,\"builtin\":true},{\"name\":\"ICD-10-PCS\",\"version\":\"2025\",\"code_count\":78717,\"builtin\":true}]}"));
        ListCodeSystemsResponse response = client.construe().listAvailableCodeSystems();
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
    public void testGetCodeSystemDetail() throws Exception {
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
                .getCodeSystemDetail(
                        "ICD-10-CM",
                        GetConstrueCodesSystemsCodesystemRequest.builder()
                                .version("2025")
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
    public void testDeleteCustomCodeSystem() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(
                new MockResponse().setResponseCode(200).setBody("{\"message\":\"code system deleted successfully\"}"));
        DeleteCodeSystemResponse response = client.construe()
                .deleteCustomCodeSystem(
                        "CUSTOM_CODES",
                        DeleteConstrueCodesSystemsCodesystemRequest.builder()
                                .version("version")
                                .build());
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
    public void testExportCustomCodeSystem() throws Exception {
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
                .exportCustomCodeSystem(
                        "CUSTOM_CODES",
                        GetConstrueCodesSystemsCodesystemExportRequest.builder()
                                .version("version")
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

    @Test
    public void testListCodesInACodeSystem() throws Exception {
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
                .listCodesInACodeSystem(
                        "ICD-10-CM",
                        GetConstrueCodesCodesystemRequest.builder()
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
    public void testGetASpecificCode() throws Exception {
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
                .getASpecificCode(
                        "ICD-10-CM",
                        "E1165",
                        GetConstrueCodesCodesystemCodeIdRequest.builder()
                                .version("version")
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
    public void testSemanticSearchEmbeddingBased() throws Exception {
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
                .semanticSearchEmbeddingBased(
                        "ICD-10-CM",
                        GetConstrueCodesCodesystemSearchSemanticRequest.builder()
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
    public void testSubmitFeedbackOnExtractionResults() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"id\":\"abc123def456\"}"));
        FeedbackResponse response = client.construe()
                .submitFeedbackOnExtractionResults(FeedbackRequest.builder()
                        .text("Patient has type 2 diabetes with hyperglycemia")
                        .receivedResult(ExtractCodesResult.builder()
                                .system(ExtractRequestSystem.builder()
                                        .name("ICD-10-CM")
                                        .version("2025")
                                        .build())
                                .codes(Arrays.asList(ExtractedCodeResult.builder()
                                        .code("E11.9")
                                        .description("Type 2 diabetes mellitus without complications")
                                        .valid(true)
                                        .build()))
                                .build())
                        .expectedResult(ExtractCodesResult.builder()
                                .system(ExtractRequestSystem.builder()
                                        .name("ICD-10-CM")
                                        .version("2025")
                                        .build())
                                .codes(Arrays.asList(ExtractedCodeResult.builder()
                                        .code("E11.65")
                                        .description("Type 2 diabetes mellitus with hyperglycemia")
                                        .valid(true)
                                        .build()))
                                .build())
                        .detail("Expected code E11.65 because the text mentions hyperglycemia")
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
                + "  \"text\": \"Patient has type 2 diabetes with hyperglycemia\",\n"
                + "  \"received_result\": {\n"
                + "    \"system\": {\n"
                + "      \"name\": \"ICD-10-CM\",\n"
                + "      \"version\": \"2025\"\n"
                + "    },\n"
                + "    \"codes\": [\n"
                + "      {\n"
                + "        \"code\": \"E11.9\",\n"
                + "        \"description\": \"Type 2 diabetes mellitus without complications\",\n"
                + "        \"valid\": true\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  \"expected_result\": {\n"
                + "    \"system\": {\n"
                + "      \"name\": \"ICD-10-CM\",\n"
                + "      \"version\": \"2025\"\n"
                + "    },\n"
                + "    \"codes\": [\n"
                + "      {\n"
                + "        \"code\": \"E11.65\",\n"
                + "        \"description\": \"Type 2 diabetes mellitus with hyperglycemia\",\n"
                + "        \"valid\": true\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  \"detail\": \"Expected code E11.65 because the text mentions hyperglycemia\"\n"
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
        String expectedResponseBody = "" + "{\n" + "  \"id\": \"abc123def456\"\n" + "}";
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
    public void testTerminologyServerTextSearch() throws Exception {
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
                .terminologyServerTextSearch(
                        "ICD-10-CM",
                        GetConstrueCodesCodesystemSearchTextRequest.builder()
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
