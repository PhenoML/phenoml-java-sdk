package com.phenoml.api;

import com.phenoml.api.wrapper.AsyncPhenoMLClient;

/**
 * Simple convenience class for the PhenoML async wrapper client.
 * 
 * Usage:
 * <pre>{@code
 * // With username/password
 * AsyncClient client = AsyncClient.withCredentials("user", "pass", "https://api.example.com");
 * 
 * // With token
 * AsyncClient client = AsyncClient.withToken("your-token", "https://api.example.com");
 * }</pre>
 */
public class AsyncClient extends AsyncPhenoMLClient {
    
    private AsyncClient(com.phenoml.api.core.ClientOptions clientOptions) {
        super(clientOptions);
    }
    
    /**
     * Create an async client with username/password authentication.
     */
    public static AsyncClient withCredentials(String username, String password, String baseUrl) {
        AsyncPhenoMLClient client = AsyncPhenoMLClient.withCredentials(username, password, baseUrl);
        return new AsyncClient(client.clientOptions);
    }
    
    /**
     * Create an async client with direct token authentication.
     */
    public static AsyncClient withToken(String token, String baseUrl) {
        AsyncPhenoMLClient client = AsyncPhenoMLClient.withToken(token, baseUrl);
        return new AsyncClient(client.clientOptions);
    }
}
