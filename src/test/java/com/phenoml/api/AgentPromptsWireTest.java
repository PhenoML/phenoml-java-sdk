package com.phenoml.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.phenoml.api.resources.agent.prompts.requests.AgentPromptsCreateRequest;
import com.phenoml.api.resources.agent.prompts.requests.AgentPromptsUpdateRequest;
import com.phenoml.api.resources.agent.types.JsonPatchOperation;
import java.util.ArrayList;
import java.util.Arrays;

public class AgentPromptsWireTest {
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
        client.agent().prompts().create(
            AgentPromptsCreateRequest
                .builder()
                .name("Medical Assistant System Prompt")
                .content("You are a helpful medical assistant specialized in FHIR data processing...")
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());
    }
    @Test
    public void testList() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.agent().prompts().list();;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("GET", request.getMethod());
    }
    @Test
    public void testGet() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.agent().prompts().get("id");;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("GET", request.getMethod());
    }
    @Test
    public void testUpdate() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.agent().prompts().update(
            "id",
            AgentPromptsUpdateRequest
                .builder()
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
        client.agent().prompts().delete("id");;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("DELETE", request.getMethod());
    }
    @Test
    public void testPatch() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.agent().prompts().patch(
            "id",
            new ArrayList<JsonPatchOperation>(
                Arrays.asList(
                    JsonPatchOperation
                        .builder()
                        .op(JsonPatchOperation.Op.REPLACE)
                        .path("/name")
                        .value("Updated Agent Name")
                        .build(),
                    JsonPatchOperation
                        .builder()
                        .op(JsonPatchOperation.Op.ADD)
                        .path("/tags/-")
                        .value("new-tag")
                        .build(),
                    JsonPatchOperation
                        .builder()
                        .op(JsonPatchOperation.Op.REMOVE)
                        .path("/description")
                        .build()
                )
            )
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("PATCH", request.getMethod());
    }
    @Test
    public void testLoadDefaults() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.agent().prompts().loadDefaults();;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());
    }
}
