package com.company.apiframework.mock.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.company.apiframework.mock.ApiMockRegistry.CustomApiMock;
import com.company.apiframework.model.ApiRequest;
import com.company.apiframework.model.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Custom mock implementation for User Service API
 * Simulates user management operations with realistic data and scenarios
 */
public class UserServiceMock implements CustomApiMock {
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceMock.class);
    private static final String API_IDENTIFIER = "user-service";
    private static final String BASE_URL_PATTERN = "https://user-service.example.com";
    
    private final ObjectMapper objectMapper;
    private final Map<String, MockUser> users = new ConcurrentHashMap<>();
    private final Map<String, UserScenario> scenarios = new HashMap<>();
    
    public UserServiceMock(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        setupScenarios();
        setupInitialData();
    }
    
    @Override
    public boolean matchesUrl(String url) {
        return url.contains(BASE_URL_PATTERN);
    }
    
    @Override
    public <T> ApiResponse<T> executeMock(ApiRequest request, Class<T> responseType) {
        String endpoint = extractEndpoint(request.getUrl());
        String method = request.getMethod().toUpperCase();
        
        logger.debug("User Service mock executing: {} {}", method, endpoint);
        
        ApiResponse<T> response = new ApiResponse<>();
        Object responseBody = null;
        
        try {
            // Route based on endpoint and method
            if (endpoint.startsWith("/users")) {
                responseBody = handleUsersEndpoint(request, endpoint, method, response);
            } else if (endpoint.startsWith("/auth")) {
                responseBody = handleAuthEndpoint(request, endpoint, method, response);
            } else {
                response.setStatusCode(404);
                response.markAsError("ENDPOINT_NOT_FOUND", "Endpoint not found: " + endpoint);
                return response;
            }
            
            // Convert response body
            if (responseBody != null) {
                if (responseType == String.class) {
                    response.setBody(responseType.cast(objectMapper.writeValueAsString(responseBody)));
                } else {
                    String json = objectMapper.writeValueAsString(responseBody);
                    T convertedBody = objectMapper.readValue(json, responseType);
                    response.setBody(convertedBody);
                }
            }
            
        } catch (Exception e) {
            logger.error("User Service mock error: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.markAsError("USER_MOCK_ERROR", "Mock execution failed: " + e.getMessage());
        }
        
        // Add service-specific headers
        response.getHeaders().put("X-User-Service", "Mock-Service-v1.0");
        response.getHeaders().put("X-Request-Id", java.util.UUID.randomUUID().toString());
        
        response.setResponseTimeMs(100); // Default response time
        return response;
    }
    
    @Override
    public String getApiIdentifier() {
        return API_IDENTIFIER;
    }
    
    @Override
    public void setupScenarios() {
        scenarios.put("success", new UserScenario("success", 200, "Operation successful"));
        scenarios.put("user_not_found", new UserScenario("user_not_found", 404, "User not found"));
        scenarios.put("validation_error", new UserScenario("validation_error", 400, "Validation failed"));
        scenarios.put("unauthorized", new UserScenario("unauthorized", 401, "Unauthorized access"));
        scenarios.put("forbidden", new UserScenario("forbidden", 403, "Access forbidden"));
        scenarios.put("server_error", new UserScenario("server_error", 500, "Internal server error"));
        
        logger.info("User Service mock scenarios setup complete");
    }
    
    @Override
    public void reset() {
        users.clear();
        setupInitialData();
        logger.info("User Service mock data reset");
    }
    
    private void setupInitialData() {
        // Create some initial test users
        users.put("1", new MockUser("1", "john.doe@example.com", "John", "Doe", "active"));
        users.put("2", new MockUser("2", "jane.smith@example.com", "Jane", "Smith", "active"));
        users.put("3", new MockUser("3", "bob.wilson@example.com", "Bob", "Wilson", "inactive"));
        users.put("999", new MockUser("999", "test.user@example.com", "Test", "User", "suspended"));
    }
    
    private Object handleUsersEndpoint(ApiRequest request, String endpoint, String method, ApiResponse<?> response) {
        // Check for scenario override
        String scenarioHint = request.getHeaders().get("X-Mock-Scenario");
        if (scenarioHint != null && scenarios.containsKey(scenarioHint)) {
            UserScenario scenario = scenarios.get(scenarioHint);
            response.setStatusCode(scenario.statusCode);
            if (scenario.statusCode >= 400) {
                response.markAsError("USER_SERVICE_ERROR", scenario.message);
                return createErrorResponse(scenario.statusCode, scenario.message);
            }
        }
        
        switch (method) {
            case "GET":
                return handleGetUsers(endpoint, response);
            case "POST":
                return handleCreateUser(request, response);
            case "PUT":
                return handleUpdateUser(request, endpoint, response);
            case "DELETE":
                return handleDeleteUser(endpoint, response);
            default:
                response.setStatusCode(405);
                response.markAsError("METHOD_NOT_ALLOWED", "Method not allowed: " + method);
                return createErrorResponse(405, "Method not allowed");
        }
    }
    
    private Object handleGetUsers(String endpoint, ApiResponse<?> response) {
        if (endpoint.equals("/users")) {
            // Get all users
            response.setStatusCode(200);
            return Map.of(
                "users", users.values(),
                "total", users.size(),
                "page", 1,
                "limit", 10
            );
        } else if (endpoint.matches("/users/\\d+")) {
            // Get specific user
            String userId = endpoint.substring(7); // Remove "/users/"
            MockUser user = users.get(userId);
            if (user != null) {
                response.setStatusCode(200);
                return user;
            } else {
                response.setStatusCode(404);
                response.markAsError("USER_NOT_FOUND", "User not found: " + userId);
                return createErrorResponse(404, "User not found");
            }
        } else {
            response.setStatusCode(400);
            response.markAsError("INVALID_ENDPOINT", "Invalid users endpoint");
            return createErrorResponse(400, "Invalid endpoint");
        }
    }
    
    private Object handleCreateUser(ApiRequest request, ApiResponse<?> response) {
        try {
            // Parse request body to get user data
            Map<String, Object> userData = parseUserFromRequest(request);
            
            // Validate required fields
            if (!userData.containsKey("email") || !userData.containsKey("firstName")) {
                response.setStatusCode(400);
                response.markAsError("VALIDATION_ERROR", "Missing required fields");
                return createErrorResponse(400, "Missing required fields: email, firstName");
            }
            
            // Create new user
            String userId = String.valueOf(users.size() + 1);
            MockUser newUser = new MockUser(
                userId,
                (String) userData.get("email"),
                (String) userData.get("firstName"),
                (String) userData.getOrDefault("lastName", ""),
                "active"
            );
            
            users.put(userId, newUser);
            response.setStatusCode(201);
            
            return Map.of(
                "user", newUser,
                "message", "User created successfully"
            );
            
        } catch (Exception e) {
            response.setStatusCode(400);
            response.markAsError("INVALID_REQUEST", "Invalid request body");
            return createErrorResponse(400, "Invalid request body");
        }
    }
    
    private Object handleUpdateUser(ApiRequest request, String endpoint, ApiResponse<?> response) {
        if (!endpoint.matches("/users/\\d+")) {
            response.setStatusCode(400);
            return createErrorResponse(400, "Invalid update endpoint");
        }
        
        String userId = endpoint.substring(7);
        MockUser existingUser = users.get(userId);
        
        if (existingUser == null) {
            response.setStatusCode(404);
            response.markAsError("USER_NOT_FOUND", "User not found");
            return createErrorResponse(404, "User not found");
        }
        
        try {
            Map<String, Object> updateData = parseUserFromRequest(request);
            
            // Update user fields
            MockUser updatedUser = new MockUser(
                existingUser.id,
                (String) updateData.getOrDefault("email", existingUser.email),
                (String) updateData.getOrDefault("firstName", existingUser.firstName),
                (String) updateData.getOrDefault("lastName", existingUser.lastName),
                (String) updateData.getOrDefault("status", existingUser.status)
            );
            
            users.put(userId, updatedUser);
            response.setStatusCode(200);
            
            return Map.of(
                "user", updatedUser,
                "message", "User updated successfully"
            );
            
        } catch (Exception e) {
            response.setStatusCode(400);
            response.markAsError("INVALID_REQUEST", "Invalid request body");
            return createErrorResponse(400, "Invalid request body");
        }
    }
    
    private Object handleDeleteUser(String endpoint, ApiResponse<?> response) {
        if (!endpoint.matches("/users/\\d+")) {
            response.setStatusCode(400);
            return createErrorResponse(400, "Invalid delete endpoint");
        }
        
        String userId = endpoint.substring(7);
        MockUser user = users.remove(userId);
        
        if (user != null) {
            response.setStatusCode(200);
            return Map.of("message", "User deleted successfully", "userId", userId);
        } else {
            response.setStatusCode(404);
            response.markAsError("USER_NOT_FOUND", "User not found");
            return createErrorResponse(404, "User not found");
        }
    }
    
    private Object handleAuthEndpoint(ApiRequest request, String endpoint, String method, ApiResponse<?> response) {
        if (endpoint.equals("/auth/login") && method.equals("POST")) {
            // Simulate login
            response.setStatusCode(200);
            return Map.of(
                "token", "mock_jwt_token_" + System.currentTimeMillis(),
                "expiresIn", 3600,
                "refreshToken", "mock_refresh_token_" + System.currentTimeMillis()
            );
        } else if (endpoint.equals("/auth/logout") && method.equals("POST")) {
            // Simulate logout
            response.setStatusCode(200);
            return Map.of("message", "Logout successful");
        } else {
            response.setStatusCode(404);
            return createErrorResponse(404, "Auth endpoint not found");
        }
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseUserFromRequest(ApiRequest request) throws Exception {
        if (request.getBody() instanceof String) {
            return objectMapper.readValue((String) request.getBody(), Map.class);
        } else if (request.getBody() instanceof Map) {
            return (Map<String, Object>) request.getBody();
        } else {
            throw new IllegalArgumentException("Invalid request body format");
        }
    }
    
    private String extractEndpoint(String url) {
        return url.replace(BASE_URL_PATTERN, "");
    }
    
    private Map<String, Object> createErrorResponse(int statusCode, String message) {
        return Map.of(
            "error", true,
            "statusCode", statusCode,
            "message", message,
            "timestamp", System.currentTimeMillis()
        );
    }
    
    /**
     * Mock User data class
     */
    public static class MockUser {
        public final String id;
        public final String email;
        public final String firstName;
        public final String lastName;
        public final String status;
        
        public MockUser(String id, String email, String firstName, String lastName, String status) {
            this.id = id;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.status = status;
        }
        
        // Getters for JSON serialization
        public String getId() { return id; }
        public String getEmail() { return email; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getStatus() { return status; }
    }
    
    /**
     * User scenario configuration
     */
    private static class UserScenario {
        final String name;
        final int statusCode;
        final String message;
        
        UserScenario(String name, int statusCode, String message) {
            this.name = name;
            this.statusCode = statusCode;
            this.message = message;
        }
    }
} 