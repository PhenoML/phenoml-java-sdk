package com.phenoml.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phenoml.api.core.ObjectMappers;
import com.phenoml.api.resources.agent.chat.requests.AgentChatRequest;
import com.phenoml.api.resources.agent.chat.requests.AgentStreamChatRequest;
import com.phenoml.api.resources.agent.chat.requests.ListMessagesRequest;
import com.phenoml.api.resources.agent.chat.types.ListMessagesRequestOrder;
import com.phenoml.api.resources.agent.chat.types.ListMessagesRequestRole;
import com.phenoml.api.resources.agent.chat.types.ListMessagesResponse;
import com.phenoml.api.resources.agent.errors.BadRequestError;
import com.phenoml.api.resources.agent.errors.ConflictError;
import com.phenoml.api.resources.agent.errors.ForbiddenError;
import com.phenoml.api.resources.agent.errors.GatewayTimeoutError;
import com.phenoml.api.resources.agent.errors.InternalServerError;
import com.phenoml.api.resources.agent.errors.NotFoundError;
import com.phenoml.api.resources.agent.errors.UnauthorizedError;
import com.phenoml.api.resources.agent.types.AgentChatResponse;
import com.phenoml.api.resources.agent.types.AgentChatStreamEvent;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AgentChatWireTest {

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
  public void testSend() throws Exception {
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
          "{\"response\":\"Based on the patient records, they have been diagnosed with Type 2 Diabetes Mellitus (ICD-10: E11.65) and Essential Hypertension (ICD-10: I10). Current medications include Metformin 500mg BID and Lisinopril 10mg daily. Most recent HbA1c was 7.2% on 2024-12-15.\",\"success\":true,\"message\":\"Response generated successfully\",\"session_id\":\"session-abc123\"}"
        )
    );
    AgentChatResponse response = client
      .agent()
      .chat()
      .send(
        AgentChatRequest.builder()
          .message("What is the patient's current condition?")
          .agentId("agent-123")
          .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
          .phenomlFhirProvider(
            "550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c..."
          )
          .sessionId("session-abc123")
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

    // Validate headers
    Assertions.assertEquals(
      "Patient/550e8400-e29b-41d4-a716-446655440000",
      request.getHeader("X-Phenoml-On-Behalf-Of"),
      "Header 'X-Phenoml-On-Behalf-Of' should match expected value"
    );
    Assertions.assertEquals(
      "550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...",
      request.getHeader("X-Phenoml-Fhir-Provider"),
      "Header 'X-Phenoml-Fhir-Provider' should match expected value"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" +
      "{\n" +
      "  \"message\": \"What is the patient's current condition?\",\n" +
      "  \"session_id\": \"session-abc123\",\n" +
      "  \"agent_id\": \"agent-123\"\n" +
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
      "  \"response\": \"Based on the patient records, they have been diagnosed with Type 2 Diabetes Mellitus (ICD-10: E11.65) and Essential Hypertension (ICD-10: I10). Current medications include Metformin 500mg BID and Lisinopril 10mg daily. Most recent HbA1c was 7.2% on 2024-12-15.\",\n" +
      "  \"success\": true,\n" +
      "  \"message\": \"Response generated successfully\",\n" +
      "  \"session_id\": \"session-abc123\"\n" +
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
  public void testSendCreateClinicalResources() throws Exception {
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
          "{\"response\":\"Created the following resources for Jane Doe:\\n\\n1. Patient: Jane Doe, 32-year-old female\\n2. Condition: Iron deficiency anemia (ICD-10: D50.9)\\n3. MedicationRequest: Ferrous sulfate 325mg daily\\n\\nAll resources bundled as a FHIR transaction Bundle.\",\"success\":true,\"message\":\"Response generated successfully\",\"session_id\":\"session-def456\"}"
        )
    );
    AgentChatResponse response = client
      .agent()
      .chat()
      .send(
        AgentChatRequest.builder()
          .message(
            "Create a patient record for Jane Doe, 32F, diagnosed with iron deficiency anemia. Prescribe ferrous sulfate 325mg daily."
          )
          .agentId("agent-123")
          .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
          .phenomlFhirProvider(
            "550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c..."
          )
          .sessionId("session-def456")
          .enhancedReasoning(true)
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

    // Validate headers
    Assertions.assertEquals(
      "Patient/550e8400-e29b-41d4-a716-446655440000",
      request.getHeader("X-Phenoml-On-Behalf-Of"),
      "Header 'X-Phenoml-On-Behalf-Of' should match expected value"
    );
    Assertions.assertEquals(
      "550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...",
      request.getHeader("X-Phenoml-Fhir-Provider"),
      "Header 'X-Phenoml-Fhir-Provider' should match expected value"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" +
      "{\n" +
      "  \"message\": \"Create a patient record for Jane Doe, 32F, diagnosed with iron deficiency anemia. Prescribe ferrous sulfate 325mg daily.\",\n" +
      "  \"session_id\": \"session-def456\",\n" +
      "  \"agent_id\": \"agent-123\",\n" +
      "  \"enhanced_reasoning\": true\n" +
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
      "  \"response\": \"Created the following resources for Jane Doe:\\n\\n1. Patient: Jane Doe, 32-year-old female\\n2. Condition: Iron deficiency anemia (ICD-10: D50.9)\\n3. MedicationRequest: Ferrous sulfate 325mg daily\\n\\nAll resources bundled as a FHIR transaction Bundle.\",\n" +
      "  \"success\": true,\n" +
      "  \"message\": \"Response generated successfully\",\n" +
      "  \"session_id\": \"session-def456\"\n" +
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
  public void testSendThrowsBadRequestError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(400).setBody("{\"key\":\"value\"}"));
    BadRequestError exception = Assertions.assertThrows(BadRequestError.class, () ->
      client
        .agent()
        .chat()
        .send(AgentChatRequest.builder().message("message").agentId("agent_id").build())
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
      "" + "{\n" + "  \"message\": \"message\",\n" + "  \"agent_id\": \"agent_id\"\n" + "}";
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
  public void testSendThrowsUnauthorizedError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(401).setBody("{\"key\":\"value\"}"));
    UnauthorizedError exception = Assertions.assertThrows(UnauthorizedError.class, () ->
      client
        .agent()
        .chat()
        .send(AgentChatRequest.builder().message("message").agentId("agent_id").build())
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
      "" + "{\n" + "  \"message\": \"message\",\n" + "  \"agent_id\": \"agent_id\"\n" + "}";
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
  public void testSendThrowsForbiddenError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(403).setBody("{\"key\":\"value\"}"));
    ForbiddenError exception = Assertions.assertThrows(ForbiddenError.class, () ->
      client
        .agent()
        .chat()
        .send(AgentChatRequest.builder().message("message").agentId("agent_id").build())
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
      "" + "{\n" + "  \"message\": \"message\",\n" + "  \"agent_id\": \"agent_id\"\n" + "}";
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
  public void testSendThrowsNotFoundError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(404).setBody("{\"key\":\"value\"}"));
    NotFoundError exception = Assertions.assertThrows(NotFoundError.class, () ->
      client
        .agent()
        .chat()
        .send(AgentChatRequest.builder().message("message").agentId("agent_id").build())
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
      "" + "{\n" + "  \"message\": \"message\",\n" + "  \"agent_id\": \"agent_id\"\n" + "}";
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
  public void testSendThrowsConflictError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(409).setBody("{\"key\":\"value\"}"));
    ConflictError exception = Assertions.assertThrows(ConflictError.class, () ->
      client
        .agent()
        .chat()
        .send(AgentChatRequest.builder().message("message").agentId("agent_id").build())
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
      "" + "{\n" + "  \"message\": \"message\",\n" + "  \"agent_id\": \"agent_id\"\n" + "}";
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
  public void testSendThrowsInternalServerError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"key\":\"value\"}"));
    InternalServerError exception = Assertions.assertThrows(InternalServerError.class, () ->
      client
        .agent()
        .chat()
        .send(AgentChatRequest.builder().message("message").agentId("agent_id").build())
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
      "" + "{\n" + "  \"message\": \"message\",\n" + "  \"agent_id\": \"agent_id\"\n" + "}";
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
  public void testSendThrowsGatewayTimeoutError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(504).setBody("{\"key\":\"value\"}"));
    GatewayTimeoutError exception = Assertions.assertThrows(GatewayTimeoutError.class, () ->
      client
        .agent()
        .chat()
        .send(AgentChatRequest.builder().message("message").agentId("agent_id").build())
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
      "" + "{\n" + "  \"message\": \"message\",\n" + "  \"agent_id\": \"agent_id\"\n" + "}";
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
  public void testStream() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
    Iterable<AgentChatStreamEvent> response = client
      .agent()
      .chat()
      .stream(
        AgentStreamChatRequest.builder()
          .message("What is the patient's current condition?")
          .agentId("agent-123")
          .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
          .phenomlFhirProvider(
            "550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c..."
          )
          .sessionId("session-abc123")
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

    // Validate headers
    Assertions.assertEquals(
      "Patient/550e8400-e29b-41d4-a716-446655440000",
      request.getHeader("X-Phenoml-On-Behalf-Of"),
      "Header 'X-Phenoml-On-Behalf-Of' should match expected value"
    );
    Assertions.assertEquals(
      "550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...",
      request.getHeader("X-Phenoml-Fhir-Provider"),
      "Header 'X-Phenoml-Fhir-Provider' should match expected value"
    );
    // Validate request body
    String actualRequestBody = request.getBody().readUtf8();
    String expectedRequestBody =
      "" +
      "{\n" +
      "  \"message\": \"What is the patient's current condition?\",\n" +
      "  \"session_id\": \"session-abc123\",\n" +
      "  \"agent_id\": \"agent-123\"\n" +
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

    // Validate response deserialization
    Assertions.assertNotNull(response, "Response should not be null");
    // Verify the response can be serialized back to JSON
    String responseJson = objectMapper.writeValueAsString(response);
    Assertions.assertNotNull(responseJson);
    Assertions.assertFalse(responseJson.isEmpty());
  }

  @Test
  public void testStreamThrowsBadRequestError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(400).setBody("{\"key\":\"value\"}"));
    BadRequestError exception = Assertions.assertThrows(BadRequestError.class, () ->
      client
        .agent()
        .chat()
        .stream(AgentStreamChatRequest.builder().message("message").agentId("agent_id").build())
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
      "" + "{\n" + "  \"message\": \"message\",\n" + "  \"agent_id\": \"agent_id\"\n" + "}";
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
  public void testStreamThrowsUnauthorizedError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(401).setBody("{\"key\":\"value\"}"));
    UnauthorizedError exception = Assertions.assertThrows(UnauthorizedError.class, () ->
      client
        .agent()
        .chat()
        .stream(AgentStreamChatRequest.builder().message("message").agentId("agent_id").build())
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
      "" + "{\n" + "  \"message\": \"message\",\n" + "  \"agent_id\": \"agent_id\"\n" + "}";
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
  public void testStreamThrowsForbiddenError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(403).setBody("{\"key\":\"value\"}"));
    ForbiddenError exception = Assertions.assertThrows(ForbiddenError.class, () ->
      client
        .agent()
        .chat()
        .stream(AgentStreamChatRequest.builder().message("message").agentId("agent_id").build())
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
      "" + "{\n" + "  \"message\": \"message\",\n" + "  \"agent_id\": \"agent_id\"\n" + "}";
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
  public void testStreamThrowsNotFoundError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(404).setBody("{\"key\":\"value\"}"));
    NotFoundError exception = Assertions.assertThrows(NotFoundError.class, () ->
      client
        .agent()
        .chat()
        .stream(AgentStreamChatRequest.builder().message("message").agentId("agent_id").build())
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
      "" + "{\n" + "  \"message\": \"message\",\n" + "  \"agent_id\": \"agent_id\"\n" + "}";
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
  public void testStreamThrowsConflictError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(409).setBody("{\"key\":\"value\"}"));
    ConflictError exception = Assertions.assertThrows(ConflictError.class, () ->
      client
        .agent()
        .chat()
        .stream(AgentStreamChatRequest.builder().message("message").agentId("agent_id").build())
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
      "" + "{\n" + "  \"message\": \"message\",\n" + "  \"agent_id\": \"agent_id\"\n" + "}";
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
  public void testStreamThrowsInternalServerError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"key\":\"value\"}"));
    InternalServerError exception = Assertions.assertThrows(InternalServerError.class, () ->
      client
        .agent()
        .chat()
        .stream(AgentStreamChatRequest.builder().message("message").agentId("agent_id").build())
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
      "" + "{\n" + "  \"message\": \"message\",\n" + "  \"agent_id\": \"agent_id\"\n" + "}";
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
  public void testStreamThrowsGatewayTimeoutError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(504).setBody("{\"key\":\"value\"}"));
    GatewayTimeoutError exception = Assertions.assertThrows(GatewayTimeoutError.class, () ->
      client
        .agent()
        .chat()
        .stream(AgentStreamChatRequest.builder().message("message").agentId("agent_id").build())
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
      "" + "{\n" + "  \"message\": \"message\",\n" + "  \"agent_id\": \"agent_id\"\n" + "}";
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
  public void testListMessages() throws Exception {
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
          "{\"messages\":[{\"id\":\"message_001\",\"session_id\":\"session_123\",\"role\":\"user\",\"content\":\"What is the patient's current condition?\",\"created\":\"2025-03-01T14:00:00Z\",\"updated\":\"2025-03-01T14:00:00Z\",\"function_name\":\"get_patient_info\",\"function_args\":{\"patient_id\":\"123\"},\"function_result\":{\"name\":\"John Doe\"},\"message_order\":1},{\"id\":\"message_002\",\"session_id\":\"session_123\",\"role\":\"assistant\",\"content\":\"Based on the patient records, they have been diagnosed with Type 2 Diabetes Mellitus (ICD-10: E11.65).\",\"created\":\"2025-03-01T14:00:02Z\",\"updated\":\"2025-03-01T14:00:02Z\",\"function_name\":\"get_patient_info\",\"function_args\":{\"patient_id\":\"123\"},\"function_result\":{\"name\":\"John Doe\"},\"message_order\":2}],\"total\":2,\"session_id\":\"session_123\"}"
        )
    );
    ListMessagesResponse response = client
      .agent()
      .chat()
      .listMessages(
        ListMessagesRequest.builder()
          .chatSessionId("chat_session_id")
          .numMessages(1)
          .role(ListMessagesRequestRole.USER)
          .order(ListMessagesRequestOrder.ASC)
          .build()
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

    // Validate response body
    Assertions.assertNotNull(response, "Response should not be null");
    String actualResponseJson = objectMapper.writeValueAsString(response);
    String expectedResponseBody =
      "" +
      "{\n" +
      "  \"messages\": [\n" +
      "    {\n" +
      "      \"id\": \"message_001\",\n" +
      "      \"session_id\": \"session_123\",\n" +
      "      \"role\": \"user\",\n" +
      "      \"content\": \"What is the patient's current condition?\",\n" +
      "      \"created\": \"2025-03-01T14:00:00Z\",\n" +
      "      \"updated\": \"2025-03-01T14:00:00Z\",\n" +
      "      \"function_name\": \"get_patient_info\",\n" +
      "      \"function_args\": {\n" +
      "        \"patient_id\": \"123\"\n" +
      "      },\n" +
      "      \"function_result\": {\n" +
      "        \"name\": \"John Doe\"\n" +
      "      },\n" +
      "      \"message_order\": 1\n" +
      "    },\n" +
      "    {\n" +
      "      \"id\": \"message_002\",\n" +
      "      \"session_id\": \"session_123\",\n" +
      "      \"role\": \"assistant\",\n" +
      "      \"content\": \"Based on the patient records, they have been diagnosed with Type 2 Diabetes Mellitus (ICD-10: E11.65).\",\n" +
      "      \"created\": \"2025-03-01T14:00:02Z\",\n" +
      "      \"updated\": \"2025-03-01T14:00:02Z\",\n" +
      "      \"function_name\": \"get_patient_info\",\n" +
      "      \"function_args\": {\n" +
      "        \"patient_id\": \"123\"\n" +
      "      },\n" +
      "      \"function_result\": {\n" +
      "        \"name\": \"John Doe\"\n" +
      "      },\n" +
      "      \"message_order\": 2\n" +
      "    }\n" +
      "  ],\n" +
      "  \"total\": 2,\n" +
      "  \"session_id\": \"session_123\"\n" +
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
  public void testListMessagesThrowsUnauthorizedError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(401).setBody("{\"key\":\"value\"}"));
    UnauthorizedError exception = Assertions.assertThrows(UnauthorizedError.class, () ->
      client
        .agent()
        .chat()
        .listMessages(ListMessagesRequest.builder().chatSessionId("chat_session_id").build())
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
  public void testListMessagesThrowsForbiddenError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(403).setBody("{\"key\":\"value\"}"));
    ForbiddenError exception = Assertions.assertThrows(ForbiddenError.class, () ->
      client
        .agent()
        .chat()
        .listMessages(ListMessagesRequest.builder().chatSessionId("chat_session_id").build())
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
  public void testListMessagesThrowsNotFoundError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(404).setBody("{\"key\":\"value\"}"));
    NotFoundError exception = Assertions.assertThrows(NotFoundError.class, () ->
      client
        .agent()
        .chat()
        .listMessages(ListMessagesRequest.builder().chatSessionId("chat_session_id").build())
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
  public void testListMessagesThrowsInternalServerError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"key\":\"value\"}"));
    InternalServerError exception = Assertions.assertThrows(InternalServerError.class, () ->
      client
        .agent()
        .chat()
        .listMessages(ListMessagesRequest.builder().chatSessionId("chat_session_id").build())
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
  public void testListMessagesThrowsGatewayTimeoutError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(504).setBody("{\"key\":\"value\"}"));
    GatewayTimeoutError exception = Assertions.assertThrows(GatewayTimeoutError.class, () ->
      client
        .agent()
        .chat()
        .listMessages(ListMessagesRequest.builder().chatSessionId("chat_session_id").build())
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
