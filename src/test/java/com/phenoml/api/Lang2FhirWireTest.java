package com.phenoml.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.phenoml.api.resources.lang2fhir.requests.CreateMultiRequest;
import com.phenoml.api.resources.lang2fhir.requests.CreateRequest;
import com.phenoml.api.resources.lang2fhir.requests.DocumentMultiRequest;
import com.phenoml.api.resources.lang2fhir.requests.DocumentRequest;
import com.phenoml.api.resources.lang2fhir.requests.ProfileUploadRequest;
import com.phenoml.api.resources.lang2fhir.requests.SearchRequest;

public class Lang2FhirWireTest {
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
    public void testCreate() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.lang2Fhir().create(
            CreateRequest
                .builder()
                .version("R4")
                .resource(CreateRequest.Resource.AUTO)
                .text("Patient has severe asthma with acute exacerbation")
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());
    }
    @Test
    public void testCreateMulti() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.lang2Fhir().createMulti(
            CreateMultiRequest
                .builder()
                .text("John Smith, 45-year-old male, diagnosed with Type 2 Diabetes. Prescribed Metformin 500mg twice daily.")
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());
    }
    @Test
    public void testSearch() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.lang2Fhir().search(
            SearchRequest
                .builder()
                .text("Appointments between March 2-9, 2025")
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());
    }
    @Test
    public void testUploadProfile() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.lang2Fhir().uploadProfile(
            ProfileUploadRequest
                .builder()
                .profile("(base64 encoded FHIR StructureDefinition JSON)")
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());
    }
    @Test
    public void testDocument() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.lang2Fhir().document(
            DocumentRequest
                .builder()
                .version("R4")
                .resource("questionnaire")
                .content("content")
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());
    }
    @Test
    public void testExtractMultipleFhirResourcesFromADocument() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.lang2Fhir().extractMultipleFhirResourcesFromADocument(
            DocumentMultiRequest
                .builder()
                .version("R4")
                .content("content")
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());
    }
}
