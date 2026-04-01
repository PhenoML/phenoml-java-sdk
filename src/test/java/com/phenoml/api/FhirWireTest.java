package com.phenoml.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.phenoml.api.resources.fhir.requests.FhirCreateRequest;
import com.phenoml.api.resources.fhir.requests.FhirDeleteRequest;
import com.phenoml.api.resources.fhir.requests.FhirExecuteBundleRequest;
import com.phenoml.api.resources.fhir.requests.FhirPatchRequest;
import com.phenoml.api.resources.fhir.requests.FhirSearchRequest;
import com.phenoml.api.resources.fhir.requests.FhirUpsertRequest;
import com.phenoml.api.resources.fhir.types.FhirBundle;
import com.phenoml.api.resources.fhir.types.FhirPatchRequestBodyItem;
import com.phenoml.api.resources.fhir.types.FhirResource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class FhirWireTest {
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
    public void testSearch() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.fhir().search(
            "550e8400-e29b-41d4-a716-446655440000",
            "Patient",
            FhirSearchRequest
                .builder()
                .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
                .phenomlFhirProvider("550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
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
        client.fhir().create(
            "550e8400-e29b-41d4-a716-446655440000",
            "Patient",
            FhirCreateRequest
                .builder()
                .body(
                    FhirResource
                        .builder()
                        .resourceType("Patient")
                        .build()
                )
                .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
                .phenomlFhirProvider("550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());
    }
    @Test
    public void testUpsert() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.fhir().upsert(
            "550e8400-e29b-41d4-a716-446655440000",
            "Patient",
            FhirUpsertRequest
                .builder()
                .body(
                    FhirResource
                        .builder()
                        .resourceType("Patient")
                        .id("123")
                        .build()
                )
                .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
                .phenomlFhirProvider("550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
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
        client.fhir().delete(
            "550e8400-e29b-41d4-a716-446655440000",
            "Patient",
            FhirDeleteRequest
                .builder()
                .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
                .phenomlFhirProvider("550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("DELETE", request.getMethod());
    }
    @Test
    public void testPatch() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.fhir().patch(
            "550e8400-e29b-41d4-a716-446655440000",
            "Patient",
            FhirPatchRequest
                .builder()
                .body(
                    new ArrayList<FhirPatchRequestBodyItem>(
                        Arrays.asList(
                            FhirPatchRequestBodyItem
                                .builder()
                                .op(FhirPatchRequestBodyItem.Op.REPLACE)
                                .path("/name/0/family")
                                .value("NewFamilyName")
                                .build()
                        )
                    )
                )
                .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
                .phenomlFhirProvider("550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("PATCH", request.getMethod());
    }
    @Test
    public void testExecuteBundle() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));
        client.fhir().executeBundle(
            "550e8400-e29b-41d4-a716-446655440000",
            FhirExecuteBundleRequest
                .builder()
                .body(
                    FhirBundle
                        .builder()
                        .resourceType("Bundle")
                        .entry(
                            new ArrayList<FhirBundle.EntryItem>(
                                Arrays.asList(
                                    FhirBundle.EntryItem
                                        .builder()
                                        .resource(
                                            new HashMap<String, Object>() {{
                                                put("resourceType", "Patient");
                                                put("name", new
                                                ArrayList<Object>(Arrays.asList(new
                                                    HashMap<String, Object>() {{
                                                        put("family", "Doe");
                                                        put("given", new
                                                        ArrayList<Object>(Arrays.asList("John")
                                                        ));
                                                    }}
                                                )));
                                            }}
                                        )
                                        .request(
                                            FhirBundle.EntryItem.Request
                                                .builder()
                                                .method(FhirBundle.EntryItem.Request.Method.POST)
                                                .url("Patient")
                                                .build()
                                        )
                                        .build(),
                                    FhirBundle.EntryItem
                                        .builder()
                                        .resource(
                                            new HashMap<String, Object>() {{
                                                put("resourceType", "Observation");
                                                put("status", "final");
                                                put("subject", new 
                                                HashMap<String, Object>() {{put("reference", "Patient/123");
                                                }});
                                            }}
                                        )
                                        .request(
                                            FhirBundle.EntryItem.Request
                                                .builder()
                                                .method(FhirBundle.EntryItem.Request.Method.POST)
                                                .url("Observation")
                                                .build()
                                        )
                                        .build()
                                )
                            )
                        )
                        .build()
                )
                .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
                .phenomlFhirProvider("550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
                .build()
        );;
        RecordedRequest request = server.takeRequest();
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.getMethod());
    }
}
