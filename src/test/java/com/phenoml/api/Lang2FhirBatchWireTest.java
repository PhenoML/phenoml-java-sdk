package com.phenoml.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phenoml.api.core.ObjectMappers;
import com.phenoml.api.resources.lang2fhirbatch.errors.BadRequestError;
import com.phenoml.api.resources.lang2fhirbatch.errors.ClientClosedRequestError;
import com.phenoml.api.resources.lang2fhirbatch.errors.ConflictError;
import com.phenoml.api.resources.lang2fhirbatch.errors.GatewayTimeoutError;
import com.phenoml.api.resources.lang2fhirbatch.errors.InternalServerError;
import com.phenoml.api.resources.lang2fhirbatch.errors.NotFoundError;
import com.phenoml.api.resources.lang2fhirbatch.errors.UnauthorizedError;
import com.phenoml.api.resources.lang2fhirbatch.requests.CreateBatchRequest;
import com.phenoml.api.resources.lang2fhirbatch.requests.GetRequest;
import com.phenoml.api.resources.lang2fhirbatch.requests.GetResultsRequest;
import com.phenoml.api.resources.lang2fhirbatch.requests.ListRequest;
import com.phenoml.api.resources.lang2fhirbatch.types.BatchJob;
import com.phenoml.api.resources.lang2fhirbatch.types.JobDetailResponse;
import com.phenoml.api.resources.lang2fhirbatch.types.JobListResponse;
import com.phenoml.api.resources.lang2fhirbatch.types.ResultsPageResponse;
import java.util.Map;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Lang2FhirBatchWireTest {

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
  public void testList() throws Exception {
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
          "{\"jobs\":[{\"job_id\":\"l2f_batch_6f1d2c3a-8b4e-4f5a-9c7d-0e1f2a3b4c5d\",\"request_id\":\"submit-2025-09-02-batch-001\",\"status\":\"pending\",\"finalized\":false,\"total_items\":12,\"error\":{\"kind\":\"processing_failed\",\"message\":\"the item could not be converted\"},\"created_at\":\"2024-01-15T09:30:00Z\",\"updated_at\":\"2024-01-15T09:30:00Z\",\"completed_at\":\"2024-01-15T09:30:00Z\",\"expires_at\":\"2024-01-15T09:30:00Z\"}],\"next_cursor\":\"next_cursor\",\"has_more\":false}"
        )
    );
    JobListResponse response = client
      .lang2FhirBatch()
      .list(ListRequest.builder().cursor("cursor").limit(1).build());
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

    // Validate response body
    Assertions.assertNotNull(response, "Response should not be null");
    String actualResponseJson = objectMapper.writeValueAsString(response);
    String expectedResponseBody =
      "" +
      "{\n" +
      "  \"jobs\": [\n" +
      "    {\n" +
      "      \"job_id\": \"l2f_batch_6f1d2c3a-8b4e-4f5a-9c7d-0e1f2a3b4c5d\",\n" +
      "      \"request_id\": \"submit-2025-09-02-batch-001\",\n" +
      "      \"status\": \"pending\",\n" +
      "      \"finalized\": false,\n" +
      "      \"total_items\": 12,\n" +
      "      \"error\": {\n" +
      "        \"kind\": \"processing_failed\",\n" +
      "        \"message\": \"the item could not be converted\"\n" +
      "      },\n" +
      "      \"created_at\": \"2024-01-15T09:30:00Z\",\n" +
      "      \"updated_at\": \"2024-01-15T09:30:00Z\",\n" +
      "      \"completed_at\": \"2024-01-15T09:30:00Z\",\n" +
      "      \"expires_at\": \"2024-01-15T09:30:00Z\"\n" +
      "    }\n" +
      "  ],\n" +
      "  \"next_cursor\": \"next_cursor\",\n" +
      "  \"has_more\": false\n" +
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
  public void testListThrowsBadRequestError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(400).setBody("{\"key\":\"value\"}"));
    BadRequestError exception = Assertions.assertThrows(BadRequestError.class, () ->
      client.lang2FhirBatch().list(ListRequest.builder().build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

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
  public void testListThrowsUnauthorizedError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(401).setBody("{\"key\":\"value\"}"));
    UnauthorizedError exception = Assertions.assertThrows(UnauthorizedError.class, () ->
      client.lang2FhirBatch().list(ListRequest.builder().build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

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
  public void testListThrowsClientClosedRequestError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(499).setBody("{\"key\":\"value\"}"));
    ClientClosedRequestError exception = Assertions.assertThrows(
      ClientClosedRequestError.class,
      () -> client.lang2FhirBatch().list(ListRequest.builder().build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

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
  public void testListThrowsInternalServerError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"key\":\"value\"}"));
    InternalServerError exception = Assertions.assertThrows(InternalServerError.class, () ->
      client.lang2FhirBatch().list(ListRequest.builder().build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

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
  public void testListThrowsGatewayTimeoutError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(504).setBody("{\"key\":\"value\"}"));
    GatewayTimeoutError exception = Assertions.assertThrows(GatewayTimeoutError.class, () ->
      client.lang2FhirBatch().list(ListRequest.builder().build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

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
  public void testCreate() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(
      new MockResponse()
        .setResponseCode(202)
        .setBody(
          "{\"job_id\":\"l2f_batch_6f1d2c3a-8b4e-4f5a-9c7d-0e1f2a3b4c5d\",\"request_id\":\"submit-2025-09-02-batch-001\",\"status\":\"pending\",\"finalized\":false,\"total_items\":12,\"error\":{\"kind\":\"processing_failed\",\"message\":\"the item could not be converted\"},\"created_at\":\"2024-01-15T09:30:00Z\",\"updated_at\":\"2024-01-15T09:30:00Z\",\"completed_at\":\"2024-01-15T09:30:00Z\",\"expires_at\":\"2024-01-15T09:30:00Z\"}"
        )
    );
    BatchJob response = client
      .lang2FhirBatch()
      .create(CreateBatchRequest.builder().requestId("submit-2025-09-02-batch-001").build());
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
      "" + "{\n" + "  \"request_id\": \"submit-2025-09-02-batch-001\"\n" + "}";
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
      "  \"job_id\": \"l2f_batch_6f1d2c3a-8b4e-4f5a-9c7d-0e1f2a3b4c5d\",\n" +
      "  \"request_id\": \"submit-2025-09-02-batch-001\",\n" +
      "  \"status\": \"pending\",\n" +
      "  \"finalized\": false,\n" +
      "  \"total_items\": 12,\n" +
      "  \"error\": {\n" +
      "    \"kind\": \"processing_failed\",\n" +
      "    \"message\": \"the item could not be converted\"\n" +
      "  },\n" +
      "  \"created_at\": \"2024-01-15T09:30:00Z\",\n" +
      "  \"updated_at\": \"2024-01-15T09:30:00Z\",\n" +
      "  \"completed_at\": \"2024-01-15T09:30:00Z\",\n" +
      "  \"expires_at\": \"2024-01-15T09:30:00Z\"\n" +
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
      client.lang2FhirBatch().create(CreateBatchRequest.builder().build())
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
    String expectedRequestBody = "" + "{}";
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
      client.lang2FhirBatch().create(CreateBatchRequest.builder().build())
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
    String expectedRequestBody = "" + "{}";
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
  public void testCreateThrowsClientClosedRequestError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(499).setBody("{\"key\":\"value\"}"));
    ClientClosedRequestError exception = Assertions.assertThrows(
      ClientClosedRequestError.class,
      () -> client.lang2FhirBatch().create(CreateBatchRequest.builder().build())
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
    String expectedRequestBody = "" + "{}";
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
  public void testCreateThrowsInternalServerError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"key\":\"value\"}"));
    InternalServerError exception = Assertions.assertThrows(InternalServerError.class, () ->
      client.lang2FhirBatch().create(CreateBatchRequest.builder().build())
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
    String expectedRequestBody = "" + "{}";
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
  public void testCreateThrowsGatewayTimeoutError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(504).setBody("{\"key\":\"value\"}"));
    GatewayTimeoutError exception = Assertions.assertThrows(GatewayTimeoutError.class, () ->
      client.lang2FhirBatch().create(CreateBatchRequest.builder().build())
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
    String expectedRequestBody = "" + "{}";
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
  public void testFinalize() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(
      new MockResponse()
        .setResponseCode(202)
        .setBody(
          "{\"job_id\":\"l2f_batch_6f1d2c3a-8b4e-4f5a-9c7d-0e1f2a3b4c5d\",\"request_id\":\"submit-2025-09-02-batch-001\",\"status\":\"pending\",\"finalized\":false,\"total_items\":12,\"error\":{\"kind\":\"processing_failed\",\"message\":\"the item could not be converted\"},\"created_at\":\"2024-01-15T09:30:00Z\",\"updated_at\":\"2024-01-15T09:30:00Z\",\"completed_at\":\"2024-01-15T09:30:00Z\",\"expires_at\":\"2024-01-15T09:30:00Z\"}"
        )
    );
    BatchJob response = client.lang2FhirBatch().finalize("job_id");
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

    // Validate response body
    Assertions.assertNotNull(response, "Response should not be null");
    String actualResponseJson = objectMapper.writeValueAsString(response);
    String expectedResponseBody =
      "" +
      "{\n" +
      "  \"job_id\": \"l2f_batch_6f1d2c3a-8b4e-4f5a-9c7d-0e1f2a3b4c5d\",\n" +
      "  \"request_id\": \"submit-2025-09-02-batch-001\",\n" +
      "  \"status\": \"pending\",\n" +
      "  \"finalized\": false,\n" +
      "  \"total_items\": 12,\n" +
      "  \"error\": {\n" +
      "    \"kind\": \"processing_failed\",\n" +
      "    \"message\": \"the item could not be converted\"\n" +
      "  },\n" +
      "  \"created_at\": \"2024-01-15T09:30:00Z\",\n" +
      "  \"updated_at\": \"2024-01-15T09:30:00Z\",\n" +
      "  \"completed_at\": \"2024-01-15T09:30:00Z\",\n" +
      "  \"expires_at\": \"2024-01-15T09:30:00Z\"\n" +
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
  public void testFinalizeThrowsBadRequestError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(400).setBody("{\"key\":\"value\"}"));
    BadRequestError exception = Assertions.assertThrows(BadRequestError.class, () ->
      client.lang2FhirBatch().finalize("job_id")
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
  public void testFinalizeThrowsUnauthorizedError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(401).setBody("{\"key\":\"value\"}"));
    UnauthorizedError exception = Assertions.assertThrows(UnauthorizedError.class, () ->
      client.lang2FhirBatch().finalize("job_id")
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
  public void testFinalizeThrowsNotFoundError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(404).setBody("{\"key\":\"value\"}"));
    NotFoundError exception = Assertions.assertThrows(NotFoundError.class, () ->
      client.lang2FhirBatch().finalize("job_id")
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
  public void testFinalizeThrowsConflictError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(409).setBody("{\"key\":\"value\"}"));
    ConflictError exception = Assertions.assertThrows(ConflictError.class, () ->
      client.lang2FhirBatch().finalize("job_id")
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

    // Validate error response
    Assertions.assertEquals(
      409,
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
  public void testFinalizeThrowsClientClosedRequestError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(499).setBody("{\"key\":\"value\"}"));
    ClientClosedRequestError exception = Assertions.assertThrows(
      ClientClosedRequestError.class,
      () -> client.lang2FhirBatch().finalize("job_id")
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
  public void testFinalizeThrowsInternalServerError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"key\":\"value\"}"));
    InternalServerError exception = Assertions.assertThrows(InternalServerError.class, () ->
      client.lang2FhirBatch().finalize("job_id")
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
  public void testFinalizeThrowsGatewayTimeoutError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(504).setBody("{\"key\":\"value\"}"));
    GatewayTimeoutError exception = Assertions.assertThrows(GatewayTimeoutError.class, () ->
      client.lang2FhirBatch().finalize("job_id")
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
  public void testCancel() throws Exception {
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
          "{\"job_id\":\"l2f_batch_6f1d2c3a-8b4e-4f5a-9c7d-0e1f2a3b4c5d\",\"request_id\":\"submit-2025-09-02-batch-001\",\"status\":\"pending\",\"finalized\":false,\"total_items\":12,\"error\":{\"kind\":\"processing_failed\",\"message\":\"the item could not be converted\"},\"created_at\":\"2024-01-15T09:30:00Z\",\"updated_at\":\"2024-01-15T09:30:00Z\",\"completed_at\":\"2024-01-15T09:30:00Z\",\"expires_at\":\"2024-01-15T09:30:00Z\"}"
        )
    );
    BatchJob response = client.lang2FhirBatch().cancel("job_id");
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

    // Validate response body
    Assertions.assertNotNull(response, "Response should not be null");
    String actualResponseJson = objectMapper.writeValueAsString(response);
    String expectedResponseBody =
      "" +
      "{\n" +
      "  \"job_id\": \"l2f_batch_6f1d2c3a-8b4e-4f5a-9c7d-0e1f2a3b4c5d\",\n" +
      "  \"request_id\": \"submit-2025-09-02-batch-001\",\n" +
      "  \"status\": \"pending\",\n" +
      "  \"finalized\": false,\n" +
      "  \"total_items\": 12,\n" +
      "  \"error\": {\n" +
      "    \"kind\": \"processing_failed\",\n" +
      "    \"message\": \"the item could not be converted\"\n" +
      "  },\n" +
      "  \"created_at\": \"2024-01-15T09:30:00Z\",\n" +
      "  \"updated_at\": \"2024-01-15T09:30:00Z\",\n" +
      "  \"completed_at\": \"2024-01-15T09:30:00Z\",\n" +
      "  \"expires_at\": \"2024-01-15T09:30:00Z\"\n" +
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
  public void testCancelThrowsUnauthorizedError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(401).setBody("{\"key\":\"value\"}"));
    UnauthorizedError exception = Assertions.assertThrows(UnauthorizedError.class, () ->
      client.lang2FhirBatch().cancel("job_id")
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
  public void testCancelThrowsNotFoundError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(404).setBody("{\"key\":\"value\"}"));
    NotFoundError exception = Assertions.assertThrows(NotFoundError.class, () ->
      client.lang2FhirBatch().cancel("job_id")
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
  public void testCancelThrowsConflictError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(409).setBody("{\"key\":\"value\"}"));
    ConflictError exception = Assertions.assertThrows(ConflictError.class, () ->
      client.lang2FhirBatch().cancel("job_id")
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

    // Validate error response
    Assertions.assertEquals(
      409,
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
  public void testCancelThrowsClientClosedRequestError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(499).setBody("{\"key\":\"value\"}"));
    ClientClosedRequestError exception = Assertions.assertThrows(
      ClientClosedRequestError.class,
      () -> client.lang2FhirBatch().cancel("job_id")
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
  public void testCancelThrowsInternalServerError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"key\":\"value\"}"));
    InternalServerError exception = Assertions.assertThrows(InternalServerError.class, () ->
      client.lang2FhirBatch().cancel("job_id")
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
  public void testCancelThrowsGatewayTimeoutError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(504).setBody("{\"key\":\"value\"}"));
    GatewayTimeoutError exception = Assertions.assertThrows(GatewayTimeoutError.class, () ->
      client.lang2FhirBatch().cancel("job_id")
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
  public void testGet() throws Exception {
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
          "{\"job_id\":\"l2f_batch_6f1d2c3a-8b4e-4f5a-9c7d-0e1f2a3b4c5d\",\"request_id\":\"submit-2025-09-02-batch-001\",\"status\":\"pending\",\"finalized\":false,\"total_items\":12,\"error\":{\"kind\":\"processing_failed\",\"message\":\"the item could not be converted\"},\"created_at\":\"2024-01-15T09:30:00Z\",\"updated_at\":\"2024-01-15T09:30:00Z\",\"completed_at\":\"2024-01-15T09:30:00Z\",\"expires_at\":\"2024-01-15T09:30:00Z\",\"counts\":{\"total\":12,\"pending\":3,\"processing\":2,\"succeeded\":6,\"failed\":1},\"items\":[{\"item_id\":\"9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08\",\"id\":\"chart-note-0042\",\"status\":\"pending\",\"attempts\":1,\"detect_retries\":1,\"result_size\":20482,\"error\":{\"kind\":\"processing_failed\",\"message\":\"the item could not be converted\"},\"completed_at\":\"2024-01-15T09:30:00Z\"}],\"next_cursor\":\"next_cursor\",\"has_more\":false}"
        )
    );
    JobDetailResponse response = client
      .lang2FhirBatch()
      .get("job_id", GetRequest.builder().cursor("cursor").limit(1).build());
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

    // Validate response body
    Assertions.assertNotNull(response, "Response should not be null");
    String actualResponseJson = objectMapper.writeValueAsString(response);
    String expectedResponseBody =
      "" +
      "{\n" +
      "  \"job_id\": \"l2f_batch_6f1d2c3a-8b4e-4f5a-9c7d-0e1f2a3b4c5d\",\n" +
      "  \"request_id\": \"submit-2025-09-02-batch-001\",\n" +
      "  \"status\": \"pending\",\n" +
      "  \"finalized\": false,\n" +
      "  \"total_items\": 12,\n" +
      "  \"error\": {\n" +
      "    \"kind\": \"processing_failed\",\n" +
      "    \"message\": \"the item could not be converted\"\n" +
      "  },\n" +
      "  \"created_at\": \"2024-01-15T09:30:00Z\",\n" +
      "  \"updated_at\": \"2024-01-15T09:30:00Z\",\n" +
      "  \"completed_at\": \"2024-01-15T09:30:00Z\",\n" +
      "  \"expires_at\": \"2024-01-15T09:30:00Z\",\n" +
      "  \"counts\": {\n" +
      "    \"total\": 12,\n" +
      "    \"pending\": 3,\n" +
      "    \"processing\": 2,\n" +
      "    \"succeeded\": 6,\n" +
      "    \"failed\": 1\n" +
      "  },\n" +
      "  \"items\": [\n" +
      "    {\n" +
      "      \"item_id\": \"9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08\",\n" +
      "      \"id\": \"chart-note-0042\",\n" +
      "      \"status\": \"pending\",\n" +
      "      \"attempts\": 1,\n" +
      "      \"detect_retries\": 1,\n" +
      "      \"result_size\": 20482,\n" +
      "      \"error\": {\n" +
      "        \"kind\": \"processing_failed\",\n" +
      "        \"message\": \"the item could not be converted\"\n" +
      "      },\n" +
      "      \"completed_at\": \"2024-01-15T09:30:00Z\"\n" +
      "    }\n" +
      "  ],\n" +
      "  \"next_cursor\": \"next_cursor\",\n" +
      "  \"has_more\": false\n" +
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
  public void testGetThrowsBadRequestError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(400).setBody("{\"key\":\"value\"}"));
    BadRequestError exception = Assertions.assertThrows(BadRequestError.class, () ->
      client.lang2FhirBatch().get("job_id", GetRequest.builder().build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

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
  public void testGetThrowsUnauthorizedError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(401).setBody("{\"key\":\"value\"}"));
    UnauthorizedError exception = Assertions.assertThrows(UnauthorizedError.class, () ->
      client.lang2FhirBatch().get("job_id", GetRequest.builder().build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

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
  public void testGetThrowsNotFoundError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(404).setBody("{\"key\":\"value\"}"));
    NotFoundError exception = Assertions.assertThrows(NotFoundError.class, () ->
      client.lang2FhirBatch().get("job_id", GetRequest.builder().build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

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
  public void testGetThrowsClientClosedRequestError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(499).setBody("{\"key\":\"value\"}"));
    ClientClosedRequestError exception = Assertions.assertThrows(
      ClientClosedRequestError.class,
      () -> client.lang2FhirBatch().get("job_id", GetRequest.builder().build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

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
  public void testGetThrowsInternalServerError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"key\":\"value\"}"));
    InternalServerError exception = Assertions.assertThrows(InternalServerError.class, () ->
      client.lang2FhirBatch().get("job_id", GetRequest.builder().build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

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
  public void testGetThrowsGatewayTimeoutError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(504).setBody("{\"key\":\"value\"}"));
    GatewayTimeoutError exception = Assertions.assertThrows(GatewayTimeoutError.class, () ->
      client.lang2FhirBatch().get("job_id", GetRequest.builder().build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

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
  public void testGetResults() throws Exception {
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
          "{\"results\":[{\"item_id\":\"9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08\",\"id\":\"chart-note-0042\",\"status\":\"pending\",\"attempts\":1,\"detect_retries\":1,\"result_size\":20482,\"error\":{\"kind\":\"processing_failed\",\"message\":\"the item could not be converted\"},\"completed_at\":\"2024-01-15T09:30:00Z\"}],\"next_cursor\":\"next_cursor\",\"has_more\":false}"
        )
    );
    ResultsPageResponse response = client
      .lang2FhirBatch()
      .getResults("job_id", GetResultsRequest.builder().cursor("cursor").limit(1).build());
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

    // Validate response body
    Assertions.assertNotNull(response, "Response should not be null");
    String actualResponseJson = objectMapper.writeValueAsString(response);
    String expectedResponseBody =
      "" +
      "{\n" +
      "  \"results\": [\n" +
      "    {\n" +
      "      \"item_id\": \"9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08\",\n" +
      "      \"id\": \"chart-note-0042\",\n" +
      "      \"status\": \"pending\",\n" +
      "      \"attempts\": 1,\n" +
      "      \"detect_retries\": 1,\n" +
      "      \"result_size\": 20482,\n" +
      "      \"error\": {\n" +
      "        \"kind\": \"processing_failed\",\n" +
      "        \"message\": \"the item could not be converted\"\n" +
      "      },\n" +
      "      \"completed_at\": \"2024-01-15T09:30:00Z\"\n" +
      "    }\n" +
      "  ],\n" +
      "  \"next_cursor\": \"next_cursor\",\n" +
      "  \"has_more\": false\n" +
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
  public void testGetResultsThrowsBadRequestError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(400).setBody("{\"key\":\"value\"}"));
    BadRequestError exception = Assertions.assertThrows(BadRequestError.class, () ->
      client.lang2FhirBatch().getResults("job_id", GetResultsRequest.builder().build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

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
  public void testGetResultsThrowsUnauthorizedError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(401).setBody("{\"key\":\"value\"}"));
    UnauthorizedError exception = Assertions.assertThrows(UnauthorizedError.class, () ->
      client.lang2FhirBatch().getResults("job_id", GetResultsRequest.builder().build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

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
  public void testGetResultsThrowsNotFoundError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(404).setBody("{\"key\":\"value\"}"));
    NotFoundError exception = Assertions.assertThrows(NotFoundError.class, () ->
      client.lang2FhirBatch().getResults("job_id", GetResultsRequest.builder().build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

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
  public void testGetResultsThrowsClientClosedRequestError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(499).setBody("{\"key\":\"value\"}"));
    ClientClosedRequestError exception = Assertions.assertThrows(
      ClientClosedRequestError.class,
      () -> client.lang2FhirBatch().getResults("job_id", GetResultsRequest.builder().build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

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
  public void testGetResultsThrowsInternalServerError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"key\":\"value\"}"));
    InternalServerError exception = Assertions.assertThrows(InternalServerError.class, () ->
      client.lang2FhirBatch().getResults("job_id", GetResultsRequest.builder().build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

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
  public void testGetResultsThrowsGatewayTimeoutError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(504).setBody("{\"key\":\"value\"}"));
    GatewayTimeoutError exception = Assertions.assertThrows(GatewayTimeoutError.class, () ->
      client.lang2FhirBatch().getResults("job_id", GetResultsRequest.builder().build())
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

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
  public void testGetResult() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"key\":\"value\"}"));
    Map<String, Object> response = client.lang2FhirBatch().getResult("job_id", "item_id");
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

    // Validate response body
    Assertions.assertNotNull(response, "Response should not be null");
    String actualResponseJson = objectMapper.writeValueAsString(response);
    String expectedResponseBody = "" + "{\n" + "  \"key\": \"value\"\n" + "}";
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
  public void testGetResultThrowsBadRequestError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(400).setBody("{\"key\":\"value\"}"));
    BadRequestError exception = Assertions.assertThrows(BadRequestError.class, () ->
      client.lang2FhirBatch().getResult("job_id", "item_id")
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

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
  public void testGetResultThrowsUnauthorizedError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(401).setBody("{\"key\":\"value\"}"));
    UnauthorizedError exception = Assertions.assertThrows(UnauthorizedError.class, () ->
      client.lang2FhirBatch().getResult("job_id", "item_id")
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

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
  public void testGetResultThrowsNotFoundError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(404).setBody("{\"key\":\"value\"}"));
    NotFoundError exception = Assertions.assertThrows(NotFoundError.class, () ->
      client.lang2FhirBatch().getResult("job_id", "item_id")
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

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
  public void testGetResultThrowsConflictError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(409).setBody("{\"key\":\"value\"}"));
    ConflictError exception = Assertions.assertThrows(ConflictError.class, () ->
      client.lang2FhirBatch().getResult("job_id", "item_id")
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

    // Validate error response
    Assertions.assertEquals(
      409,
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
  public void testGetResultThrowsClientClosedRequestError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(499).setBody("{\"key\":\"value\"}"));
    ClientClosedRequestError exception = Assertions.assertThrows(
      ClientClosedRequestError.class,
      () -> client.lang2FhirBatch().getResult("job_id", "item_id")
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

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
  public void testGetResultThrowsInternalServerError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"key\":\"value\"}"));
    InternalServerError exception = Assertions.assertThrows(InternalServerError.class, () ->
      client.lang2FhirBatch().getResult("job_id", "item_id")
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

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
  public void testGetResultThrowsGatewayTimeoutError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(504).setBody("{\"key\":\"value\"}"));
    GatewayTimeoutError exception = Assertions.assertThrows(GatewayTimeoutError.class, () ->
      client.lang2FhirBatch().getResult("job_id", "item_id")
    );
    // OAuth: consume the token request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    Assertions.assertNotNull(request);
    Assertions.assertEquals("GET", request.getMethod());

    // Validate OAuth Authorization header
    Assertions.assertEquals(
      "Bearer test-token",
      request.getHeader("Authorization"),
      "OAuth Authorization header should contain Bearer token from OAuth flow"
    );

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
