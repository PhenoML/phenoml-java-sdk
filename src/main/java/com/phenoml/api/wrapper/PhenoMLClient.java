package com.phenoml.api.wrapper;

import com.phenoml.api.PhenoML;
import com.phenoml.api.core.ClientOptions;
import com.phenoml.api.core.Environment;
import com.phenoml.api.resources.authtoken.auth.requests.AuthGenerateTokenRequest;
import com.phenoml.api.resources.authtoken.auth.AuthClient;
import com.phenoml.api.resources.authtoken.AuthtokenClient;
import com.phenoml.api.resources.authtoken.auth.types.AuthGenerateTokenResponse;

/**
 * Simple wrapper that extends the base PhenoML client with automatic token generation.
 * Allows users to either pass a token directly or provide username+password.
 */
public class PhenoMLClient extends PhenoML {
    
    public PhenoMLClient(ClientOptions clientOptions) {
        super(clientOptions);
    }
    
    /**
     * Create a client with username/password authentication.
     * Automatically generates a token using the auth endpoint.
     */
    public static PhenoMLClient withCredentials(String username, String password, String baseUrl) {
        // Validate inputs
        if (username == null || password == null || baseUrl == null) {
            throw new IllegalArgumentException("username, password, and baseUrl are required");
        }
        
        // Generate token
        String token = generateToken(username, password, baseUrl);
        
        // Create client with generated token
        ClientOptions options = ClientOptions.builder()
                .environment(Environment.custom(baseUrl))
                .addHeader("Authorization", "Bearer " + token)
                .build();
        
        return new PhenoMLClient(options);
    }
    
    /**
     * Create a client with direct token authentication.
     */
    public static PhenoMLClient withToken(String token, String baseUrl) {
        if (token == null || baseUrl == null) {
            throw new IllegalArgumentException("token and baseUrl are required");
        }
        
        ClientOptions options = ClientOptions.builder()
                .environment(Environment.custom(baseUrl))
                .addHeader("Authorization", "Bearer " + token)
                .build();
        
        return new PhenoMLClient(options);
    }
    
    private static String generateToken(String username, String password, String baseUrl) {
        try {
            System.out.println("🔐 Generating token for " + username + " at " + baseUrl);
            
            // Create temporary client for auth request
            ClientOptions tempOptions = ClientOptions.builder()
                    .environment(Environment.custom(baseUrl))
                    .build();
            
            AuthtokenClient authClient = new AuthtokenClient(tempOptions);
            AuthClient auth = authClient.auth();
            
            AuthGenerateTokenRequest request = AuthGenerateTokenRequest.builder()
                    .identity(username)
                    .password(password)
                    .build();
            
            System.out.println("📤 Auth Request: " + request.toString());
            AuthGenerateTokenResponse response = auth.generateToken(request);
            System.out.println("📥 Auth Response: " + response.toString());
            
            String token = response.getToken();
            System.out.println("✅ Token generated successfully (length: " + token.length() + ")");
            return token;
            
        } catch (Exception e) {
            System.out.println("❌ Failed to generate token: " + e.getMessage());
            throw new RuntimeException("Failed to generate token: " + e.getMessage(), e);
        }
    }
}
