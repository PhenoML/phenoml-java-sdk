package com.phenoml.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phenoml.api.core.ObjectMappers;
import com.phenoml.api.resources.tools.requests.CohortRequest;
import com.phenoml.api.resources.tools.requests.Lang2FhirAndCreateMultiRequest;
import com.phenoml.api.resources.tools.requests.Lang2FhirAndCreateRequest;
import com.phenoml.api.resources.tools.requests.Lang2FhirAndSearchRequest;
import com.phenoml.api.resources.tools.types.CohortResponse;
import com.phenoml.api.resources.tools.types.Lang2FhirAndCreateMultiResponse;
import com.phenoml.api.resources.tools.types.Lang2FhirAndCreateRequestResource;
import com.phenoml.api.resources.tools.types.Lang2FhirAndCreateResponse;
import com.phenoml.api.resources.tools.types.Lang2FhirAndSearchResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ToolsWireTest {
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
    public void testCreateFhirResource() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"fhir_resource\":{\"resourceType\":\"Condition\",\"id\":\"condition-123\",\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"195967001\",\"display\":\"Asthma\"}]},\"subject\":{\"reference\":\"Patient/patient-123\",\"display\":\"John Doe\"}},\"fhir_id\":\"condition-123\",\"success\":true,\"message\":\"FHIR resource created and stored in FHIR server successfully\"}"));
        Lang2FhirAndCreateResponse response = client.tools()
                .createFhirResource(Lang2FhirAndCreateRequest.builder()
                        .resource(Lang2FhirAndCreateRequestResource.AUTO)
                        .text("Patient John Doe has severe asthma with acute exacerbation")
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
                + "  \"resource\": \"auto\",\n"
                + "  \"text\": \"Patient John Doe has severe asthma with acute exacerbation\"\n"
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
                + "  \"fhir_resource\": {\n"
                + "    \"resourceType\": \"Condition\",\n"
                + "    \"id\": \"condition-123\",\n"
                + "    \"code\": {\n"
                + "      \"coding\": [\n"
                + "        {\n"
                + "          \"system\": \"http://snomed.info/sct\",\n"
                + "          \"code\": \"195967001\",\n"
                + "          \"display\": \"Asthma\"\n"
                + "        }\n"
                + "      ]\n"
                + "    },\n"
                + "    \"subject\": {\n"
                + "      \"reference\": \"Patient/patient-123\",\n"
                + "      \"display\": \"John Doe\"\n"
                + "    }\n"
                + "  },\n"
                + "  \"fhir_id\": \"condition-123\",\n"
                + "  \"success\": true,\n"
                + "  \"message\": \"FHIR resource created and stored in FHIR server successfully\"\n"
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
    public void testCreateFhirResourcesMulti() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"success\":true,\"message\":\"Created 3 resources\",\"response_bundle\":{\"resourceType\":\"Bundle\",\"type\":\"transaction-response\",\"entry\":[{\"key\":\"value\"}]},\"resource_info\":[{\"tempId\":\"urn:uuid:patient-abc123\",\"resourceType\":\"Patient\",\"description\":\"John Smith, 45-year-old male\"}]}"));
        Lang2FhirAndCreateMultiResponse response = client.tools()
                .createFhirResourcesMulti(Lang2FhirAndCreateMultiRequest.builder()
                        .text(
                                "John Smith, 45-year-old male, diagnosed with Type 2 Diabetes. Prescribed Metformin 500mg twice daily.")
                        .provider("medplum")
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
                + "  \"text\": \"John Smith, 45-year-old male, diagnosed with Type 2 Diabetes. Prescribed Metformin 500mg twice daily.\",\n"
                + "  \"provider\": \"medplum\"\n"
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
                + "  \"message\": \"Created 3 resources\",\n"
                + "  \"response_bundle\": {\n"
                + "    \"resourceType\": \"Bundle\",\n"
                + "    \"type\": \"transaction-response\",\n"
                + "    \"entry\": [\n"
                + "      {\n"
                + "        \"key\": \"value\"\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  \"resource_info\": [\n"
                + "    {\n"
                + "      \"tempId\": \"urn:uuid:patient-abc123\",\n"
                + "      \"resourceType\": \"Patient\",\n"
                + "      \"description\": \"John Smith, 45-year-old male\"\n"
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
    public void testSearchFhirResources() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"resource_type\":\"Appointment\",\"search_params\":\"patient=Patient/patient-123&date=ge2024-01-15&date=le2024-01-22\",\"fhir_results\":[{\"resourceType\":\"Appointment\",\"id\":\"appointment-123\",\"status\":\"booked\",\"start\":\"2024-01-16T10:00:00Z\",\"end\":\"2024-01-16T11:00:00Z\",\"participant\":[{\"actor\":{\"reference\":\"Patient/patient-123\",\"display\":\"John Doe\"}}]}],\"success\":true,\"message\":\"Search query generated and executed in FHIR server successfully. Found 1 results.\"}"));
        Lang2FhirAndSearchResponse response = client.tools()
                .searchFhirResources(Lang2FhirAndSearchRequest.builder()
                        .text("Find all appointments for patient John Doe next week")
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
        String expectedRequestBody =
                "" + "{\n" + "  \"text\": \"Find all appointments for patient John Doe next week\"\n" + "}";
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
                + "  \"resource_type\": \"Appointment\",\n"
                + "  \"search_params\": \"patient=Patient/patient-123&date=ge2024-01-15&date=le2024-01-22\",\n"
                + "  \"fhir_results\": [\n"
                + "    {\n"
                + "      \"resourceType\": \"Appointment\",\n"
                + "      \"id\": \"appointment-123\",\n"
                + "      \"status\": \"booked\",\n"
                + "      \"start\": \"2024-01-16T10:00:00Z\",\n"
                + "      \"end\": \"2024-01-16T11:00:00Z\",\n"
                + "      \"participant\": [\n"
                + "        {\n"
                + "          \"actor\": {\n"
                + "            \"reference\": \"Patient/patient-123\",\n"
                + "            \"display\": \"John Doe\"\n"
                + "          }\n"
                + "        }\n"
                + "      ]\n"
                + "    }\n"
                + "  ],\n"
                + "  \"success\": true,\n"
                + "  \"message\": \"Search query generated and executed in FHIR server successfully. Found 1 results.\"\n"
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
    public void testAnalyzeCohort() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"success\":true,\"message\":\"Cohort analysis completed successfully. Found 33 patients from 2 search concepts.\",\"patientIds\":[\"patient-123\",\"patient-456\",\"patient-789\"],\"patientCount\":33,\"queries\":[{\"resource_type\":\"Patient\",\"search_params\":\"gender=female&birthdate=le2004-01-01\",\"concept\":\"female patients over 20\",\"exclude\":false},{\"resource_type\":\"Condition\",\"search_params\":\"code=55822004\",\"concept\":\"hyperlipidemia\",\"exclude\":false}]}"));
        CohortResponse response = client.tools()
                .analyzeCohort(CohortRequest.builder()
                        .text("female patients over 20 with diabetes but not hypertension")
                        .provider("550e8400-e29b-41d4-a716-446655440000")
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
                + "  \"text\": \"female patients over 20 with diabetes but not hypertension\",\n"
                + "  \"provider\": \"550e8400-e29b-41d4-a716-446655440000\"\n"
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
                + "  \"message\": \"Cohort analysis completed successfully. Found 33 patients from 2 search concepts.\",\n"
                + "  \"patientIds\": [\n"
                + "    \"patient-123\",\n"
                + "    \"patient-456\",\n"
                + "    \"patient-789\"\n"
                + "  ],\n"
                + "  \"patientCount\": 33,\n"
                + "  \"queries\": [\n"
                + "    {\n"
                + "      \"resource_type\": \"Patient\",\n"
                + "      \"search_params\": \"gender=female&birthdate=le2004-01-01\",\n"
                + "      \"concept\": \"female patients over 20\",\n"
                + "      \"exclude\": false\n"
                + "    },\n"
                + "    {\n"
                + "      \"resource_type\": \"Condition\",\n"
                + "      \"search_params\": \"code=55822004\",\n"
                + "      \"concept\": \"hyperlipidemia\",\n"
                + "      \"exclude\": false\n"
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
