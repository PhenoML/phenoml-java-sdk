package com.phenoml.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phenoml.api.core.ObjectMappers;
import com.phenoml.api.resources.construe.codes.requests.CodesListRequest;
import com.phenoml.api.resources.construe.codes.requests.CrosswalkRequest;
import com.phenoml.api.resources.construe.codes.requests.ExtractRequest;
import com.phenoml.api.resources.construe.codes.requests.LookupRequest;
import com.phenoml.api.resources.construe.codes.requests.PhenoCrRequest;
import com.phenoml.api.resources.construe.codes.requests.SearchSemanticRequest;
import com.phenoml.api.resources.construe.codes.requests.SearchTextRequest;
import com.phenoml.api.resources.construe.errors.BadRequestError;
import com.phenoml.api.resources.construe.errors.GatewayTimeoutError;
import com.phenoml.api.resources.construe.errors.InternalServerError;
import com.phenoml.api.resources.construe.errors.NotFoundError;
import com.phenoml.api.resources.construe.errors.NotImplementedError;
import com.phenoml.api.resources.construe.errors.ServiceUnavailableError;
import com.phenoml.api.resources.construe.errors.UnauthorizedError;
import com.phenoml.api.resources.construe.types.CrosswalkResponse;
import com.phenoml.api.resources.construe.types.ExtractCodesResult;
import com.phenoml.api.resources.construe.types.ExtractRequestConfig;
import com.phenoml.api.resources.construe.types.ExtractRequestConfigChunkingMethod;
import com.phenoml.api.resources.construe.types.ExtractRequestConfigValidationMethod;
import com.phenoml.api.resources.construe.types.ExtractRequestSystem;
import com.phenoml.api.resources.construe.types.GetCodeResponse;
import com.phenoml.api.resources.construe.types.ListCodesResponse;
import com.phenoml.api.resources.construe.types.PhenocrExtractRequestSystem;
import com.phenoml.api.resources.construe.types.SemanticSearchResponse;
import com.phenoml.api.resources.construe.types.TextSearchResponse;
import java.util.Arrays;
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
      .maxRetries(0)
      .build();
  }

  @AfterEach
  public void teardown() throws Exception {
    server.shutdown();
  }

  @Test
  public void testExtract() throws Exception {
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
          TestResources.loadResource("/wire-tests/ConstrueCodesWireTest_testExtract_response.json")
        )
    );
    ExtractCodesResult response = client
      .construe()
      .codes()
      .extract(
        ExtractRequest.builder()
          .text(
            "Patient is a 14-year-old female, previously healthy, who is here for evaluation of abnormal renal ultrasound with atrophic right kidney."
          )
          .system(ExtractRequestSystem.builder().name("ICD-10-CM").version("2025").build())
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
      "  \"text\": \"Patient is a 14-year-old female, previously healthy, who is here for evaluation of abnormal renal ultrasound with atrophic right kidney.\",\n" +
      "  \"system\": {\n" +
      "    \"name\": \"ICD-10-CM\",\n" +
      "    \"version\": \"2025\"\n" +
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
      "/wire-tests/ConstrueCodesWireTest_testExtract_response.json"
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
  public void testExtractIcd10CmUmls2026Aa() throws Exception {
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
            "/wire-tests/ConstrueCodesWireTest_testExtractIcd10CmUmls2026Aa_response.json"
          )
        )
    );
    ExtractCodesResult response = client
      .construe()
      .codes()
      .extract(
        ExtractRequest.builder()
          .text("Patient has type 2 diabetes mellitus with diabetic chronic kidney disease.")
          .system(ExtractRequestSystem.builder().name("ICD-10-CM").version("umls-2026aa").build())
          .config(
            ExtractRequestConfig.builder()
              .validationMethod(ExtractRequestConfigValidationMethod.SIMPLE)
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
      "  \"text\": \"Patient has type 2 diabetes mellitus with diabetic chronic kidney disease.\",\n" +
      "  \"system\": {\n" +
      "    \"name\": \"ICD-10-CM\",\n" +
      "    \"version\": \"umls-2026aa\"\n" +
      "  },\n" +
      "  \"config\": {\n" +
      "    \"validation_method\": \"simple\"\n" +
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
      "/wire-tests/ConstrueCodesWireTest_testExtractIcd10CmUmls2026Aa_response.json"
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
  public void testExtractWithSourceCitations() throws Exception {
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
            "/wire-tests/ConstrueCodesWireTest_testExtractWithSourceCitations_response.json"
          )
        )
    );
    ExtractCodesResult response = client
      .construe()
      .codes()
      .extract(
        ExtractRequest.builder()
          .text(
            "45-year-old male presents with chest pain radiating to left arm, shortness of breath, and diaphoresis. ECG shows ST elevation in leads II, III, and aVF."
          )
          .system(
            ExtractRequestSystem.builder().name("SNOMED_CT_US_LITE").version("20240901").build()
          )
          .config(
            ExtractRequestConfig.builder()
              .chunkingMethod(ExtractRequestConfigChunkingMethod.SENTENCES)
              .validationMethod(ExtractRequestConfigValidationMethod.SIMPLE)
              .includeRationale(true)
              .includeCitations(true)
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
      "  \"text\": \"45-year-old male presents with chest pain radiating to left arm, shortness of breath, and diaphoresis. ECG shows ST elevation in leads II, III, and aVF.\",\n" +
      "  \"system\": {\n" +
      "    \"name\": \"SNOMED_CT_US_LITE\",\n" +
      "    \"version\": \"20240901\"\n" +
      "  },\n" +
      "  \"config\": {\n" +
      "    \"chunking_method\": \"sentences\",\n" +
      "    \"validation_method\": \"simple\",\n" +
      "    \"include_rationale\": true,\n" +
      "    \"include_citations\": true\n" +
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
      "/wire-tests/ConstrueCodesWireTest_testExtractWithSourceCitations_response.json"
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
  public void testExtractMedicationValidation() throws Exception {
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
            "/wire-tests/ConstrueCodesWireTest_testExtractMedicationValidation_response.json"
          )
        )
    );
    ExtractCodesResult response = client
      .construe()
      .codes()
      .extract(
        ExtractRequest.builder()
          .text(
            "Start Metformin 500mg twice daily for type 2 diabetes. Continue Lisinopril 10mg daily for hypertension. Add Atorvastatin 20mg at bedtime."
          )
          .system(ExtractRequestSystem.builder().name("RXNORM").version("11042024").build())
          .config(
            ExtractRequestConfig.builder()
              .validationMethod(ExtractRequestConfigValidationMethod.MEDICATION_SEARCH)
              .includeRationale(true)
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
      "  \"text\": \"Start Metformin 500mg twice daily for type 2 diabetes. Continue Lisinopril 10mg daily for hypertension. Add Atorvastatin 20mg at bedtime.\",\n" +
      "  \"system\": {\n" +
      "    \"name\": \"RXNORM\",\n" +
      "    \"version\": \"11042024\"\n" +
      "  },\n" +
      "  \"config\": {\n" +
      "    \"validation_method\": \"medication_search\",\n" +
      "    \"include_rationale\": true\n" +
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
      "/wire-tests/ConstrueCodesWireTest_testExtractMedicationValidation_response.json"
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
  public void testExtractWithAncestorCodes() throws Exception {
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
            "/wire-tests/ConstrueCodesWireTest_testExtractWithAncestorCodes_response.json"
          )
        )
    );
    ExtractCodesResult response = client
      .construe()
      .codes()
      .extract(
        ExtractRequest.builder()
          .text(
            "Patient diagnosed with Type 2 diabetes mellitus with diabetic chronic kidney disease, stage 3."
          )
          .system(ExtractRequestSystem.builder().name("ICD-10-CM").version("2025").build())
          .config(
            ExtractRequestConfig.builder()
              .validationMethod(ExtractRequestConfigValidationMethod.SIMPLE)
              .includeAncestors(true)
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
      "  \"text\": \"Patient diagnosed with Type 2 diabetes mellitus with diabetic chronic kidney disease, stage 3.\",\n" +
      "  \"system\": {\n" +
      "    \"name\": \"ICD-10-CM\",\n" +
      "    \"version\": \"2025\"\n" +
      "  },\n" +
      "  \"config\": {\n" +
      "    \"validation_method\": \"simple\",\n" +
      "    \"include_ancestors\": true\n" +
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
      "/wire-tests/ConstrueCodesWireTest_testExtractWithAncestorCodes_response.json"
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
  public void testExtractHpoPhenotypeExtraction() throws Exception {
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
            "/wire-tests/ConstrueCodesWireTest_testExtractHpoPhenotypeExtraction_response.json"
          )
        )
    );
    ExtractCodesResult response = client
      .construe()
      .codes()
      .extract(
        ExtractRequest.builder()
          .text(
            "5-year-old male with seizures, severe intellectual disability, microcephaly, and hypotonia."
          )
          .system(ExtractRequestSystem.builder().name("HPO").version("2025").build())
          .config(
            ExtractRequestConfig.builder()
              .chunkingMethod(ExtractRequestConfigChunkingMethod.FASTHPOCR)
              .validationMethod(ExtractRequestConfigValidationMethod.NONE)
              .includeRationale(false)
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
      "  \"text\": \"5-year-old male with seizures, severe intellectual disability, microcephaly, and hypotonia.\",\n" +
      "  \"system\": {\n" +
      "    \"name\": \"HPO\",\n" +
      "    \"version\": \"2025\"\n" +
      "  },\n" +
      "  \"config\": {\n" +
      "    \"chunking_method\": \"fasthpocr\",\n" +
      "    \"validation_method\": \"none\",\n" +
      "    \"include_rationale\": false\n" +
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
      "/wire-tests/ConstrueCodesWireTest_testExtractHpoPhenotypeExtraction_response.json"
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
  public void testExtractThrowsBadRequestError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(400).setBody("{\"key\":\"value\"}"));
    BadRequestError exception = Assertions.assertThrows(BadRequestError.class, () ->
      client.construe().codes().extract(ExtractRequest.builder().text("text").build())
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
  public void testExtractThrowsUnauthorizedError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(401).setBody("{\"key\":\"value\"}"));
    UnauthorizedError exception = Assertions.assertThrows(UnauthorizedError.class, () ->
      client.construe().codes().extract(ExtractRequest.builder().text("text").build())
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
  public void testExtractThrowsNotFoundError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(404).setBody("{\"key\":\"value\"}"));
    NotFoundError exception = Assertions.assertThrows(NotFoundError.class, () ->
      client.construe().codes().extract(ExtractRequest.builder().text("text").build())
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
  public void testExtractThrowsInternalServerError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"key\":\"value\"}"));
    InternalServerError exception = Assertions.assertThrows(InternalServerError.class, () ->
      client.construe().codes().extract(ExtractRequest.builder().text("text").build())
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
  public void testExtractThrowsServiceUnavailableError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(503).setBody("{\"key\":\"value\"}"));
    ServiceUnavailableError exception = Assertions.assertThrows(ServiceUnavailableError.class, () ->
      client.construe().codes().extract(ExtractRequest.builder().text("text").build())
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

  @Test
  public void testExtractThrowsGatewayTimeoutError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(504).setBody("{\"key\":\"value\"}"));
    GatewayTimeoutError exception = Assertions.assertThrows(GatewayTimeoutError.class, () ->
      client.construe().codes().extract(ExtractRequest.builder().text("text").build())
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
  public void testPhenocr() throws Exception {
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
          "{\"system\":{\"name\":\"SNOMED_CT_US_LITE\",\"version\":\"umls-2026aa\"},\"codes\":[{\"code\":\"195967001\",\"description\":\"Asthma\",\"valid\":true,\"reason\":\"reason\",\"is_ancestor\":true,\"citations\":[{\"text\":\"Patient has type 2 diabetes\",\"begin_offset\":0,\"end_offset\":27}],\"categories\":[{\"uri\":\"HP:0025142\",\"label\":\"Constitutional symptom\"}]}]}"
        )
    );
    ExtractCodesResult response = client
      .construe()
      .codes()
      .phenocr(
        PhenoCrRequest.builder()
          .text(
            "5-year-old male with seizures, severe intellectual disability, microcephaly, and hypotonia."
          )
          .system(PhenocrExtractRequestSystem.builder().name("HPO").version("umls-2026AA").build())
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
      "  \"text\": \"5-year-old male with seizures, severe intellectual disability, microcephaly, and hypotonia.\",\n" +
      "  \"system\": {\n" +
      "    \"name\": \"HPO\",\n" +
      "    \"version\": \"umls-2026AA\"\n" +
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
    String expectedResponseBody =
      "" +
      "{\n" +
      "  \"system\": {\n" +
      "    \"name\": \"SNOMED_CT_US_LITE\",\n" +
      "    \"version\": \"umls-2026aa\"\n" +
      "  },\n" +
      "  \"codes\": [\n" +
      "    {\n" +
      "      \"code\": \"195967001\",\n" +
      "      \"description\": \"Asthma\",\n" +
      "      \"valid\": true,\n" +
      "      \"reason\": \"reason\",\n" +
      "      \"is_ancestor\": true,\n" +
      "      \"citations\": [\n" +
      "        {\n" +
      "          \"text\": \"Patient has type 2 diabetes\",\n" +
      "          \"begin_offset\": 0,\n" +
      "          \"end_offset\": 27\n" +
      "        }\n" +
      "      ],\n" +
      "      \"categories\": [\n" +
      "        {\n" +
      "          \"uri\": \"HP:0025142\",\n" +
      "          \"label\": \"Constitutional symptom\"\n" +
      "        }\n" +
      "      ]\n" +
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
  public void testPhenocrThrowsBadRequestError() throws Exception {
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
        .codes()
        .phenocr(
          PhenoCrRequest.builder()
            .text("text")
            .system(PhenocrExtractRequestSystem.builder().name("name").version("version").build())
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
      "  \"system\": {\n" +
      "    \"name\": \"name\",\n" +
      "    \"version\": \"version\"\n" +
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
  public void testPhenocrThrowsUnauthorizedError() throws Exception {
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
        .codes()
        .phenocr(
          PhenoCrRequest.builder()
            .text("text")
            .system(PhenocrExtractRequestSystem.builder().name("name").version("version").build())
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
      "  \"system\": {\n" +
      "    \"name\": \"name\",\n" +
      "    \"version\": \"version\"\n" +
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
  public void testPhenocrThrowsNotFoundError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(404).setBody("{\"key\":\"value\"}"));
    NotFoundError exception = Assertions.assertThrows(NotFoundError.class, () ->
      client
        .construe()
        .codes()
        .phenocr(
          PhenoCrRequest.builder()
            .text("text")
            .system(PhenocrExtractRequestSystem.builder().name("name").version("version").build())
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
      "  \"system\": {\n" +
      "    \"name\": \"name\",\n" +
      "    \"version\": \"version\"\n" +
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
  public void testPhenocrThrowsInternalServerError() throws Exception {
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
        .codes()
        .phenocr(
          PhenoCrRequest.builder()
            .text("text")
            .system(PhenocrExtractRequestSystem.builder().name("name").version("version").build())
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
      "  \"system\": {\n" +
      "    \"name\": \"name\",\n" +
      "    \"version\": \"version\"\n" +
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
  public void testPhenocrThrowsServiceUnavailableError() throws Exception {
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
        .codes()
        .phenocr(
          PhenoCrRequest.builder()
            .text("text")
            .system(PhenocrExtractRequestSystem.builder().name("name").version("version").build())
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
      "  \"system\": {\n" +
      "    \"name\": \"name\",\n" +
      "    \"version\": \"version\"\n" +
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

  @Test
  public void testPhenocrThrowsGatewayTimeoutError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(504).setBody("{\"key\":\"value\"}"));
    GatewayTimeoutError exception = Assertions.assertThrows(GatewayTimeoutError.class, () ->
      client
        .construe()
        .codes()
        .phenocr(
          PhenoCrRequest.builder()
            .text("text")
            .system(PhenocrExtractRequestSystem.builder().name("name").version("version").build())
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
      "  \"system\": {\n" +
      "    \"name\": \"name\",\n" +
      "    \"version\": \"version\"\n" +
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
  public void testCrosswalk() throws Exception {
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
          "{\"resolved_umls_release\":\"2026AA\",\"system\":\"http://hl7.org/fhir/sid/icd-10-cm\",\"code\":\"A02.24\",\"reason_code\":\"NO_SOURCE_CUI\",\"targets\":[{\"system\":\"http://human-phenotype-ontology.org\",\"reason_code\":\"NO_TARGET_MATCHES\",\"matches\":[{\"code\":\"HP:0005661\",\"display\":\"Salmonella infection\",\"cui\":\"C0036111\",\"suppress\":\"N\"}]}]}"
        )
    );
    CrosswalkResponse response = client
      .construe()
      .codes()
      .crosswalk(
        CrosswalkRequest.builder()
          .system("http://hl7.org/fhir/sid/icd-10-cm")
          .code("A02.24")
          .targets(Arrays.asList("http://human-phenotype-ontology.org"))
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
      "  \"system\": \"http://hl7.org/fhir/sid/icd-10-cm\",\n" +
      "  \"code\": \"A02.24\",\n" +
      "  \"targets\": [\n" +
      "    \"http://human-phenotype-ontology.org\"\n" +
      "  ]\n" +
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
      "  \"resolved_umls_release\": \"2026AA\",\n" +
      "  \"system\": \"http://hl7.org/fhir/sid/icd-10-cm\",\n" +
      "  \"code\": \"A02.24\",\n" +
      "  \"reason_code\": \"NO_SOURCE_CUI\",\n" +
      "  \"targets\": [\n" +
      "    {\n" +
      "      \"system\": \"http://human-phenotype-ontology.org\",\n" +
      "      \"reason_code\": \"NO_TARGET_MATCHES\",\n" +
      "      \"matches\": [\n" +
      "        {\n" +
      "          \"code\": \"HP:0005661\",\n" +
      "          \"display\": \"Salmonella infection\",\n" +
      "          \"cui\": \"C0036111\",\n" +
      "          \"suppress\": \"N\"\n" +
      "        }\n" +
      "      ]\n" +
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
  public void testCrosswalkThrowsBadRequestError() throws Exception {
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
        .codes()
        .crosswalk(
          CrosswalkRequest.builder()
            .system("system")
            .code("code")
            .targets(Arrays.asList("targets", "targets"))
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
      "  \"system\": \"system\",\n" +
      "  \"code\": \"code\",\n" +
      "  \"targets\": [\n" +
      "    \"targets\",\n" +
      "    \"targets\"\n" +
      "  ]\n" +
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
  public void testCrosswalkThrowsUnauthorizedError() throws Exception {
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
        .codes()
        .crosswalk(
          CrosswalkRequest.builder()
            .system("system")
            .code("code")
            .targets(Arrays.asList("targets", "targets"))
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
      "  \"system\": \"system\",\n" +
      "  \"code\": \"code\",\n" +
      "  \"targets\": [\n" +
      "    \"targets\",\n" +
      "    \"targets\"\n" +
      "  ]\n" +
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
  public void testCrosswalkThrowsInternalServerError() throws Exception {
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
        .codes()
        .crosswalk(
          CrosswalkRequest.builder()
            .system("system")
            .code("code")
            .targets(Arrays.asList("targets", "targets"))
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
      "  \"system\": \"system\",\n" +
      "  \"code\": \"code\",\n" +
      "  \"targets\": [\n" +
      "    \"targets\",\n" +
      "    \"targets\"\n" +
      "  ]\n" +
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
          "{\"system\":{\"name\":\"ICD-10-CM\",\"version\":\"2025\"},\"codes\":[{\"code\":\"A00\",\"description\":\"Cholera\",\"definition\":\"definition\"},{\"code\":\"A000\",\"description\":\"Cholera due to Vibrio cholerae 01, biovar cholerae\",\"definition\":\"definition\"},{\"code\":\"A001\",\"description\":\"Cholera due to Vibrio cholerae 01, biovar eltor\",\"definition\":\"definition\"},{\"code\":\"A009\",\"description\":\"Cholera, unspecified\",\"definition\":\"definition\"},{\"code\":\"A01\",\"description\":\"Typhoid and paratyphoid fevers\",\"definition\":\"definition\"}],\"next_cursor\":\"QTAx\",\"has_more\":true}"
        )
    );
    ListCodesResponse response = client
      .construe()
      .codes()
      .list(
        "ICD-10-CM",
        CodesListRequest.builder().version("2025").cursor("cursor").limit(1).build()
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
      "  \"system\": {\n" +
      "    \"name\": \"ICD-10-CM\",\n" +
      "    \"version\": \"2025\"\n" +
      "  },\n" +
      "  \"codes\": [\n" +
      "    {\n" +
      "      \"code\": \"A00\",\n" +
      "      \"description\": \"Cholera\",\n" +
      "      \"definition\": \"definition\"\n" +
      "    },\n" +
      "    {\n" +
      "      \"code\": \"A000\",\n" +
      "      \"description\": \"Cholera due to Vibrio cholerae 01, biovar cholerae\",\n" +
      "      \"definition\": \"definition\"\n" +
      "    },\n" +
      "    {\n" +
      "      \"code\": \"A001\",\n" +
      "      \"description\": \"Cholera due to Vibrio cholerae 01, biovar eltor\",\n" +
      "      \"definition\": \"definition\"\n" +
      "    },\n" +
      "    {\n" +
      "      \"code\": \"A009\",\n" +
      "      \"description\": \"Cholera, unspecified\",\n" +
      "      \"definition\": \"definition\"\n" +
      "    },\n" +
      "    {\n" +
      "      \"code\": \"A01\",\n" +
      "      \"description\": \"Typhoid and paratyphoid fevers\",\n" +
      "      \"definition\": \"definition\"\n" +
      "    }\n" +
      "  ],\n" +
      "  \"next_cursor\": \"QTAx\",\n" +
      "  \"has_more\": true\n" +
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
  public void testListBrowseSnomed() throws Exception {
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
          "{\"system\":{\"name\":\"SNOMED_CT_US_LITE\",\"version\":\"20240901\"},\"codes\":[{\"code\":\"10000006\",\"description\":\"Radiating chest pain (finding)\",\"definition\":\"definition\"},{\"code\":\"10001005\",\"description\":\"Bacterial septicemia (disorder)\",\"definition\":\"definition\"},{\"code\":\"10007009\",\"description\":\"Coffin-Siris syndrome (disorder)\",\"definition\":\"definition\"},{\"code\":\"1001000119102\",\"description\":\"Pulmonary embolism with pulmonary infarction (disorder)\",\"definition\":\"definition\"},{\"code\":\"10017004\",\"description\":\"Occlusal wear of teeth (disorder)\",\"definition\":\"definition\"}],\"next_cursor\":\"MTAwMTcwMDQ\",\"has_more\":true}"
        )
    );
    ListCodesResponse response = client
      .construe()
      .codes()
      .list(
        "ICD-10-CM",
        CodesListRequest.builder().version("2025").cursor("cursor").limit(1).build()
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
      "  \"system\": {\n" +
      "    \"name\": \"SNOMED_CT_US_LITE\",\n" +
      "    \"version\": \"20240901\"\n" +
      "  },\n" +
      "  \"codes\": [\n" +
      "    {\n" +
      "      \"code\": \"10000006\",\n" +
      "      \"description\": \"Radiating chest pain (finding)\",\n" +
      "      \"definition\": \"definition\"\n" +
      "    },\n" +
      "    {\n" +
      "      \"code\": \"10001005\",\n" +
      "      \"description\": \"Bacterial septicemia (disorder)\",\n" +
      "      \"definition\": \"definition\"\n" +
      "    },\n" +
      "    {\n" +
      "      \"code\": \"10007009\",\n" +
      "      \"description\": \"Coffin-Siris syndrome (disorder)\",\n" +
      "      \"definition\": \"definition\"\n" +
      "    },\n" +
      "    {\n" +
      "      \"code\": \"1001000119102\",\n" +
      "      \"description\": \"Pulmonary embolism with pulmonary infarction (disorder)\",\n" +
      "      \"definition\": \"definition\"\n" +
      "    },\n" +
      "    {\n" +
      "      \"code\": \"10017004\",\n" +
      "      \"description\": \"Occlusal wear of teeth (disorder)\",\n" +
      "      \"definition\": \"definition\"\n" +
      "    }\n" +
      "  ],\n" +
      "  \"next_cursor\": \"MTAwMTcwMDQ\",\n" +
      "  \"has_more\": true\n" +
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
      client.construe().codes().list("codesystem", CodesListRequest.builder().build())
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
      client.construe().codes().list("codesystem", CodesListRequest.builder().build())
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
  public void testListThrowsNotFoundError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(404).setBody("{\"key\":\"value\"}"));
    NotFoundError exception = Assertions.assertThrows(NotFoundError.class, () ->
      client.construe().codes().list("codesystem", CodesListRequest.builder().build())
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
  public void testListThrowsInternalServerError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"key\":\"value\"}"));
    InternalServerError exception = Assertions.assertThrows(InternalServerError.class, () ->
      client.construe().codes().list("codesystem", CodesListRequest.builder().build())
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
  public void testLookup() throws Exception {
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
          "{\"system\":{\"name\":\"ICD-10-CM\",\"version\":\"2025\"},\"code\":\"E1165\",\"description\":\"Type 2 diabetes mellitus with hyperglycemia\",\"definition\":\"definition\"}"
        )
    );
    GetCodeResponse response = client
      .construe()
      .codes()
      .lookup("ICD-10-CM", "E1165", LookupRequest.builder().version("version").build());
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
      "  \"system\": {\n" +
      "    \"name\": \"ICD-10-CM\",\n" +
      "    \"version\": \"2025\"\n" +
      "  },\n" +
      "  \"code\": \"E1165\",\n" +
      "  \"description\": \"Type 2 diabetes mellitus with hyperglycemia\",\n" +
      "  \"definition\": \"definition\"\n" +
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
  public void testLookupLookUpSnomedConcept() throws Exception {
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
          "{\"system\":{\"name\":\"SNOMED_CT_US_LITE\",\"version\":\"20240901\"},\"code\":\"73211009\",\"description\":\"Diabetes mellitus (disorder)\",\"definition\":\"definition\"}"
        )
    );
    GetCodeResponse response = client
      .construe()
      .codes()
      .lookup("ICD-10-CM", "E1165", LookupRequest.builder().version("version").build());
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
      "  \"system\": {\n" +
      "    \"name\": \"SNOMED_CT_US_LITE\",\n" +
      "    \"version\": \"20240901\"\n" +
      "  },\n" +
      "  \"code\": \"73211009\",\n" +
      "  \"description\": \"Diabetes mellitus (disorder)\",\n" +
      "  \"definition\": \"definition\"\n" +
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
  public void testLookupThrowsBadRequestError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(400).setBody("{\"key\":\"value\"}"));
    BadRequestError exception = Assertions.assertThrows(BadRequestError.class, () ->
      client.construe().codes().lookup("codesystem", "codeID", LookupRequest.builder().build())
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
  public void testLookupThrowsUnauthorizedError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(401).setBody("{\"key\":\"value\"}"));
    UnauthorizedError exception = Assertions.assertThrows(UnauthorizedError.class, () ->
      client.construe().codes().lookup("codesystem", "codeID", LookupRequest.builder().build())
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
  public void testLookupThrowsNotFoundError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(404).setBody("{\"key\":\"value\"}"));
    NotFoundError exception = Assertions.assertThrows(NotFoundError.class, () ->
      client.construe().codes().lookup("codesystem", "codeID", LookupRequest.builder().build())
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
  public void testLookupThrowsInternalServerError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"key\":\"value\"}"));
    InternalServerError exception = Assertions.assertThrows(InternalServerError.class, () ->
      client.construe().codes().lookup("codesystem", "codeID", LookupRequest.builder().build())
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
  public void testSearchSemantic() throws Exception {
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
          "{\"system\":{\"name\":\"ICD-10-CM\",\"version\":\"2025\"},\"results\":[{\"code\":\"R06.00\",\"description\":\"Dyspnea, unspecified\"},{\"code\":\"R06.01\",\"description\":\"Orthopnea\"},{\"code\":\"G47.33\",\"description\":\"Obstructive sleep apnea\"},{\"code\":\"R06.83\",\"description\":\"Snoring\"},{\"code\":\"J45.20\",\"description\":\"Mild intermittent asthma, uncomplicated\"}]}"
        )
    );
    SemanticSearchResponse response = client
      .construe()
      .codes()
      .searchSemantic(
        "ICD-10-CM",
        SearchSemanticRequest.builder()
          .text("patient has trouble breathing at night and wakes up gasping")
          .version("version")
          .limit(1)
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
      "  \"system\": {\n" +
      "    \"name\": \"ICD-10-CM\",\n" +
      "    \"version\": \"2025\"\n" +
      "  },\n" +
      "  \"results\": [\n" +
      "    {\n" +
      "      \"code\": \"R06.00\",\n" +
      "      \"description\": \"Dyspnea, unspecified\"\n" +
      "    },\n" +
      "    {\n" +
      "      \"code\": \"R06.01\",\n" +
      "      \"description\": \"Orthopnea\"\n" +
      "    },\n" +
      "    {\n" +
      "      \"code\": \"G47.33\",\n" +
      "      \"description\": \"Obstructive sleep apnea\"\n" +
      "    },\n" +
      "    {\n" +
      "      \"code\": \"R06.83\",\n" +
      "      \"description\": \"Snoring\"\n" +
      "    },\n" +
      "    {\n" +
      "      \"code\": \"J45.20\",\n" +
      "      \"description\": \"Mild intermittent asthma, uncomplicated\"\n" +
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
  public void testSearchSemanticDiabetesComplications() throws Exception {
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
          "{\"system\":{\"name\":\"ICD-10-CM\",\"version\":\"2025\"},\"results\":[{\"code\":\"E11.22\",\"description\":\"Type 2 diabetes mellitus with diabetic chronic kidney disease\"},{\"code\":\"E11.311\",\"description\":\"Type 2 diabetes mellitus with unspecified diabetic retinopathy with macular edema\"},{\"code\":\"E11.65\",\"description\":\"Type 2 diabetes mellitus with hyperglycemia\"}]}"
        )
    );
    SemanticSearchResponse response = client
      .construe()
      .codes()
      .searchSemantic(
        "ICD-10-CM",
        SearchSemanticRequest.builder()
          .text("patient has trouble breathing at night and wakes up gasping")
          .version("version")
          .limit(1)
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
      "  \"system\": {\n" +
      "    \"name\": \"ICD-10-CM\",\n" +
      "    \"version\": \"2025\"\n" +
      "  },\n" +
      "  \"results\": [\n" +
      "    {\n" +
      "      \"code\": \"E11.22\",\n" +
      "      \"description\": \"Type 2 diabetes mellitus with diabetic chronic kidney disease\"\n" +
      "    },\n" +
      "    {\n" +
      "      \"code\": \"E11.311\",\n" +
      "      \"description\": \"Type 2 diabetes mellitus with unspecified diabetic retinopathy with macular edema\"\n" +
      "    },\n" +
      "    {\n" +
      "      \"code\": \"E11.65\",\n" +
      "      \"description\": \"Type 2 diabetes mellitus with hyperglycemia\"\n" +
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
  public void testSearchSemanticThrowsBadRequestError() throws Exception {
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
        .codes()
        .searchSemantic("codesystem", SearchSemanticRequest.builder().text("text").build())
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
  public void testSearchSemanticThrowsUnauthorizedError() throws Exception {
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
        .codes()
        .searchSemantic("codesystem", SearchSemanticRequest.builder().text("text").build())
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
  public void testSearchSemanticThrowsNotFoundError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(404).setBody("{\"key\":\"value\"}"));
    NotFoundError exception = Assertions.assertThrows(NotFoundError.class, () ->
      client
        .construe()
        .codes()
        .searchSemantic("codesystem", SearchSemanticRequest.builder().text("text").build())
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
  public void testSearchSemanticThrowsInternalServerError() throws Exception {
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
        .codes()
        .searchSemantic("codesystem", SearchSemanticRequest.builder().text("text").build())
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
  public void testSearchText() throws Exception {
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
          "{\"system\":{\"name\":\"ICD-10-CM\",\"version\":\"2025\"},\"results\":[{\"code\":\"E11.65\",\"description\":\"Type 2 diabetes mellitus with hyperglycemia\"},{\"code\":\"E11.649\",\"description\":\"Type 2 diabetes mellitus with hypoglycemia without coma\"},{\"code\":\"E11.69\",\"description\":\"Type 2 diabetes mellitus with other specified complication\"}],\"found\":3}"
        )
    );
    TextSearchResponse response = client
      .construe()
      .codes()
      .searchText(
        "ICD-10-CM",
        SearchTextRequest.builder().q("E11.65").version("version").limit(1).build()
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
      "  \"system\": {\n" +
      "    \"name\": \"ICD-10-CM\",\n" +
      "    \"version\": \"2025\"\n" +
      "  },\n" +
      "  \"results\": [\n" +
      "    {\n" +
      "      \"code\": \"E11.65\",\n" +
      "      \"description\": \"Type 2 diabetes mellitus with hyperglycemia\"\n" +
      "    },\n" +
      "    {\n" +
      "      \"code\": \"E11.649\",\n" +
      "      \"description\": \"Type 2 diabetes mellitus with hypoglycemia without coma\"\n" +
      "    },\n" +
      "    {\n" +
      "      \"code\": \"E11.69\",\n" +
      "      \"description\": \"Type 2 diabetes mellitus with other specified complication\"\n" +
      "    }\n" +
      "  ],\n" +
      "  \"found\": 3\n" +
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
  public void testSearchTextSearchByDescription() throws Exception {
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
          "{\"system\":{\"name\":\"ICD-10-CM\",\"version\":\"2025\"},\"results\":[{\"code\":\"I10\",\"description\":\"Essential (primary) hypertension\"},{\"code\":\"I11.9\",\"description\":\"Hypertensive heart disease without heart failure\"},{\"code\":\"I12.9\",\"description\":\"Hypertensive chronic kidney disease with stage 1-4 or unspecified CKD\"}],\"found\":3}"
        )
    );
    TextSearchResponse response = client
      .construe()
      .codes()
      .searchText(
        "ICD-10-CM",
        SearchTextRequest.builder().q("E11.65").version("version").limit(1).build()
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
      "  \"system\": {\n" +
      "    \"name\": \"ICD-10-CM\",\n" +
      "    \"version\": \"2025\"\n" +
      "  },\n" +
      "  \"results\": [\n" +
      "    {\n" +
      "      \"code\": \"I10\",\n" +
      "      \"description\": \"Essential (primary) hypertension\"\n" +
      "    },\n" +
      "    {\n" +
      "      \"code\": \"I11.9\",\n" +
      "      \"description\": \"Hypertensive heart disease without heart failure\"\n" +
      "    },\n" +
      "    {\n" +
      "      \"code\": \"I12.9\",\n" +
      "      \"description\": \"Hypertensive chronic kidney disease with stage 1-4 or unspecified CKD\"\n" +
      "    }\n" +
      "  ],\n" +
      "  \"found\": 3\n" +
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
  public void testSearchTextThrowsBadRequestError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(400).setBody("{\"key\":\"value\"}"));
    BadRequestError exception = Assertions.assertThrows(BadRequestError.class, () ->
      client.construe().codes().searchText("codesystem", SearchTextRequest.builder().q("q").build())
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
  public void testSearchTextThrowsUnauthorizedError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(401).setBody("{\"key\":\"value\"}"));
    UnauthorizedError exception = Assertions.assertThrows(UnauthorizedError.class, () ->
      client.construe().codes().searchText("codesystem", SearchTextRequest.builder().q("q").build())
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
  public void testSearchTextThrowsNotFoundError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(404).setBody("{\"key\":\"value\"}"));
    NotFoundError exception = Assertions.assertThrows(NotFoundError.class, () ->
      client.construe().codes().searchText("codesystem", SearchTextRequest.builder().q("q").build())
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
  public void testSearchTextThrowsInternalServerError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"key\":\"value\"}"));
    InternalServerError exception = Assertions.assertThrows(InternalServerError.class, () ->
      client.construe().codes().searchText("codesystem", SearchTextRequest.builder().q("q").build())
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
  public void testSearchTextThrowsNotImplementedError() throws Exception {
    // OAuth: enqueue token response (client fetches token before API call)
    server.enqueue(
      new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\":\"test-token\",\"expires_in\":3600}")
    );
    server.enqueue(new MockResponse().setResponseCode(501).setBody("{\"key\":\"value\"}"));
    NotImplementedError exception = Assertions.assertThrows(NotImplementedError.class, () ->
      client.construe().codes().searchText("codesystem", SearchTextRequest.builder().q("q").build())
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
      501,
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
