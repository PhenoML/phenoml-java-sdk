package com.phenoml.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import com.phenoml.api.resources.construe.types.ExtractCodesResult;
import com.phenoml.api.resources.construe.types.ExtractRequestSystem;
import com.phenoml.api.resources.construe.types.ExtractedCodeResult;
import java.util.ArrayList;
import java.util.Arrays;

public class ConstrueWireTest {
    private MockWebServer server;
    private PhenomlClient client;
    private ObjectMapper objectMapper = new ObjectMapper();
    @BeforeEach
    public void setup() throws Exception {
        server = new MockWebServer();
        server.start();
        client = PhenomlClient.builder()
            .url(server.url("/").toString())
            .addHeader("Authorization", "Bearer test-token")
            .build();
    }
    @AfterEach
    public void teardown() throws Exception {
        server.shutdown();
    }
    @Test
    public void testUploadCodeSystem() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.construe().uploadCodeSystem(
            UploadRequest
                .builder()
                .name("CUSTOM_CODES")
                .version("1.0")
                .format(UploadRequest.Format.CSV)
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());
    }
    @Test
    public void testExtractCodes() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.construe().extractCodes(
            ExtractRequest
                .builder()
                .text("Patient is a 14-year-old female, previously healthy, who is here for evaluation of abnormal renal ultrasound with atrophic right kidney")
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());
    }
    @Test
    public void testListAvailableCodeSystems() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.construe().listAvailableCodeSystems();;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("GET", request.getMethod());
    }
    @Test
    public void testGetCodeSystemDetail() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.construe().getCodeSystemDetail(
            "ICD-10-CM",
            GetConstrueCodesSystemsCodesystemRequest
                .builder()
                .version("2025")
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("GET", request.getMethod());
    }
    @Test
    public void testDeleteCustomCodeSystem() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.construe().deleteCustomCodeSystem(
            "CUSTOM_CODES",
            DeleteConstrueCodesSystemsCodesystemRequest
                .builder()
                .version("version")
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("DELETE", request.getMethod());
    }
    @Test
    public void testExportCustomCodeSystem() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.construe().exportCustomCodeSystem(
            "CUSTOM_CODES",
            GetConstrueCodesSystemsCodesystemExportRequest
                .builder()
                .version("version")
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("GET", request.getMethod());
    }
    @Test
    public void testListCodesInACodeSystem() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.construe().listCodesInACodeSystem(
            "ICD-10-CM",
            GetConstrueCodesCodesystemRequest
                .builder()
                .version("2025")
                .cursor("cursor")
                .limit(1)
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("GET", request.getMethod());
    }
    @Test
    public void testGetASpecificCode() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.construe().getASpecificCode(
            "ICD-10-CM",
            "E11.65",
            GetConstrueCodesCodesystemCodeIdRequest
                .builder()
                .version("version")
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("GET", request.getMethod());
    }
    @Test
    public void testSemanticSearchEmbeddingBased() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.construe().semanticSearchEmbeddingBased(
            "ICD-10-CM",
            GetConstrueCodesCodesystemSearchSemanticRequest
                .builder()
                .text("patient has trouble breathing at night and wakes up gasping")
                .version("version")
                .limit(1)
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("GET", request.getMethod());
    }
    @Test
    public void testSubmitFeedbackOnExtractionResults() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.construe().submitFeedbackOnExtractionResults(
            FeedbackRequest
                .builder()
                .text("Patient has type 2 diabetes with hyperglycemia")
                .receivedResult(
                    ExtractCodesResult
                        .builder()
                        .system(
                            ExtractRequestSystem
                                .builder()
                                .build()
                        )
                        .codes(
                            new ArrayList<ExtractedCodeResult>(
                                Arrays.asList(
                                    ExtractedCodeResult
                                        .builder()
                                        .code("195967001")
                                        .description("Asthma")
                                        .valid(true)
                                        .build()
                                )
                            )
                        )
                        .build()
                )
                .expectedResult(
                    ExtractCodesResult
                        .builder()
                        .system(
                            ExtractRequestSystem
                                .builder()
                                .build()
                        )
                        .codes(
                            new ArrayList<ExtractedCodeResult>(
                                Arrays.asList(
                                    ExtractedCodeResult
                                        .builder()
                                        .code("195967001")
                                        .description("Asthma")
                                        .valid(true)
                                        .build()
                                )
                            )
                        )
                        .build()
                )
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());
    }
    @Test
    public void testTerminologyServerTextSearch() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.construe().terminologyServerTextSearch(
            "ICD-10-CM",
            GetConstrueCodesCodesystemSearchTextRequest
                .builder()
                .q("E11.65")
                .version("version")
                .limit(1)
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("GET", request.getMethod());
    }
}
