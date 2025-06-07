package com.company.apiframework.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.company.apiframework.client.ApiCallback;
import com.company.apiframework.client.ApiClient;
import com.company.apiframework.client.rest.RestClientFactory;
import com.company.apiframework.client.soap.SoapClientFactory;
import com.company.apiframework.config.ApiProperties;
import com.company.apiframework.config.RestTemplateBeanConfiguration;
import com.company.apiframework.model.ApiRequest;
import com.company.apiframework.model.ApiResponse;

/**
 * Main API Integration Service - Refactored for Spring Bean Approach
 * 
 * <p>This service provides a unified interface for making REST and SOAP API calls
 * with automatic RestTemplate bean selection based on URL patterns. The service
 * leverages Spring's dependency injection and bean management for optimal performance
 * and maintainability.</p>
 * 
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li>Automatic protocol detection (REST vs SOAP)</li>
 *   <li>Spring bean-based RestTemplate management</li>
 *   <li>URL pattern to RestTemplate bean mapping</li>
 *   <li>Synchronous and asynchronous API execution</li>
 *   <li>Comprehensive error handling and logging</li>
 *   <li>Health monitoring and configuration inspection</li>
 * </ul>
 * 
 * <p><strong>RestTemplate Selection Priority:</strong></p>
 * <ol>
 *   <li>Explicit RestTemplate parameter (method-level override)</li>
 *   <li>URL pattern matched Spring bean (configured in RestTemplateBeanConfiguration)</li>
 *   <li>Default RestTemplate bean (fallback)</li>
 * </ol>
 * 
 * <p><strong>Usage Examples:</strong></p>
 * <pre>
 * // Simple REST call - automatic bean selection
 * ApiRequest request = ApiRequest.builder()
 *     .url("https://payment.gateway.com/process")  // Uses paymentApiRestTemplate
 *     .method("POST")
 *     .body("{\"amount\":100.00}")
 *     .build();
 * ApiResponse&lt;String&gt; response = apiService.executeRest(request, String.class);
 * 
 * // SOAP call - automatic detection
 * ApiRequest soapRequest = ApiRequest.builder()
 *     .url("http://soap.service.com/endpoint")
 *     .soapAction("GetData")
 *     .body("&lt;soap:Envelope&gt;...&lt;/soap:Envelope&gt;")
 *     .build();
 * ApiResponse&lt;String&gt; soapResponse = apiService.executeSoap(soapRequest, String.class);
 * 
 * // Auto-detection based on request characteristics
 * ApiResponse&lt;DataDto&gt; autoResponse = apiService.executeAuto(request, DataDto.class);
 * </pre>
 * 
 * @author API Framework Team
 * @version 1.1.0
 * @since 1.0.0
 * @see RestTemplateBeanConfiguration
 * @see SpringBeanApiService
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
    private ApplicationContext applicationContext;
    
    @Autowired
    private RestTemplateBeanConfiguration restTemplateBeanConfiguration;
    
    // Inject Spring bean RestTemplates
    @Autowired
    @Qualifier("paymentApiRestTemplate")
    private RestTemplate paymentApiRestTemplate;
    
    @Autowired
    @Qualifier("batchApiRestTemplate")
    private RestTemplate batchApiRestTemplate;
    
    @Autowired
    @Qualifier("externalApiRestTemplate")
    private RestTemplate externalApiRestTemplate;
    
    @Autowired
    @Qualifier("highVolumeApiRestTemplate")
    private RestTemplate highVolumeApiRestTemplate;
    
    @Autowired
    private RestTemplate defaultRestTemplate; // Primary bean
    
    // Legacy support: Registry for manually registered RestTemplates
    private final Map<String, RestTemplate> legacyCustomRestTemplates = new HashMap<>();
    
    /**
     * Execute REST API call with automatic RestTemplate bean selection.
     * 
     * <p>This method automatically selects the most appropriate RestTemplate bean
     * based on URL pattern matching configured in RestTemplateBeanConfiguration.</p>
     * 
     * @param <T> Response type
     * @param request API request configuration
     * @param responseType Expected response type class
     * @return API response with success/error information
     */
    public <T> ApiResponse<T> executeRest(ApiRequest request, Class<T> responseType) {
        logger.debug("Executing REST API call to: {}", request.getUrl());
        RestTemplate restTemplate = selectRestTemplateForUrl(request.getUrl());
        ApiClient client = restClientFactory.createClient(restTemplate);
        return client.execute(request, responseType);
    }
    
    /**
     * Execute REST API call with explicit RestTemplate.
     * 
     * <p>This method bypasses automatic RestTemplate selection and uses the
     * provided RestTemplate directly. Useful for special cases or testing.</p>
     * 
     * @param <T> Response type
     * @param request API request configuration
     * @param responseType Expected response type class
     * @param customRestTemplate Specific RestTemplate to use
     * @return API response with success/error information
     */
    public <T> ApiResponse<T> executeRest(ApiRequest request, Class<T> responseType, RestTemplate customRestTemplate) {
        logger.debug("Executing REST API call to: {} with explicit RestTemplate", request.getUrl());
        ApiClient client = restClientFactory.createClient(customRestTemplate);
        return client.execute(request, responseType);
    }
    
    /**
     * Execute REST API call with automatic bean selection and String response.
     * 
     * @param request API request configuration
     * @return API response with String body
     */
    public ApiResponse<String> executeRest(ApiRequest request) {
        return executeRest(request, String.class);
    }
    
    /**
     * Execute REST API call with explicit RestTemplate and String response.
     * 
     * @param request API request configuration
     * @param customRestTemplate Specific RestTemplate to use
     * @return API response with String body
     */
    public ApiResponse<String> executeRest(ApiRequest request, RestTemplate customRestTemplate) {
        return executeRest(request, String.class, customRestTemplate);
    }
    
    /**
     * Execute SOAP API call with automatic RestTemplate bean selection.
     * 
     * @param <T> Response type
     * @param request SOAP API request configuration
     * @param responseType Expected response type class
     * @return API response with success/error information
     */
    public <T> ApiResponse<T> executeSoap(ApiRequest request, Class<T> responseType) {
        logger.debug("Executing SOAP API call to: {}", request.getUrl());
        RestTemplate restTemplate = selectRestTemplateForUrl(request.getUrl());
        ApiClient client = soapClientFactory.createClient(restTemplate);
        return client.execute(request, responseType);
    }
    
    /**
     * Execute SOAP API call with explicit RestTemplate.
     * 
     * @param <T> Response type
     * @param request SOAP API request configuration
     * @param responseType Expected response type class
     * @param customRestTemplate Specific RestTemplate to use
     * @return API response with success/error information
     */
    public <T> ApiResponse<T> executeSoap(ApiRequest request, Class<T> responseType, RestTemplate customRestTemplate) {
        logger.debug("Executing SOAP API call to: {} with explicit RestTemplate", request.getUrl());
        ApiClient client = soapClientFactory.createClient(customRestTemplate);
        return client.execute(request, responseType);
    }
    
    /**
     * Execute SOAP API call with automatic bean selection and String response.
     * 
     * @param request SOAP API request configuration
     * @return API response with String body
     */
    public ApiResponse<String> executeSoap(ApiRequest request) {
        return executeSoap(request, String.class);
    }
    
    /**
     * Execute SOAP API call with explicit RestTemplate and String response.
     * 
     * @param request SOAP API request configuration
     * @param customRestTemplate Specific RestTemplate to use
     * @return API response with String body
     */
    public ApiResponse<String> executeSoap(ApiRequest request, RestTemplate customRestTemplate) {
        return executeSoap(request, String.class, customRestTemplate);
    }
    
    /**
     * Execute API call with automatic protocol detection and RestTemplate selection.
     * 
     * <p>This method automatically determines whether to use REST or SOAP based on
     * request characteristics (presence of soapAction, content type, etc.) and
     * selects the appropriate RestTemplate bean based on URL patterns.</p>
     * 
     * @param <T> Response type
     * @param request API request configuration
     * @param responseType Expected response type class
     * @return API response with success/error information
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
     * Execute API call with automatic protocol detection and explicit RestTemplate.
     * 
     * @param <T> Response type
     * @param request API request configuration
     * @param responseType Expected response type class
     * @param customRestTemplate Specific RestTemplate to use
     * @return API response with success/error information
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
     * Execute API call asynchronously with automatic protocol and RestTemplate selection.
     * 
     * @param <T> Response type
     * @param request API request configuration
     * @param responseType Expected response type class
     * @param callback Callback to handle success/error responses
     */
    public <T> void executeAsync(ApiRequest request, Class<T> responseType, ApiCallback<T> callback) {
        String protocol = detectProtocol(request);
        RestTemplate restTemplate = selectRestTemplateForUrl(request.getUrl());
        ApiClient client;
        
        if ("SOAP".equalsIgnoreCase(protocol)) {
            client = soapClientFactory.createClient(restTemplate);
        } else {
            client = restClientFactory.createClient(restTemplate);
        }
        
        client.executeAsync(request, responseType, callback);
    }
    
    /**
     * Execute API call asynchronously with explicit RestTemplate.
     * 
     * @param <T> Response type
     * @param request API request configuration
     * @param responseType Expected response type class
     * @param callback Callback to handle success/error responses
     * @param customRestTemplate Specific RestTemplate to use
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
     * Register a custom RestTemplate for a specific URL pattern (Legacy Support).
     * 
     * <p><strong>Note:</strong> This method is provided for backward compatibility.
     * For new implementations, prefer using Spring bean configuration in
     * RestTemplateBeanConfiguration class.</p>
     * 
     * @param urlPattern URL pattern (supports wildcards like "https://api.example.com/*")
     * @param restTemplate Custom RestTemplate to use for this pattern
     */
    public void registerCustomRestTemplate(String urlPattern, RestTemplate restTemplate) {
        legacyCustomRestTemplates.put(urlPattern, restTemplate);
        logger.info("Registered legacy custom RestTemplate for URL pattern: {}", urlPattern);
    }
    
    /**
     * Remove custom RestTemplate registration (Legacy Support).
     * 
     * @param urlPattern URL pattern to remove
     */
    public void removeCustomRestTemplate(String urlPattern) {
        RestTemplate removed = legacyCustomRestTemplates.remove(urlPattern);
        if (removed != null) {
            logger.info("Removed legacy custom RestTemplate for URL pattern: {}", urlPattern);
        }
    }
    
    /**
     * Clear all legacy custom RestTemplate registrations.
     */
    public void clearCustomRestTemplates() {
        legacyCustomRestTemplates.clear();
        logger.info("Cleared all legacy custom RestTemplate registrations");
    }
    
    /**
     * Get all legacy custom RestTemplate registrations.
     * 
     * @return Map of URL patterns to RestTemplate instances
     */
    public Map<String, RestTemplate> getCustomRestTemplates() {
        return new HashMap<>(legacyCustomRestTemplates);
    }
    
    /**
     * Get comprehensive configuration summary including Spring beans and legacy registrations.
     * 
     * @return Configuration summary with bean information and URL pattern mappings
     */
    public Map<String, Object> getConfigurationSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // Spring bean information
        Map<String, String> springBeans = Map.of(
            "paymentApiRestTemplate", "Optimized for payment processing",
            "batchApiRestTemplate", "Optimized for batch operations",
            "externalApiRestTemplate", "Optimized for external partners", 
            "highVolumeApiRestTemplate", "Optimized for high-volume APIs",
            "defaultRestTemplate", "Default configuration"
        );
        
        summary.put("springBeans", springBeans);
        summary.put("springBeanCount", springBeans.size());
        summary.put("urlPatternMappings", restTemplateBeanConfiguration.getUrlPatternMappings());
        summary.put("legacyCustomTemplates", legacyCustomRestTemplates.size());
        summary.put("totalConfiguredTemplates", springBeans.size() + legacyCustomRestTemplates.size());
        
        return summary;
    }
    
    /**
     * Create a new REST request builder.
     * 
     * @return ApiRequest.Builder configured for REST
     */
    public ApiRequest.Builder restRequest() {
        return ApiRequest.builder().method("GET");
    }
    
    /**
     * Create a new SOAP request builder.
     * 
     * @return ApiRequest.Builder configured for SOAP
     */
    public ApiRequest.Builder soapRequest() {
        return ApiRequest.builder().method("POST");
    }
    
    /**
     * Check if the API service is healthy and operational.
     * 
     * @return true if service is healthy
     */
    public boolean isHealthy() {
        try {
            // Verify Spring beans are available
            return defaultRestTemplate != null && 
                   paymentApiRestTemplate != null &&
                   batchApiRestTemplate != null &&
                   externalApiRestTemplate != null &&
                   highVolumeApiRestTemplate != null;
        } catch (Exception e) {
            logger.error("Health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get current API framework configuration.
     * 
     * @return Current ApiProperties configuration
     */
    public ApiProperties getConfiguration() {
        return apiProperties;
    }
    
    /**
     * Select the most appropriate RestTemplate for the given URL.
     * 
     * <p>Selection priority:</p>
     * <ol>
     *   <li>Legacy custom RestTemplate (backward compatibility)</li>
     *   <li>URL pattern matched Spring bean</li>
     *   <li>Default RestTemplate bean</li>
     * </ol>
     * 
     * @param url Request URL
     * @return Best matching RestTemplate
     */
    private RestTemplate selectRestTemplateForUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return defaultRestTemplate;
        }
        
        // 1. Check legacy custom RestTemplates first (backward compatibility)
        RestTemplate legacyTemplate = findLegacyRestTemplate(url);
        if (legacyTemplate != null) {
            logger.debug("Using legacy custom RestTemplate for URL: {}", url);
            return legacyTemplate;
        }
        
        // 2. Check Spring bean URL pattern mappings
        Map<String, String> urlPatternMappings = restTemplateBeanConfiguration.getUrlPatternMappings();
        for (Map.Entry<String, String> entry : urlPatternMappings.entrySet()) {
            String pattern = entry.getKey();
            String beanName = entry.getValue();
            
            if (matchesPattern(url, pattern)) {
                try {
                    RestTemplate springBean = applicationContext.getBean(beanName, RestTemplate.class);
                    logger.debug("Using Spring bean RestTemplate '{}' for URL: {}", beanName, url);
                    return springBean;
                } catch (Exception e) {
                    logger.warn("Failed to get Spring bean '{}', using default: {}", beanName, e.getMessage());
                }
            }
        }
        
        // 3. Fallback to default RestTemplate
        logger.debug("Using default RestTemplate for URL: {}", url);
        return defaultRestTemplate;
    }
    
    /**
     * Find legacy custom RestTemplate for URL (backward compatibility).
     * 
     * @param url Request URL
     * @return Matching RestTemplate or null if not found
     */
    private RestTemplate findLegacyRestTemplate(String url) {
        for (Map.Entry<String, RestTemplate> entry : legacyCustomRestTemplates.entrySet()) {
            String pattern = entry.getKey();
            if (matchesPattern(url, pattern)) {
                return entry.getValue();
            }
        }
        return null;
    }
    
    /**
     * Check if URL matches a wildcard pattern.
     * 
     * @param url URL to check
     * @param pattern Pattern with wildcards (* supported)
     * @return true if URL matches pattern
     */
    private boolean matchesPattern(String url, String pattern) {
        if (pattern.equals(url)) {
            return true; // Exact match
        }
        
        if (pattern.contains("*")) {
            // Simple wildcard matching
            String regex = pattern.replace("*", ".*");
            return url.matches(regex);
        }
        
        return false;
    }
    
    /**
     * Detect protocol (REST vs SOAP) based on request characteristics.
     * 
     * @param request API request to analyze
     * @return Detected protocol ("REST" or "SOAP")
     */
    private String detectProtocol(ApiRequest request) {
        // SOAP indicators
        if (StringUtils.hasText(request.getSoapAction())) {
            return "SOAP";
        }
        
        if (request.getBody() != null && 
            request.getBody().toString().contains("soap:Envelope")) {
            return "SOAP";
        }
        
        if (request.getHeaders() != null) {
            String contentType = request.getHeaders().get("Content-Type");
            if (contentType != null && 
                (contentType.contains("text/xml") || contentType.contains("application/soap+xml"))) {
                return "SOAP";
            }
        }
        
        // Default to REST
        return "REST";
    }
} 