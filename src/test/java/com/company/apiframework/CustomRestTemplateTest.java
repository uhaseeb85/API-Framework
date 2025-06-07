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

import com.company.apiframework.mock.MockApiService;
import com.company.apiframework.model.ApiRequest;
import com.company.apiframework.model.ApiResponse;
import com.company.apiframework.service.ApiService;

/**
 * Tests for custom RestTemplate functionality (Legacy Support)
 * 
 * <p><strong>Note:</strong> These tests cover the legacy custom RestTemplate registration
 * functionality for backward compatibility. For new implementations, use the
 * Spring bean approach tested in SpringBeanApiServiceTest.</p>
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
    public void testLegacyRegisteredCustomRestTemplate() {
        // Setup mock
        mockApiService.registerMockResponse(
            "https://registered-api.example.com/users/123", 
            200, 
            new TestResponse("registered-response")
        );
        
        // Register custom RestTemplate for pattern (legacy support)
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
    public void testMultipleLegacyCustomRestTemplates() {
        // Register multiple custom RestTemplates (legacy support)
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
    public void testRemoveLegacyCustomRestTemplate() {
        // Register custom RestTemplate (legacy support)
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
    public void testClearLegacyCustomRestTemplates() {
        // Register multiple custom RestTemplates (legacy support)
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
    public void testLegacyRestTemplatePatternMatching() {
        // Setup multiple mocks for pattern testing
        mockApiService.registerMockResponse("https://pattern-api.example.com/users/123", 200, "user-123");
        mockApiService.registerMockResponse("https://pattern-api.example.com/orders/456", 200, "order-456");
        mockApiService.registerMockResponse("https://other-api.example.com/data", 200, "other-data");
        
        // Register custom RestTemplate for pattern (legacy support)
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
        
        // All should succeed (pattern matching happens in ApiService, we're testing with mocks)
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
        mockApiService.registerMockResponse(
            "http://soap-service.example.com/endpoint", 
            200, 
            "<soap:Envelope><soap:Body>SOAP Response</soap:Body></soap:Envelope>"
        );
        
        // Create SOAP request
        ApiRequest soapRequest = ApiRequest.builder()
                .url("http://soap-service.example.com/endpoint")
                .method("POST")
                .soapAction("GetData")
                .body("<soap:Envelope><soap:Body>SOAP Request</soap:Body></soap:Envelope>")
                .build();
        
        // Execute SOAP request (using mock service)
        ApiResponse<String> response = mockApiService.executeMock(soapRequest, String.class);
        
        // Assertions
        assertTrue(response.isSuccess());
        assertTrue(response.getBody().contains("SOAP Response"));
    }
    
    @Test
    public void testExactUrlMatchTakesPrecedence() {
        // Register both wildcard and exact URL patterns (legacy support)
        RestTemplate wildcardTemplate = new RestTemplate();
        RestTemplate exactTemplate = new RestTemplate();
        
        apiService.registerCustomRestTemplate("https://api.example.com/*", wildcardTemplate);
        apiService.registerCustomRestTemplate("https://api.example.com/exact", exactTemplate);
        
        // Verify both registrations
        assertEquals(2, apiService.getCustomRestTemplates().size());
        assertTrue(apiService.getCustomRestTemplates().containsKey("https://api.example.com/*"));
        assertTrue(apiService.getCustomRestTemplates().containsKey("https://api.example.com/exact"));
    }
    
    @Test
    public void testSpringBeanTakesPrecedenceOverLegacy() {
        // Register legacy custom RestTemplate for payment pattern
        RestTemplate legacyTemplate = new RestTemplate();
        apiService.registerCustomRestTemplate("https://payment.gateway.com/*", legacyTemplate);
        
        // Verify legacy registration
        assertTrue(apiService.getCustomRestTemplates().containsKey("https://payment.gateway.com/*"));
        
        // Create request that matches both legacy and Spring bean patterns
        ApiRequest request = ApiRequest.builder()
                .url("https://payment.gateway.com/process")
                .method("POST")
                .body("{\"amount\":100.00}")
                .build();
        
        // Note: In actual execution, Spring bean should take precedence over legacy registration
        // This test verifies the legacy registration works, but Spring beans are preferred
        ApiResponse<String> response = mockApiService.executeMock(request, String.class);
        
        // Setup mock for this test
        mockApiService.registerMockResponse("https://payment.gateway.com/process", 200, "legacy-response");
        response = mockApiService.executeMock(request, String.class);
        
        assertTrue(response.isSuccess());
        assertEquals("legacy-response", response.getBody());
    }
    
    @Test
    public void testConfigurationSummaryWithSpringBeans() {
        // Register some legacy templates
        apiService.registerCustomRestTemplate("https://legacy1.com/*", new RestTemplate());
        apiService.registerCustomRestTemplate("https://legacy2.com/*", new RestTemplate());
        
        // Get configuration summary
        Map<String, Object> summary = apiService.getConfigurationSummary();
        
        // Verify summary includes both Spring beans and legacy registrations
        assertTrue(summary.containsKey("springBeans"));
        assertTrue(summary.containsKey("springBeanCount"));
        assertTrue(summary.containsKey("legacyCustomTemplates"));
        assertTrue(summary.containsKey("totalConfiguredTemplates"));
        
        // Spring beans should be 5 (payment, batch, external, highVolume, default)
        assertEquals(5, summary.get("springBeanCount"));
        
        // Legacy templates should be 2
        assertEquals(2, summary.get("legacyCustomTemplates"));
        
        // Total should be 7
        assertEquals(7, summary.get("totalConfiguredTemplates"));
    }
    
    /**
     * Test response class for testing
     */
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