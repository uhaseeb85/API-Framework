package com.company.apiframework.example;

import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.company.apiframework.model.ApiRequest;
import com.company.apiframework.model.ApiResponse;
import com.company.apiframework.service.ApiService;

/**
 * Example showing how to use custom RestTemplates for different API integrations
 */
@Component
public class CustomRestTemplateExample {
    
    @Autowired
    private ApiService apiService;
    
    /**
     * Example 1: Custom RestTemplate for a specific API call
     */
    public void exampleCustomRestTemplatePerCall() {
        // Create custom RestTemplate with specific timeout
        RestTemplate customRestTemplate = createCustomRestTemplate(60000, 120000);
        
        ApiRequest request = ApiRequest.builder()
                .url("https://slow-api.example.com/data")
                .method("GET")
                .header("Accept", "application/json")
                .build();
        
        // Use custom RestTemplate for this specific call
        ApiResponse<String> response = apiService.executeRest(request, customRestTemplate);
        
        if (response.isSuccess()) {
            System.out.println("Slow API call successful: " + response.getBody());
        }
    }
    
    /**
     * Example 2: Register custom RestTemplate for URL patterns
     */
    public void exampleRegisteredCustomRestTemplates() {
        // Create different RestTemplates for different services
        
        // 1. High-security API with custom SSL configuration
        RestTemplate secureRestTemplate = createSecureRestTemplate();
        apiService.registerCustomRestTemplate("https://secure-api.example.com/*", secureRestTemplate);
        
        // 2. Slow API with extended timeouts
        RestTemplate slowApiRestTemplate = createCustomRestTemplate(30000, 60000);
        apiService.registerCustomRestTemplate("https://slow-api.example.com/*", slowApiRestTemplate);
        
        // 3. Legacy API with custom authentication
        RestTemplate legacyApiRestTemplate = createLegacyApiRestTemplate();
        apiService.registerCustomRestTemplate("https://legacy-api.example.com/*", legacyApiRestTemplate);
        
        // Now all calls to these patterns will automatically use the custom RestTemplates
        
        // This will use secureRestTemplate automatically
        ApiRequest secureRequest = ApiRequest.builder()
                .url("https://secure-api.example.com/users")
                .method("GET")
                .build();
        ApiResponse<String> secureResponse = apiService.executeRest(secureRequest);
        
        // This will use slowApiRestTemplate automatically
        ApiRequest slowRequest = ApiRequest.builder()
                .url("https://slow-api.example.com/process")
                .method("POST")
                .body("{\"data\":\"large-dataset\"}")
                .build();
        ApiResponse<String> slowResponse = apiService.executeRest(slowRequest);
        
        // This will use legacyApiRestTemplate automatically
        ApiRequest legacyRequest = ApiRequest.builder()
                .url("https://legacy-api.example.com/old-endpoint")
                .method("GET")
                .build();
        ApiResponse<String> legacyResponse = apiService.executeRest(legacyRequest);
    }
    
    /**
     * Example 3: Per-client custom configuration
     */
    public void examplePerClientConfiguration() {
        // Different configurations for different external systems
        
        // Payment API - needs special SSL and authentication
        RestTemplate paymentApiTemplate = createPaymentApiRestTemplate();
        
        ApiRequest paymentRequest = ApiRequest.builder()
                .url("https://payment.gateway.com/process")
                .method("POST")
                .header("Authorization", "Bearer payment-token")
                .body("{\"amount\":100,\"currency\":\"USD\"}")
                .build();
        
        ApiResponse<PaymentResponseDto> paymentResponse = 
                apiService.executeRest(paymentRequest, PaymentResponseDto.class, paymentApiTemplate);
        
        // Notification API - needs rate limiting
        RestTemplate notificationApiTemplate = createRateLimitedRestTemplate();
        
        ApiRequest notificationRequest = ApiRequest.builder()
                .url("https://notification.service.com/send")
                .method("POST")
                .body("{\"message\":\"Hello\",\"recipient\":\"user@example.com\"}")
                .build();
        
        ApiResponse<String> notificationResponse = 
                apiService.executeRest(notificationRequest, notificationApiTemplate);
    }
    
    /**
     * Example 4: SOAP with custom RestTemplate
     */
    public void exampleSoapWithCustomRestTemplate() {
        // Create RestTemplate optimized for SOAP calls
        RestTemplate soapRestTemplate = createSoapOptimizedRestTemplate();
        
        String soapBody = 
            "<GetUserRequest>" +
            "  <UserId>12345</UserId>" +
            "</GetUserRequest>";
        
        ApiRequest soapRequest = ApiRequest.builder()
                .url("http://soap.legacy-system.com/UserService")
                .method("POST")
                .soapAction("GetUser")
                .body(soapBody)
                .build();
        
        // Use custom RestTemplate for SOAP call
        ApiResponse<String> soapResponse = apiService.executeSoap(soapRequest, soapRestTemplate);
        
        if (soapResponse.isSuccess()) {
            System.out.println("SOAP response: " + soapResponse.getBody());
        }
    }
    
    /**
     * Example 5: Async calls with custom RestTemplate
     */
    public void exampleAsyncWithCustomRestTemplate() {
        RestTemplate asyncRestTemplate = createAsyncOptimizedRestTemplate();
        
        ApiRequest asyncRequest = ApiRequest.builder()
                .url("https://async-api.example.com/long-process")
                .method("POST")
                .body("{\"processId\":\"async-123\"}")
                .build();
        
        apiService.executeAsync(asyncRequest, ProcessResultDto.class, 
            new com.company.apiframework.client.ApiCallback<ProcessResultDto>() {
                @Override
                public void onSuccess(com.company.apiframework.model.ApiResponse<ProcessResultDto> response) {
                    System.out.println("Async process completed: " + response.getBody().getStatus());
                }
                
                @Override
                public void onError(com.company.apiframework.model.ApiResponse<ProcessResultDto> response) {
                    System.err.println("Async process failed: " + response.getErrorMessage());
                }
                
                @Override
                public void onException(Exception exception) {
                    System.err.println("Async process exception: " + exception.getMessage());
                }
            }, 
            asyncRestTemplate  // Custom RestTemplate for async call
        );
    }
    
    // Helper methods to create custom RestTemplates
    
    private RestTemplate createCustomRestTemplate(int connectTimeout, int readTimeout) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        
        return new RestTemplate(factory);
    }
    
    private RestTemplate createSecureRestTemplate() {
        try {
            // Create custom SSL context (for demo - in real use, configure properly)
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                public void checkServerTrusted(X509Certificate[] certs, String authType) { }
            }}, null);
            
            HttpClient httpClient = HttpClientBuilder.create()
                    .setSSLContext(sslContext)
                    .build();
            
            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
            factory.setHttpClient(httpClient);
            
            return new RestTemplate(factory);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create secure RestTemplate", e);
        }
    }
    
    private RestTemplate createLegacyApiRestTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(30000);
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        // Add custom interceptors for legacy API
        restTemplate.getInterceptors().add((request, body, execution) -> {
            // Add legacy API specific headers
            request.getHeaders().add("X-Legacy-Version", "1.0");
            request.getHeaders().add("X-Client-Id", "modern-client");
            return execution.execute(request, body);
        });
        
        return restTemplate;
    }
    
    private RestTemplate createPaymentApiRestTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(15000);
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        // Add payment-specific interceptors
        restTemplate.getInterceptors().add((request, body, execution) -> {
            // Add payment security headers
            request.getHeaders().add("X-Payment-Version", "2.0");
            request.getHeaders().add("X-Idempotency-Key", java.util.UUID.randomUUID().toString());
            return execution.execute(request, body);
        });
        
        return restTemplate;
    }
    
    private RestTemplate createRateLimitedRestTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(10000);
        
        // Configure for rate limiting (smaller connection pool)
        HttpClient httpClient = HttpClientBuilder.create()
                .setMaxConnTotal(5)
                .setMaxConnPerRoute(2)
                .build();
        
        factory.setHttpClient(httpClient);
        return new RestTemplate(factory);
    }
    
    private RestTemplate createSoapOptimizedRestTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(8000);
        factory.setReadTimeout(45000);  // SOAP can be slower
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        // Add SOAP-specific interceptors
        restTemplate.getInterceptors().add((request, body, execution) -> {
            // Ensure proper SOAP headers
            if (!request.getHeaders().containsKey("Content-Type")) {
                request.getHeaders().add("Content-Type", "text/xml; charset=utf-8");
            }
            return execution.execute(request, body);
        });
        
        return restTemplate;
    }
    
    private RestTemplate createAsyncOptimizedRestTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(60000);  // Long timeout for async processes
        
        // Optimize for async calls
        HttpClient httpClient = HttpClientBuilder.create()
                .setMaxConnTotal(50)
                .setMaxConnPerRoute(10)
                .build();
        
        factory.setHttpClient(httpClient);
        return new RestTemplate(factory);
    }
    
    // Example DTOs
    public static class PaymentResponseDto {
        private String transactionId;
        private String status;
        private String message;
        
        // Getters and setters
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    public static class ProcessResultDto {
        private String processId;
        private String status;
        private String result;
        
        // Getters and setters
        public String getProcessId() { return processId; }
        public void setProcessId(String processId) { this.processId = processId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
    }
} 