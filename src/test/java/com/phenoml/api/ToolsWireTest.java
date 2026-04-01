package com.phenoml.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.phenoml.api.resources.tools.requests.CohortRequest;
import com.phenoml.api.resources.tools.requests.Lang2FhirAndCreateMultiRequest;
import com.phenoml.api.resources.tools.requests.Lang2FhirAndCreateRequest;
import com.phenoml.api.resources.tools.requests.Lang2FhirAndSearchRequest;

public class ToolsWireTest {
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
    public void testCreateFhirResource() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.tools().createFhirResource(
            Lang2FhirAndCreateRequest
                .builder()
                .resource(Lang2FhirAndCreateRequest.Resource.AUTO)
                .text("Patient John Doe has severe asthma with acute exacerbation")
                .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
                .phenomlFhirProvider("550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());
    }
    @Test
    public void testCreateFhirResourcesMulti() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.tools().createFhirResourcesMulti(
            Lang2FhirAndCreateMultiRequest
                .builder()
                .text("John Smith, 45-year-old male, diagnosed with Type 2 Diabetes. Prescribed Metformin 500mg twice daily.")
                .provider("medplum")
                .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
                .phenomlFhirProvider("550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());
    }
    @Test
    public void testSearchFhirResources() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.tools().searchFhirResources(
            Lang2FhirAndSearchRequest
                .builder()
                .text("Find all appointments for patient John Doe next week")
                .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
                .phenomlFhirProvider("550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());
    }
    @Test
    public void testAnalyzeCohort() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.tools().analyzeCohort(
            CohortRequest
                .builder()
                .text("female patients over 20 with diabetes but not hypertension")
                .provider("550e8400-e29b-41d4-a716-446655440000")
                .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
                .phenomlFhirProvider("550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());
    }
}
