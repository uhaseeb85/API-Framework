package com.company.apiframework;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.company.apiframework.mock.MockApiService;
import com.company.apiframework.mock.impl.PaymentApiMock;
import com.company.apiframework.mock.impl.UserServiceMock;
import com.company.apiframework.model.ApiRequest;
import com.company.apiframework.model.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
public class CustomApiMockTest {
    
    @Autowired
    private MockApiService mockApiService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @BeforeEach
    public void setUp() {
        // Clear any existing mocks
        mockApiService.clearMockResponses();
        
        // Register custom API mocks
        PaymentApiMock paymentMock = new PaymentApiMock(objectMapper);
        mockApiService.registerApiMock("payment-api", paymentMock);
        
        UserServiceMock userServiceMock = new UserServiceMock(objectMapper);
        mockApiService.registerApiMock("user-service", userServiceMock);
    }
    
    @Test
    public void testPaymentApiMockRegistration() {
        // Verify payment API mock is registered
        assertTrue(mockApiService.hasApiMock("payment-api"));
        assertNotNull(mockApiService.getApiMock("payment-api"));
        assertEquals("payment-api", mockApiService.getApiMock("payment-api").getApiIdentifier());
    }
    
    @Test
    public void testUserServiceMockRegistration() {
        // Verify user service mock is registered
        assertTrue(mockApiService.hasApiMock("user-service"));
        assertNotNull(mockApiService.getApiMock("user-service"));
        assertEquals("user-service", mockApiService.getApiMock("user-service").getApiIdentifier());
    }
    
    @Test
    public void testPaymentApiSuccessfulTransaction() {
        ApiRequest request = ApiRequest.builder()
                .url("https://payment.gateway.com/process")
                .method("POST")
                .header("Content-Type", "application/json")
                .header("X-Mock-Scenario", "process_success")
                .body("{\"amount\":100.00,\"currency\":\"USD\",\"cardNumber\":\"4111111111111111\"}")
                .build();
        
        ApiResponse<String> response = mockApiService.executeMock(request, String.class);
        
        assertTrue(response.isSuccess());
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("transactionId"));
        assertTrue(response.getBody().contains("completed"));
        
        // Check payment-specific headers
        assertTrue(response.getHeaders().containsKey("X-Payment-Gateway"));
        assertTrue(response.getHeaders().containsKey("X-Transaction-Id"));
    }
    
    @Test
    public void testPaymentApiInsufficientFunds() {
        ApiRequest request = ApiRequest.builder()
                .url("https://payment.gateway.com/process")
                .method("POST")
                .body("{\"amount\":1,\"currency\":\"USD\"}")
                .build();
        
        ApiResponse<String> response = mockApiService.executeMock(request, String.class);
        
        assertFalse(response.isSuccess());
        assertEquals(400, response.getStatusCode());
        assertEquals("INSUFFICIENT_FUNDS", response.getErrorCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("INSUFFICIENT_FUNDS"));
    }
    
    @Test
    public void testPaymentApiInvalidCard() {
        ApiRequest request = ApiRequest.builder()
                .url("https://payment.gateway.com/process")
                .method("POST")
                .body("{\"amount\":50.00,\"cardNumber\":\"4000000000000002\"}")
                .build();
        
        ApiResponse<String> response = mockApiService.executeMock(request, String.class);
        
        assertFalse(response.isSuccess());
        assertEquals(400, response.getStatusCode());
        assertEquals("INVALID_CARD", response.getErrorCode());
        assertTrue(response.getBody().contains("Invalid card number"));
    }
    
    @Test
    public void testPaymentApiNetworkTimeout() {
        ApiRequest request = ApiRequest.builder()
                .url("https://payment.gateway.com/process")
                .method("POST")
                .header("X-Mock-Scenario", "network_timeout")
                .body("{\"amount\":200.00}")
                .build();
        
        long startTime = System.currentTimeMillis();
        ApiResponse<String> response = mockApiService.executeMock(request, String.class);
        long duration = System.currentTimeMillis() - startTime;
        
        assertFalse(response.isSuccess());
        assertEquals(504, response.getStatusCode());
        assertEquals("GATEWAY_TIMEOUT", response.getErrorCode());
        // Should simulate delay
        assertTrue(duration >= 4000); // Allow some tolerance
    }
    
    @Test
    public void testUserServiceGetAllUsers() {
        ApiRequest request = ApiRequest.builder()
                .url("https://user-service.example.com/users")
                .method("GET")
                .build();
        
        ApiResponse<String> response = mockApiService.executeMock(request, String.class);
        
        assertTrue(response.isSuccess());
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("users"));
        assertTrue(response.getBody().contains("total"));
        
        // Check service-specific headers
        assertTrue(response.getHeaders().containsKey("X-User-Service"));
        assertTrue(response.getHeaders().containsKey("X-Request-Id"));
    }
    
    @Test
    public void testUserServiceGetSpecificUser() {
        ApiRequest request = ApiRequest.builder()
                .url("https://user-service.example.com/users/1")
                .method("GET")
                .build();
        
        ApiResponse<String> response = mockApiService.executeMock(request, String.class);
        
        assertTrue(response.isSuccess());
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("john.doe@example.com"));
        assertTrue(response.getBody().contains("John"));
    }
    
    @Test
    public void testUserServiceUserNotFound() {
        ApiRequest request = ApiRequest.builder()
                .url("https://user-service.example.com/users/999999")
                .method("GET")
                .build();
        
        ApiResponse<String> response = mockApiService.executeMock(request, String.class);
        
        assertFalse(response.isSuccess());
        assertEquals(404, response.getStatusCode());
        assertEquals("USER_NOT_FOUND", response.getErrorCode());
        assertTrue(response.getBody().contains("User not found"));
    }
    
    @Test
    public void testUserServiceCreateUser() {
        ApiRequest request = ApiRequest.builder()
                .url("https://user-service.example.com/users")
                .method("POST")
                .header("Content-Type", "application/json")
                .body("{\"email\":\"test@example.com\",\"firstName\":\"Test\",\"lastName\":\"User\"}")
                .build();
        
        ApiResponse<String> response = mockApiService.executeMock(request, String.class);
        
        assertTrue(response.isSuccess());
        assertEquals(201, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("User created successfully"));
        assertTrue(response.getBody().contains("test@example.com"));
    }
    
    @Test
    public void testUserServiceCreateUserValidationError() {
        ApiRequest request = ApiRequest.builder()
                .url("https://user-service.example.com/users")
                .method("POST")
                .header("Content-Type", "application/json")
                .body("{\"lastName\":\"User\"}")  // Missing required email and firstName
                .build();
        
        ApiResponse<String> response = mockApiService.executeMock(request, String.class);
        
        assertFalse(response.isSuccess());
        assertEquals(400, response.getStatusCode());
        assertEquals("VALIDATION_ERROR", response.getErrorCode());
        assertTrue(response.getBody().contains("Missing required fields"));
    }
    
    @Test
    public void testUserServiceUpdateUser() {
        ApiRequest request = ApiRequest.builder()
                .url("https://user-service.example.com/users/1")
                .method("PUT")
                .header("Content-Type", "application/json")
                .body("{\"firstName\":\"Updated\",\"lastName\":\"Name\"}")
                .build();
        
        ApiResponse<String> response = mockApiService.executeMock(request, String.class);
        
        assertTrue(response.isSuccess());
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("User updated successfully"));
        assertTrue(response.getBody().contains("Updated"));
    }
    
    @Test
    public void testUserServiceDeleteUser() {
        ApiRequest request = ApiRequest.builder()
                .url("https://user-service.example.com/users/2")
                .method("DELETE")
                .build();
        
        ApiResponse<String> response = mockApiService.executeMock(request, String.class);
        
        assertTrue(response.isSuccess());
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("User deleted successfully"));
    }
    
    @Test
    public void testUserServiceAuthentication() {
        ApiRequest request = ApiRequest.builder()
                .url("https://user-service.example.com/auth/login")
                .method("POST")
                .header("Content-Type", "application/json")
                .body("{\"email\":\"user@example.com\",\"password\":\"password123\"}")
                .build();
        
        ApiResponse<String> response = mockApiService.executeMock(request, String.class);
        
        assertTrue(response.isSuccess());
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("token"));
        assertTrue(response.getBody().contains("expiresIn"));
    }
    
    @Test
    public void testScenarioBasedMocking() {
        String[] scenarios = {
            "user_not_found", "validation_error", "unauthorized", 
            "forbidden", "server_error"
        };
        
        int[] expectedStatusCodes = {404, 400, 401, 403, 500};
        
        for (int i = 0; i < scenarios.length; i++) {
            ApiRequest request = ApiRequest.builder()
                    .url("https://user-service.example.com/users/1")
                    .method("GET")
                    .header("X-Mock-Scenario", scenarios[i])
                    .build();
            
            ApiResponse<String> response = mockApiService.executeMock(request, String.class);
            
            assertFalse(response.isSuccess());
            assertEquals(expectedStatusCodes[i], response.getStatusCode());
            assertEquals("USER_SERVICE_ERROR", response.getErrorCode());
        }
    }
    
    @Test
    public void testMockManagement() {
        // Test mock summary
        Map<String, Object> summary = mockApiService.getMockSummary();
        assertNotNull(summary);
        assertTrue(summary.containsKey("customApiMocks"));
        assertTrue(summary.containsKey("totalCustomMocks"));
        assertEquals(2, summary.get("totalCustomMocks"));
        
        // Test removing a mock
        mockApiService.removeApiMock("user-service");
        assertFalse(mockApiService.hasApiMock("user-service"));
        
        // Test clearing all mocks
        mockApiService.clearCustomApiMocks();
        assertFalse(mockApiService.hasApiMock("payment-api"));
        
        summary = mockApiService.getMockSummary();
        assertEquals(0, summary.get("totalCustomMocks"));
    }
    
    @Test
    public void testMockReset() {
        // Test that reset functionality works by making API calls and verifying mock state
        ApiRequest request = ApiRequest.builder()
                .url("https://payment.gateway.com/process")
                .method("POST")
                .header("X-Mock-Scenario", "process_success")
                .body("{\"amount\":100.00}")
                .build();
        
        // Make several requests to ensure the mock is working
        ApiResponse<String> response1 = mockApiService.executeMock(request, String.class);
        ApiResponse<String> response2 = mockApiService.executeMock(request, String.class);
        
        // All should be successful
        assertTrue(response1.isSuccess());
        assertTrue(response2.isSuccess());
        assertTrue(response1.getBody().contains("transactionId"));
        assertTrue(response2.getBody().contains("transactionId"));
        
        // Verify that the custom mock is registered
        assertTrue(mockApiService.hasApiMock("payment-api"));
        
        // Reset payment mock
        mockApiService.resetApiMock("payment-api");
        
        // After reset, mock should still be available and working
        ApiResponse<String> response3 = mockApiService.executeMock(request, String.class);
        assertTrue(response3.isSuccess());
        assertTrue(response3.getBody().contains("transactionId"));
        
        // Mock should still be registered after reset
        assertTrue(mockApiService.hasApiMock("payment-api"));
    }
    
    @Test
    public void testCombinedMocking() {
        // Register a general mock
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
        
        assertTrue(paymentResponse.isSuccess());
        assertTrue(paymentResponse.getBody().contains("transactionId"));
        assertTrue(paymentResponse.getHeaders().containsKey("X-Payment-Gateway"));
        
        // Test general mock (Notification Service)
        ApiRequest notificationRequest = ApiRequest.builder()
                .url("https://notification.service.com/send")
                .method("POST")
                .body("{\"message\":\"Hello\"}")
                .build();
        ApiResponse<String> notificationResponse = mockApiService.executeMock(notificationRequest, String.class);
        
        assertTrue(notificationResponse.isSuccess());
        assertTrue(notificationResponse.getBody().contains("sent"));
        assertTrue(notificationResponse.getBody().contains("msg_12345"));
    }
    
    @Test
    public void testRequestCounting() {
        String paymentUrl = "https://payment.gateway.com/process";
        String userUrl = "https://user-service.example.com/users";
        
        // Make multiple requests
        for (int i = 0; i < 3; i++) {
            ApiRequest paymentRequest = ApiRequest.builder()
                    .url(paymentUrl)
                    .method("POST")
                    .header("X-Mock-Scenario", "process_success")
                    .body("{\"amount\":100.00}")
                    .build();
            mockApiService.executeMock(paymentRequest, String.class);
            
            ApiRequest userRequest = ApiRequest.builder()
                    .url(userUrl)
                    .method("GET")
                    .build();
            mockApiService.executeMock(userRequest, String.class);
        }
        
        // Verify request counts
        assertEquals(3, mockApiService.getRequestCount(paymentUrl));
        assertEquals(3, mockApiService.getRequestCount(userUrl));
    }
    
    @Test
    public void testUnknownUrlHandling() {
        ApiRequest request = ApiRequest.builder()
                .url("https://unknown.service.com/endpoint")
                .method("GET")
                .build();
        
        ApiResponse<String> response = mockApiService.executeMock(request, String.class);
        
        assertFalse(response.isSuccess());
        assertEquals(404, response.getStatusCode());
        assertEquals("MOCK_NOT_FOUND", response.getErrorCode());
        assertTrue(response.getErrorMessage().contains("No mock response"));
    }
    
    private String extractTransactionId(String responseBody) {
        try {
            // Simple extraction for test purposes
            int start = responseBody.indexOf("txn_") + 4;
            int end = responseBody.indexOf("\"", start);
            return responseBody.substring(start, end);
        } catch (Exception e) {
            return "0";
        }
    }
} 