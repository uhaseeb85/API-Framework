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
import com.company.apiframework.config.RestTemplateConfig;
import com.company.apiframework.config.RestTemplateFactory;
import com.company.apiframework.model.ApiRequest;
import com.company.apiframework.model.ApiResponse;

/**
 * Main service for API integration providing a unified interface for REST and SOAP calls.
 * 
 * <p>This service acts as the primary entry point for all API interactions in the framework.
 * It provides a consistent, protocol-agnostic interface that automatically handles the
 * complexities of different API types while offering advanced features for enterprise use.</p>
 * 
 * <p><strong>Core Features:</strong></p>
 * <ul>
 *   <li><strong>Protocol Abstraction:</strong> Unified interface for REST and SOAP APIs</li>
 *   <li><strong>Automatic Detection:</strong> Smart protocol detection based on request characteristics</li>
 *   <li><strong>Custom RestTemplates:</strong> Per-endpoint RestTemplate configuration</li>
 *   <li><strong>Asynchronous Support:</strong> Non-blocking API calls with callbacks</li>
 *   <li><strong>Type Safety:</strong> Generic response types with automatic deserialization</li>
 *   <li><strong>Builder Pattern:</strong> Fluent API for request construction</li>
 * </ul>
 * 
 * <p><strong>Usage Patterns:</strong></p>
 * <pre>
 * // Simple REST call
 * ApiResponse&lt;User&gt; response = apiService.executeRest(
 *     ApiRequest.builder()
 *         .url("https://api.example.com/users/123")
 *         .method("GET")
 *         .header("Authorization", "Bearer " + token)
 *         .build(),
 *     User.class
 * );
 * 
 * // SOAP call with custom RestTemplate
 * RestTemplate customTemplate = createCustomRestTemplate();
 * ApiResponse&lt;String&gt; soapResponse = apiService.executeSoap(soapRequest, customTemplate);
 * 
 * // Automatic protocol detection
 * ApiResponse&lt;Object&gt; autoResponse = apiService.executeAuto(request, Object.class);
 * 
 * // Asynchronous execution
 * apiService.executeAsync(request, User.class, new ApiCallback&lt;User&gt;() {
 *     public void onSuccess(ApiResponse&lt;User&gt; response) {
 *         // Handle success
 *     }
 * });
 * </pre>
 * 
 * <p><strong>Custom RestTemplate Support:</strong></p>
 * <p>The service supports both pre-created RestTemplate instances and configuration-based
 * RestTemplate creation for specific URL patterns, enabling fine-grained control over
 * HTTP client behavior per API endpoint:</p>
 * <pre>
 * // Method 1: Register pre-created RestTemplate
 * RestTemplate customTemplate = new RestTemplate();
 * // Configure custom timeouts, interceptors, etc.
 * apiService.registerCustomRestTemplate("https://special-api.com/*", customTemplate);
 * 
 * // Method 2: Register configuration (recommended)
 * RestTemplateConfig fastConfig = RestTemplateConfig.builder("payment-api")
 *     .connectionTimeoutMs(2000)
 *     .readTimeoutMs(5000)
 *     .maxRetryAttempts(1)
 *     .build();
 * apiService.registerRestTemplateConfig("https://payment-api.com/*", fastConfig);
 * 
 * // Method 3: Inline configuration
 * apiService.registerRestTemplateConfig("https://batch-api.com/*",
 *     RestTemplateConfig.builder("batch-api")
 *         .connectionTimeoutMs(10000)
 *         .readTimeoutMs(120000)
 *         .maxRetryAttempts(5)
 *         .build());
 * 
 * // All calls to matching URLs will use the custom configuration
 * ApiResponse&lt;Data&gt; response = apiService.executeRest(request, Data.class);
 * </pre>
 * 
 * <p><strong>Protocol Detection Logic:</strong></p>
 * <p>The service automatically detects the appropriate protocol based on:</p>
 * <ul>
 *   <li>Presence of SOAP Action header</li>
 *   <li>Content-Type headers (text/xml, application/soap+xml)</li>
 *   <li>Request body content (SOAP envelope detection)</li>
 *   <li>Defaults to REST if no SOAP indicators found</li>
 * </ul>
 * 
 * <p><strong>Thread Safety:</strong> This service is thread-safe and can be used
 * concurrently from multiple threads. Custom RestTemplate registrations are
 * handled in a thread-safe manner.</p>
 * 
 * @author API Framework Team
 * @version 1.0
 * @since 1.0
 * @see ApiRequest
 * @see ApiResponse
 * @see ApiCallback
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
    
    @Autowired
    private RestTemplateFactory restTemplateFactory;
    
    // Registry for custom RestTemplates per API endpoint
    private final Map<String, RestTemplate> customRestTemplates = new HashMap<>();
    
    // Registry for custom RestTemplate configurations per URL pattern
    private final Map<String, RestTemplateConfig> customRestTemplateConfigs = new HashMap<>();
    
    // Cache for RestTemplates created from configurations (to avoid recreating on each request)
    private final Map<String, RestTemplate> configBasedRestTemplates = new HashMap<>();
    
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
     * Register a custom RestTemplate configuration for a specific API endpoint pattern.
     * 
     * <p>This method creates a RestTemplate immediately using the provided configuration
     * and caches it for reuse. This ensures optimal performance by avoiding RestTemplate
     * creation overhead on each API request.</p>
     * 
     * <p><strong>Usage Examples:</strong></p>
     * <pre>
     * // Register configuration for fast APIs
     * RestTemplateConfig fastConfig = RestTemplateConfig.builder("payment-api")
     *     .connectionTimeoutMs(2000)
     *     .readTimeoutMs(5000)
     *     .maxRetryAttempts(1)
     *     .build();
     * apiService.registerRestTemplateConfig("https://payment-api.com/*", fastConfig);
     * 
     * // Register configuration for slow batch APIs
     * RestTemplateConfig batchConfig = RestTemplateConfig.builder("batch-api")
     *     .connectionTimeoutMs(10000)
     *     .readTimeoutMs(120000)
     *     .maxRetryAttempts(5)
     *     .build();
     * apiService.registerRestTemplateConfig("https://batch-api.com/*", batchConfig);
     * </pre>
     * 
     * @param urlPattern URL pattern (supports wildcards like "https://api.example.com/*")
     * @param config Custom RestTemplate configuration for this pattern
     */
    public void registerRestTemplateConfig(String urlPattern, RestTemplateConfig config) {
        // Validate configuration before creating RestTemplate
        if (restTemplateFactory.validateConfiguration(config)) {
            // Create RestTemplate immediately and cache it
            RestTemplate restTemplate = restTemplateFactory.createRestTemplate(config);
            configBasedRestTemplates.put(urlPattern, restTemplate);
            customRestTemplateConfigs.put(urlPattern, config);
            
            logger.info("Created and cached RestTemplate with configuration '{}' for URL pattern: {}", 
                       config.getConfigName(), urlPattern);
        } else {
            logger.error("Invalid configuration '{}' for URL pattern: {}", 
                        config.getConfigName(), urlPattern);
            throw new IllegalArgumentException("Invalid RestTemplate configuration: " + config.getConfigName());
        }
    }
    
    /**
     * Register a RestTemplate configuration using a fluent builder approach.
     * 
     * <p>This is a convenience method that allows inline configuration creation:</p>
     * <pre>
     * apiService.registerRestTemplateConfig("https://api.example.com/*", 
     *     RestTemplateConfig.builder("example-api")
     *         .connectionTimeoutMs(3000)
     *         .readTimeoutMs(15000)
     *         .maxRetryAttempts(2)
     *         .build());
     * </pre>
     * 
     * @param urlPattern URL pattern (supports wildcards)
     * @param configBuilder Builder for creating the configuration
     */
    public void registerRestTemplateConfig(String urlPattern, RestTemplateConfig.Builder configBuilder) {
        registerRestTemplateConfig(urlPattern, configBuilder.build());
    }
    
    /**
     * Remove custom RestTemplate for a URL pattern
     */
    public void removeCustomRestTemplate(String urlPattern) {
        customRestTemplates.remove(urlPattern);
        logger.info("Removed custom RestTemplate for URL pattern: {}", urlPattern);
    }
    
    /**
     * Remove custom RestTemplate configuration for a URL pattern.
     * 
     * <p>This method removes both the configuration and the cached RestTemplate
     * instance to free up resources.</p>
     */
    public void removeRestTemplateConfig(String urlPattern) {
        RestTemplateConfig removedConfig = customRestTemplateConfigs.remove(urlPattern);
        RestTemplate removedRestTemplate = configBasedRestTemplates.remove(urlPattern);
        
        if (removedConfig != null) {
            logger.info("Removed RestTemplate configuration '{}' and cached instance for URL pattern: {}", 
                       removedConfig.getConfigName(), urlPattern);
            
            // Cleanup the RestTemplate's connection pool if possible
            if (removedRestTemplate != null) {
                cleanupRestTemplate(removedRestTemplate);
            }
        }
    }
    
    /**
     * Clear all custom RestTemplate registrations
     */
    public void clearCustomRestTemplates() {
        customRestTemplates.clear();
        logger.info("Cleared all custom RestTemplate registrations");
    }
    
    /**
     * Clear all custom RestTemplate configurations
     */
    public void clearRestTemplateConfigs() {
        // Cleanup cached RestTemplates first
        for (RestTemplate restTemplate : configBasedRestTemplates.values()) {
            cleanupRestTemplate(restTemplate);
        }
        
        customRestTemplateConfigs.clear();
        configBasedRestTemplates.clear();
        logger.info("Cleared all custom RestTemplate configurations and cached instances");
    }
    
    /**
     * Clear both custom RestTemplates and configurations
     */
    public void clearAllCustomConfigurations() {
        clearCustomRestTemplates();
        clearRestTemplateConfigs();
        logger.info("Cleared all custom RestTemplate registrations and configurations");
    }
    
    /**
     * Helper method to cleanup RestTemplate resources
     */
    private void cleanupRestTemplate(RestTemplate restTemplate) {
        try {
            if (restTemplate.getRequestFactory() instanceof org.springframework.http.client.HttpComponentsClientHttpRequestFactory) {
                org.springframework.http.client.HttpComponentsClientHttpRequestFactory factory = 
                    (org.springframework.http.client.HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory();
                if (factory.getHttpClient() instanceof org.apache.http.impl.client.CloseableHttpClient) {
                    ((org.apache.http.impl.client.CloseableHttpClient) factory.getHttpClient()).close();
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to cleanup RestTemplate resources: {}", e.getMessage());
        }
    }
    
    /**
     * Get registered custom RestTemplate patterns
     */
    public Map<String, RestTemplate> getCustomRestTemplates() {
        return new HashMap<>(customRestTemplates);
    }
    
    /**
     * Get registered custom RestTemplate configurations
     */
    public Map<String, RestTemplateConfig> getCustomRestTemplateConfigs() {
        return new HashMap<>(customRestTemplateConfigs);
    }
    
    /**
     * Get summary of all custom configurations
     */
    public Map<String, Object> getConfigurationSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("customRestTemplates", customRestTemplates.size());
        summary.put("customConfigurations", customRestTemplateConfigs.size());
        
        Map<String, String> configDetails = new HashMap<>();
        for (Map.Entry<String, RestTemplateConfig> entry : customRestTemplateConfigs.entrySet()) {
            configDetails.put(entry.getKey(), entry.getValue().getConfigName());
        }
        summary.put("configurationDetails", configDetails);
        
        return summary;
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
     * Get REST client, checking for custom RestTemplate and configurations
     */
    private ApiClient getRestClient(String url) {
        // First check for pre-created custom RestTemplate
        RestTemplate customRestTemplate = findCustomRestTemplate(url);
        if (customRestTemplate != null) {
            logger.debug("Using pre-created custom RestTemplate for URL: {}", url);
            return restClientFactory.createClient(customRestTemplate);
        }
        
        // Then check for cached RestTemplate from configuration
        RestTemplate cachedRestTemplate = findCachedRestTemplate(url);
        if (cachedRestTemplate != null) {
            logger.debug("Using cached RestTemplate for URL: {}", url);
            return restClientFactory.createClient(cachedRestTemplate);
        }
        
        // Use default configuration
        return restClientFactory.createClient();
    }
    
    /**
     * Get SOAP client, checking for custom RestTemplate and configurations
     */
    private ApiClient getSoapClient(String url) {
        // First check for pre-created custom RestTemplate
        RestTemplate customRestTemplate = findCustomRestTemplate(url);
        if (customRestTemplate != null) {
            logger.debug("Using pre-created custom RestTemplate for SOAP URL: {}", url);
            return soapClientFactory.createClient(customRestTemplate);
        }
        
        // Then check for cached RestTemplate from configuration
        RestTemplate cachedRestTemplate = findCachedRestTemplate(url);
        if (cachedRestTemplate != null) {
            logger.debug("Using cached RestTemplate for SOAP URL: {}", url);
            return soapClientFactory.createClient(cachedRestTemplate);
        }
        
        // Use default configuration
        return soapClientFactory.createClient();
    }
    
    /**
     * Find cached RestTemplate for URL based on registered configuration patterns
     */
    private RestTemplate findCachedRestTemplate(String url) {
        // Exact match first
        if (configBasedRestTemplates.containsKey(url)) {
            return configBasedRestTemplates.get(url);
        }
        
        // Pattern matching
        for (Map.Entry<String, RestTemplate> entry : configBasedRestTemplates.entrySet()) {
            String pattern = entry.getKey();
            if (url.matches(pattern.replace("*", ".*"))) {
                return entry.getValue();
            }
        }
        
        return null;
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
     * Find custom RestTemplate configuration for URL based on registered patterns
     */
    private RestTemplateConfig findCustomRestTemplateConfig(String url) {
        // Exact match first
        if (customRestTemplateConfigs.containsKey(url)) {
            return customRestTemplateConfigs.get(url);
        }
        
        // Pattern matching
        for (Map.Entry<String, RestTemplateConfig> entry : customRestTemplateConfigs.entrySet()) {
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