package com.company.apiframework.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.company.apiframework.model.ApiRequest;
import com.company.apiframework.model.ApiResponse;

/**
 * Registry for custom API-specific mock configurations
 */
@Component
public class ApiMockRegistry {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiMockRegistry.class);
    
    // Map of API identifiers to their custom mock services
    private final Map<String, CustomApiMock> apiMocks = new ConcurrentHashMap<>();
    
    /**
     * Register a custom mock service for a specific API
     * 
     * @param apiIdentifier Unique identifier for the API (e.g., "payment-api", "user-service")
     * @param customMock Custom mock implementation for this API
     */
    public void registerApiMock(String apiIdentifier, CustomApiMock customMock) {
        apiMocks.put(apiIdentifier, customMock);
        logger.info("Registered custom mock for API: {}", apiIdentifier);
    }
    
    /**
     * Get custom mock service for an API
     * 
     * @param apiIdentifier API identifier
     * @return Custom mock service or null if not found
     */
    public CustomApiMock getApiMock(String apiIdentifier) {
        return apiMocks.get(apiIdentifier);
    }
    
    /**
     * Check if an API has a custom mock registered
     */
    public boolean hasApiMock(String apiIdentifier) {
        return apiMocks.containsKey(apiIdentifier);
    }
    
    /**
     * Remove custom mock for an API
     */
    public void removeApiMock(String apiIdentifier) {
        apiMocks.remove(apiIdentifier);
        logger.info("Removed custom mock for API: {}", apiIdentifier);
    }
    
    /**
     * Clear all custom API mocks
     */
    public void clearApiMocks() {
        apiMocks.clear();
        logger.info("Cleared all custom API mocks");
    }
    
    /**
     * Get all registered API mock identifiers
     */
    public Map<String, CustomApiMock> getAllApiMocks() {
        return new HashMap<>(apiMocks);
    }
    
    /**
     * Find API mock by URL pattern matching
     * 
     * @param url The request URL
     * @return CustomApiMock if found, null otherwise
     */
    public CustomApiMock findApiMockByUrl(String url) {
        for (Map.Entry<String, CustomApiMock> entry : apiMocks.entrySet()) {
            CustomApiMock mock = entry.getValue();
            if (mock.matchesUrl(url)) {
                logger.debug("Found custom mock for API: {} matching URL: {}", entry.getKey(), url);
                return mock;
            }
        }
        return null;
    }
    
    /**
     * Interface for custom API mock implementations
     */
    public interface CustomApiMock {
        
        /**
         * Check if this mock handles the given URL
         */
        boolean matchesUrl(String url);
        
        /**
         * Execute mock response for the API
         */
        <T> ApiResponse<T> executeMock(ApiRequest request, Class<T> responseType);
        
        /**
         * Get API identifier
         */
        String getApiIdentifier();
        
        /**
         * Setup mock scenarios (optional)
         */
        default void setupScenarios() {
            // Default implementation - override for custom scenarios
        }
        
        /**
         * Reset mock state (optional)
         */
        default void reset() {
            // Default implementation - override for custom reset logic
        }
    }
} 