package com.company.apiframework.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.company.apiframework.interceptor.LoggingInterceptor;

/**
 * Spring Bean-based RestTemplate Configuration
 * 
 * <p>This configuration class creates RestTemplate beans that can be injected
 * throughout the application using Spring's dependency injection mechanism.
 * This approach provides better integration with Spring's lifecycle management
 * and testing capabilities.</p>
 * 
 * <p><strong>Benefits of Bean Approach:</strong></p>
 * <ul>
 *   <li>Spring manages lifecycle automatically</li>
 *   <li>Better integration with dependency injection</li>
 *   <li>Can use @Qualifier for specific RestTemplate injection</li>
 *   <li>Better testability with @MockBean</li>
 *   <li>Consistent with Spring best practices</li>
 *   <li>Spring Boot actuator integration</li>
 * </ul>
 * 
 * <p><strong>Usage Examples:</strong></p>
 * <pre>
 * // Inject specific RestTemplate
 * {@literal @}Autowired
 * {@literal @}Qualifier("paymentApiRestTemplate")
 * private RestTemplate paymentRestTemplate;
 * 
 * // Inject default RestTemplate
 * {@literal @}Autowired
 * private RestTemplate defaultRestTemplate;
 * 
 * // Use in service
 * ResponseEntity&lt;String&gt; response = paymentRestTemplate.getForEntity(url, String.class);
 * </pre>
 * 
 * @author API Framework Team
 * @version 1.1.0
 * @since 1.1.0
 */
@Configuration
public class RestTemplateBeanConfiguration {

    @Autowired
    private ApiProperties apiProperties;
    
    @Autowired
    private LoggingInterceptor loggingInterceptor;
    
    /**
     * Registry of RestTemplate beans for URL pattern matching
     */
    private final Map<String, String> urlPatternToBeanMapping = new HashMap<>();
    
    /**
     * Default RestTemplate bean (Primary)
     * 
     * <p>This is the primary RestTemplate that will be injected when no qualifier is specified.
     * Uses global configuration settings from ApiProperties.</p>
     * 
     * @return configured RestTemplate with global settings
     */
    @Bean
    @Primary
    public RestTemplate defaultRestTemplate() {
        return createRestTemplate("default", 
                                 apiProperties.getConnectionTimeoutMs(),
                                 apiProperties.getReadTimeoutMs(),
                                 apiProperties.getMaxConnections(),
                                 apiProperties.getMaxConnectionsPerRoute(),
                                 true);  // Enable logging for default
    }
    
    /**
     * Payment API RestTemplate bean
     * 
     * <p>Optimized for payment processing APIs with fast timeouts and minimal retries.
     * Use {@literal @}Qualifier("paymentApiRestTemplate") to inject this bean.</p>
     * 
     * @return RestTemplate optimized for payment APIs
     */
    @Bean
    public RestTemplate paymentApiRestTemplate() {
        RestTemplate restTemplate = createRestTemplate("payment-api", 
                                                       2000,   // 2 second connection timeout
                                                       5000,   // 5 second read timeout  
                                                       50,     // 50 max connections
                                                       10,     // 10 per route
                                                       true);  // Enable logging for auditing
        return restTemplate;
    }
    
    /**
     * Batch Processing RestTemplate bean
     * 
     * <p>Optimized for slow batch operations with extended timeouts and higher retry tolerance.
     * Use {@literal @}Qualifier("batchApiRestTemplate") to inject this bean.</p>
     * 
     * @return RestTemplate optimized for batch processing APIs
     */
    @Bean
    public RestTemplate batchApiRestTemplate() {
        return createRestTemplate("batch-api",
                                 10000,   // 10 second connection timeout
                                 300000,  // 5 minute read timeout
                                 10,      // 10 max connections
                                 2,       // 2 per route
                                 false);  // Disable logging to reduce noise
    }
    
    /**
     * External Partner RestTemplate bean
     * 
     * <p>Conservative settings for external partner APIs with higher retry tolerance.
     * Use {@literal @}Qualifier("externalApiRestTemplate") to inject this bean.</p>
     * 
     * @return RestTemplate optimized for external partner APIs
     */
    @Bean 
    public RestTemplate externalApiRestTemplate() {
        return createRestTemplate("external-api",
                                 5000,    // 5 second connection timeout
                                 30000,   // 30 second read timeout
                                 20,      // 20 max connections
                                 5,       // 5 per route
                                 true);   // Enable logging
    }
    
    /**
     * High Volume RestTemplate bean
     * 
     * <p>Optimized for high-volume APIs with large connection pools.
     * Use {@literal @}Qualifier("highVolumeApiRestTemplate") to inject this bean.</p>
     * 
     * @return RestTemplate optimized for high-volume APIs
     */
    @Bean
    public RestTemplate highVolumeApiRestTemplate() {
        return createRestTemplate("high-volume-api",
                                 3000,    // 3 second connection timeout
                                 15000,   // 15 second read timeout
                                 100,     // 100 max connections
                                 20,      // 20 per route
                                 false);  // Disable logging for performance
    }
    
    /**
     * Initialize URL pattern mappings after all beans are created
     */
    @PostConstruct
    public void initializeUrlPatternMappings() {
        // Map URL patterns to bean names for automatic selection
        urlPatternToBeanMapping.put("https://payment.gateway.com/*", "paymentApiRestTemplate");
        urlPatternToBeanMapping.put("https://*/payment/*", "paymentApiRestTemplate");
        
        urlPatternToBeanMapping.put("https://batch.processor.com/*", "batchApiRestTemplate");
        urlPatternToBeanMapping.put("https://*/batch/*", "batchApiRestTemplate");
        
        urlPatternToBeanMapping.put("https://*.external.com/*", "externalApiRestTemplate");
        urlPatternToBeanMapping.put("https://partner-*.com/*", "externalApiRestTemplate");
        
        urlPatternToBeanMapping.put("https://high-volume.api.com/*", "highVolumeApiRestTemplate");
        urlPatternToBeanMapping.put("https://*/stream/*", "highVolumeApiRestTemplate");
        
        System.out.println("RestTemplate beans initialized with URL pattern mappings:");
        urlPatternToBeanMapping.forEach((pattern, bean) -> 
            System.out.println("  " + pattern + " -> " + bean));
    }
    
    /**
     * Get URL pattern to bean name mappings
     * 
     * @return map of URL patterns to RestTemplate bean names
     */
    public Map<String, String> getUrlPatternMappings() {
        return new HashMap<>(urlPatternToBeanMapping);
    }
    
    /**
     * Helper method to create configured RestTemplate instances
     * 
     * @param name RestTemplate identifier for logging
     * @param connectionTimeout connection timeout in milliseconds
     * @param readTimeout read timeout in milliseconds  
     * @param maxConnections maximum total connections
     * @param maxConnectionsPerRoute maximum connections per route
     * @param enableLogging whether to enable request/response logging
     * @return configured RestTemplate instance
     */
    private RestTemplate createRestTemplate(String name,
                                          int connectionTimeout,
                                          int readTimeout,
                                          int maxConnections,
                                          int maxConnectionsPerRoute,
                                          boolean enableLogging) {
        
        // Create HTTP client with specified settings
        HttpClient httpClient = HttpClientBuilder.create()
                .setMaxConnTotal(maxConnections)
                .setMaxConnPerRoute(maxConnectionsPerRoute)
                .build();
        
        // Create request factory with timeouts
        HttpComponentsClientHttpRequestFactory factory = 
            new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient);
        factory.setConnectTimeout(connectionTimeout);
        factory.setReadTimeout(readTimeout);
        
        // Create RestTemplate
        RestTemplate restTemplate = new RestTemplate(factory);
        
        // Add interceptors conditionally
        if (enableLogging) {
            restTemplate.setInterceptors(Arrays.<ClientHttpRequestInterceptor>asList(loggingInterceptor));
        }
        
        System.out.println("Created RestTemplate bean: " + name + 
                          " (connectionTimeout=" + connectionTimeout + 
                          "ms, readTimeout=" + readTimeout + 
                          "ms, maxConnections=" + maxConnections + 
                          ", logging=" + enableLogging + ")");
        
        return restTemplate;
    }
} 