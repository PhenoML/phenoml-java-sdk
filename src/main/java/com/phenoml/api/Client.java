package com.phenoml.api;

import com.phenoml.api.wrapper.PhenoMLClient;

/**
 * Simple convenience class for the PhenoML wrapper client.
 *
 * Usage:
 * <pre>{@code
 * // With username/password
 * Client client = Client.withCredentials("user", "pass", "https://api.example.com");
 *
 * // With token
 * Client client = Client.withToken("your-token", "https://api.example.com");
 * }</pre>
 */
public class Client extends PhenoMLClient {

    private Client(com.phenoml.api.core.ClientOptions clientOptions) {
        super(clientOptions);
    }

    /**
     * Create a client with username/password authentication.
     */
    public static Client withCredentials(String username, String password, String baseUrl) {
        PhenoMLClient client = PhenoMLClient.withCredentials(username, password, baseUrl);
        return new Client(client.clientOptions);
    }

    /**
     * Create a client with direct token authentication.
     */
    public static Client withToken(String token, String baseUrl) {
        PhenoMLClient client = PhenoMLClient.withToken(token, baseUrl);
        return new Client(client.clientOptions);
    }
}
