package com.company.apiframework.config;

/**
 * Configuration class for individual RestTemplate instances.
 * 
 * <p>This class encapsulates all configuration settings that can be applied
 * to individual RestTemplate instances, allowing fine-grained control over
 * HTTP client behavior per API endpoint or service.</p>
 * 
 * <p><strong>Key Benefits:</strong></p>
 * <ul>
 *   <li>Per-API timeout configuration</li>
 *   <li>Endpoint-specific connection pooling</li>
 *   <li>Custom retry strategies per service</li>
 *   <li>Service-specific performance tuning</li>
 * </ul>
 * 
 * <p><strong>Usage Examples:</strong></p>
 * <pre>
 * // Fast API with short timeouts
 * RestTemplateConfig fastApiConfig = RestTemplateConfig.builder()
 *     .connectionTimeoutMs(2000)
 *     .readTimeoutMs(5000)
 *     .maxRetryAttempts(1)
 *     .build();
 * 
 * // Slow batch API with long timeouts
 * RestTemplateConfig batchApiConfig = RestTemplateConfig.builder()
 *     .connectionTimeoutMs(10000)
 *     .readTimeoutMs(120000)
 *     .maxRetryAttempts(5)
 *     .retryDelayMs(2000)
 *     .build();
 * 
 * // Register with specific patterns
 * apiService.registerCustomRestTemplate("https://fast-api.com/*", fastApiConfig);
 * apiService.registerCustomRestTemplate("https://batch-api.com/*", batchApiConfig);
 * </pre>
 * 
 * @author API Framework Team
 * @version 1.0
 * @since 1.0
 */
public class RestTemplateConfig {
    
    /**
     * Connection timeout in milliseconds for establishing HTTP connections.
     * If not set, defaults to global configuration.
     */
    private Integer connectionTimeoutMs;
    
    /**
     * Read timeout in milliseconds for reading response data.
     * If not set, defaults to global configuration.
     */
    private Integer readTimeoutMs;
    
    /**
     * Maximum total number of connections for this RestTemplate.
     * If not set, defaults to global configuration.
     */
    private Integer maxConnections;
    
    /**
     * Maximum number of connections per route for this RestTemplate.
     * If not set, defaults to global configuration.
     */
    private Integer maxConnectionsPerRoute;
    
    /**
     * Maximum number of retry attempts for failed API calls.
     * If not set, defaults to global configuration.
     */
    private Integer maxRetryAttempts;
    
    /**
     * Initial delay in milliseconds between retry attempts.
     * If not set, defaults to global configuration.
     */
    private Long retryDelayMs;
    
    /**
     * Whether to enable request/response logging for this RestTemplate.
     * If not set, defaults to global configuration.
     */
    private Boolean enableLogging;
    
    /**
     * Custom name/identifier for this configuration.
     * Useful for logging and debugging purposes.
     */
    private String configName;
    
    /**
     * Default constructor.
     */
    public RestTemplateConfig() {
    }
    
    /**
     * Constructor with configuration name.
     * 
     * @param configName Descriptive name for this configuration
     */
    public RestTemplateConfig(String configName) {
        this.configName = configName;
    }
    
    // Getters and Setters
    
    public Integer getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }
    
    public void setConnectionTimeoutMs(Integer connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }
    
    public Integer getReadTimeoutMs() {
        return readTimeoutMs;
    }
    
    public void setReadTimeoutMs(Integer readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }
    
    public Integer getMaxConnections() {
        return maxConnections;
    }
    
    public void setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
    }
    
    public Integer getMaxConnectionsPerRoute() {
        return maxConnectionsPerRoute;
    }
    
    public void setMaxConnectionsPerRoute(Integer maxConnectionsPerRoute) {
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
    }
    
    public Integer getMaxRetryAttempts() {
        return maxRetryAttempts;
    }
    
    public void setMaxRetryAttempts(Integer maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
    }
    
    public Long getRetryDelayMs() {
        return retryDelayMs;
    }
    
    public void setRetryDelayMs(Long retryDelayMs) {
        this.retryDelayMs = retryDelayMs;
    }
    
    public Boolean getEnableLogging() {
        return enableLogging;
    }
    
    public void setEnableLogging(Boolean enableLogging) {
        this.enableLogging = enableLogging;
    }
    
    public String getConfigName() {
        return configName;
    }
    
    public void setConfigName(String configName) {
        this.configName = configName;
    }
    
    /**
     * Utility method to get connection timeout with fallback to global config.
     * 
     * @param globalConfig Global configuration to use as fallback
     * @return Connection timeout value
     */
    public int getConnectionTimeoutMs(ApiProperties globalConfig) {
        return connectionTimeoutMs != null ? connectionTimeoutMs : globalConfig.getConnectionTimeoutMs();
    }
    
    /**
     * Utility method to get read timeout with fallback to global config.
     * 
     * @param globalConfig Global configuration to use as fallback
     * @return Read timeout value
     */
    public int getReadTimeoutMs(ApiProperties globalConfig) {
        return readTimeoutMs != null ? readTimeoutMs : globalConfig.getReadTimeoutMs();
    }
    
    /**
     * Utility method to get max connections with fallback to global config.
     * 
     * @param globalConfig Global configuration to use as fallback
     * @return Max connections value
     */
    public int getMaxConnections(ApiProperties globalConfig) {
        return maxConnections != null ? maxConnections : globalConfig.getMaxConnections();
    }
    
    /**
     * Utility method to get max connections per route with fallback to global config.
     * 
     * @param globalConfig Global configuration to use as fallback
     * @return Max connections per route value
     */
    public int getMaxConnectionsPerRoute(ApiProperties globalConfig) {
        return maxConnectionsPerRoute != null ? maxConnectionsPerRoute : globalConfig.getMaxConnectionsPerRoute();
    }
    
    /**
     * Utility method to get max retry attempts with fallback to global config.
     * 
     * @param globalConfig Global configuration to use as fallback
     * @return Max retry attempts value
     */
    public int getMaxRetryAttempts(ApiProperties globalConfig) {
        return maxRetryAttempts != null ? maxRetryAttempts : globalConfig.getMaxRetryAttempts();
    }
    
    /**
     * Utility method to get retry delay with fallback to global config.
     * 
     * @param globalConfig Global configuration to use as fallback
     * @return Retry delay value
     */
    public long getRetryDelayMs(ApiProperties globalConfig) {
        return retryDelayMs != null ? retryDelayMs : globalConfig.getRetryDelayMs();
    }
    
    /**
     * Utility method to get logging setting with fallback to global config.
     * 
     * @param globalConfig Global configuration to use as fallback
     * @return Logging enabled flag
     */
    public boolean isEnableLogging(ApiProperties globalConfig) {
        return enableLogging != null ? enableLogging : globalConfig.isEnableLogging();
    }
    
    /**
     * Creates a new builder instance for fluent configuration construction.
     * 
     * @return New Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Creates a new builder instance with a configuration name.
     * 
     * @param configName Descriptive name for this configuration
     * @return New Builder instance
     */
    public static Builder builder(String configName) {
        return new Builder(configName);
    }
    
    /**
     * Builder class for fluent RestTemplateConfig construction.
     */
    public static class Builder {
        private RestTemplateConfig config;
        
        public Builder() {
            this.config = new RestTemplateConfig();
        }
        
        public Builder(String configName) {
            this.config = new RestTemplateConfig(configName);
        }
        
        public Builder connectionTimeoutMs(int connectionTimeoutMs) {
            config.setConnectionTimeoutMs(connectionTimeoutMs);
            return this;
        }
        
        public Builder readTimeoutMs(int readTimeoutMs) {
            config.setReadTimeoutMs(readTimeoutMs);
            return this;
        }
        
        public Builder maxConnections(int maxConnections) {
            config.setMaxConnections(maxConnections);
            return this;
        }
        
        public Builder maxConnectionsPerRoute(int maxConnectionsPerRoute) {
            config.setMaxConnectionsPerRoute(maxConnectionsPerRoute);
            return this;
        }
        
        public Builder maxRetryAttempts(int maxRetryAttempts) {
            config.setMaxRetryAttempts(maxRetryAttempts);
            return this;
        }
        
        public Builder retryDelayMs(long retryDelayMs) {
            config.setRetryDelayMs(retryDelayMs);
            return this;
        }
        
        public Builder enableLogging(boolean enableLogging) {
            config.setEnableLogging(enableLogging);
            return this;
        }
        
        public Builder configName(String configName) {
            config.setConfigName(configName);
            return this;
        }
        
        public RestTemplateConfig build() {
            return config;
        }
    }
    
    @Override
    public String toString() {
        return "RestTemplateConfig{" +
                "configName='" + configName + '\'' +
                ", connectionTimeoutMs=" + connectionTimeoutMs +
                ", readTimeoutMs=" + readTimeoutMs +
                ", maxConnections=" + maxConnections +
                ", maxConnectionsPerRoute=" + maxConnectionsPerRoute +
                ", maxRetryAttempts=" + maxRetryAttempts +
                ", retryDelayMs=" + retryDelayMs +
                ", enableLogging=" + enableLogging +
                '}';
    }
} 