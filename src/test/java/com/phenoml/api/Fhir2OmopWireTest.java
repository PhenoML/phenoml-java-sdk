package com.phenoml.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phenoml.api.core.ObjectMappers;
import com.phenoml.api.resources.fhir2omop.errors.BadRequestError;
import com.phenoml.api.resources.fhir2omop.errors.InternalServerError;
import com.phenoml.api.resources.fhir2omop.errors.UnauthorizedError;
import com.phenoml.api.resources.fhir2omop.requests.CreateOmopRequest;
import com.phenoml.api.resources.fhir2omop.types.CreateOmopResponse;
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

public class Fhir2OmopWireTest {
    private MockWebServer server;
    private PhenomlClient client;
    private ObjectMapper objectMapper = ObjectMappers.JSON_MAPPER;

    @BeforeEach
    public void setup() throws Exception {
        server = new MockWebServer();
        server.start();
        client = PhenomlClient.withCredentials("test-client-id", "test-client-secret")
                .url(server.url("/").toString())
                .maxRetries(0)
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
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(TestResources.loadResource("/wire-tests/Fhir2OmopWireTest_testCreate_response.json")));
        CreateOmopResponse response = client.fhir2Omop()
                .create(CreateOmopRequest.builder()
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
                                                                put("id", "patient-1");
                                                                put("gender", "female");
                                                                put("birthDate", "1985-07-22");
                                                            }
                                                        });
                                                    }
                                                },
                                                new HashMap<String, Object>() {
                                                    {
                                                        put("resource", new HashMap<String, Object>() {
                                                            {
                                                                put("resourceType", "Condition");
                                                                put("id", "condition-1");
                                                                put("subject", new HashMap<String, Object>() {
                                                                    {
                                                                        put("reference", "Patient/patient-1");
                                                                    }
                                                                });
                                                                put("code", new HashMap<String, Object>() {
                                                                    {
                                                                        put(
                                                                                "coding",
                                                                                new ArrayList<Object>(
                                                                                        Arrays.asList(
                                                                                                new HashMap<
                                                                                                        String,
                                                                                                        Object>() {
                                                                                                    {
                                                                                                        put(
                                                                                                                "system",
                                                                                                                "http://snomed.info/sct");
                                                                                                        put(
                                                                                                                "code",
                                                                                                                "44054006");
                                                                                                        put(
                                                                                                                "display",
                                                                                                                "Type 2 diabetes mellitus");
                                                                                                    }
                                                                                                })));
                                                                    }
                                                                });
                                                                put("onsetDateTime", "2024-01-15");
                                                            }
                                                        });
                                                    }
                                                },
                                                new HashMap<String, Object>() {
                                                    {
                                                        put("resource", new HashMap<String, Object>() {
                                                            {
                                                                put("resourceType", "MedicationRequest");
                                                                put("id", "medreq-1");
                                                                put("status", "active");
                                                                put("intent", "order");
                                                                put("subject", new HashMap<String, Object>() {
                                                                    {
                                                                        put("reference", "Patient/patient-1");
                                                                    }
                                                                });
                                                                put(
                                                                        "medicationReference",
                                                                        new HashMap<String, Object>() {
                                                                            {
                                                                                put("reference", "#med0");
                                                                            }
                                                                        });
                                                                put("authoredOn", "2024-01-16");
                                                                put(
                                                                        "contained",
                                                                        new ArrayList<Object>(Arrays.asList(
                                                                                new HashMap<String, Object>() {
                                                                                    {
                                                                                        put(
                                                                                                "resourceType",
                                                                                                "Medication");
                                                                                        put("id", "med0");
                                                                                        put(
                                                                                                "code",
                                                                                                new HashMap<
                                                                                                        String,
                                                                                                        Object>() {
                                                                                                    {
                                                                                                        put(
                                                                                                                "coding",
                                                                                                                new ArrayList<
                                                                                                                        Object>(
                                                                                                                        Arrays
                                                                                                                                .asList(
                                                                                                                                        new HashMap<
                                                                                                                                                String,
                                                                                                                                                Object>() {
                                                                                                                                            {
                                                                                                                                                put(
                                                                                                                                                        "system",
                                                                                                                                                        "http://www.nlm.nih.gov/research/umls/rxnorm");
                                                                                                                                                put(
                                                                                                                                                        "code",
                                                                                                                                                        "860975");
                                                                                                                                                put(
                                                                                                                                                        "display",
                                                                                                                                                        "metformin hydrochloride 500 MG");
                                                                                                                                            }
                                                                                                                                        })));
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                })));
                                                            }
                                                        });
                                                    }
                                                })));
                            }
                        })
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
        String expectedRequestBody =
                TestResources.loadResource("/wire-tests/Fhir2OmopWireTest_testCreate_request.json");
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
                TestResources.loadResource("/wire-tests/Fhir2OmopWireTest_testCreate_response.json");
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
    public void testCreateOidFormLoincCoding() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(TestResources.loadResource(
                        "/wire-tests/Fhir2OmopWireTest_testCreateOidFormLoincCoding_response.json")));
        CreateOmopResponse response = client.fhir2Omop()
                .create(CreateOmopRequest.builder()
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
                                                                put("id", "patient-1");
                                                            }
                                                        });
                                                    }
                                                },
                                                new HashMap<String, Object>() {
                                                    {
                                                        put("resource", new HashMap<String, Object>() {
                                                            {
                                                                put("resourceType", "Observation");
                                                                put("id", "hemoglobin-1");
                                                                put("status", "final");
                                                                put("subject", new HashMap<String, Object>() {
                                                                    {
                                                                        put("reference", "Patient/patient-1");
                                                                    }
                                                                });
                                                                put("code", new HashMap<String, Object>() {
                                                                    {
                                                                        put(
                                                                                "coding",
                                                                                new ArrayList<Object>(
                                                                                        Arrays.asList(
                                                                                                new HashMap<
                                                                                                        String,
                                                                                                        Object>() {
                                                                                                    {
                                                                                                        put(
                                                                                                                "system",
                                                                                                                "urn:oid:2.16.840.1.113883.6.1");
                                                                                                        put(
                                                                                                                "code",
                                                                                                                "718-7");
                                                                                                        put(
                                                                                                                "display",
                                                                                                                "Hemoglobin");
                                                                                                    }
                                                                                                })));
                                                                    }
                                                                });
                                                                put("valueQuantity", new HashMap<String, Object>() {
                                                                    {
                                                                        put("value", 13.5);
                                                                        put("unit", "g/dL");
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    }
                                                })));
                            }
                        })
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
                + "  \"fhir_resources\": {\n"
                + "    \"resourceType\": \"Bundle\",\n"
                + "    \"type\": \"collection\",\n"
                + "    \"entry\": [\n"
                + "      {\n"
                + "        \"resource\": {\n"
                + "          \"resourceType\": \"Patient\",\n"
                + "          \"id\": \"patient-1\"\n"
                + "        }\n"
                + "      },\n"
                + "      {\n"
                + "        \"resource\": {\n"
                + "          \"resourceType\": \"Observation\",\n"
                + "          \"id\": \"hemoglobin-1\",\n"
                + "          \"status\": \"final\",\n"
                + "          \"subject\": {\n"
                + "            \"reference\": \"Patient/patient-1\"\n"
                + "          },\n"
                + "          \"code\": {\n"
                + "            \"coding\": [\n"
                + "              {\n"
                + "                \"system\": \"urn:oid:2.16.840.1.113883.6.1\",\n"
                + "                \"code\": \"718-7\",\n"
                + "                \"display\": \"Hemoglobin\"\n"
                + "              }\n"
                + "            ]\n"
                + "          },\n"
                + "          \"valueQuantity\": {\n"
                + "            \"value\": 13.5,\n"
                + "            \"unit\": \"g/dL\"\n"
                + "          }\n"
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
        String expectedResponseBody =
                TestResources.loadResource("/wire-tests/Fhir2OmopWireTest_testCreateOidFormLoincCoding_response.json");
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
    public void testCreateThrowsBadRequestError() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(new MockResponse().setResponseCode(400).setBody("{\"key\":\"value\"}"));
        BadRequestError exception = Assertions.assertThrows(BadRequestError.class, () -> client.fhir2Omop()
                .create(CreateOmopRequest.builder()
                        .fhirResources(new HashMap<String, Object>() {
                            {
                                put("fhir_resources", new HashMap<String, Object>() {
                                    {
                                        put("key", "value");
                                    }
                                });
                            }
                        })
                        .build()));
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
                + "  \"fhir_resources\": {\n"
                + "    \"fhir_resources\": {\n"
                + "      \"key\": \"value\"\n"
                + "    }\n"
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

        // Validate error response
        Assertions.assertEquals(400, exception.statusCode(), "Error status code does not match expected");
        String actualErrorJson = objectMapper.writeValueAsString(exception.body());
        String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
        JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
        JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
        Assertions.assertTrue(jsonEquals(expectedErrorNode, actualErrorNode), "Error body does not match expected");
    }

    @Test
    public void testCreateThrowsUnauthorizedError() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(new MockResponse().setResponseCode(401).setBody("{\"key\":\"value\"}"));
        UnauthorizedError exception = Assertions.assertThrows(UnauthorizedError.class, () -> client.fhir2Omop()
                .create(CreateOmopRequest.builder()
                        .fhirResources(new HashMap<String, Object>() {
                            {
                                put("fhir_resources", new HashMap<String, Object>() {
                                    {
                                        put("key", "value");
                                    }
                                });
                            }
                        })
                        .build()));
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
                + "  \"fhir_resources\": {\n"
                + "    \"fhir_resources\": {\n"
                + "      \"key\": \"value\"\n"
                + "    }\n"
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

        // Validate error response
        Assertions.assertEquals(401, exception.statusCode(), "Error status code does not match expected");
        String actualErrorJson = objectMapper.writeValueAsString(exception.body());
        String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
        JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
        JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
        Assertions.assertTrue(jsonEquals(expectedErrorNode, actualErrorNode), "Error body does not match expected");
    }

    @Test
    public void testCreateThrowsInternalServerError() throws Exception {
        // OAuth: enqueue token response (client fetches token before API call)
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"key\":\"value\"}"));
        InternalServerError exception = Assertions.assertThrows(InternalServerError.class, () -> client.fhir2Omop()
                .create(CreateOmopRequest.builder()
                        .fhirResources(new HashMap<String, Object>() {
                            {
                                put("fhir_resources", new HashMap<String, Object>() {
                                    {
                                        put("key", "value");
                                    }
                                });
                            }
                        })
                        .build()));
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
                + "  \"fhir_resources\": {\n"
                + "    \"fhir_resources\": {\n"
                + "      \"key\": \"value\"\n"
                + "    }\n"
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

        // Validate error response
        Assertions.assertEquals(500, exception.statusCode(), "Error status code does not match expected");
        String actualErrorJson = objectMapper.writeValueAsString(exception.body());
        String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
        JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
        JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
        Assertions.assertTrue(jsonEquals(expectedErrorNode, actualErrorNode), "Error body does not match expected");
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
