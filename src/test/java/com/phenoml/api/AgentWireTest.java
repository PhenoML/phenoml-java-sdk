package com.phenoml.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AgentWireTest {
    private MockWebServer server;
    private PhenomlClient client;
    private ObjectMapper objectMapper = new ObjectMapper();
    @BeforeEach
    public void setup() throws Exception {
        server = new MockWebServer();
        server.start();
        client = PhenomlClient.builder()
            .url(server.url("/").toString())
            .token("test-token")
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
        client.agent().create(
            AgentCreateRequest
                .builder()
                .name("name")
                .prompts(
                    new ArrayList<String>(
                        Arrays.asList("prompt_123", "prompt_456")
                    )
                )
                .provider(
                    AgentCreateRequestProvider.of("provider")
                )
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
        client.agent().list(
            AgentListRequest
                .builder()
                .tags("tags")
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("GET", request.getMethod());
    }
    @Test
    public void testGet() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.agent().get("id");;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("GET", request.getMethod());
    }
    @Test
    public void testUpdate() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.agent().update(
            "id",
            AgentCreateRequest
                .builder()
                .name("name")
                .prompts(
                    new ArrayList<String>(
                        Arrays.asList("prompt_123", "prompt_456")
                    )
                )
                .provider(
                    AgentCreateRequestProvider.of("provider")
                )
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
        client.agent().delete("id");;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("DELETE", request.getMethod());
    }
    @Test
    public void testPatch() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.agent().patch(
            "id",
            new ArrayList<JsonPatchOperation>(
                Arrays.asList(
                    JsonPatchOperation
                        .builder()
                        .op(JsonPatchOperationOp.REPLACE)
                        .path("/name")
                        .value("Updated Agent Name")
                        .build(),
                    JsonPatchOperation
                        .builder()
                        .op(JsonPatchOperationOp.ADD)
                        .path("/tags/-")
                        .value("new-tag")
                        .build(),
                    JsonPatchOperation
                        .builder()
                        .op(JsonPatchOperationOp.REMOVE)
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
    public void testChat() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.agent().chat(
            AgentChatRequest
                .builder()
                .message("What is the patient's current condition?")
                .agentId("agent-123")
                .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
                .phenomlFhirProvider("550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());
    }
    @Test
    public void testStreamChat() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.agent().streamChat(
            AgentStreamChatRequest
                .builder()
                .message("What is the patient's current condition?")
                .agentId("agent-123")
                .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
                .phenomlFhirProvider("550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());
    }
    @Test
    public void testGetChatMessages() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.agent().getChatMessages(
            AgentGetChatMessagesRequest
                .builder()
                .chatSessionId("chat_session_id")
                .numMessages(1)
                .role(AgentGetChatMessagesRequestRole.USER)
                .order(AgentGetChatMessagesRequestOrder.ASC)
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("GET", request.getMethod());
    }
}
