package com.company.apiframework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.company.apiframework.mock.MockApiService;
import com.company.apiframework.model.ApiRequest;
import com.company.apiframework.model.ApiResponse;

/**
 * Comprehensive tests for the API Integration Framework
 */
@SpringBootTest
@TestPropertySource(properties = {
    "api.framework.enableMocking=true",
    "api.framework.connectionTimeoutMs=5000",
    "api.framework.readTimeoutMs=10000"
})
public class ApiFrameworkTest {
    
    @Autowired
    private MockApiService mockApiService;
    
    @BeforeEach
    void setUp() {
        mockApiService.clearMockResponses();
    }
    
    @Test
    public void testRestApiCall() {
        // Setup mock response
        TestDto expectedResponse = new TestDto("test-id", "Test Message");
        mockApiService.registerMockResponse(
            "https://api.example.com/test", 
            200, 
            expectedResponse
        );
        
        // Create request
        ApiRequest request = ApiRequest.builder()
                .url("https://api.example.com/test")
                .method("GET")
                .header("Accept", "application/json")
                .build();
        
        // Execute using mock
        ApiResponse<TestDto> response = mockApiService.executeMock(request, TestDto.class);
        
        // Assertions
        assertTrue(response.isSuccess());
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test-id", response.getBody().getId());
        assertEquals("Test Message", response.getBody().getMessage());
    }
    
    @Test
    public void testSoapApiCall() {
        // Setup mock SOAP response
        String soapResponse = 
            "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<soap:Body>" +
            "<GetWeatherResponse>" +
            "<Temperature>22</Temperature>" +
            "<Condition>Sunny</Condition>" +
            "</GetWeatherResponse>" +
            "</soap:Body>" +
            "</soap:Envelope>";
        
        mockApiService.registerMockResponse(
            "http://soap.example.com/weather", 
            200, 
            soapResponse
        );
        
        // Create SOAP request
        String soapBody = 
            "<GetWeatherRequest>" +
            "<City>London</City>" +
            "</GetWeatherRequest>";
        
        ApiRequest request = ApiRequest.builder()
                .url("http://soap.example.com/weather")
                .method("POST")
                .soapAction("GetWeather")
                .body(soapBody)
                .build();
        
        // Execute using mock
        ApiResponse<String> response = mockApiService.executeMock(request, String.class);
        
        // Assertions
        assertTrue(response.isSuccess());
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("GetWeatherResponse"));
    }
    
    @Test
    public void testRequestBuilder() {
        ApiRequest request = ApiRequest.builder()
                .url("https://api.example.com/users")
                .method("POST")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer token123")
                .parameter("limit", 10)
                .parameter("offset", 0)
                .body("{\"name\":\"John\"}")
                .build();
        
        assertEquals("https://api.example.com/users", request.getUrl());
        assertEquals("POST", request.getMethod());
        assertEquals("application/json", request.getHeaders().get("Content-Type"));
        assertEquals("Bearer token123", request.getHeaders().get("Authorization"));
        assertEquals(10, request.getParameters().get("limit"));
        assertEquals(0, request.getParameters().get("offset"));
        assertEquals("{\"name\":\"John\"}", request.getBody());
    }
    
    @Test
    public void testApiResponseHandling() {
        ApiResponse<String> response = new ApiResponse<>(200, "Success");
        
        assertTrue(response.isSuccess());
        assertFalse(response.hasError());
        assertEquals(200, response.getStatusCode());
        assertEquals("Success", response.getBody());
        
        // Test error handling
        response.markAsError("ERROR_CODE", "Something went wrong");
        
        assertFalse(response.isSuccess());
        assertTrue(response.hasError());
        assertEquals("ERROR_CODE", response.getErrorCode());
        assertEquals("Something went wrong", response.getErrorMessage());
    }
    
    @Test
    public void testMockResponsePatternMatching() {
        // Register mock with wildcard pattern
        mockApiService.registerMockResponse(
            "https://api.example.com/users/*", 
            200, 
            new TestDto("user-123", "User found")
        );
        
        ApiRequest request = ApiRequest.builder()
                .url("https://api.example.com/users/123")
                .method("GET")
                .build();
        
        ApiResponse<TestDto> response = mockApiService.executeMock(request, TestDto.class);
        
        assertTrue(response.isSuccess());
        assertEquals("user-123", response.getBody().getId());
    }
    
    @Test
    public void testMockRequestCounting() {
        String url = "https://api.example.com/count-test";
        
        mockApiService.registerMockResponse(url, 200, "OK");
        
        assertEquals(0, mockApiService.getRequestCount(url));
        
        ApiRequest request = ApiRequest.builder().url(url).method("GET").build();
        
        mockApiService.executeMock(request, String.class);
        assertEquals(1, mockApiService.getRequestCount(url));
        
        mockApiService.executeMock(request, String.class);
        assertEquals(2, mockApiService.getRequestCount(url));
    }
    
    @Test
    public void testErrorHandling() {
        // Test 404 response
        ApiRequest request = ApiRequest.builder()
                .url("https://api.example.com/not-found")
                .method("GET")
                .build();
        
        ApiResponse<String> response = mockApiService.executeMock(request, String.class);
        
        assertFalse(response.isSuccess());
        assertEquals(404, response.getStatusCode());
        assertEquals("MOCK_NOT_FOUND", response.getErrorCode());
    }
    
    /**
     * Test DTO class
     */
    public static class TestDto {
        private String id;
        private String message;
        
        public TestDto() {}
        
        public TestDto(String id, String message) {
            this.id = id;
            this.message = message;
        }
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
} 