package com.phenoml.api.wrapper;

import com.phenoml.api.AsyncPhenoML;
import com.phenoml.api.core.ClientOptions;
import com.phenoml.api.core.Environment;
import com.phenoml.api.resources.authtoken.auth.requests.AuthGenerateTokenRequest;
import com.phenoml.api.resources.authtoken.auth.AsyncAuthClient;
import com.phenoml.api.resources.authtoken.AsyncAuthtokenClient;
import com.phenoml.api.resources.authtoken.auth.types.AuthGenerateTokenResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Simple async wrapper that extends the base AsyncPhenoML client with automatic token generation.
 * Allows users to either pass a token directly or provide username+password.
 */
public class AsyncPhenoMLClient extends AsyncPhenoML {
    
    public AsyncPhenoMLClient(ClientOptions clientOptions) {
        super(clientOptions);
    }
    
    /**
     * Create an async client with username/password authentication.
     * Automatically generates a token using the auth endpoint.
     */
    public static AsyncPhenoMLClient withCredentials(String username, String password, String baseUrl) {
        if (username == null || password == null || baseUrl == null) {
            throw new IllegalArgumentException("username, password, and baseUrl are required");
        }
        
        // Generate token immediately (like sync client)
        String token = generateTokenSync(username, password, baseUrl);
        
        // Create client with generated token
        ClientOptions options = ClientOptions.builder()
                .environment(Environment.custom(baseUrl))
                .addHeader("Authorization", "Bearer " + token)
                .build();
        
        return new AsyncPhenoMLClient(options);
    }
    
    /**
     * Create an async client with direct token authentication.
     */
    public static AsyncPhenoMLClient withToken(String token, String baseUrl) {
        if (token == null || baseUrl == null) {
            throw new IllegalArgumentException("token and baseUrl are required");
        }
        
        ClientOptions options = ClientOptions.builder()
                .environment(Environment.custom(baseUrl))
                .addHeader("Authorization", "Bearer " + token)
                .build();
        
        return new AsyncPhenoMLClient(options);
    }
    
    /**
     * Initialize the client. Override this for custom initialization.
     */
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.completedFuture(null);
    }
    
    private static CompletableFuture<String> generateTokenAsync(String username, String password, String baseUrl) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ClientOptions tempOptions = ClientOptions.builder()
                        .environment(Environment.custom(baseUrl))
                        .build();
                
                AsyncAuthtokenClient authClient = new AsyncAuthtokenClient(tempOptions);
                AsyncAuthClient auth = authClient.auth();
                
                AuthGenerateTokenRequest request = AuthGenerateTokenRequest.builder()
                        .username(username)
                        .password(password)
                        .build();
                
                AuthGenerateTokenResponse response = auth.generateToken(request).join();
                return response.getToken();
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate token: " + e.getMessage(), e);
            }
        });
    }
    
    private static String generateTokenSync(String username, String password, String baseUrl) {
        try {
            System.out.println("🔐 Generating async token for " + username + " at " + baseUrl);
            
            ClientOptions tempOptions = ClientOptions.builder()
                    .environment(Environment.custom(baseUrl))
                    .build();
            
            AsyncAuthtokenClient authClient = new AsyncAuthtokenClient(tempOptions);
            AsyncAuthClient auth = authClient.auth();
            
            AuthGenerateTokenRequest request = AuthGenerateTokenRequest.builder()
                    .username(username)
                    .password(password)
                    .build();
            
            System.out.println("📤 Async Auth Request: " + request.toString());
            AuthGenerateTokenResponse response = auth.generateToken(request).join();
            System.out.println("📥 Async Auth Response: " + response.toString());
            
            String token = response.getToken();
            System.out.println("✅ Async token generated successfully (length: " + token.length() + ")");
            return token;
            
        } catch (Exception e) {
            System.out.println("❌ Failed to generate async token: " + e.getMessage());
            throw new RuntimeException("Failed to generate token: " + e.getMessage(), e);
        }
    }
}
