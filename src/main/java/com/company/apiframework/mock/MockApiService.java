package com.company.apiframework.mock;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.company.apiframework.model.ApiRequest;
import com.company.apiframework.model.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Enhanced Mock API service for testing and development
 * Supports both custom API-specific mocks and general mocking
 */
@Service
public class MockApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(MockApiService.class);
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ApiMockRegistry apiMockRegistry;
    
    private final Map<String, MockResponse> mockResponses = new HashMap<>();
    private final Map<String, Integer> requestCounts = new HashMap<>();
    
    /**
     * Execute mock API call - tries custom API mocks first, then falls back to general mocks
     */
    public <T> ApiResponse<T> executeMock(ApiRequest request, Class<T> responseType) {
        String url = request.getUrl();
        logger.debug("Executing mock API call for URL: {}", url);
        
        // First, try to find a custom API mock
        ApiMockRegistry.CustomApiMock customMock = apiMockRegistry.findApiMockByUrl(url);
        if (customMock != null) {
            logger.debug("Using custom API mock: {}", customMock.getApiIdentifier());
            incrementRequestCount(url);
            return customMock.executeMock(request, responseType);
        }
        
        // Fall back to general mock responses
        return executeGeneralMock(request, responseType);
    }
    
    /**
     * Execute using general mock responses (existing functionality)
     */
    public <T> ApiResponse<T> executeGeneralMock(ApiRequest request, Class<T> responseType) {
        String url = request.getUrl();
        logger.debug("Executing general mock API call for URL: {}", url);
        
        // Find matching mock response
        MockResponse mockResponse = findMockResponse(url);
        
        if (mockResponse == null) {
            return createNotFoundResponse();
        }
        
        // Increment request count
        incrementRequestCount(url);
        
        // Simulate network delay
        simulateDelay(mockResponse.getDelay());
        
        // Create API response
        ApiResponse<T> apiResponse = new ApiResponse<>();
        apiResponse.setStatusCode(mockResponse.getStatusCode());
        apiResponse.setSuccess(mockResponse.getStatusCode() >= 200 && mockResponse.getStatusCode() < 300);
        
        // Convert response body
        try {
            if (mockResponse.getResponseBody() != null) {
                if (responseType == String.class) {
                    apiResponse.setBody(responseType.cast(mockResponse.getResponseBody().toString()));
                } else {
                    String json = objectMapper.writeValueAsString(mockResponse.getResponseBody());
                    T convertedBody = objectMapper.readValue(json, responseType);
                    apiResponse.setBody(convertedBody);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to convert mock response body: {}", e.getMessage());
            apiResponse.markAsError("MOCK_CONVERSION_ERROR", "Failed to convert mock response");
        }
        
        // Add mock headers
        if (mockResponse.getHeaders() != null) {
            apiResponse.getHeaders().putAll(mockResponse.getHeaders());
        }
        
        // Set response time
        apiResponse.setResponseTimeMs(mockResponse.getDelay());
        
        return apiResponse;
    }
    
    /**
     * Register a custom API mock
     */
    public void registerApiMock(String apiIdentifier, ApiMockRegistry.CustomApiMock customMock) {
        apiMockRegistry.registerApiMock(apiIdentifier, customMock);
        logger.info("Registered custom API mock: {}", apiIdentifier);
    }
    
    /**
     * Remove a custom API mock
     */
    public void removeApiMock(String apiIdentifier) {
        apiMockRegistry.removeApiMock(apiIdentifier);
        logger.info("Removed custom API mock: {}", apiIdentifier);
    }
    
    /**
     * Get custom API mock
     */
    public ApiMockRegistry.CustomApiMock getApiMock(String apiIdentifier) {
        return apiMockRegistry.getApiMock(apiIdentifier);
    }
    
    /**
     * Check if custom API mock exists
     */
    public boolean hasApiMock(String apiIdentifier) {
        return apiMockRegistry.hasApiMock(apiIdentifier);
    }
    
    /**
     * Reset specific API mock
     */
    public void resetApiMock(String apiIdentifier) {
        ApiMockRegistry.CustomApiMock mock = apiMockRegistry.getApiMock(apiIdentifier);
        if (mock != null) {
            mock.reset();
            logger.info("Reset custom API mock: {}", apiIdentifier);
        }
    }
    
    /**
     * Reset all API mocks
     */
    public void resetAllApiMocks() {
        for (Map.Entry<String, ApiMockRegistry.CustomApiMock> entry : apiMockRegistry.getAllApiMocks().entrySet()) {
            entry.getValue().reset();
        }
        logger.info("Reset all custom API mocks");
    }
    
    /**
     * Register a general mock response for a specific URL pattern
     */
    public void registerMockResponse(String urlPattern, MockResponse mockResponse) {
        mockResponses.put(urlPattern, mockResponse);
        logger.info("Registered general mock response for URL pattern: {}", urlPattern);
    }
    
    /**
     * Register a simple general mock response
     */
    public void registerMockResponse(String urlPattern, int statusCode, Object responseBody) {
        MockResponse mockResponse = new MockResponse(statusCode, responseBody);
        registerMockResponse(urlPattern, mockResponse);
    }
    
    /**
     * Clear all mock responses (both custom and general)
     */
    public void clearMockResponses() {
        mockResponses.clear();
        requestCounts.clear();
        apiMockRegistry.clearApiMocks();
        logger.info("Cleared all mock responses");
    }
    
    /**
     * Clear only general mock responses
     */
    public void clearGeneralMockResponses() {
        mockResponses.clear();
        requestCounts.clear();
        logger.info("Cleared general mock responses");
    }
    
    /**
     * Clear only custom API mocks
     */
    public void clearCustomApiMocks() {
        apiMockRegistry.clearApiMocks();
        logger.info("Cleared custom API mocks");
    }
    
    /**
     * Get request count for a URL
     */
    public int getRequestCount(String url) {
        return requestCounts.getOrDefault(url, 0);
    }
    
    /**
     * Check if any mock response exists for a URL (custom or general)
     */
    public boolean hasMockResponse(String url) {
        return apiMockRegistry.findApiMockByUrl(url) != null || findMockResponse(url) != null;
    }
    
    /**
     * Check if general mock response exists for a URL
     */
    public boolean hasGeneralMockResponse(String url) {
        return findMockResponse(url) != null;
    }
    
    /**
     * Check if custom API mock exists for a URL
     */
    public boolean hasCustomApiMock(String url) {
        return apiMockRegistry.findApiMockByUrl(url) != null;
    }
    
    /**
     * Get all registered custom API mocks
     */
    public Map<String, ApiMockRegistry.CustomApiMock> getAllCustomApiMocks() {
        return apiMockRegistry.getAllApiMocks();
    }
    
    /**
     * Get mock response summary for monitoring/debugging
     */
    public Map<String, Object> getMockSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("customApiMocks", apiMockRegistry.getAllApiMocks().keySet());
        summary.put("generalMockPatterns", mockResponses.keySet());
        summary.put("requestCounts", new HashMap<>(requestCounts));
        summary.put("totalCustomMocks", apiMockRegistry.getAllApiMocks().size());
        summary.put("totalGeneralMocks", mockResponses.size());
        return summary;
    }
    
    private MockResponse findMockResponse(String url) {
        // Exact match first
        if (mockResponses.containsKey(url)) {
            return mockResponses.get(url);
        }
        
        // Pattern matching
        for (Map.Entry<String, MockResponse> entry : mockResponses.entrySet()) {
            String pattern = entry.getKey();
            if (url.matches(pattern.replace("*", ".*"))) {
                return entry.getValue();
            }
        }
        
        return null;
    }
    
    private void incrementRequestCount(String url) {
        requestCounts.put(url, requestCounts.getOrDefault(url, 0) + 1);
    }
    
    private void simulateDelay(long delay) {
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Mock delay interrupted");
            }
        }
    }
    
    private <T> ApiResponse<T> createNotFoundResponse() {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatusCode(404);
        response.markAsError("MOCK_NOT_FOUND", "No mock response configured for this URL");
        return response;
    }
    
    /**
     * Mock response configuration (for general mocks)
     */
    public static class MockResponse {
        private int statusCode;
        private Object responseBody;
        private Map<String, String> headers;
        private long delay;
        
        public MockResponse() {
            this.headers = new HashMap<>();
        }
        
        public MockResponse(int statusCode, Object responseBody) {
            this();
            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }
        
        public MockResponse(int statusCode, Object responseBody, long delay) {
            this(statusCode, responseBody);
            this.delay = delay;
        }
        
        // Getters and Setters
        public int getStatusCode() {
            return statusCode;
        }
        
        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }
        
        public Object getResponseBody() {
            return responseBody;
        }
        
        public void setResponseBody(Object responseBody) {
            this.responseBody = responseBody;
        }
        
        public Map<String, String> getHeaders() {
            return headers;
        }
        
        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }
        
        public long getDelay() {
            return delay;
        }
        
        public void setDelay(long delay) {
            this.delay = delay;
        }
        
        public MockResponse addHeader(String name, String value) {
            this.headers.put(name, value);
            return this;
        }
        
        public MockResponse withDelay(long delayMs) {
            this.delay = delayMs;
            return this;
        }
    }
} 