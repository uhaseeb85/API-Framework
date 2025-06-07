package com.company.apiframework.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.company.apiframework.config.RestTemplateBeanConfiguration;
import com.company.apiframework.model.ApiRequest;
import com.company.apiframework.model.ApiResponse;

/**
 * Spring Bean-based API Service
 * 
 * <p>This service demonstrates how to use RestTemplate beans with Spring's dependency injection
 * mechanism instead of programmatic RestTemplate management. This approach provides better
 * integration with Spring's lifecycle management and testing capabilities.</p>
 * 
 * <p><strong>Benefits of Bean-based Approach:</strong></p>
 * <ul>
 *   <li>Spring manages RestTemplate lifecycle automatically</li>
 *   <li>Better integration with dependency injection (@Autowired, @Qualifier)</li>
 *   <li>Easier testing with @MockBean</li>
 *   <li>Consistent with Spring best practices</li>
 *   <li>Spring Boot actuator integration for metrics</li>
 *   <li>Automatic URL pattern to bean mapping</li>
 * </ul>
 * 
 * <p><strong>Usage Examples:</strong></p>
 * <pre>
 * // Automatic bean selection based on URL
 * ApiRequest request = ApiRequest.builder()
 *     .url("https://payment.gateway.com/process")  // Uses paymentApiRestTemplate
 *     .method("POST")
 *     .build();
 * ApiResponse&lt;String&gt; response = springBeanApiService.executeRequest(request, String.class);
 * 
 * // Explicit bean selection
 * ApiResponse&lt;String&gt; response = springBeanApiService.executeWithBean(request, "batchApiRestTemplate", String.class);
 * </pre>
 * 
 * @author API Framework Team
 * @version 1.1.0
 * @since 1.1.0
 * @see RestTemplateBeanConfiguration
 */
@Service
public class SpringBeanApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(SpringBeanApiService.class);
    
    // Inject specific RestTemplate beans using @Qualifier
    
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
    private RestTemplate defaultRestTemplate; // Primary bean, no qualifier needed
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private RestTemplateBeanConfiguration restTemplateBeanConfiguration;
    
    /**
     * Execute API request with automatic RestTemplate bean selection based on URL pattern.
     * 
     * <p>This method automatically selects the appropriate RestTemplate bean based on
     * URL pattern matching. It provides the simplest way to leverage the bean-based
     * configuration without manual bean selection.</p>
     * 
     * @param <T> Response type
     * @param request API request configuration
     * @param responseType Expected response type
     * @return API response with success/error information
     */
    public <T> ApiResponse<T> executeRequest(ApiRequest request, Class<T> responseType) {
        RestTemplate restTemplate = selectRestTemplateByUrl(request.getUrl());
        return executeWithRestTemplate(request, restTemplate, responseType);
    }
    
    /**
     * Execute API request with explicit RestTemplate bean selection.
     * 
     * <p>This method allows explicit specification of which RestTemplate bean to use,
     * providing full control over the HTTP client configuration.</p>
     * 
     * @param <T> Response type
     * @param request API request configuration
     * @param beanName Name of the RestTemplate bean to use
     * @param responseType Expected response type
     * @return API response with success/error information
     */
    public <T> ApiResponse<T> executeWithBean(ApiRequest request, String beanName, Class<T> responseType) {
        try {
            RestTemplate restTemplate = applicationContext.getBean(beanName, RestTemplate.class);
            logger.debug("Using explicitly specified RestTemplate bean: {}", beanName);
            return executeWithRestTemplate(request, restTemplate, responseType);
        } catch (Exception e) {
            logger.error("Failed to get RestTemplate bean '{}': {}", beanName, e.getMessage());
            // Fallback to default RestTemplate
            return executeWithRestTemplate(request, defaultRestTemplate, responseType);
        }
    }
    
    /**
     * Execute payment API request using the payment-optimized RestTemplate.
     * 
     * <p>Convenience method for payment processing APIs that require fast timeouts
     * and specific configuration for financial transactions.</p>
     * 
     * @param <T> Response type
     * @param request Payment API request
     * @param responseType Expected response type
     * @return API response optimized for payment processing
     */
    public <T> ApiResponse<T> executePaymentRequest(ApiRequest request, Class<T> responseType) {
        logger.debug("Executing payment request with payment-optimized RestTemplate");
        return executeWithRestTemplate(request, paymentApiRestTemplate, responseType);
    }
    
    /**
     * Execute batch processing request using the batch-optimized RestTemplate.
     * 
     * <p>Convenience method for batch operations that require extended timeouts
     * and higher tolerance for slow responses.</p>
     * 
     * @param <T> Response type
     * @param request Batch processing request
     * @param responseType Expected response type
     * @return API response optimized for batch processing
     */
    public <T> ApiResponse<T> executeBatchRequest(ApiRequest request, Class<T> responseType) {
        logger.debug("Executing batch request with batch-optimized RestTemplate");
        return executeWithRestTemplate(request, batchApiRestTemplate, responseType);
    }
    
    /**
     * Execute external partner API request using the external-optimized RestTemplate.
     * 
     * <p>Convenience method for external partner APIs that require conservative
     * settings and higher retry tolerance.</p>
     * 
     * @param <T> Response type
     * @param request External API request
     * @param responseType Expected response type
     * @return API response optimized for external partner APIs
     */
    public <T> ApiResponse<T> executeExternalRequest(ApiRequest request, Class<T> responseType) {
        logger.debug("Executing external request with external-optimized RestTemplate");
        return executeWithRestTemplate(request, externalApiRestTemplate, responseType);
    }
    
    /**
     * Execute high-volume API request using the high-volume-optimized RestTemplate.
     * 
     * <p>Convenience method for high-volume APIs that require large connection pools
     * and optimized performance settings.</p>
     * 
     * @param <T> Response type
     * @param request High-volume API request
     * @param responseType Expected response type
     * @return API response optimized for high-volume processing
     */
    public <T> ApiResponse<T> executeHighVolumeRequest(ApiRequest request, Class<T> responseType) {
        logger.debug("Executing high-volume request with high-volume-optimized RestTemplate");
        return executeWithRestTemplate(request, highVolumeApiRestTemplate, responseType);
    }
    
    /**
     * Get summary of available RestTemplate beans and their configurations.
     * 
     * @return Map containing bean information and URL pattern mappings
     */
    public Map<String, Object> getBeanSummary() {
        Map<String, Object> summary = Map.of(
            "availableBeans", Map.of(
                "paymentApiRestTemplate", "Optimized for payment processing",
                "batchApiRestTemplate", "Optimized for batch operations", 
                "externalApiRestTemplate", "Optimized for external partners",
                "highVolumeApiRestTemplate", "Optimized for high-volume APIs",
                "defaultRestTemplate", "Default configuration"
            ),
            "urlPatternMappings", restTemplateBeanConfiguration.getUrlPatternMappings()
        );
        
        return summary;
    }
    
    /**
     * Select appropriate RestTemplate bean based on URL pattern matching.
     * 
     * @param url Request URL
     * @return Best matching RestTemplate bean
     */
    private RestTemplate selectRestTemplateByUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return defaultRestTemplate;
        }
        
        Map<String, String> urlPatternMappings = restTemplateBeanConfiguration.getUrlPatternMappings();
        
        // Check for exact URL match first
        for (Map.Entry<String, String> entry : urlPatternMappings.entrySet()) {
            String pattern = entry.getKey();
            String beanName = entry.getValue();
            
            if (matchesPattern(url, pattern)) {
                try {
                    RestTemplate restTemplate = applicationContext.getBean(beanName, RestTemplate.class);
                    logger.debug("Selected RestTemplate bean '{}' for URL: {}", beanName, url);
                    return restTemplate;
                } catch (Exception e) {
                    logger.warn("Failed to get RestTemplate bean '{}', using default: {}", beanName, e.getMessage());
                }
            }
        }
        
        logger.debug("No specific RestTemplate bean found for URL: {}, using default", url);
        return defaultRestTemplate;
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
     * Execute API request with specific RestTemplate instance.
     * 
     * @param <T> Response type
     * @param request API request configuration
     * @param restTemplate RestTemplate instance to use
     * @param responseType Expected response type
     * @return API response with success/error information
     */
    private <T> ApiResponse<T> executeWithRestTemplate(ApiRequest request, RestTemplate restTemplate, Class<T> responseType) {
        ApiResponse<T> response = new ApiResponse<>();
        
        try {
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            if (request.getHeaders() != null) {
                request.getHeaders().forEach(headers::add);
            }
            
            // Prepare entity  
            String body = request.getBody() != null ? request.getBody().toString() : null;
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            
            // Execute request
            HttpMethod method = HttpMethod.valueOf(request.getMethod().toUpperCase());
            ResponseEntity<T> responseEntity = restTemplate.exchange(
                request.getUrl(),
                method,
                entity,
                responseType
            );
            
            // Build successful response
            response.setSuccess(true);
            response.setStatusCode(responseEntity.getStatusCodeValue());
            response.setBody(responseEntity.getBody());
            response.setHeaders(responseEntity.getHeaders().toSingleValueMap());
            
            logger.debug("API call successful: {} {} - Status: {}", 
                        request.getMethod(), request.getUrl(), response.getStatusCode());
            
        } catch (Exception e) {
            // Build error response
            response.setSuccess(false);
            response.setErrorCode("REST_ERROR");
            response.setErrorMessage(e.getMessage());
            response.setStatusCode(500); // Default error status
            
            logger.error("API call failed: {} {} - Error: {}", 
                        request.getMethod(), request.getUrl(), e.getMessage());
        }
        
        return response;
    }
} 