package com.phenoml.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phenoml.api.core.ObjectMappers;
import com.phenoml.api.resources.lang2fhir.errors.BadRequestError;
import com.phenoml.api.resources.lang2fhir.errors.ClientClosedRequestError;
import com.phenoml.api.resources.lang2fhir.errors.ForbiddenError;
import com.phenoml.api.resources.lang2fhir.errors.GatewayTimeoutError;
import com.phenoml.api.resources.lang2fhir.errors.InternalServerError;
import com.phenoml.api.resources.lang2fhir.errors.NotFoundError;
import com.phenoml.api.resources.lang2fhir.errors.UnauthorizedError;
import com.phenoml.api.resources.lang2fhir.errors.UnprocessableEntityError;
import com.phenoml.api.resources.lang2fhir.requests.CreateMultiRequest;
import com.phenoml.api.resources.lang2fhir.requests.CreateRequest;
import com.phenoml.api.resources.lang2fhir.requests.DocumentMultiRequest;
import com.phenoml.api.resources.lang2fhir.requests.DocumentRequest;
import com.phenoml.api.resources.lang2fhir.requests.SearchRequest;
import com.phenoml.api.resources.lang2fhir.types.CreateMultiResponse;
import com.phenoml.api.resources.lang2fhir.types.CreateRequestResource;
import com.phenoml.api.resources.lang2fhir.types.DocumentConfig;
import com.phenoml.api.resources.lang2fhir.types.DocumentMultiResponse;
import com.phenoml.api.resources.lang2fhir.types.ProfileUploadRequest;
import com.phenoml.api.resources.lang2fhir.types.SearchResponse;
import com.phenoml.api.resources.lang2fhir.types.SplitClassification;
import com.phenoml.api.resources.lang2fhir.types.SplitClassificationOperation;
import com.phenoml.api.resources.lang2fhir.types.UploadProfileResponse;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Lang2FhirWireTest {

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
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody(
          "{\"resourceType\":\"Condition\",\"clinicalStatus\":{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/condition-clinical\",\"code\":\"active\"}]},\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"195967001\",\"display\":\"Asthma\"}],\"text\":\"Severe persistent asthma with acute exacerbation\"},\"severity\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"24484000\",\"display\":\"Severe\"}]}}"
        )
    );
    Map<String, Object> response = client
      .lang2Fhir()
      .create(
        CreateRequest.builder()
          .version("R4")
          .resource(CreateRequestResource.CONDITION_ENCOUNTER_DIAGNOSIS)
          .text("Patient has severe persistent asthma with acute exacerbation")
          .build()
      );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" +
      "{\n" +
      "  \"version\": \"R4\",\n" +
      "  \"resource\": \"condition-encounter-diagnosis\",\n" +
      "  \"text\": \"Patient has severe persistent asthma with acute exacerbation\"\n" +
      "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
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
      "" +
      "{\n" +
      "  \"resourceType\": \"Condition\",\n" +
      "  \"clinicalStatus\": {\n" +
      "    \"coding\": [\n" +
      "      {\n" +
      "        \"system\": \"http://terminology.hl7.org/CodeSystem/condition-clinical\",\n" +
      "        \"code\": \"active\"\n" +
      "      }\n" +
      "    ]\n" +
      "  },\n" +
      "  \"code\": {\n" +
      "    \"coding\": [\n" +
      "      {\n" +
      "        \"system\": \"http://snomed.info/sct\",\n" +
      "        \"code\": \"195967001\",\n" +
      "        \"display\": \"Asthma\"\n" +
      "      }\n" +
      "    ],\n" +
      "    \"text\": \"Severe persistent asthma with acute exacerbation\"\n" +
      "  },\n" +
      "  \"severity\": {\n" +
      "    \"coding\": [\n" +
      "      {\n" +
      "        \"system\": \"http://snomed.info/sct\",\n" +
      "        \"code\": \"24484000\",\n" +
      "        \"display\": \"Severe\"\n" +
      "      }\n" +
      "    ]\n" +
      "  }\n" +
      "}";
    JsonNode actualResponseNode = objectMapper.readTree(actualResponseJson);
    JsonNode expectedResponseNode = objectMapper.readTree(expectedResponseBody);
    Assertions.assertTrue(
      jsonEquals(expectedResponseNode, actualResponseNode),
      "Response body structure does not match expected"
    );
    if (
      actualResponseNode.has("type") ||
      actualResponseNode.has("_type") ||
      actualResponseNode.has("kind")
    ) {
      String discriminator = null;
      if (actualResponseNode.has("type")) discriminator = actualResponseNode.get("type").asText();
      else if (actualResponseNode.has("_type")) discriminator = actualResponseNode
        .get("_type")
        .asText();
      else if (actualResponseNode.has("kind")) discriminator = actualResponseNode
        .get("kind")
        .asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualResponseNode.isNull()) {
      Assertions.assertTrue(
        actualResponseNode.isObject() ||
        actualResponseNode.isArray() ||
        actualResponseNode.isValueNode(),
        "response should be a valid JSON value"
      );
    }

    if (actualResponseNode.isArray()) {
      Assertions.assertTrue(actualResponseNode.size() >= 0, "Array should have valid size");
    }
    if (actualResponseNode.isObject()) {
      Assertions.assertTrue(actualResponseNode.size() >= 0, "Object should have valid field count");
    }
  }

  @Test
  public void testCreateMedicationRequest() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody(
          "{\"resourceType\":\"MedicationRequest\",\"status\":\"active\",\"intent\":\"order\",\"medicationCodeableConcept\":{\"coding\":[{\"system\":\"http://www.nlm.nih.gov/research/umls/rxnorm\",\"code\":\"308182\",\"display\":\"Amoxicillin 500 MG Oral Capsule\"}]},\"dosageInstruction\":[{\"text\":\"Take one capsule three times daily\",\"timing\":{\"repeat\":{\"frequency\":3,\"period\":1,\"periodUnit\":\"d\"}},\"doseAndRate\":[{\"doseQuantity\":{\"value\":500,\"unit\":\"mg\"}}]}],\"dispenseRequest\":{\"expectedSupplyDuration\":{\"value\":10,\"unit\":\"days\"}}}"
        )
    );
    Map<String, Object> response = client
      .lang2Fhir()
      .create(
        CreateRequest.builder()
          .version("R4")
          .resource(CreateRequestResource.MEDICATIONREQUEST)
          .text(
            "Prescribe Amoxicillin 500mg capsules, take one capsule three times daily for 10 days"
          )
          .build()
      );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" +
      "{\n" +
      "  \"version\": \"R4\",\n" +
      "  \"resource\": \"medicationrequest\",\n" +
      "  \"text\": \"Prescribe Amoxicillin 500mg capsules, take one capsule three times daily for 10 days\"\n" +
      "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
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
      "" +
      "{\n" +
      "  \"resourceType\": \"MedicationRequest\",\n" +
      "  \"status\": \"active\",\n" +
      "  \"intent\": \"order\",\n" +
      "  \"medicationCodeableConcept\": {\n" +
      "    \"coding\": [\n" +
      "      {\n" +
      "        \"system\": \"http://www.nlm.nih.gov/research/umls/rxnorm\",\n" +
      "        \"code\": \"308182\",\n" +
      "        \"display\": \"Amoxicillin 500 MG Oral Capsule\"\n" +
      "      }\n" +
      "    ]\n" +
      "  },\n" +
      "  \"dosageInstruction\": [\n" +
      "    {\n" +
      "      \"text\": \"Take one capsule three times daily\",\n" +
      "      \"timing\": {\n" +
      "        \"repeat\": {\n" +
      "          \"frequency\": 3,\n" +
      "          \"period\": 1,\n" +
      "          \"periodUnit\": \"d\"\n" +
      "        }\n" +
      "      },\n" +
      "      \"doseAndRate\": [\n" +
      "        {\n" +
      "          \"doseQuantity\": {\n" +
      "            \"value\": 500,\n" +
      "            \"unit\": \"mg\"\n" +
      "          }\n" +
      "        }\n" +
      "      ]\n" +
      "    }\n" +
      "  ],\n" +
      "  \"dispenseRequest\": {\n" +
      "    \"expectedSupplyDuration\": {\n" +
      "      \"value\": 10,\n" +
      "      \"unit\": \"days\"\n" +
      "    }\n" +
      "  }\n" +
      "}";
    JsonNode actualResponseNode = objectMapper.readTree(actualResponseJson);
    JsonNode expectedResponseNode = objectMapper.readTree(expectedResponseBody);
    Assertions.assertTrue(
      jsonEquals(expectedResponseNode, actualResponseNode),
      "Response body structure does not match expected"
    );
    if (
      actualResponseNode.has("type") ||
      actualResponseNode.has("_type") ||
      actualResponseNode.has("kind")
    ) {
      String discriminator = null;
      if (actualResponseNode.has("type")) discriminator = actualResponseNode.get("type").asText();
      else if (actualResponseNode.has("_type")) discriminator = actualResponseNode
        .get("_type")
        .asText();
      else if (actualResponseNode.has("kind")) discriminator = actualResponseNode
        .get("kind")
        .asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualResponseNode.isNull()) {
      Assertions.assertTrue(
        actualResponseNode.isObject() ||
        actualResponseNode.isArray() ||
        actualResponseNode.isValueNode(),
        "response should be a valid JSON value"
      );
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
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(400).setBody("{\"key\":\"value\"}"));
    BadRequestError exception = Assertions.assertThrows(BadRequestError.class, () ->
      client
        .lang2Fhir()
        .create(
          CreateRequest.builder()
            .version("R4")
            .resource(CreateRequestResource.AUTO)
            .text("text")
            .build()
        )
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" +
      "{\n" +
      "  \"version\": \"R4\",\n" +
      "  \"resource\": \"auto\",\n" +
      "  \"text\": \"text\"\n" +
      "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      400,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testCreateThrowsUnauthorizedError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(401).setBody("{\"key\":\"value\"}"));
    UnauthorizedError exception = Assertions.assertThrows(UnauthorizedError.class, () ->
      client
        .lang2Fhir()
        .create(
          CreateRequest.builder()
            .version("R4")
            .resource(CreateRequestResource.AUTO)
            .text("text")
            .build()
        )
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" +
      "{\n" +
      "  \"version\": \"R4\",\n" +
      "  \"resource\": \"auto\",\n" +
      "  \"text\": \"text\"\n" +
      "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      401,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testCreateThrowsNotFoundError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(404).setBody("{\"key\":\"value\"}"));
    NotFoundError exception = Assertions.assertThrows(NotFoundError.class, () ->
      client
        .lang2Fhir()
        .create(
          CreateRequest.builder()
            .version("R4")
            .resource(CreateRequestResource.AUTO)
            .text("text")
            .build()
        )
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" +
      "{\n" +
      "  \"version\": \"R4\",\n" +
      "  \"resource\": \"auto\",\n" +
      "  \"text\": \"text\"\n" +
      "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      404,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testCreateThrowsUnprocessableEntityError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(422).setBody("{\"key\":\"value\"}"));
    UnprocessableEntityError exception = Assertions.assertThrows(
      UnprocessableEntityError.class,
      () ->
        client
          .lang2Fhir()
          .create(
            CreateRequest.builder()
              .version("R4")
              .resource(CreateRequestResource.AUTO)
              .text("text")
              .build()
          )
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" +
      "{\n" +
      "  \"version\": \"R4\",\n" +
      "  \"resource\": \"auto\",\n" +
      "  \"text\": \"text\"\n" +
      "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      422,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testCreateThrowsInternalServerError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"key\":\"value\"}"));
    InternalServerError exception = Assertions.assertThrows(InternalServerError.class, () ->
      client
        .lang2Fhir()
        .create(
          CreateRequest.builder()
            .version("R4")
            .resource(CreateRequestResource.AUTO)
            .text("text")
            .build()
        )
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" +
      "{\n" +
      "  \"version\": \"R4\",\n" +
      "  \"resource\": \"auto\",\n" +
      "  \"text\": \"text\"\n" +
      "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      500,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testCreateMulti() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody(
          TestResources.loadResource("/wire-tests/Lang2FhirWireTest_testCreateMulti_response.json")
        )
    );
    CreateMultiResponse response = client
      .lang2Fhir()
      .createMulti(
        CreateMultiRequest.builder()
          .text(
            "John Smith, 45-year-old male, diagnosed with Type 2 Diabetes. Prescribed Metformin 500mg twice daily. Blood pressure 140/90."
          )
          .version("R4")
          .build()
      );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" +
      "{\n" +
      "  \"text\": \"John Smith, 45-year-old male, diagnosed with Type 2 Diabetes. Prescribed Metformin 500mg twice daily. Blood pressure 140/90.\",\n" +
      "  \"version\": \"R4\"\n" +
      "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
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
    String expectedResponseBody = TestResources.loadResource(
      "/wire-tests/Lang2FhirWireTest_testCreateMulti_response.json"
    );
    JsonNode actualResponseNode = objectMapper.readTree(actualResponseJson);
    JsonNode expectedResponseNode = objectMapper.readTree(expectedResponseBody);
    Assertions.assertTrue(
      jsonEquals(expectedResponseNode, actualResponseNode),
      "Response body structure does not match expected"
    );
    if (
      actualResponseNode.has("type") ||
      actualResponseNode.has("_type") ||
      actualResponseNode.has("kind")
    ) {
      String discriminator = null;
      if (actualResponseNode.has("type")) discriminator = actualResponseNode.get("type").asText();
      else if (actualResponseNode.has("_type")) discriminator = actualResponseNode
        .get("_type")
        .asText();
      else if (actualResponseNode.has("kind")) discriminator = actualResponseNode
        .get("kind")
        .asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualResponseNode.isNull()) {
      Assertions.assertTrue(
        actualResponseNode.isObject() ||
        actualResponseNode.isArray() ||
        actualResponseNode.isValueNode(),
        "response should be a valid JSON value"
      );
    }

    if (actualResponseNode.isArray()) {
      Assertions.assertTrue(actualResponseNode.size() >= 0, "Array should have valid size");
    }
    if (actualResponseNode.isObject()) {
      Assertions.assertTrue(actualResponseNode.size() >= 0, "Object should have valid field count");
    }
  }

  @Test
  public void testCreateMultiThrowsBadRequestError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(400).setBody("{\"key\":\"value\"}"));
    BadRequestError exception = Assertions.assertThrows(BadRequestError.class, () ->
      client.lang2Fhir().createMulti(CreateMultiRequest.builder().text("text").build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody = "" + "{\n" + "  \"text\": \"text\"\n" + "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      400,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testCreateMultiThrowsUnauthorizedError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(401).setBody("{\"key\":\"value\"}"));
    UnauthorizedError exception = Assertions.assertThrows(UnauthorizedError.class, () ->
      client.lang2Fhir().createMulti(CreateMultiRequest.builder().text("text").build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody = "" + "{\n" + "  \"text\": \"text\"\n" + "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      401,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testCreateMultiThrowsNotFoundError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(404).setBody("{\"key\":\"value\"}"));
    NotFoundError exception = Assertions.assertThrows(NotFoundError.class, () ->
      client.lang2Fhir().createMulti(CreateMultiRequest.builder().text("text").build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody = "" + "{\n" + "  \"text\": \"text\"\n" + "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      404,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testCreateMultiThrowsUnprocessableEntityError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(422).setBody("{\"key\":\"value\"}"));
    UnprocessableEntityError exception = Assertions.assertThrows(
      UnprocessableEntityError.class,
      () -> client.lang2Fhir().createMulti(CreateMultiRequest.builder().text("text").build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody = "" + "{\n" + "  \"text\": \"text\"\n" + "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      422,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testCreateMultiThrowsInternalServerError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"key\":\"value\"}"));
    InternalServerError exception = Assertions.assertThrows(InternalServerError.class, () ->
      client.lang2Fhir().createMulti(CreateMultiRequest.builder().text("text").build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody = "" + "{\n" + "  \"text\": \"text\"\n" + "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      500,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testSearch() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody(
          "{\"resource_type\":\"Appointment\",\"search_params\":\"date=ge2025-03-02&date=le2025-03-09\"}"
        )
    );
    SearchResponse response = client
      .lang2Fhir()
      .search(SearchRequest.builder().text("Appointments between March 2-9, 2025").build());
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" + "{\n" + "  \"text\": \"Appointments between March 2-9, 2025\"\n" + "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
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
      "" +
      "{\n" +
      "  \"resource_type\": \"Appointment\",\n" +
      "  \"search_params\": \"date=ge2025-03-02&date=le2025-03-09\"\n" +
      "}";
    JsonNode actualResponseNode = objectMapper.readTree(actualResponseJson);
    JsonNode expectedResponseNode = objectMapper.readTree(expectedResponseBody);
    Assertions.assertTrue(
      jsonEquals(expectedResponseNode, actualResponseNode),
      "Response body structure does not match expected"
    );
    if (
      actualResponseNode.has("type") ||
      actualResponseNode.has("_type") ||
      actualResponseNode.has("kind")
    ) {
      String discriminator = null;
      if (actualResponseNode.has("type")) discriminator = actualResponseNode.get("type").asText();
      else if (actualResponseNode.has("_type")) discriminator = actualResponseNode
        .get("_type")
        .asText();
      else if (actualResponseNode.has("kind")) discriminator = actualResponseNode
        .get("kind")
        .asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualResponseNode.isNull()) {
      Assertions.assertTrue(
        actualResponseNode.isObject() ||
        actualResponseNode.isArray() ||
        actualResponseNode.isValueNode(),
        "response should be a valid JSON value"
      );
    }

    if (actualResponseNode.isArray()) {
      Assertions.assertTrue(actualResponseNode.size() >= 0, "Array should have valid size");
    }
    if (actualResponseNode.isObject()) {
      Assertions.assertTrue(actualResponseNode.size() >= 0, "Object should have valid field count");
    }
  }

  @Test
  public void testSearchThrowsBadRequestError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(400).setBody("{\"key\":\"value\"}"));
    BadRequestError exception = Assertions.assertThrows(BadRequestError.class, () ->
      client.lang2Fhir().search(SearchRequest.builder().text("text").build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody = "" + "{\n" + "  \"text\": \"text\"\n" + "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      400,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testSearchThrowsUnauthorizedError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(401).setBody("{\"key\":\"value\"}"));
    UnauthorizedError exception = Assertions.assertThrows(UnauthorizedError.class, () ->
      client.lang2Fhir().search(SearchRequest.builder().text("text").build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody = "" + "{\n" + "  \"text\": \"text\"\n" + "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      401,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testSearchThrowsInternalServerError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"key\":\"value\"}"));
    InternalServerError exception = Assertions.assertThrows(InternalServerError.class, () ->
      client.lang2Fhir().search(SearchRequest.builder().text("text").build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody = "" + "{\n" + "  \"text\": \"text\"\n" + "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      500,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testUploadProfile() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(
      new MockResponse()
        .setResponseCode(201)
        .setBody(
          "{\"message\":\"Profile uploaded successfully\",\"id\":\"custom-patient\",\"type\":\"Patient\",\"url\":\"http://phenoml.com/fhir/StructureDefinition/custom-patient\"}"
        )
    );
    UploadProfileResponse response = client
      .lang2Fhir()
      .uploadProfile(
        ProfileUploadRequest.builder()
          .profile(
            "eyJyZXNvdXJjZVR5cGUiOiJTdHJ1Y3R1cmVEZWZpbml0aW9uIiwiaWQiOiJjdXN0b20tcGF0aWVudCIsInVybCI6Imh0dHA6Ly9waGVub21sLmNvbS9maGlyL1N0cnVjdHVyZURlZmluaXRpb24vY3VzdG9tLXBhdGllbnQiLCJuYW1lIjoiQ3VzdG9tUGF0aWVudCIsInN0YXR1cyI6ImFjdGl2ZSIsImZoaXJWZXJzaW9uIjoiNC4wLjEiLCJraW5kIjoicmVzb3VyY2UiLCJhYnN0cmFjdCI6ZmFsc2UsInR5cGUiOiJQYXRpZW50IiwiYmFzZURlZmluaXRpb24iOiJodHRwOi8vaGw3Lm9yZy9maGlyL1N0cnVjdHVyZURlZmluaXRpb24vUGF0aWVudCIsImRlcml2YXRpb24iOiJjb25zdHJhaW50Iiwic25hcHNob3QiOnsiZWxlbWVudCI6W3siaWQiOiJQYXRpZW50IiwicGF0aCI6IlBhdGllbnQiLCJtaW4iOjAsIm1heCI6IioifSx7ImlkIjoiUGF0aWVudC5uYW1lIiwicGF0aCI6IlBhdGllbnQubmFtZSIsIm1pbiI6MSwibWF4IjoiKiJ9XX19Cg=="
          )
          .implementationGuide("acme-cardiology")
          .profileContext(
            "When clinical text describes cardiology-specific findings, prefer this profile over the generic US Core Condition."
          )
          .build()
      );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" +
      "{\n" +
      "  \"profile\": \"eyJyZXNvdXJjZVR5cGUiOiJTdHJ1Y3R1cmVEZWZpbml0aW9uIiwiaWQiOiJjdXN0b20tcGF0aWVudCIsInVybCI6Imh0dHA6Ly9waGVub21sLmNvbS9maGlyL1N0cnVjdHVyZURlZmluaXRpb24vY3VzdG9tLXBhdGllbnQiLCJuYW1lIjoiQ3VzdG9tUGF0aWVudCIsInN0YXR1cyI6ImFjdGl2ZSIsImZoaXJWZXJzaW9uIjoiNC4wLjEiLCJraW5kIjoicmVzb3VyY2UiLCJhYnN0cmFjdCI6ZmFsc2UsInR5cGUiOiJQYXRpZW50IiwiYmFzZURlZmluaXRpb24iOiJodHRwOi8vaGw3Lm9yZy9maGlyL1N0cnVjdHVyZURlZmluaXRpb24vUGF0aWVudCIsImRlcml2YXRpb24iOiJjb25zdHJhaW50Iiwic25hcHNob3QiOnsiZWxlbWVudCI6W3siaWQiOiJQYXRpZW50IiwicGF0aCI6IlBhdGllbnQiLCJtaW4iOjAsIm1heCI6IioifSx7ImlkIjoiUGF0aWVudC5uYW1lIiwicGF0aCI6IlBhdGllbnQubmFtZSIsIm1pbiI6MSwibWF4IjoiKiJ9XX19Cg==\",\n" +
      "  \"implementation_guide\": \"acme-cardiology\",\n" +
      "  \"profile_context\": \"When clinical text describes cardiology-specific findings, prefer this profile over the generic US Core Condition.\"\n" +
      "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
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
      "" +
      "{\n" +
      "  \"message\": \"Profile uploaded successfully\",\n" +
      "  \"id\": \"custom-patient\",\n" +
      "  \"type\": \"Patient\",\n" +
      "  \"url\": \"http://phenoml.com/fhir/StructureDefinition/custom-patient\"\n" +
      "}";
    JsonNode actualResponseNode = objectMapper.readTree(actualResponseJson);
    JsonNode expectedResponseNode = objectMapper.readTree(expectedResponseBody);
    Assertions.assertTrue(
      jsonEquals(expectedResponseNode, actualResponseNode),
      "Response body structure does not match expected"
    );
    if (
      actualResponseNode.has("type") ||
      actualResponseNode.has("_type") ||
      actualResponseNode.has("kind")
    ) {
      String discriminator = null;
      if (actualResponseNode.has("type")) discriminator = actualResponseNode.get("type").asText();
      else if (actualResponseNode.has("_type")) discriminator = actualResponseNode
        .get("_type")
        .asText();
      else if (actualResponseNode.has("kind")) discriminator = actualResponseNode
        .get("kind")
        .asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualResponseNode.isNull()) {
      Assertions.assertTrue(
        actualResponseNode.isObject() ||
        actualResponseNode.isArray() ||
        actualResponseNode.isValueNode(),
        "response should be a valid JSON value"
      );
    }

    if (actualResponseNode.isArray()) {
      Assertions.assertTrue(actualResponseNode.size() >= 0, "Array should have valid size");
    }
    if (actualResponseNode.isObject()) {
      Assertions.assertTrue(actualResponseNode.size() >= 0, "Object should have valid field count");
    }
  }

  @Test
  public void testUploadProfileThrowsBadRequestError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(400).setBody("{\"key\":\"value\"}"));
    BadRequestError exception = Assertions.assertThrows(BadRequestError.class, () ->
      client.lang2Fhir().uploadProfile(ProfileUploadRequest.builder().profile("profile").build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody = "" + "{\n" + "  \"profile\": \"profile\"\n" + "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      400,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testUploadProfileThrowsUnauthorizedError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(401).setBody("{\"key\":\"value\"}"));
    UnauthorizedError exception = Assertions.assertThrows(UnauthorizedError.class, () ->
      client.lang2Fhir().uploadProfile(ProfileUploadRequest.builder().profile("profile").build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody = "" + "{\n" + "  \"profile\": \"profile\"\n" + "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      401,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testUploadProfileThrowsForbiddenError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(403).setBody("{\"key\":\"value\"}"));
    ForbiddenError exception = Assertions.assertThrows(ForbiddenError.class, () ->
      client.lang2Fhir().uploadProfile(ProfileUploadRequest.builder().profile("profile").build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody = "" + "{\n" + "  \"profile\": \"profile\"\n" + "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      403,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testUploadProfileThrowsInternalServerError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"key\":\"value\"}"));
    InternalServerError exception = Assertions.assertThrows(InternalServerError.class, () ->
      client.lang2Fhir().uploadProfile(ProfileUploadRequest.builder().profile("profile").build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody = "" + "{\n" + "  \"profile\": \"profile\"\n" + "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      500,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testDocument() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody(
          "{\"resourceType\":\"Questionnaire\",\"status\":\"active\",\"title\":\"Patient Intake Form\",\"item\":[{\"linkId\":\"1\",\"text\":\"What is your name?\",\"type\":\"string\"},{\"linkId\":\"2\",\"text\":\"What is your date of birth?\",\"type\":\"date\"}]}"
        )
    );
    Map<String, Object> response = client
      .lang2Fhir()
      .document(
        DocumentRequest.builder()
          .version("R4")
          .resource("questionnaire")
          .content("JVBERi0xLjQKJeLjz9MK...(base64-encoded document bytes)")
          .build()
      );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" +
      "{\n" +
      "  \"version\": \"R4\",\n" +
      "  \"resource\": \"questionnaire\",\n" +
      "  \"content\": \"JVBERi0xLjQKJeLjz9MK...(base64-encoded document bytes)\"\n" +
      "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
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
      "" +
      "{\n" +
      "  \"resourceType\": \"Questionnaire\",\n" +
      "  \"status\": \"active\",\n" +
      "  \"title\": \"Patient Intake Form\",\n" +
      "  \"item\": [\n" +
      "    {\n" +
      "      \"linkId\": \"1\",\n" +
      "      \"text\": \"What is your name?\",\n" +
      "      \"type\": \"string\"\n" +
      "    },\n" +
      "    {\n" +
      "      \"linkId\": \"2\",\n" +
      "      \"text\": \"What is your date of birth?\",\n" +
      "      \"type\": \"date\"\n" +
      "    }\n" +
      "  ]\n" +
      "}";
    JsonNode actualResponseNode = objectMapper.readTree(actualResponseJson);
    JsonNode expectedResponseNode = objectMapper.readTree(expectedResponseBody);
    Assertions.assertTrue(
      jsonEquals(expectedResponseNode, actualResponseNode),
      "Response body structure does not match expected"
    );
    if (
      actualResponseNode.has("type") ||
      actualResponseNode.has("_type") ||
      actualResponseNode.has("kind")
    ) {
      String discriminator = null;
      if (actualResponseNode.has("type")) discriminator = actualResponseNode.get("type").asText();
      else if (actualResponseNode.has("_type")) discriminator = actualResponseNode
        .get("_type")
        .asText();
      else if (actualResponseNode.has("kind")) discriminator = actualResponseNode
        .get("kind")
        .asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualResponseNode.isNull()) {
      Assertions.assertTrue(
        actualResponseNode.isObject() ||
        actualResponseNode.isArray() ||
        actualResponseNode.isValueNode(),
        "response should be a valid JSON value"
      );
    }

    if (actualResponseNode.isArray()) {
      Assertions.assertTrue(actualResponseNode.size() >= 0, "Array should have valid size");
    }
    if (actualResponseNode.isObject()) {
      Assertions.assertTrue(actualResponseNode.size() >= 0, "Object should have valid field count");
    }
  }

  @Test
  public void testDocumentThrowsBadRequestError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(400).setBody("{\"key\":\"value\"}"));
    BadRequestError exception = Assertions.assertThrows(BadRequestError.class, () ->
      client
        .lang2Fhir()
        .document(
          DocumentRequest.builder()
            .version("version")
            .resource("resource")
            .content("content")
            .build()
        )
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" +
      "{\n" +
      "  \"version\": \"version\",\n" +
      "  \"resource\": \"resource\",\n" +
      "  \"content\": \"content\"\n" +
      "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      400,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testDocumentThrowsUnauthorizedError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(401).setBody("{\"key\":\"value\"}"));
    UnauthorizedError exception = Assertions.assertThrows(UnauthorizedError.class, () ->
      client
        .lang2Fhir()
        .document(
          DocumentRequest.builder()
            .version("version")
            .resource("resource")
            .content("content")
            .build()
        )
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" +
      "{\n" +
      "  \"version\": \"version\",\n" +
      "  \"resource\": \"resource\",\n" +
      "  \"content\": \"content\"\n" +
      "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      401,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testDocumentThrowsForbiddenError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(403).setBody("{\"key\":\"value\"}"));
    ForbiddenError exception = Assertions.assertThrows(ForbiddenError.class, () ->
      client
        .lang2Fhir()
        .document(
          DocumentRequest.builder()
            .version("version")
            .resource("resource")
            .content("content")
            .build()
        )
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" +
      "{\n" +
      "  \"version\": \"version\",\n" +
      "  \"resource\": \"resource\",\n" +
      "  \"content\": \"content\"\n" +
      "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      403,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testDocumentThrowsNotFoundError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(404).setBody("{\"key\":\"value\"}"));
    NotFoundError exception = Assertions.assertThrows(NotFoundError.class, () ->
      client
        .lang2Fhir()
        .document(
          DocumentRequest.builder()
            .version("version")
            .resource("resource")
            .content("content")
            .build()
        )
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" +
      "{\n" +
      "  \"version\": \"version\",\n" +
      "  \"resource\": \"resource\",\n" +
      "  \"content\": \"content\"\n" +
      "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      404,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testDocumentThrowsUnprocessableEntityError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(422).setBody("{\"key\":\"value\"}"));
    UnprocessableEntityError exception = Assertions.assertThrows(
      UnprocessableEntityError.class,
      () ->
        client
          .lang2Fhir()
          .document(
            DocumentRequest.builder()
              .version("version")
              .resource("resource")
              .content("content")
              .build()
          )
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" +
      "{\n" +
      "  \"version\": \"version\",\n" +
      "  \"resource\": \"resource\",\n" +
      "  \"content\": \"content\"\n" +
      "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      422,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testDocumentThrowsClientClosedRequestError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(499).setBody("{\"key\":\"value\"}"));
    ClientClosedRequestError exception = Assertions.assertThrows(
      ClientClosedRequestError.class,
      () ->
        client
          .lang2Fhir()
          .document(
            DocumentRequest.builder()
              .version("version")
              .resource("resource")
              .content("content")
              .build()
          )
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" +
      "{\n" +
      "  \"version\": \"version\",\n" +
      "  \"resource\": \"resource\",\n" +
      "  \"content\": \"content\"\n" +
      "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      499,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testDocumentThrowsInternalServerError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"key\":\"value\"}"));
    InternalServerError exception = Assertions.assertThrows(InternalServerError.class, () ->
      client
        .lang2Fhir()
        .document(
          DocumentRequest.builder()
            .version("version")
            .resource("resource")
            .content("content")
            .build()
        )
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" +
      "{\n" +
      "  \"version\": \"version\",\n" +
      "  \"resource\": \"resource\",\n" +
      "  \"content\": \"content\"\n" +
      "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      500,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testDocumentThrowsGatewayTimeoutError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(504).setBody("{\"key\":\"value\"}"));
    GatewayTimeoutError exception = Assertions.assertThrows(GatewayTimeoutError.class, () ->
      client
        .lang2Fhir()
        .document(
          DocumentRequest.builder()
            .version("version")
            .resource("resource")
            .content("content")
            .build()
        )
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" +
      "{\n" +
      "  \"version\": \"version\",\n" +
      "  \"resource\": \"resource\",\n" +
      "  \"content\": \"content\"\n" +
      "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      504,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testDocumentMulti() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody(
          TestResources.loadResource(
            "/wire-tests/Lang2FhirWireTest_testDocumentMulti_response.json"
          )
        )
    );
    DocumentMultiResponse response = client
      .lang2Fhir()
      .documentMulti(
        DocumentMultiRequest.builder()
          .version("R4")
          .content("JVBERi0xLjQKJeLjz9MK...(base64-encoded document bytes)")
          .provider("medplum")
          .config(
            DocumentConfig.builder()
              .splitClassifications(
                Optional.of(
                  Arrays.asList(
                    SplitClassification.builder()
                      .id("clinical")
                      .description(
                        "Clinical notes, diagnoses, medications, observations, and patient demographics."
                      )
                      .operation(SplitClassificationOperation.GROUP)
                      .build(),
                    SplitClassification.builder()
                      .id("admin")
                      .description("Administrative boilerplate, insurance forms, and cover sheets.")
                      .operation(SplitClassificationOperation.DROP)
                      .build()
                  )
                )
              )
              .build()
          )
          .build()
      );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" +
      "{\n" +
      "  \"version\": \"R4\",\n" +
      "  \"content\": \"JVBERi0xLjQKJeLjz9MK...(base64-encoded document bytes)\",\n" +
      "  \"provider\": \"medplum\",\n" +
      "  \"config\": {\n" +
      "    \"split_classifications\": [\n" +
      "      {\n" +
      "        \"id\": \"clinical\",\n" +
      "        \"description\": \"Clinical notes, diagnoses, medications, observations, and patient demographics.\",\n" +
      "        \"operation\": \"group\"\n" +
      "      },\n" +
      "      {\n" +
      "        \"id\": \"admin\",\n" +
      "        \"description\": \"Administrative boilerplate, insurance forms, and cover sheets.\",\n" +
      "        \"operation\": \"drop\"\n" +
      "      }\n" +
      "    ]\n" +
      "  }\n" +
      "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
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
    String expectedResponseBody = TestResources.loadResource(
      "/wire-tests/Lang2FhirWireTest_testDocumentMulti_response.json"
    );
    JsonNode actualResponseNode = objectMapper.readTree(actualResponseJson);
    JsonNode expectedResponseNode = objectMapper.readTree(expectedResponseBody);
    Assertions.assertTrue(
      jsonEquals(expectedResponseNode, actualResponseNode),
      "Response body structure does not match expected"
    );
    if (
      actualResponseNode.has("type") ||
      actualResponseNode.has("_type") ||
      actualResponseNode.has("kind")
    ) {
      String discriminator = null;
      if (actualResponseNode.has("type")) discriminator = actualResponseNode.get("type").asText();
      else if (actualResponseNode.has("_type")) discriminator = actualResponseNode
        .get("_type")
        .asText();
      else if (actualResponseNode.has("kind")) discriminator = actualResponseNode
        .get("kind")
        .asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualResponseNode.isNull()) {
      Assertions.assertTrue(
        actualResponseNode.isObject() ||
        actualResponseNode.isArray() ||
        actualResponseNode.isValueNode(),
        "response should be a valid JSON value"
      );
    }

    if (actualResponseNode.isArray()) {
      Assertions.assertTrue(actualResponseNode.size() >= 0, "Array should have valid size");
    }
    if (actualResponseNode.isObject()) {
      Assertions.assertTrue(actualResponseNode.size() >= 0, "Object should have valid field count");
    }
  }

  @Test
  public void testDocumentMultiThrowsBadRequestError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(400).setBody("{\"key\":\"value\"}"));
    BadRequestError exception = Assertions.assertThrows(BadRequestError.class, () ->
      client
        .lang2Fhir()
        .documentMulti(DocumentMultiRequest.builder().version("version").content("content").build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" + "{\n" + "  \"version\": \"version\",\n" + "  \"content\": \"content\"\n" + "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      400,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testDocumentMultiThrowsUnauthorizedError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(401).setBody("{\"key\":\"value\"}"));
    UnauthorizedError exception = Assertions.assertThrows(UnauthorizedError.class, () ->
      client
        .lang2Fhir()
        .documentMulti(DocumentMultiRequest.builder().version("version").content("content").build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" + "{\n" + "  \"version\": \"version\",\n" + "  \"content\": \"content\"\n" + "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      401,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testDocumentMultiThrowsForbiddenError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(403).setBody("{\"key\":\"value\"}"));
    ForbiddenError exception = Assertions.assertThrows(ForbiddenError.class, () ->
      client
        .lang2Fhir()
        .documentMulti(DocumentMultiRequest.builder().version("version").content("content").build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" + "{\n" + "  \"version\": \"version\",\n" + "  \"content\": \"content\"\n" + "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      403,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testDocumentMultiThrowsNotFoundError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(404).setBody("{\"key\":\"value\"}"));
    NotFoundError exception = Assertions.assertThrows(NotFoundError.class, () ->
      client
        .lang2Fhir()
        .documentMulti(DocumentMultiRequest.builder().version("version").content("content").build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" + "{\n" + "  \"version\": \"version\",\n" + "  \"content\": \"content\"\n" + "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      404,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testDocumentMultiThrowsUnprocessableEntityError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(422).setBody("{\"key\":\"value\"}"));
    UnprocessableEntityError exception = Assertions.assertThrows(
      UnprocessableEntityError.class,
      () ->
        client
          .lang2Fhir()
          .documentMulti(
            DocumentMultiRequest.builder().version("version").content("content").build()
          )
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" + "{\n" + "  \"version\": \"version\",\n" + "  \"content\": \"content\"\n" + "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      422,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testDocumentMultiThrowsClientClosedRequestError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(499).setBody("{\"key\":\"value\"}"));
    ClientClosedRequestError exception = Assertions.assertThrows(
      ClientClosedRequestError.class,
      () ->
        client
          .lang2Fhir()
          .documentMulti(
            DocumentMultiRequest.builder().version("version").content("content").build()
          )
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" + "{\n" + "  \"version\": \"version\",\n" + "  \"content\": \"content\"\n" + "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      499,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testDocumentMultiThrowsInternalServerError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"key\":\"value\"}"));
    InternalServerError exception = Assertions.assertThrows(InternalServerError.class, () ->
      client
        .lang2Fhir()
        .documentMulti(DocumentMultiRequest.builder().version("version").content("content").build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" + "{\n" + "  \"version\": \"version\",\n" + "  \"content\": \"content\"\n" + "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      500,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
  }

  @Test
  public void testDocumentMultiThrowsGatewayTimeoutError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(504).setBody("{\"key\":\"value\"}"));
    GatewayTimeoutError exception = Assertions.assertThrows(GatewayTimeoutError.class, () ->
      client
        .lang2Fhir()
        .documentMulti(DocumentMultiRequest.builder().version("version").content("content").build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("POST", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" + "{\n" + "  \"version\": \"version\",\n" + "  \"content\": \"content\"\n" + "}";
    JsonNode actualJson = objectMapper.readTree(actualRequestBody);
    JsonNode expectedJson = objectMapper.readTree(expectedRequestBody);
    Assertions.assertTrue(
      jsonEquals(expectedJson, actualJson),
      "Request body structure does not match expected"
    );
    if (actualJson.has("type") || actualJson.has("_type") || actualJson.has("kind")) {
      String discriminator = null;
      if (actualJson.has("type")) discriminator = actualJson.get("type").asText();
      else if (actualJson.has("_type")) discriminator = actualJson.get("_type").asText();
      else if (actualJson.has("kind")) discriminator = actualJson.get("kind").asText();
      Assertions.assertNotNull(discriminator, "Union type should have a discriminator field");
      Assertions.assertFalse(discriminator.isEmpty(), "Union discriminator should not be empty");
    }

    if (!actualJson.isNull()) {
      Assertions.assertTrue(
        actualJson.isObject() || actualJson.isArray() || actualJson.isValueNode(),
        "request should be a valid JSON value"
      );
    }

    if (actualJson.isArray()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Array should have valid size");
    }
    if (actualJson.isObject()) {
      Assertions.assertTrue(actualJson.size() >= 0, "Object should have valid field count");
    }

    // Validate error response
    Assertions.assertEquals(
      504,
      exception.statusCode(),
      "Error status code does not match expected"
    );
    String actualErrorJson = objectMapper.writeValueAsString(exception.body());
    String expectedErrorBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
    JsonNode actualErrorNode = objectMapper.readTree(actualErrorJson);
    JsonNode expectedErrorNode = objectMapper.readTree(expectedErrorBody);
    Assertions.assertTrue(
      jsonEquals(expectedErrorNode, actualErrorNode),
      "Error body does not match expected"
    );
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
    if (expected.isNumber() && actual.isNumber()) return (
      Math.abs(expected.doubleValue() - actual.doubleValue()) < 1e-10
    );
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
