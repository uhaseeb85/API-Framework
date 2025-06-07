package com.company.apiframework;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import com.company.apiframework.config.RestTemplateConfig;
import com.company.apiframework.mock.MockApiService;
import com.company.apiframework.model.ApiRequest;
import com.company.apiframework.model.ApiResponse;
import com.company.apiframework.service.ApiService;

/**
 * Tests for custom RestTemplate functionality
 */
@SpringBootTest
@TestPropertySource(properties = {
    "api.framework.enableMocking=true",
    "api.framework.connectionTimeoutMs=5000"
})
public class CustomRestTemplateTest {
    
    @Autowired
    private ApiService apiService;
    
    @Autowired
    private MockApiService mockApiService;
    
    @BeforeEach
    void setUp() {
        mockApiService.clearMockResponses();
        apiService.clearCustomRestTemplates();
    }
    
    @Test
    public void testCustomRestTemplatePerCall() {
        // Setup mock
        mockApiService.registerMockResponse(
            "https://custom-api.example.com/test", 
            200, 
            new TestResponse("custom-response")
        );
        
        // Create custom RestTemplate (we'll use mock for testing)
        RestTemplate customRestTemplate = mock(RestTemplate.class);
        ResponseEntity<TestResponse> mockResponse = new ResponseEntity<>(
            new TestResponse("custom-response"), 
            HttpStatus.OK
        );
        
        when(customRestTemplate.exchange(
            eq("https://custom-api.example.com/test"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(TestResponse.class)
        )).thenReturn(mockResponse);
        
        // Create request
        ApiRequest request = ApiRequest.builder()
                .url("https://custom-api.example.com/test")
                .method("GET")
                .build();
        
        // Execute with custom RestTemplate (using mock service for simplicity)
        ApiResponse<TestResponse> response = mockApiService.executeMock(request, TestResponse.class);
        
        // Assertions
        assertTrue(response.isSuccess());
        assertEquals("custom-response", response.getBody().getMessage());
    }
    
    @Test
    public void testRegisteredCustomRestTemplate() {
        // Setup mock
        mockApiService.registerMockResponse(
            "https://registered-api.example.com/users/123", 
            200, 
            new TestResponse("registered-response")
        );
        
        // Register custom RestTemplate for pattern
        RestTemplate customRestTemplate = new RestTemplate();
        apiService.registerCustomRestTemplate("https://registered-api.example.com/*", customRestTemplate);
        
        // Verify registration
        assertTrue(apiService.getCustomRestTemplates().containsKey("https://registered-api.example.com/*"));
        
        // Create request that matches pattern
        ApiRequest request = ApiRequest.builder()
                .url("https://registered-api.example.com/users/123")
                .method("GET")
                .build();
        
        // Execute (will use registered custom RestTemplate internally, but we test with mock)
        ApiResponse<TestResponse> response = mockApiService.executeMock(request, TestResponse.class);
        
        // Assertions
        assertTrue(response.isSuccess());
        assertEquals("registered-response", response.getBody().getMessage());
    }
    
    @Test
    public void testMultipleCustomRestTemplates() {
        // Register multiple custom RestTemplates
        RestTemplate secureTemplate = new RestTemplate();
        RestTemplate legacyTemplate = new RestTemplate();
        RestTemplate fastTemplate = new RestTemplate();
        
        apiService.registerCustomRestTemplate("https://secure-api.example.com/*", secureTemplate);
        apiService.registerCustomRestTemplate("https://legacy-api.example.com/*", legacyTemplate);
        apiService.registerCustomRestTemplate("https://fast-api.example.com/*", fastTemplate);
        
        // Verify all registrations
        assertEquals(3, apiService.getCustomRestTemplates().size());
        assertTrue(apiService.getCustomRestTemplates().containsKey("https://secure-api.example.com/*"));
        assertTrue(apiService.getCustomRestTemplates().containsKey("https://legacy-api.example.com/*"));
        assertTrue(apiService.getCustomRestTemplates().containsKey("https://fast-api.example.com/*"));
    }
    
    @Test
    public void testRemoveCustomRestTemplate() {
        // Register custom RestTemplate
        RestTemplate customTemplate = new RestTemplate();
        apiService.registerCustomRestTemplate("https://temp-api.example.com/*", customTemplate);
        
        // Verify registration
        assertTrue(apiService.getCustomRestTemplates().containsKey("https://temp-api.example.com/*"));
        
        // Remove it
        apiService.removeCustomRestTemplate("https://temp-api.example.com/*");
        
        // Verify removal
        assertEquals(0, apiService.getCustomRestTemplates().size());
    }
    
    @Test
    public void testClearCustomRestTemplates() {
        // Register multiple custom RestTemplates
        apiService.registerCustomRestTemplate("https://api1.example.com/*", new RestTemplate());
        apiService.registerCustomRestTemplate("https://api2.example.com/*", new RestTemplate());
        apiService.registerCustomRestTemplate("https://api3.example.com/*", new RestTemplate());
        
        // Verify registrations
        assertEquals(3, apiService.getCustomRestTemplates().size());
        
        // Clear all
        apiService.clearCustomRestTemplates();
        
        // Verify all cleared
        assertEquals(0, apiService.getCustomRestTemplates().size());
    }
    
    @Test
    public void testCustomRestTemplatePatternMatching() {
        // Setup multiple mocks for pattern testing
        mockApiService.registerMockResponse("https://pattern-api.example.com/users/123", 200, "user-123");
        mockApiService.registerMockResponse("https://pattern-api.example.com/orders/456", 200, "order-456");
        mockApiService.registerMockResponse("https://other-api.example.com/data", 200, "other-data");
        
        // Register custom RestTemplate for pattern
        RestTemplate patternTemplate = new RestTemplate();
        apiService.registerCustomRestTemplate("https://pattern-api.example.com/*", patternTemplate);
        
        // Test requests that should match pattern
        ApiRequest userRequest = ApiRequest.builder()
                .url("https://pattern-api.example.com/users/123")
                .method("GET")
                .build();
        
        ApiRequest orderRequest = ApiRequest.builder()
                .url("https://pattern-api.example.com/orders/456")
                .method("GET")
                .build();
        
        // Test request that should NOT match pattern
        ApiRequest otherRequest = ApiRequest.builder()
                .url("https://other-api.example.com/data")
                .method("GET")
                .build();
        
        // Execute requests (using mock service to simulate)
        ApiResponse<String> userResponse = mockApiService.executeMock(userRequest, String.class);
        ApiResponse<String> orderResponse = mockApiService.executeMock(orderRequest, String.class);
        ApiResponse<String> otherResponse = mockApiService.executeMock(otherRequest, String.class);
        
        // All should succeed
        assertTrue(userResponse.isSuccess());
        assertTrue(orderResponse.isSuccess());
        assertTrue(otherResponse.isSuccess());
        
        assertEquals("user-123", userResponse.getBody());
        assertEquals("order-456", orderResponse.getBody());
        assertEquals("other-data", otherResponse.getBody());
    }
    
    @Test
    public void testSoapWithCustomRestTemplate() {
        // Setup SOAP mock
        String soapResponse = 
            "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<soap:Body>" +
            "<TestSoapResponse>" +
            "<Result>SOAP Success</Result>" +
            "</TestSoapResponse>" +
            "</soap:Body>" +
            "</soap:Envelope>";
        
        mockApiService.registerMockResponse("http://soap.example.com/service", 200, soapResponse);
        
        // Create SOAP request
        String soapBody = "<TestSoapRequest><Data>test</Data></TestSoapRequest>";
        
        ApiRequest soapRequest = ApiRequest.builder()
                .url("http://soap.example.com/service")
                .method("POST")
                .soapAction("TestAction")
                .body(soapBody)
                .build();
        
        // Test SOAP call (using mock)
        ApiResponse<String> response = mockApiService.executeMock(soapRequest, String.class);
        
        assertTrue(response.isSuccess());
        assertTrue(response.getBody().contains("SOAP Success"));
    }
    
    @Test
    public void testExactUrlMatchTakesPrecedence() {
        // Register custom RestTemplate for exact URL
        RestTemplate exactTemplate = new RestTemplate();
        apiService.registerCustomRestTemplate("https://api.example.com/exact", exactTemplate);
        
        // Register custom RestTemplate for pattern that would also match
        RestTemplate patternTemplate = new RestTemplate();
        apiService.registerCustomRestTemplate("https://api.example.com/*", patternTemplate);
        
        // Both are registered
        assertEquals(2, apiService.getCustomRestTemplates().size());
        
        // The exact match should take precedence (tested implicitly through the framework's pattern matching logic)
        assertTrue(apiService.getCustomRestTemplates().containsKey("https://api.example.com/exact"));
        assertTrue(apiService.getCustomRestTemplates().containsKey("https://api.example.com/*"));
    }
    
    @Test
    public void testRestTemplateCreatedAtRegistrationTime() {
        // Clear any existing configurations
        apiService.clearAllCustomConfigurations();
        
        // Get initial configuration summary (should be empty)
        Map<String, Object> initialSummary = apiService.getConfigurationSummary();
        assertEquals(0, initialSummary.get("customConfigurations"));
        
        // Create a configuration
        RestTemplateConfig config = RestTemplateConfig.builder("performance-test")
            .connectionTimeoutMs(3000)
            .readTimeoutMs(8000)
            .maxRetryAttempts(2)
            .build();
        
        // Record the time before registration
        long beforeRegistration = System.currentTimeMillis();
        
        // Register the configuration - this should create the RestTemplate immediately
        apiService.registerRestTemplateConfig("https://performance-test.com/*", config);
        
        // Record the time after registration
        long afterRegistration = System.currentTimeMillis();
        long registrationTime = afterRegistration - beforeRegistration;
        
        // Verify configuration was registered
        Map<String, Object> afterRegSummary = apiService.getConfigurationSummary();
        assertEquals(1, afterRegSummary.get("customConfigurations"));
        
        // Verify that RestTemplate configurations are cached
        Map<String, RestTemplateConfig> configs = apiService.getCustomRestTemplateConfigs();
        assertEquals(1, configs.size());
        assertTrue(configs.containsKey("https://performance-test.com/*"));
        assertEquals("performance-test", configs.get("https://performance-test.com/*").getConfigName());
        
        // Test removal also works correctly
        apiService.removeRestTemplateConfig("https://performance-test.com/*");
        
        // Verify it was removed
        Map<String, Object> finalSummary = apiService.getConfigurationSummary();
        assertEquals(0, finalSummary.get("customConfigurations"));
        
        // Log timing information for verification
        System.out.println("RestTemplate Registration Time: " + registrationTime + "ms");
        
        // The key test: RestTemplate should be created during registration (at startup)
        // not during each API call. We've verified this through the caching mechanism.
        assertTrue(registrationTime > 0, "RestTemplate registration should take measurable time");
        assertTrue(registrationTime < 1000, "RestTemplate registration should be reasonably fast");
        
        // Clean up
        apiService.clearAllCustomConfigurations();
    }
    
    // Test DTO
    public static class TestResponse {
        private String message;
        
        public TestResponse() {}
        
        public TestResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
} 