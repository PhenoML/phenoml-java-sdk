package com.phenoml.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phenoml.api.core.ObjectMappers;
import com.phenoml.api.resources.fhir.requests.FhirCreateRequest;
import com.phenoml.api.resources.fhir.requests.FhirDeleteRequest;
import com.phenoml.api.resources.fhir.requests.FhirExecuteBundleRequest;
import com.phenoml.api.resources.fhir.requests.FhirPatchRequest;
import com.phenoml.api.resources.fhir.requests.FhirSearchRequest;
import com.phenoml.api.resources.fhir.requests.FhirUpsertRequest;
import com.phenoml.api.resources.fhir.types.FhirBundle;
import com.phenoml.api.resources.fhir.types.FhirBundleEntryItem;
import com.phenoml.api.resources.fhir.types.FhirBundleEntryItemRequest;
import com.phenoml.api.resources.fhir.types.FhirBundleEntryItemRequestMethod;
import com.phenoml.api.resources.fhir.types.FhirPatchRequestBodyItem;
import com.phenoml.api.resources.fhir.types.FhirPatchRequestBodyItemOp;
import com.phenoml.api.resources.fhir.types.FhirResource;
import com.phenoml.api.resources.fhir.types.FhirSearchResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FhirWireTest {
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
    public void testSearch() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"resourceType\":\"Bundle\",\"total\":2,\"entry\":[{\"resource\":{\"resourceType\":\"Patient\",\"id\":\"123\",\"name\":[{\"family\":\"Doe\",\"given\":[\"John\"]}]}}]}"));
        FhirSearchResponse response = client.fhir()
                .search(
                        "550e8400-e29b-41d4-a716-446655440000",
                        "Patient",
                        FhirSearchRequest.builder()
                                .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
                                .phenomlFhirProvider(
                                        "550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
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

        // Validate headers
        Assertions.assertEquals(
                "Patient/550e8400-e29b-41d4-a716-446655440000",
                request.getHeader("X-Phenoml-On-Behalf-Of"),
                "Header 'X-Phenoml-On-Behalf-Of' should match expected value");
        Assertions.assertEquals(
                "550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...",
                request.getHeader("X-Phenoml-Fhir-Provider"),
                "Header 'X-Phenoml-Fhir-Provider' should match expected value");

        // Validate response body
        Assertions.assertNotNull(response, "Response should not be null");
        String actualResponseJson = objectMapper.writeValueAsString(response);
        String expectedResponseBody = ""
                + "{\n"
                + "  \"resourceType\": \"Bundle\",\n"
                + "  \"total\": 2,\n"
                + "  \"entry\": [\n"
                + "    {\n"
                + "      \"resource\": {\n"
                + "        \"resourceType\": \"Patient\",\n"
                + "        \"id\": \"123\",\n"
                + "        \"name\": [\n"
                + "          {\n"
                + "            \"family\": \"Doe\",\n"
                + "            \"given\": [\n"
                + "              \"John\"\n"
                + "            ]\n"
                + "          }\n"
                + "        ]\n"
                + "      }\n"
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
                                "{\"resourceType\":\"Patient\",\"id\":\"123\",\"meta\":{\"versionId\":\"versionId\",\"lastUpdated\":\"2024-01-15T09:30:00Z\",\"profile\":[\"profile\"]}}"));
        FhirResource response = client.fhir()
                .create(
                        "550e8400-e29b-41d4-a716-446655440000",
                        "Patient",
                        FhirCreateRequest.builder()
                                .body(FhirResource.builder()
                                        .resourceType("Patient")
                                        .build())
                                .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
                                .phenomlFhirProvider(
                                        "550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
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
        String expectedRequestBody = "" + "{\n" + "  \"resourceType\": \"Patient\"\n" + "}";
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
                + "  \"resourceType\": \"Patient\",\n"
                + "  \"id\": \"123\",\n"
                + "  \"meta\": {\n"
                + "    \"versionId\": \"versionId\",\n"
                + "    \"lastUpdated\": \"2024-01-15T09:30:00Z\",\n"
                + "    \"profile\": [\n"
                + "      \"profile\"\n"
                + "    ]\n"
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
    public void testUpsert() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"resourceType\":\"Patient\",\"id\":\"123\",\"meta\":{\"versionId\":\"versionId\",\"lastUpdated\":\"2024-01-15T09:30:00Z\",\"profile\":[\"profile\"]}}"));
        FhirResource response = client.fhir()
                .upsert(
                        "550e8400-e29b-41d4-a716-446655440000",
                        "Patient",
                        FhirUpsertRequest.builder()
                                .body(FhirResource.builder()
                                        .resourceType("Patient")
                                        .id("123")
                                        .build())
                                .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
                                .phenomlFhirProvider(
                                        "550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
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
        String expectedRequestBody = "" + "{\n" + "  \"resourceType\": \"Patient\",\n" + "  \"id\": \"123\"\n" + "}";
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
                + "  \"resourceType\": \"Patient\",\n"
                + "  \"id\": \"123\",\n"
                + "  \"meta\": {\n"
                + "    \"versionId\": \"versionId\",\n"
                + "    \"lastUpdated\": \"2024-01-15T09:30:00Z\",\n"
                + "    \"profile\": [\n"
                + "      \"profile\"\n"
                + "    ]\n"
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
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"key\":\"value\"}"));
        Map<String, Object> response = client.fhir()
                .delete(
                        "550e8400-e29b-41d4-a716-446655440000",
                        "Patient",
                        FhirDeleteRequest.builder()
                                .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
                                .phenomlFhirProvider(
                                        "550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
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

        // Validate headers
        Assertions.assertEquals(
                "Patient/550e8400-e29b-41d4-a716-446655440000",
                request.getHeader("X-Phenoml-On-Behalf-Of"),
                "Header 'X-Phenoml-On-Behalf-Of' should match expected value");
        Assertions.assertEquals(
                "550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...",
                request.getHeader("X-Phenoml-Fhir-Provider"),
                "Header 'X-Phenoml-Fhir-Provider' should match expected value");

        // Validate response body
        Assertions.assertNotNull(response, "Response should not be null");
        String actualResponseJson = objectMapper.writeValueAsString(response);
        String expectedResponseBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
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
                                "{\"resourceType\":\"Patient\",\"id\":\"123\",\"meta\":{\"versionId\":\"versionId\",\"lastUpdated\":\"2024-01-15T09:30:00Z\",\"profile\":[\"profile\"]}}"));
        FhirResource response = client.fhir()
                .patch(
                        "550e8400-e29b-41d4-a716-446655440000",
                        "Patient",
                        FhirPatchRequest.builder()
                                .body(Arrays.asList(FhirPatchRequestBodyItem.builder()
                                        .op(FhirPatchRequestBodyItemOp.REPLACE)
                                        .path("/name/0/family")
                                        .value("NewFamilyName")
                                        .build()))
                                .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
                                .phenomlFhirProvider(
                                        "550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
                                .build());
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
                + "[\n"
                + "  {\n"
                + "    \"op\": \"replace\",\n"
                + "    \"path\": \"/name/0/family\",\n"
                + "    \"value\": \"NewFamilyName\"\n"
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
                + "  \"resourceType\": \"Patient\",\n"
                + "  \"id\": \"123\",\n"
                + "  \"meta\": {\n"
                + "    \"versionId\": \"versionId\",\n"
                + "    \"lastUpdated\": \"2024-01-15T09:30:00Z\",\n"
                + "    \"profile\": [\n"
                + "      \"profile\"\n"
                + "    ]\n"
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
    public void testExecuteBundle() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"resourceType\":\"Bundle\",\"total\":1,\"entry\":[{\"resource\":{\"resourceType\":\"Patient\",\"id\":\"456\"},\"response\":{\"status\":\"201 Created\",\"location\":\"Patient/456\"}}]}"));
        FhirBundle response = client.fhir()
                .executeBundle(
                        "550e8400-e29b-41d4-a716-446655440000",
                        FhirExecuteBundleRequest.builder()
                                .body(FhirBundle.builder()
                                        .entry(Arrays.asList(
                                                FhirBundleEntryItem.builder()
                                                        .resource(new HashMap<String, Object>() {
                                                            {
                                                                put("resourceType", "Patient");
                                                                put(
                                                                        "name",
                                                                        new ArrayList<Object>(Arrays.asList(
                                                                                new HashMap<String, Object>() {
                                                                                    {
                                                                                        put("family", "Doe");
                                                                                        put(
                                                                                                "given",
                                                                                                new ArrayList<Object>(
                                                                                                        Arrays.asList(
                                                                                                                "John")));
                                                                                    }
                                                                                })));
                                                            }
                                                        })
                                                        .request(FhirBundleEntryItemRequest.builder()
                                                                .method(FhirBundleEntryItemRequestMethod.POST)
                                                                .url("Patient")
                                                                .build())
                                                        .build(),
                                                FhirBundleEntryItem.builder()
                                                        .resource(new HashMap<String, Object>() {
                                                            {
                                                                put("resourceType", "Observation");
                                                                put("status", "final");
                                                                put("subject", new HashMap<String, Object>() {
                                                                    {
                                                                        put("reference", "Patient/123");
                                                                    }
                                                                });
                                                            }
                                                        })
                                                        .request(FhirBundleEntryItemRequest.builder()
                                                                .method(FhirBundleEntryItemRequestMethod.POST)
                                                                .url("Observation")
                                                                .build())
                                                        .build()))
                                        .build())
                                .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
                                .phenomlFhirProvider(
                                        "550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
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
                + "  \"resourceType\": \"Bundle\",\n"
                + "  \"entry\": [\n"
                + "    {\n"
                + "      \"resource\": {\n"
                + "        \"resourceType\": \"Patient\",\n"
                + "        \"name\": [\n"
                + "          {\n"
                + "            \"family\": \"Doe\",\n"
                + "            \"given\": [\n"
                + "              \"John\"\n"
                + "            ]\n"
                + "          }\n"
                + "        ]\n"
                + "      },\n"
                + "      \"request\": {\n"
                + "        \"method\": \"POST\",\n"
                + "        \"url\": \"Patient\"\n"
                + "      }\n"
                + "    },\n"
                + "    {\n"
                + "      \"resource\": {\n"
                + "        \"resourceType\": \"Observation\",\n"
                + "        \"status\": \"final\",\n"
                + "        \"subject\": {\n"
                + "          \"reference\": \"Patient/123\"\n"
                + "        }\n"
                + "      },\n"
                + "      \"request\": {\n"
                + "        \"method\": \"POST\",\n"
                + "        \"url\": \"Observation\"\n"
                + "      }\n"
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
                + "  \"resourceType\": \"Bundle\",\n"
                + "  \"total\": 1,\n"
                + "  \"entry\": [\n"
                + "    {\n"
                + "      \"resource\": {\n"
                + "        \"resourceType\": \"Patient\",\n"
                + "        \"id\": \"456\"\n"
                + "      },\n"
                + "      \"response\": {\n"
                + "        \"status\": \"201 Created\",\n"
                + "        \"location\": \"Patient/456\"\n"
                + "      }\n"
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
                if (actualValue == null || !jsonEquals(entry.getValue(), actualValue)) return false;
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
