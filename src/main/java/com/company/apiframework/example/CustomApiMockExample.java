package com.company.apiframework.example;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.company.apiframework.mock.MockApiService;
import com.company.apiframework.mock.impl.PaymentApiMock;
import com.company.apiframework.mock.impl.UserServiceMock;
import com.company.apiframework.model.ApiRequest;
import com.company.apiframework.model.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Examples showing how to use custom API mocks for different external services
 */
@Component
public class CustomApiMockExample {
    
    @Autowired
    private MockApiService mockApiService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Example 1: Setting up custom mocks for different APIs
     */
    public void setupCustomApiMocks() {
        // Setup Payment API mock
        PaymentApiMock paymentMock = new PaymentApiMock(objectMapper);
        mockApiService.registerApiMock("payment-api", paymentMock);
        
        // Setup User Service mock
        UserServiceMock userServiceMock = new UserServiceMock(objectMapper);
        mockApiService.registerApiMock("user-service", userServiceMock);
        
        System.out.println("Custom API mocks registered successfully!");
        
        // Display mock summary
        Map<String, Object> summary = mockApiService.getMockSummary();
        System.out.println("Mock Summary: " + summary);
    }
    
    /**
     * Example 2: Testing Payment API scenarios
     */
    public void testPaymentApiScenarios() {
        // Setup payment mock
        setupCustomApiMocks();
        
        // Test successful payment
        testSuccessfulPayment();
        
        // Test payment failures
        testPaymentFailures();
        
        // Test payment edge cases
        testPaymentEdgeCases();
    }
    
    private void testSuccessfulPayment() {
        System.out.println("\n=== Testing Successful Payment ===");
        
        ApiRequest paymentRequest = ApiRequest.builder()
                .url("https://payment.gateway.com/process")
                .method("POST")
                .header("Content-Type", "application/json")
                .body("{\"amount\":100.00,\"currency\":\"USD\",\"cardNumber\":\"4111111111111111\"}")
                .build();
        
        ApiResponse<String> response = mockApiService.executeMock(paymentRequest, String.class);
        
        System.out.println("Payment Status: " + (response.isSuccess() ? "SUCCESS" : "FAILED"));
        System.out.println("Response: " + response.getBody());
        System.out.println("Headers: " + response.getHeaders());
    }
    
    private void testPaymentFailures() {
        System.out.println("\n=== Testing Payment Failures ===");
        
        // Test insufficient funds
        ApiRequest insufficientFundsRequest = ApiRequest.builder()
                .url("https://payment.gateway.com/process")
                .method("POST")
                .body("{\"amount\":1,\"currency\":\"USD\"}")
                .build();
        
        ApiResponse<String> response1 = mockApiService.executeMock(insufficientFundsRequest, String.class);
        System.out.println("Insufficient Funds Test - Status: " + response1.getStatusCode() + 
                          ", Error: " + response1.getErrorMessage());
        
        // Test invalid card
        ApiRequest invalidCardRequest = ApiRequest.builder()
                .url("https://payment.gateway.com/process")
                .method("POST")
                .body("{\"amount\":50.00,\"cardNumber\":\"4000000000000002\"}")
                .build();
        
        ApiResponse<String> response2 = mockApiService.executeMock(invalidCardRequest, String.class);
        System.out.println("Invalid Card Test - Status: " + response2.getStatusCode() + 
                          ", Error: " + response2.getErrorMessage());
    }
    
    private void testPaymentEdgeCases() {
        System.out.println("\n=== Testing Payment Edge Cases ===");
        
        // Test with scenario hint header
        ApiRequest timeoutRequest = ApiRequest.builder()
                .url("https://payment.gateway.com/process")
                .method("POST")
                .header("X-Mock-Scenario", "network_timeout")
                .body("{\"amount\":200.00,\"currency\":\"USD\"}")
                .build();
        
        long startTime = System.currentTimeMillis();
        ApiResponse<String> response = mockApiService.executeMock(timeoutRequest, String.class);
        long duration = System.currentTimeMillis() - startTime;
        
        System.out.println("Timeout Test - Status: " + response.getStatusCode() + 
                          ", Duration: " + duration + "ms");
        System.out.println("Error: " + response.getErrorMessage());
    }
    
    /**
     * Example 3: Testing User Service operations
     */
    public void testUserServiceOperations() {
        System.out.println("\n=== Testing User Service Operations ===");
        
        setupCustomApiMocks();
        
        // Test get all users
        testGetAllUsers();
        
        // Test get specific user
        testGetSpecificUser();
        
        // Test create user
        testCreateUser();
        
        // Test update user
        testUpdateUser();
        
        // Test user authentication
        testUserAuthentication();
    }
    
    private void testGetAllUsers() {
        ApiRequest request = ApiRequest.builder()
                .url("https://user-service.example.com/users")
                .method("GET")
                .build();
        
        ApiResponse<String> response = mockApiService.executeMock(request, String.class);
        System.out.println("Get All Users - Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());
    }
    
    private void testGetSpecificUser() {
        ApiRequest request = ApiRequest.builder()
                .url("https://user-service.example.com/users/1")
                .method("GET")
                .build();
        
        ApiResponse<String> response = mockApiService.executeMock(request, String.class);
        System.out.println("Get User 1 - Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());
        
        // Test non-existent user
        ApiRequest notFoundRequest = ApiRequest.builder()
                .url("https://user-service.example.com/users/999999")
                .method("GET")
                .build();
        
        ApiResponse<String> notFoundResponse = mockApiService.executeMock(notFoundRequest, String.class);
        System.out.println("Get Non-existent User - Status: " + notFoundResponse.getStatusCode());
        System.out.println("Error: " + notFoundResponse.getErrorMessage());
    }
    
    private void testCreateUser() {
        ApiRequest request = ApiRequest.builder()
                .url("https://user-service.example.com/users")
                .method("POST")
                .header("Content-Type", "application/json")
                .body("{\"email\":\"new.user@example.com\",\"firstName\":\"New\",\"lastName\":\"User\"}")
                .build();
        
        ApiResponse<String> response = mockApiService.executeMock(request, String.class);
        System.out.println("Create User - Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());
    }
    
    private void testUpdateUser() {
        ApiRequest request = ApiRequest.builder()
                .url("https://user-service.example.com/users/1")
                .method("PUT")
                .header("Content-Type", "application/json")
                .body("{\"firstName\":\"Updated\",\"lastName\":\"Name\"}")
                .build();
        
        ApiResponse<String> response = mockApiService.executeMock(request, String.class);
        System.out.println("Update User - Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());
    }
    
    private void testUserAuthentication() {
        ApiRequest loginRequest = ApiRequest.builder()
                .url("https://user-service.example.com/auth/login")
                .method("POST")
                .header("Content-Type", "application/json")
                .body("{\"email\":\"user@example.com\",\"password\":\"password123\"}")
                .build();
        
        ApiResponse<String> response = mockApiService.executeMock(loginRequest, String.class);
        System.out.println("User Login - Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());
    }
    
    /**
     * Example 4: Testing scenario-based mocking
     */
    public void testScenarioBasedMocking() {
        System.out.println("\n=== Testing Scenario-Based Mocking ===");
        
        setupCustomApiMocks();
        
        // Test with different scenario hints
        String[] scenarios = {
            "success", "user_not_found", "validation_error", 
            "unauthorized", "forbidden", "server_error"
        };
        
        for (String scenario : scenarios) {
            ApiRequest request = ApiRequest.builder()
                    .url("https://user-service.example.com/users/1")
                    .method("GET")
                    .header("X-Mock-Scenario", scenario)
                    .build();
            
            ApiResponse<String> response = mockApiService.executeMock(request, String.class);
            System.out.println("Scenario '" + scenario + "' - Status: " + response.getStatusCode() + 
                              ", Success: " + response.isSuccess());
            
            if (!response.isSuccess()) {
                System.out.println("  Error: " + response.getErrorMessage());
            }
        }
    }
    
    /**
     * Example 5: Mock management and monitoring
     */
    public void demonstrateMockManagement() {
        System.out.println("\n=== Mock Management Demo ===");
        
        setupCustomApiMocks();
        
        // Show mock summary
        Map<String, Object> summary = mockApiService.getMockSummary();
        System.out.println("Initial Mock Summary: " + summary);
        
        // Make some test calls to generate request counts
        makeTestCalls();
        
        // Show updated summary
        summary = mockApiService.getMockSummary();
        System.out.println("Updated Mock Summary: " + summary);
        
        // Reset specific mock
        mockApiService.resetApiMock("payment-api");
        System.out.println("Reset payment-api mock");
        
        // Remove a mock
        mockApiService.removeApiMock("user-service");
        System.out.println("Removed user-service mock");
        
        // Final summary
        summary = mockApiService.getMockSummary();
        System.out.println("Final Mock Summary: " + summary);
    }
    
    private void makeTestCalls() {
        // Make several test calls to generate request counts
        for (int i = 0; i < 3; i++) {
            ApiRequest paymentRequest = ApiRequest.builder()
                    .url("https://payment.gateway.com/process")
                    .method("POST")
                    .body("{\"amount\":100.00}")
                    .build();
            mockApiService.executeMock(paymentRequest, String.class);
            
            ApiRequest userRequest = ApiRequest.builder()
                    .url("https://user-service.example.com/users")
                    .method("GET")
                    .build();
            mockApiService.executeMock(userRequest, String.class);
        }
    }
    
    /**
     * Example 6: Combining custom and general mocks
     */
    public void testCombinedMocking() {
        System.out.println("\n=== Testing Combined Mocking ===");
        
        // Setup custom API mocks
        setupCustomApiMocks();
        
        // Register a general mock for an API not covered by custom mocks
        mockApiService.registerMockResponse(
            "https://notification.service.com/*", 
            200, 
            Map.of("status", "sent", "messageId", "msg_12345")
        );
        
        // Test custom mock (Payment API)
        ApiRequest paymentRequest = ApiRequest.builder()
                .url("https://payment.gateway.com/process")
                .method("POST")
                .body("{\"amount\":50.00}")
                .build();
        ApiResponse<String> paymentResponse = mockApiService.executeMock(paymentRequest, String.class);
        System.out.println("Payment (Custom Mock) - Status: " + paymentResponse.getStatusCode());
        
        // Test general mock (Notification Service)
        ApiRequest notificationRequest = ApiRequest.builder()
                .url("https://notification.service.com/send")
                .method("POST")
                .body("{\"message\":\"Hello\"}")
                .build();
        ApiResponse<String> notificationResponse = mockApiService.executeMock(notificationRequest, String.class);
        System.out.println("Notification (General Mock) - Status: " + notificationResponse.getStatusCode());
        System.out.println("Notification Response: " + notificationResponse.getBody());
        
        // Test URL without any mock
        ApiRequest unknownRequest = ApiRequest.builder()
                .url("https://unknown.service.com/endpoint")
                .method("GET")
                .build();
        ApiResponse<String> unknownResponse = mockApiService.executeMock(unknownRequest, String.class);
        System.out.println("Unknown Service - Status: " + unknownResponse.getStatusCode());
        System.out.println("Error: " + unknownResponse.getErrorMessage());
    }
} 