package com.phenoml.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.phenoml.api.resources.summary.requests.CreateSummaryRequest;
import com.phenoml.api.resources.summary.requests.CreateSummaryTemplateRequest;
import com.phenoml.api.resources.summary.requests.UpdateSummaryTemplateRequest;
import com.phenoml.api.resources.summary.types.FhirResource;
import java.util.ArrayList;
import java.util.Arrays;

public class SummaryWireTest {
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
    public void testListTemplates() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.summary().listTemplates();;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("GET", request.getMethod());
    }
    @Test
    public void testCreateTemplate() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.summary().createTemplate(
            CreateSummaryTemplateRequest
                .builder()
                .name("name")
                .exampleSummary("Patient John Doe, age 45, presents with hypertension diagnosed on 2024-01-15.")
                .mode("mode")
                .targetResources(
                    new ArrayList<String>(
                        Arrays.asList("Patient", "Condition", "Observation")
                    )
                )
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());
    }
    @Test
    public void testGetTemplate() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.summary().getTemplate("id");;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("GET", request.getMethod());
    }
    @Test
    public void testUpdateTemplate() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.summary().updateTemplate(
            "id",
            UpdateSummaryTemplateRequest
                .builder()
                .name("name")
                .template("template")
                .mode("mode")
                .targetResources(
                    new ArrayList<String>(
                        Arrays.asList("target_resources")
                    )
                )
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("PUT", request.getMethod());
    }
    @Test
    public void testDeleteTemplate() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.summary().deleteTemplate("id");;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("DELETE", request.getMethod());
    }
    @Test
    public void testCreate() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.summary().create(
            CreateSummaryRequest
                .builder()
                .fhirResources(
                    CreateSummaryRequest.FhirResources.of(
                        FhirResource
                            .builder()
                            .resourceType("resourceType")
                            .build()
                    )
                )
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());
    }
}
