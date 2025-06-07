package com.company.apiframework.config;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.company.apiframework.interceptor.LoggingInterceptor;

/**
 * Factory service for creating RestTemplate instances with custom configurations.
 * 
 * <p>This factory allows the creation of RestTemplate instances with specific
 * configurations for different API endpoints or services. Each RestTemplate can
 * have its own timeout settings, connection pool limits, and other HTTP client
 * properties, enabling fine-grained performance tuning.</p>
 * 
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li>Per-RestTemplate configuration</li>
 *   <li>Custom timeout settings</li>
 *   <li>Individual connection pool management</li>
 *   <li>Configurable logging per template</li>
 *   <li>Fallback to global configuration</li>
 * </ul>
 * 
 * <p><strong>Usage Examples:</strong></p>
 * <pre>
 * // Create RestTemplate with custom timeouts
 * RestTemplateConfig config = RestTemplateConfig.builder()
 *     .connectionTimeoutMs(3000)
 *     .readTimeoutMs(10000)
 *     .build();
 * RestTemplate customTemplate = factory.createRestTemplate(config);
 * 
 * // Create RestTemplate with global defaults
 * RestTemplate defaultTemplate = factory.createRestTemplate();
 * 
 * // Create with configuration name for debugging
 * RestTemplateConfig namedConfig = RestTemplateConfig.builder("payment-api")
 *     .connectionTimeoutMs(5000)
 *     .maxRetryAttempts(5)
 *     .build();
 * RestTemplate paymentTemplate = factory.createRestTemplate(namedConfig);
 * </pre>
 * 
 * @author API Framework Team
 * @version 1.0
 * @since 1.0
 */
@Service
public class RestTemplateFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(RestTemplateFactory.class);
    
    @Autowired
    private ApiProperties apiProperties;
    
    /**
     * Creates a RestTemplate with global default configuration.
     * 
     * <p>This method creates a RestTemplate using the global configuration
     * settings from ApiProperties. Useful when no custom configuration
     * is needed for a particular API.</p>
     * 
     * @return RestTemplate configured with global settings
     */
    public RestTemplate createRestTemplate() {
        logger.debug("Creating RestTemplate with global configuration");
        return createRestTemplate(new RestTemplateConfig("global-default"));
    }
    
    /**
     * Creates a RestTemplate with custom configuration.
     * 
     * <p>This method creates a RestTemplate using the provided configuration,
     * with fallback to global settings for any unspecified properties.
     * Each RestTemplate gets its own HttpClient instance for isolated
     * connection pool management.</p>
     * 
     * @param config Custom configuration for this RestTemplate
     * @return RestTemplate configured according to the provided settings
     */
    public RestTemplate createRestTemplate(RestTemplateConfig config) {
        String configName = config.getConfigName() != null ? config.getConfigName() : "unnamed";
        logger.debug("Creating RestTemplate with configuration: {}", configName);
        
        // Create custom HttpClient with the specified configuration
        HttpClient httpClient = createHttpClient(config);
        
        // Create and configure the request factory
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        requestFactory.setConnectTimeout(config.getConnectionTimeoutMs(apiProperties));
        requestFactory.setReadTimeout(config.getReadTimeoutMs(apiProperties));
        
        // Create RestTemplate with the custom request factory
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        
        // Add logging interceptor if enabled
        if (config.isEnableLogging(apiProperties)) {
            restTemplate.getInterceptors().add(new LoggingInterceptor());
            logger.debug("Added logging interceptor to RestTemplate: {}", configName);
        }
        
        logger.info("Created RestTemplate '{}' with config: {}", configName, config);
        return restTemplate;
    }
    
    /**
     * Creates a custom HttpClient based on the provided configuration.
     * 
     * <p>This method builds an Apache HttpClient with the connection pool
     * and timeout settings specified in the configuration. Each RestTemplate
     * gets its own HttpClient instance to ensure isolation between different
     * API configurations.</p>
     * 
     * @param config Configuration containing HTTP client settings
     * @return Configured HttpClient instance
     */
    private HttpClient createHttpClient(RestTemplateConfig config) {
        return HttpClientBuilder.create()
                .setConnectionTimeToLive(
                    config.getConnectionTimeoutMs(apiProperties), 
                    java.util.concurrent.TimeUnit.MILLISECONDS
                )
                .setMaxConnTotal(config.getMaxConnections(apiProperties))
                .setMaxConnPerRoute(config.getMaxConnectionsPerRoute(apiProperties))
                .build();
    }
    
    /**
     * Creates a RestTemplate with predefined configuration for fast APIs.
     * 
     * <p>This is a convenience method for creating RestTemplates optimized
     * for fast APIs that typically respond quickly. Uses shorter timeouts
     * and fewer retry attempts.</p>
     * 
     * @return RestTemplate optimized for fast APIs
     */
    public RestTemplate createFastApiRestTemplate() {
        RestTemplateConfig fastConfig = RestTemplateConfig.builder("fast-api")
                .connectionTimeoutMs(2000)    // 2 seconds
                .readTimeoutMs(5000)          // 5 seconds
                .maxRetryAttempts(1)          // Single retry
                .retryDelayMs(500L)           // Short delay
                .build();
        
        return createRestTemplate(fastConfig);
    }
    
    /**
     * Creates a RestTemplate with predefined configuration for slow APIs.
     * 
     * <p>This is a convenience method for creating RestTemplates optimized
     * for slow APIs like batch processing or data-intensive operations.
     * Uses longer timeouts and more retry attempts.</p>
     * 
     * @return RestTemplate optimized for slow APIs
     */
    public RestTemplate createSlowApiRestTemplate() {
        RestTemplateConfig slowConfig = RestTemplateConfig.builder("slow-api")
                .connectionTimeoutMs(10000)   // 10 seconds
                .readTimeoutMs(120000)        // 2 minutes
                .maxRetryAttempts(5)          // More retries
                .retryDelayMs(2000L)          // Longer delay
                .build();
        
        return createRestTemplate(slowConfig);
    }
    
    /**
     * Creates a RestTemplate with predefined configuration for external APIs.
     * 
     * <p>This is a convenience method for creating RestTemplates optimized
     * for external third-party APIs that may have variable performance.
     * Balances timeout settings with reasonable retry policies.</p>
     * 
     * @return RestTemplate optimized for external APIs
     */
    public RestTemplate createExternalApiRestTemplate() {
        RestTemplateConfig externalConfig = RestTemplateConfig.builder("external-api")
                .connectionTimeoutMs(8000)    // 8 seconds
                .readTimeoutMs(45000)         // 45 seconds
                .maxRetryAttempts(3)          // Standard retries
                .retryDelayMs(1500L)          // Moderate delay
                .maxConnectionsPerRoute(10)   // Conservative connection limit
                .build();
        
        return createRestTemplate(externalConfig);
    }
    
    /**
     * Validates a RestTemplateConfig and logs any potential issues.
     * 
     * <p>This method performs basic validation on the configuration
     * and logs warnings for potentially problematic settings.</p>
     * 
     * @param config Configuration to validate
     * @return true if configuration appears valid, false otherwise
     */
    public boolean validateConfiguration(RestTemplateConfig config) {
        boolean isValid = true;
        String configName = config.getConfigName() != null ? config.getConfigName() : "unnamed";
        
        // Check for extremely short timeouts
        int connectionTimeout = config.getConnectionTimeoutMs(apiProperties);
        if (connectionTimeout < 1000) {
            logger.warn("Configuration '{}' has very short connection timeout: {}ms", 
                       configName, connectionTimeout);
        }
        
        int readTimeout = config.getReadTimeoutMs(apiProperties);
        if (readTimeout < 2000) {
            logger.warn("Configuration '{}' has very short read timeout: {}ms", 
                       configName, readTimeout);
        }
        
        // Check for extremely high connection limits
        int maxConnections = config.getMaxConnections(apiProperties);
        if (maxConnections > 1000) {
            logger.warn("Configuration '{}' has very high max connections: {}", 
                       configName, maxConnections);
        }
        
        // Check for reasonable connection per route limits
        int maxPerRoute = config.getMaxConnectionsPerRoute(apiProperties);
        if (maxPerRoute > maxConnections) {
            logger.error("Configuration '{}' has max per route ({}) > max total ({})", 
                        configName, maxPerRoute, maxConnections);
            isValid = false;
        }
        
        return isValid;
    }
} 