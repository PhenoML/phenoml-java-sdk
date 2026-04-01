package com.phenoml.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.phenoml.api.resources.workflows.requests.CreateWorkflowRequest;
import com.phenoml.api.resources.workflows.requests.ExecuteWorkflowRequest;
import com.phenoml.api.resources.workflows.requests.UpdateWorkflowRequest;
import com.phenoml.api.resources.workflows.requests.WorkflowsGetRequest;
import com.phenoml.api.resources.workflows.requests.WorkflowsListRequest;
import java.util.HashMap;

public class WorkflowsWireTest {
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
    public void testList() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.workflows().list(
            WorkflowsListRequest
                .builder()
                .verbose(true)
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("GET", request.getMethod());
    }
    @Test
    public void testCreate() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.workflows().create(
            CreateWorkflowRequest
                .builder()
                .name("Patient Data Mapping Workflow")
                .workflowInstructions("Given diagnosis data, find the patient and create condition record")
                .fhirProviderId(
                    CreateWorkflowRequest.FhirProviderId.of("550e8400-e29b-41d4-a716-446655440000")
                )
                .sampleData(
                    new HashMap<String, Object>() {{
                        put("patient_last_name", "Rippin");
                        put("patient_first_name", "Clay");
                        put("diagnosis_code", "I10");
                    }}
                )
                .verbose(true)
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());
    }
    @Test
    public void testGet() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.workflows().get(
            "id",
            WorkflowsGetRequest
                .builder()
                .verbose(true)
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("GET", request.getMethod());
    }
    @Test
    public void testUpdate() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.workflows().update(
            "id",
            UpdateWorkflowRequest
                .builder()
                .name("Updated Patient Data Mapping Workflow")
                .workflowInstructions("Given diagnosis data, find the patient and create condition record")
                .fhirProviderId(
                    UpdateWorkflowRequest.FhirProviderId.of("550e8400-e29b-41d4-a716-446655440000")
                )
                .sampleData(
                    new HashMap<String, Object>() {{
                        put("patient_last_name", "Smith");
                        put("patient_first_name", "John");
                        put("diagnosis_code", "E11");
                    }}
                )
                .verbose(true)
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("PUT", request.getMethod());
    }
    @Test
    public void testDelete() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.workflows().delete("id");;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("DELETE", request.getMethod());
    }
    @Test
    public void testExecute() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.workflows().execute(
            "id",
            ExecuteWorkflowRequest
                .builder()
                .inputData(
                    new HashMap<String, Object>() {{
                        put("patient_last_name", "Johnson");
                        put("patient_first_name", "Mary");
                        put("diagnosis_code", "M79.3");
                        put("encounter_date", "2024-01-15");
                    }}
                )
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());
    }
}
