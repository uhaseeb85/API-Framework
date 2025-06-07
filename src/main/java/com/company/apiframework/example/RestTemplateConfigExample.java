package com.company.apiframework.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.company.apiframework.config.RestTemplateConfig;
import com.company.apiframework.model.ApiRequest;
import com.company.apiframework.model.ApiResponse;
import com.company.apiframework.service.ApiService;

/**
 * Example class demonstrating per-RestTemplate configuration usage.
 * 
 * <p>This example shows how to configure different RestTemplate settings
 * for different types of APIs based on their performance characteristics
 * and requirements.</p>
 * 
 * <p><strong>Key Scenarios Demonstrated:</strong></p>
 * <ul>
 *   <li>Fast APIs (payment processing, user authentication)</li>
 *   <li>Slow APIs (batch processing, data analytics)</li>
 *   <li>External third-party APIs</li>
 *   <li>High-volume internal APIs</li>
 * </ul>
 * 
 * @author API Framework Team
 * @version 1.0
 * @since 1.0
 */
@Component
public class RestTemplateConfigExample {
    
    @Autowired
    private ApiService apiService;
    
    /**
     * Demonstrates configuration setup for different API types.
     * This method would typically be called during application startup
     * or configuration initialization.
     */
    public void setupApiConfigurations() {
        
        // Configuration for fast payment APIs
        RestTemplateConfig paymentConfig = RestTemplateConfig.builder("payment-api")
                .connectionTimeoutMs(2000)    // Fast connection required
                .readTimeoutMs(5000)          // Quick response expected
                .maxRetryAttempts(1)          // Financial operations - avoid duplicates
                .retryDelayMs(500L)           // Short retry delay
                .maxConnectionsPerRoute(5)    // Limited connections for security
                .enableLogging(true)          // Enable for compliance auditing
                .build();
        
        apiService.registerRestTemplateConfig("https://payment-api.company.com/*", paymentConfig);
        apiService.registerRestTemplateConfig("https://billing-api.company.com/*", paymentConfig);
        
        // Configuration for slow batch processing APIs
        RestTemplateConfig batchConfig = RestTemplateConfig.builder("batch-processing")
                .connectionTimeoutMs(10000)   // Allow longer to establish connection
                .readTimeoutMs(300000)        // 5 minutes for batch operations
                .maxRetryAttempts(5)          // Retry batch operations
                .retryDelayMs(5000L)          // Longer delay between retries
                .maxConnections(20)           // More connections for batch work
                .maxConnectionsPerRoute(10)   // Higher per-route limit
                .enableLogging(false)         // Reduce log volume for batch
                .build();
        
        apiService.registerRestTemplateConfig("https://batch-api.company.com/*", batchConfig);
        apiService.registerRestTemplateConfig("https://analytics-api.company.com/*", batchConfig);
        
        // Configuration for external third-party APIs
        RestTemplateConfig externalConfig = RestTemplateConfig.builder("external-apis")
                .connectionTimeoutMs(8000)    // Conservative timeout for external
                .readTimeoutMs(45000)         // Allow for network latency
                .maxRetryAttempts(3)          // Standard retry for external
                .retryDelayMs(2000L)          // Reasonable delay
                .maxConnectionsPerRoute(5)    // Conservative connection limit
                .enableLogging(true)          // Log external API calls
                .build();
        
        apiService.registerRestTemplateConfig("https://*.external-vendor.com/*", externalConfig);
        apiService.registerRestTemplateConfig("https://api.third-party.com/*", externalConfig);
        
        // Configuration for high-volume internal APIs
        RestTemplateConfig highVolumeConfig = RestTemplateConfig.builder("high-volume")
                .connectionTimeoutMs(3000)    // Quick connection for internal
                .readTimeoutMs(10000)         // Fast internal response
                .maxRetryAttempts(2)          // Limited retries for high volume
                .retryDelayMs(1000L)          // Quick retry
                .maxConnections(50)           // High connection limit
                .maxConnectionsPerRoute(25)   // High per-route limit
                .enableLogging(false)         // Reduce logging overhead
                .build();
        
        apiService.registerRestTemplateConfig("https://user-api.company.com/*", highVolumeConfig);
        apiService.registerRestTemplateConfig("https://catalog-api.company.com/*", highVolumeConfig);
    }
    
    /**
     * Example of making API calls with different configurations.
     * The framework automatically selects the appropriate configuration
     * based on the URL pattern.
     */
    public void demonstrateApiCalls() {
        
        // Payment API call - uses fast, secure configuration
        ApiRequest paymentRequest = ApiRequest.builder()
                .url("https://payment-api.company.com/process")
                .method("POST")
                .header("Authorization", "Bearer payment-token")
                .body("{\"amount\":100.00,\"currency\":\"USD\"}")
                .build();
        
        ApiResponse<String> paymentResponse = apiService.executeRest(paymentRequest);
        System.out.println("Payment API Response: " + paymentResponse.getStatusCode());
        
        // Batch API call - uses slow, retry-heavy configuration
        ApiRequest batchRequest = ApiRequest.builder()
                .url("https://batch-api.company.com/process-batch")
                .method("POST")
                .header("Content-Type", "application/json")
                .body("{\"batchId\":\"batch-123\",\"records\":1000}")
                .build();
        
        ApiResponse<String> batchResponse = apiService.executeRest(batchRequest);
        System.out.println("Batch API Response: " + batchResponse.getStatusCode());
        
        // External API call - uses conservative configuration
        ApiRequest externalRequest = ApiRequest.builder()
                .url("https://api.third-party.com/data")
                .method("GET")
                .header("API-Key", "external-api-key")
                .build();
        
        ApiResponse<String> externalResponse = apiService.executeRest(externalRequest);
        System.out.println("External API Response: " + externalResponse.getStatusCode());
        
        // High-volume internal API call - uses optimized configuration
        ApiRequest userRequest = ApiRequest.builder()
                .url("https://user-api.company.com/users/123")
                .method("GET")
                .header("Authorization", "Bearer internal-token")
                .build();
        
        ApiResponse<String> userResponse = apiService.executeRest(userRequest);
        System.out.println("User API Response: " + userResponse.getStatusCode());
    }
    
    /**
     * Demonstrates dynamic configuration management.
     * Configurations can be updated at runtime based on monitoring
     * or changing requirements.
     */
    public void demonstrateDynamicConfiguration() {
        
        // Check current configuration summary
        System.out.println("Current configurations: " + apiService.getConfigurationSummary());
        
        // Add a new configuration for a new API
        RestTemplateConfig newApiConfig = RestTemplateConfig.builder("new-api")
                .connectionTimeoutMs(4000)
                .readTimeoutMs(15000)
                .maxRetryAttempts(3)
                .build();
        
        apiService.registerRestTemplateConfig("https://new-api.company.com/*", newApiConfig);
        
        // Update configuration by removing and re-adding with new settings
        apiService.removeRestTemplateConfig("https://payment-api.company.com/*");
        
        RestTemplateConfig updatedPaymentConfig = RestTemplateConfig.builder("payment-api-v2")
                .connectionTimeoutMs(1500)    // Even faster for improved performance
                .readTimeoutMs(4000)          // Shorter timeout
                .maxRetryAttempts(0)          // No retries for financial safety
                .enableLogging(true)
                .build();
        
        apiService.registerRestTemplateConfig("https://payment-api.company.com/*", updatedPaymentConfig);
        
        System.out.println("Updated configurations: " + apiService.getConfigurationSummary());
    }
    
    /**
     * Example of environment-specific configuration.
     * Different environments may need different settings.
     */
    public void setupEnvironmentSpecificConfig(String environment) {
        
        RestTemplateConfig.Builder baseConfigBuilder = RestTemplateConfig.builder("env-" + environment);
        
        switch (environment.toLowerCase()) {
            case "dev":
                // Development - more logging, shorter timeouts for faster feedback
                baseConfigBuilder
                    .connectionTimeoutMs(2000)
                    .readTimeoutMs(10000)
                    .maxRetryAttempts(1)
                    .enableLogging(true);
                break;
                
            case "test":
                // Testing - moderate timeouts, limited retries
                baseConfigBuilder
                    .connectionTimeoutMs(3000)
                    .readTimeoutMs(15000)
                    .maxRetryAttempts(2)
                    .enableLogging(true);
                break;
                
            case "prod":
                // Production - optimized for performance and reliability
                baseConfigBuilder
                    .connectionTimeoutMs(5000)
                    .readTimeoutMs(30000)
                    .maxRetryAttempts(3)
                    .enableLogging(false);  // Reduced logging for performance
                break;
                
            default:
                throw new IllegalArgumentException("Unknown environment: " + environment);
        }
        
        RestTemplateConfig envConfig = baseConfigBuilder.build();
        
        // Apply environment-specific configuration to all APIs
        apiService.registerRestTemplateConfig("https://*.company.com/*", envConfig);
        
        System.out.println("Applied " + environment + " configuration: " + envConfig);
    }
} 