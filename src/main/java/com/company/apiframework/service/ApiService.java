package com.company.apiframework.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.company.apiframework.client.ApiCallback;
import com.company.apiframework.client.ApiClient;
import com.company.apiframework.client.rest.RestClientFactory;
import com.company.apiframework.client.soap.SoapClientFactory;
import com.company.apiframework.config.ApiProperties;
import com.company.apiframework.model.ApiRequest;
import com.company.apiframework.model.ApiResponse;

/**
 * Main service for API integration providing unified interface for REST and SOAP calls
 * Supports custom RestTemplates for individual API integrations
 */
@Service
public class ApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);
    
    @Autowired
    private RestClientFactory restClientFactory;
    
    @Autowired
    private SoapClientFactory soapClientFactory;
    
    @Autowired
    private ApiProperties apiProperties;
    
    // Registry for custom RestTemplates per API endpoint
    private final Map<String, RestTemplate> customRestTemplates = new HashMap<>();
    
    /**
     * Execute REST API call
     */
    public <T> ApiResponse<T> executeRest(ApiRequest request, Class<T> responseType) {
        logger.debug("Executing REST API call to: {}", request.getUrl());
        ApiClient client = getRestClient(request.getUrl());
        return client.execute(request, responseType);
    }
    
    /**
     * Execute REST API call with custom RestTemplate
     */
    public <T> ApiResponse<T> executeRest(ApiRequest request, Class<T> responseType, RestTemplate customRestTemplate) {
        logger.debug("Executing REST API call to: {} with custom RestTemplate", request.getUrl());
        ApiClient client = restClientFactory.createClient(customRestTemplate);
        return client.execute(request, responseType);
    }
    
    /**
     * Execute REST API call with string response
     */
    public ApiResponse<String> executeRest(ApiRequest request) {
        return executeRest(request, String.class);
    }
    
    /**
     * Execute REST API call with custom RestTemplate and string response
     */
    public ApiResponse<String> executeRest(ApiRequest request, RestTemplate customRestTemplate) {
        return executeRest(request, String.class, customRestTemplate);
    }
    
    /**
     * Execute SOAP API call
     */
    public <T> ApiResponse<T> executeSoap(ApiRequest request, Class<T> responseType) {
        logger.debug("Executing SOAP API call to: {}", request.getUrl());
        ApiClient client = getSoapClient(request.getUrl());
        return client.execute(request, responseType);
    }
    
    /**
     * Execute SOAP API call with custom RestTemplate
     */
    public <T> ApiResponse<T> executeSoap(ApiRequest request, Class<T> responseType, RestTemplate customRestTemplate) {
        logger.debug("Executing SOAP API call to: {} with custom RestTemplate", request.getUrl());
        ApiClient client = soapClientFactory.createClient(customRestTemplate);
        return client.execute(request, responseType);
    }
    
    /**
     * Execute SOAP API call with string response
     */
    public ApiResponse<String> executeSoap(ApiRequest request) {
        return executeSoap(request, String.class);
    }
    
    /**
     * Execute SOAP API call with custom RestTemplate and string response
     */
    public ApiResponse<String> executeSoap(ApiRequest request, RestTemplate customRestTemplate) {
        return executeSoap(request, String.class, customRestTemplate);
    }
    
    /**
     * Execute API call based on protocol detection
     */
    public <T> ApiResponse<T> executeAuto(ApiRequest request, Class<T> responseType) {
        String protocol = detectProtocol(request);
        
        if ("SOAP".equalsIgnoreCase(protocol)) {
            return executeSoap(request, responseType);
        } else {
            return executeRest(request, responseType);
        }
    }
    
    /**
     * Execute API call based on protocol detection with custom RestTemplate
     */
    public <T> ApiResponse<T> executeAuto(ApiRequest request, Class<T> responseType, RestTemplate customRestTemplate) {
        String protocol = detectProtocol(request);
        
        if ("SOAP".equalsIgnoreCase(protocol)) {
            return executeSoap(request, responseType, customRestTemplate);
        } else {
            return executeRest(request, responseType, customRestTemplate);
        }
    }
    
    /**
     * Execute API call asynchronously
     */
    public <T> void executeAsync(ApiRequest request, Class<T> responseType, ApiCallback<T> callback) {
        String protocol = detectProtocol(request);
        ApiClient client;
        
        if ("SOAP".equalsIgnoreCase(protocol)) {
            client = getSoapClient(request.getUrl());
        } else {
            client = getRestClient(request.getUrl());
        }
        
        client.executeAsync(request, responseType, callback);
    }
    
    /**
     * Execute API call asynchronously with custom RestTemplate
     */
    public <T> void executeAsync(ApiRequest request, Class<T> responseType, ApiCallback<T> callback, RestTemplate customRestTemplate) {
        String protocol = detectProtocol(request);
        ApiClient client;
        
        if ("SOAP".equalsIgnoreCase(protocol)) {
            client = soapClientFactory.createClient(customRestTemplate);
        } else {
            client = restClientFactory.createClient(customRestTemplate);
        }
        
        client.executeAsync(request, responseType, callback);
    }
    
    /**
     * Register a custom RestTemplate for a specific API endpoint pattern
     * 
     * @param urlPattern URL pattern (supports wildcards like "https://api.example.com/*")
     * @param restTemplate Custom RestTemplate to use for this pattern
     */
    public void registerCustomRestTemplate(String urlPattern, RestTemplate restTemplate) {
        customRestTemplates.put(urlPattern, restTemplate);
        logger.info("Registered custom RestTemplate for URL pattern: {}", urlPattern);
    }
    
    /**
     * Remove custom RestTemplate for a URL pattern
     */
    public void removeCustomRestTemplate(String urlPattern) {
        customRestTemplates.remove(urlPattern);
        logger.info("Removed custom RestTemplate for URL pattern: {}", urlPattern);
    }
    
    /**
     * Clear all custom RestTemplate registrations
     */
    public void clearCustomRestTemplates() {
        customRestTemplates.clear();
        logger.info("Cleared all custom RestTemplate registrations");
    }
    
    /**
     * Get registered custom RestTemplate patterns
     */
    public Map<String, RestTemplate> getCustomRestTemplates() {
        return new HashMap<>(customRestTemplates);
    }
    
    /**
     * Create a new REST request builder
     */
    public ApiRequest.Builder restRequest() {
        return ApiRequest.builder().method("GET");
    }
    
    /**
     * Create a new SOAP request builder
     */
    public ApiRequest.Builder soapRequest() {
        return ApiRequest.builder().method("POST");
    }
    
    /**
     * Get REST client, checking for custom RestTemplate first
     */
    private ApiClient getRestClient(String url) {
        RestTemplate customRestTemplate = findCustomRestTemplate(url);
        if (customRestTemplate != null) {
            logger.debug("Using custom RestTemplate for URL: {}", url);
            return restClientFactory.createClient(customRestTemplate);
        }
        return restClientFactory.createClient();
    }
    
    /**
     * Get SOAP client, checking for custom RestTemplate first
     */
    private ApiClient getSoapClient(String url) {
        RestTemplate customRestTemplate = findCustomRestTemplate(url);
        if (customRestTemplate != null) {
            logger.debug("Using custom RestTemplate for SOAP URL: {}", url);
            return soapClientFactory.createClient(customRestTemplate);
        }
        return soapClientFactory.createClient();
    }
    
    /**
     * Find custom RestTemplate for URL based on registered patterns
     */
    private RestTemplate findCustomRestTemplate(String url) {
        // Exact match first
        if (customRestTemplates.containsKey(url)) {
            return customRestTemplates.get(url);
        }
        
        // Pattern matching
        for (Map.Entry<String, RestTemplate> entry : customRestTemplates.entrySet()) {
            String pattern = entry.getKey();
            if (url.matches(pattern.replace("*", ".*"))) {
                return entry.getValue();
            }
        }
        
        return null;
    }
    
    /**
     * Detect protocol based on request characteristics
     */
    private String detectProtocol(ApiRequest request) {
        // Check if SOAP action is present
        if (request.getSoapAction() != null && !request.getSoapAction().trim().isEmpty()) {
            return "SOAP";
        }
        
        // Check content type headers
        String contentType = request.getHeaders().get("Content-Type");
        if (contentType != null && (contentType.contains("text/xml") || contentType.contains("application/soap+xml"))) {
            return "SOAP";
        }
        
        // Check if body contains SOAP envelope
        if (request.getBody() instanceof String) {
            String body = (String) request.getBody();
            if (body.contains("soap:Envelope") || body.contains("Envelope")) {
                return "SOAP";
            }
        }
        
        // Default to REST
        return "REST";
    }
    
    /**
     * Health check method
     */
    public boolean isHealthy() {
        return true; // Can be extended to check client health
    }
    
    /**
     * Get framework configuration
     */
    public ApiProperties getConfiguration() {
        return apiProperties;
    }
} 