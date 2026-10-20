package com.phenoml.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phenoml.api.core.ObjectMappers;
import com.phenoml.api.resources.construe.errors.BadRequestError;
import com.phenoml.api.resources.construe.errors.InternalServerError;
import com.phenoml.api.resources.construe.errors.ServiceUnavailableError;
import com.phenoml.api.resources.construe.errors.UnauthorizedError;
import com.phenoml.api.resources.construe.requests.FeedbackRequest;
import com.phenoml.api.resources.construe.types.ExtractCodesResult;
import com.phenoml.api.resources.construe.types.ExtractRequestSystem;
import com.phenoml.api.resources.construe.types.ExtractedCodeResult;
import com.phenoml.api.resources.construe.types.FeedbackResponse;
import java.util.Arrays;
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
      .maxRetries(0)
      .build();
  }

  @AfterEach
  public void teardown() throws Exception {
    server.shutdown();
  }

  @Test
  public void testSubmitFeedback() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(201).setBody("{\"id\":\"abc123def456\"}"));
    FeedbackResponse response = client
      .construe()
      .submitFeedback(
        FeedbackRequest.builder()
          .text("Patient has type 2 diabetes with hyperglycemia")
          .receivedResult(
            ExtractCodesResult.builder()
              .system(ExtractRequestSystem.builder().name("ICD-10-CM").version("2025").build())
              .codes(
                Arrays.asList(
                  ExtractedCodeResult.builder()
                    .code("E11.9")
                    .description("Type 2 diabetes mellitus without complications")
                    .valid(true)
                    .build()
                )
              )
              .build()
          )
          .expectedResult(
            ExtractCodesResult.builder()
              .system(ExtractRequestSystem.builder().name("ICD-10-CM").version("2025").build())
              .codes(
                Arrays.asList(
                  ExtractedCodeResult.builder()
                    .code("E11.65")
                    .description("Type 2 diabetes mellitus with hyperglycemia")
                    .valid(true)
                    .build()
                )
              )
              .build()
          )
          .detail("Expected code E11.65 because the text mentions hyperglycemia")
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
      "  \"text\": \"Patient has type 2 diabetes with hyperglycemia\",\n" +
      "  \"received_result\": {\n" +
      "    \"system\": {\n" +
      "      \"name\": \"ICD-10-CM\",\n" +
      "      \"version\": \"2025\"\n" +
      "    },\n" +
      "    \"codes\": [\n" +
      "      {\n" +
      "        \"code\": \"E11.9\",\n" +
      "        \"description\": \"Type 2 diabetes mellitus without complications\",\n" +
      "        \"valid\": true\n" +
      "      }\n" +
      "    ]\n" +
      "  },\n" +
      "  \"expected_result\": {\n" +
      "    \"system\": {\n" +
      "      \"name\": \"ICD-10-CM\",\n" +
      "      \"version\": \"2025\"\n" +
      "    },\n" +
      "    \"codes\": [\n" +
      "      {\n" +
      "        \"code\": \"E11.65\",\n" +
      "        \"description\": \"Type 2 diabetes mellitus with hyperglycemia\",\n" +
      "        \"valid\": true\n" +
      "      }\n" +
      "    ]\n" +
      "  },\n" +
      "  \"detail\": \"Expected code E11.65 because the text mentions hyperglycemia\"\n" +
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
    String expectedResponseBody = "" + "{\n" + "  \"id\": \"abc123def456\"\n" + "}";
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
  public void testSubmitFeedbackThrowsBadRequestError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(400).setBody("{\"key\":\"value\"}"));
    BadRequestError exception = Assertions.assertThrows(BadRequestError.class, () ->
      client
        .construe()
        .submitFeedback(
          FeedbackRequest.builder()
            .text("text")
            .receivedResult(
              ExtractCodesResult.builder()
                .system(ExtractRequestSystem.builder().build())
                .codes(
                  Arrays.asList(
                    ExtractedCodeResult.builder()
                      .code("code")
                      .description("description")
                      .valid(true)
                      .build(),
                    ExtractedCodeResult.builder()
                      .code("code")
                      .description("description")
                      .valid(true)
                      .build()
                  )
                )
                .build()
            )
            .expectedResult(
              ExtractCodesResult.builder()
                .system(ExtractRequestSystem.builder().build())
                .codes(
                  Arrays.asList(
                    ExtractedCodeResult.builder()
                      .code("code")
                      .description("description")
                      .valid(true)
                      .build(),
                    ExtractedCodeResult.builder()
                      .code("code")
                      .description("description")
                      .valid(true)
                      .build()
                  )
                )
                .build()
            )
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
      "  \"text\": \"text\",\n" +
      "  \"received_result\": {\n" +
      "    \"system\": {},\n" +
      "    \"codes\": [\n" +
      "      {\n" +
      "        \"code\": \"code\",\n" +
      "        \"description\": \"description\",\n" +
      "        \"valid\": true\n" +
      "      },\n" +
      "      {\n" +
      "        \"code\": \"code\",\n" +
      "        \"description\": \"description\",\n" +
      "        \"valid\": true\n" +
      "      }\n" +
      "    ]\n" +
      "  },\n" +
      "  \"expected_result\": {\n" +
      "    \"system\": {},\n" +
      "    \"codes\": [\n" +
      "      {\n" +
      "        \"code\": \"code\",\n" +
      "        \"description\": \"description\",\n" +
      "        \"valid\": true\n" +
      "      },\n" +
      "      {\n" +
      "        \"code\": \"code\",\n" +
      "        \"description\": \"description\",\n" +
      "        \"valid\": true\n" +
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
  public void testSubmitFeedbackThrowsUnauthorizedError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(401).setBody("{\"key\":\"value\"}"));
    UnauthorizedError exception = Assertions.assertThrows(UnauthorizedError.class, () ->
      client
        .construe()
        .submitFeedback(
          FeedbackRequest.builder()
            .text("text")
            .receivedResult(
              ExtractCodesResult.builder()
                .system(ExtractRequestSystem.builder().build())
                .codes(
                  Arrays.asList(
                    ExtractedCodeResult.builder()
                      .code("code")
                      .description("description")
                      .valid(true)
                      .build(),
                    ExtractedCodeResult.builder()
                      .code("code")
                      .description("description")
                      .valid(true)
                      .build()
                  )
                )
                .build()
            )
            .expectedResult(
              ExtractCodesResult.builder()
                .system(ExtractRequestSystem.builder().build())
                .codes(
                  Arrays.asList(
                    ExtractedCodeResult.builder()
                      .code("code")
                      .description("description")
                      .valid(true)
                      .build(),
                    ExtractedCodeResult.builder()
                      .code("code")
                      .description("description")
                      .valid(true)
                      .build()
                  )
                )
                .build()
            )
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
      "  \"text\": \"text\",\n" +
      "  \"received_result\": {\n" +
      "    \"system\": {},\n" +
      "    \"codes\": [\n" +
      "      {\n" +
      "        \"code\": \"code\",\n" +
      "        \"description\": \"description\",\n" +
      "        \"valid\": true\n" +
      "      },\n" +
      "      {\n" +
      "        \"code\": \"code\",\n" +
      "        \"description\": \"description\",\n" +
      "        \"valid\": true\n" +
      "      }\n" +
      "    ]\n" +
      "  },\n" +
      "  \"expected_result\": {\n" +
      "    \"system\": {},\n" +
      "    \"codes\": [\n" +
      "      {\n" +
      "        \"code\": \"code\",\n" +
      "        \"description\": \"description\",\n" +
      "        \"valid\": true\n" +
      "      },\n" +
      "      {\n" +
      "        \"code\": \"code\",\n" +
      "        \"description\": \"description\",\n" +
      "        \"valid\": true\n" +
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
  public void testSubmitFeedbackThrowsInternalServerError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"key\":\"value\"}"));
    InternalServerError exception = Assertions.assertThrows(InternalServerError.class, () ->
      client
        .construe()
        .submitFeedback(
          FeedbackRequest.builder()
            .text("text")
            .receivedResult(
              ExtractCodesResult.builder()
                .system(ExtractRequestSystem.builder().build())
                .codes(
                  Arrays.asList(
                    ExtractedCodeResult.builder()
                      .code("code")
                      .description("description")
                      .valid(true)
                      .build(),
                    ExtractedCodeResult.builder()
                      .code("code")
                      .description("description")
                      .valid(true)
                      .build()
                  )
                )
                .build()
            )
            .expectedResult(
              ExtractCodesResult.builder()
                .system(ExtractRequestSystem.builder().build())
                .codes(
                  Arrays.asList(
                    ExtractedCodeResult.builder()
                      .code("code")
                      .description("description")
                      .valid(true)
                      .build(),
                    ExtractedCodeResult.builder()
                      .code("code")
                      .description("description")
                      .valid(true)
                      .build()
                  )
                )
                .build()
            )
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
      "  \"text\": \"text\",\n" +
      "  \"received_result\": {\n" +
      "    \"system\": {},\n" +
      "    \"codes\": [\n" +
      "      {\n" +
      "        \"code\": \"code\",\n" +
      "        \"description\": \"description\",\n" +
      "        \"valid\": true\n" +
      "      },\n" +
      "      {\n" +
      "        \"code\": \"code\",\n" +
      "        \"description\": \"description\",\n" +
      "        \"valid\": true\n" +
      "      }\n" +
      "    ]\n" +
      "  },\n" +
      "  \"expected_result\": {\n" +
      "    \"system\": {},\n" +
      "    \"codes\": [\n" +
      "      {\n" +
      "        \"code\": \"code\",\n" +
      "        \"description\": \"description\",\n" +
      "        \"valid\": true\n" +
      "      },\n" +
      "      {\n" +
      "        \"code\": \"code\",\n" +
      "        \"description\": \"description\",\n" +
      "        \"valid\": true\n" +
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
  public void testSubmitFeedbackThrowsServiceUnavailableError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(503).setBody("{\"key\":\"value\"}"));
    ServiceUnavailableError exception = Assertions.assertThrows(ServiceUnavailableError.class, () ->
      client
        .construe()
        .submitFeedback(
          FeedbackRequest.builder()
            .text("text")
            .receivedResult(
              ExtractCodesResult.builder()
                .system(ExtractRequestSystem.builder().build())
                .codes(
                  Arrays.asList(
                    ExtractedCodeResult.builder()
                      .code("code")
                      .description("description")
                      .valid(true)
                      .build(),
                    ExtractedCodeResult.builder()
                      .code("code")
                      .description("description")
                      .valid(true)
                      .build()
                  )
                )
                .build()
            )
            .expectedResult(
              ExtractCodesResult.builder()
                .system(ExtractRequestSystem.builder().build())
                .codes(
                  Arrays.asList(
                    ExtractedCodeResult.builder()
                      .code("code")
                      .description("description")
                      .valid(true)
                      .build(),
                    ExtractedCodeResult.builder()
                      .code("code")
                      .description("description")
                      .valid(true)
                      .build()
                  )
                )
                .build()
            )
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
      "  \"text\": \"text\",\n" +
      "  \"received_result\": {\n" +
      "    \"system\": {},\n" +
      "    \"codes\": [\n" +
      "      {\n" +
      "        \"code\": \"code\",\n" +
      "        \"description\": \"description\",\n" +
      "        \"valid\": true\n" +
      "      },\n" +
      "      {\n" +
      "        \"code\": \"code\",\n" +
      "        \"description\": \"description\",\n" +
      "        \"valid\": true\n" +
      "      }\n" +
      "    ]\n" +
      "  },\n" +
      "  \"expected_result\": {\n" +
      "    \"system\": {},\n" +
      "    \"codes\": [\n" +
      "      {\n" +
      "        \"code\": \"code\",\n" +
      "        \"description\": \"description\",\n" +
      "        \"valid\": true\n" +
      "      },\n" +
      "      {\n" +
      "        \"code\": \"code\",\n" +
      "        \"description\": \"description\",\n" +
      "        \"valid\": true\n" +
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

    // Validate error response
    Assertions.assertEquals(
      503,
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
