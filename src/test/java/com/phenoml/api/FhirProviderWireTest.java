package com.phenoml.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FhirProviderWireTest {
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
        client.fhirProvider().create(
            FhirProviderCreateRequest
                .builder()
                .name("Epic Sandbox")
                .provider(Provider.ATHENAHEALTH)
                .baseUrl("https://fhir.epic.com/interconnect-fhir-oauth/api/FHIR/R4")
                .auth(
                    FhirProviderCreateRequestAuth.jwt(
                        JwtAuth
                            .builder()
                            .clientId("your-client-id")
                            .build()
                    )
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
        client.fhirProvider().list();;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("GET", request.getMethod());
    }
    @Test
    public void testGet() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.fhirProvider().get("fhir_provider_id");;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("GET", request.getMethod());
    }
    @Test
    public void testDelete() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.fhirProvider().delete("fhir_provider_id");;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("DELETE", request.getMethod());
    }
    @Test
    public void testAddAuthConfig() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.fhirProvider().addAuthConfig(
            "1716d214-de93-43a4-aa6b-a878d864e2ad",
            FhirProviderAddAuthConfigRequest.jwt(
                JwtAuth
                    .builder()
                    .clientId("your-client-id")
                    .build()
            )
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("PATCH", request.getMethod());
    }
    @Test
    public void testSetActiveAuthConfig() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.fhirProvider().setActiveAuthConfig(
            "1716d214-de93-43a4-aa6b-a878d864e2ad",
            FhirProviderSetActiveAuthConfigRequest
                .builder()
                .authConfigId("auth-config-123")
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("PATCH", request.getMethod());
    }
    @Test
    public void testRemoveAuthConfig() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.fhirProvider().removeAuthConfig(
            "1716d214-de93-43a4-aa6b-a878d864e2ad",
            FhirProviderRemoveAuthConfigRequest
                .builder()
                .authConfigId("auth-config-123")
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("PATCH", request.getMethod());
    }
}
