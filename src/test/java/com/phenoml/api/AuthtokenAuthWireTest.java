package com.phenoml.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.phenoml.api.resources.authtoken.auth.requests.AuthGenerateTokenRequest;
import com.phenoml.api.resources.authtoken.auth.requests.ClientCredentialsRequest;

public class AuthtokenAuthWireTest {
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
    public void testGenerateToken() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.authtoken().auth().generateToken(
            AuthGenerateTokenRequest
                .builder()
                .username("username")
                .password("password")
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());
    }
    @Test
    public void testGetToken() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.authtoken().auth().getToken(
            ClientCredentialsRequest
                .builder()
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());
    }
}
