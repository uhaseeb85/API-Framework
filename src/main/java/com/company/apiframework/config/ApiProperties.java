package com.company.apiframework.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the API Integration Framework.
 * 
 * <p>This class contains all configurable properties that control the behavior
 * of the API framework. Properties are bound from application configuration files
 * using the prefix 'api.framework'.</p>
 * 
 * <p><strong>Example Configuration (application.yml):</strong></p>
 * <pre>
 * api:
 *   framework:
 *     connection-timeout-ms: 5000
 *     read-timeout-ms: 30000
 *     max-connections: 100
 *     max-connections-per-route: 20
 *     max-retry-attempts: 3
 *     retry-delay-ms: 1000
 *     enable-logging: true
 *     enable-mocking: false
 *     mock-server-url: "http://localhost:8089"
 * </pre>
 * 
 * <p><strong>Property Categories:</strong></p>
 * <ul>
 *   <li><strong>Connection Settings:</strong> Timeouts and connection pooling</li>
 *   <li><strong>Retry Configuration:</strong> Fault tolerance settings</li>
 *   <li><strong>Feature Toggles:</strong> Enable/disable framework features</li>
 *   <li><strong>Mock Configuration:</strong> Testing and development support</li>
 * </ul>
 * 
 * @author API Framework Team
 * @version 1.0
 * @since 1.0
 */
@ConfigurationProperties(prefix = "api.framework")
public class ApiProperties {
    
    /**
     * Connection timeout in milliseconds for establishing HTTP connections.
     * 
     * <p>This timeout controls how long the framework will wait when trying to
     * establish a connection to the target API server. If the connection cannot
     * be established within this time, a timeout exception will be thrown.</p>
     * 
     * <p><strong>Default:</strong> 5000ms (5 seconds)</p>
     * <p><strong>Recommended Range:</strong> 1000-10000ms</p>
     */
    private int connectionTimeoutMs = 5000;
    
    /**
     * Read timeout in milliseconds for reading response data.
     * 
     * <p>This timeout controls how long the framework will wait for response data
     * after a connection has been established. This should be set based on the
     * expected response time of your APIs.</p>
     * 
     * <p><strong>Default:</strong> 30000ms (30 seconds)</p>
     * <p><strong>Recommended Range:</strong> 5000-60000ms</p>
     */
    private int readTimeoutMs = 30000;
    
    /**
     * Maximum total number of connections in the connection pool.
     * 
     * <p>This setting controls the total number of HTTP connections that can be
     * maintained in the connection pool across all routes. Higher values allow
     * more concurrent API calls but consume more system resources.</p>
     * 
     * <p><strong>Default:</strong> 100</p>
     * <p><strong>Recommended Range:</strong> 50-500 (depending on load)</p>
     */
    private int maxConnections = 100;
    
    /**
     * Maximum number of connections per route (per target host).
     * 
     * <p>This setting limits the number of concurrent connections to any single
     * API endpoint/host. It helps prevent overwhelming individual services while
     * allowing the total connection pool to be distributed across multiple APIs.</p>
     * 
     * <p><strong>Default:</strong> 20</p>
     * <p><strong>Recommended Range:</strong> 10-50 (per API endpoint)</p>
     */
    private int maxConnectionsPerRoute = 20;
    
    /**
     * Maximum number of retry attempts for failed API calls.
     * 
     * <p>When an API call fails due to network issues or temporary server problems,
     * the framework will automatically retry the request up to this many times.
     * Retries use exponential backoff to avoid overwhelming the target service.</p>
     * 
     * <p><strong>Default:</strong> 3</p>
     * <p><strong>Recommended Range:</strong> 1-5</p>
     * 
     * @see #retryDelayMs
     */
    private int maxRetryAttempts = 3;
    
    /**
     * Initial delay in milliseconds between retry attempts.
     * 
     * <p>This is the base delay used for the exponential backoff retry strategy.
     * The actual delay for each retry will be: retryDelayMs * (2 ^ attemptNumber).
     * For example, with 1000ms base delay: 1s, 2s, 4s, 8s...</p>
     * 
     * <p><strong>Default:</strong> 1000ms (1 second)</p>
     * <p><strong>Recommended Range:</strong> 500-5000ms</p>
     * 
     * @see #maxRetryAttempts
     */
    private long retryDelayMs = 1000;
    
    /**
     * Enable or disable request/response logging.
     * 
     * <p>When enabled, the framework will log HTTP requests and responses.
     * The logging level depends on the configured log level:</p>
     * <ul>
     *   <li><strong>DEBUG:</strong> Full request/response details including headers and body</li>
     *   <li><strong>INFO:</strong> Summary information (method, URL, status, duration)</li>
     * </ul>
     * 
     * <p><strong>Default:</strong> true</p>
     * <p><strong>Note:</strong> Disable in production if sensitive data is being logged</p>
     */
    private boolean enableLogging = true;
    
    /**
     * Enable or disable mock API functionality.
     * 
     * <p>When enabled, the framework can intercept API calls and return mock responses
     * instead of making actual HTTP requests. This is useful for:</p>
     * <ul>
     *   <li>Unit and integration testing</li>
     *   <li>Development when external APIs are unavailable</li>
     *   <li>Performance testing with predictable responses</li>
     * </ul>
     * 
     * <p><strong>Default:</strong> false</p>
     * <p><strong>Usage:</strong> Enable only in test/development environments</p>
     * 
     * @see MockApiService
     */
    private boolean enableMocking = false;
    
    /**
     * URL of the mock server for external mock services.
     * 
     * <p>When using external mock services (like WireMock, MockServer, etc.),
     * this URL specifies where the mock server is running. The framework can
     * redirect API calls to this mock server when mocking is enabled.</p>
     * 
     * <p><strong>Default:</strong> "http://localhost:8089"</p>
     * <p><strong>Note:</strong> Only used when enableMocking is true and external mocking is configured</p>
     */
    private String mockServerUrl = "http://localhost:8089";

    // Getters and Setters with additional documentation
    
    /**
     * Gets the connection timeout in milliseconds.
     * @return Connection timeout value
     */
    public int getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    /**
     * Sets the connection timeout in milliseconds.
     * @param connectionTimeoutMs Connection timeout value (must be positive)
     */
    public void setConnectionTimeoutMs(int connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    /**
     * Gets the read timeout in milliseconds.
     * @return Read timeout value
     */
    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    /**
     * Sets the read timeout in milliseconds.
     * @param readTimeoutMs Read timeout value (must be positive)
     */
    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    /**
     * Gets the maximum total connections in the pool.
     * @return Maximum connections value
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    /**
     * Sets the maximum total connections in the pool.
     * @param maxConnections Maximum connections value (must be positive)
     */
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    /**
     * Gets the maximum connections per route.
     * @return Maximum connections per route value
     */
    public int getMaxConnectionsPerRoute() {
        return maxConnectionsPerRoute;
    }

    /**
     * Sets the maximum connections per route.
     * @param maxConnectionsPerRoute Maximum connections per route (must be positive and <= maxConnections)
     */
    public void setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
    }

    /**
     * Gets the maximum retry attempts.
     * @return Maximum retry attempts value
     */
    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    /**
     * Sets the maximum retry attempts.
     * @param maxRetryAttempts Maximum retry attempts (must be non-negative)
     */
    public void setMaxRetryAttempts(int maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
    }

    /**
     * Gets the retry delay in milliseconds.
     * @return Retry delay value
     */
    public long getRetryDelayMs() {
        return retryDelayMs;
    }

    /**
     * Sets the retry delay in milliseconds.
     * @param retryDelayMs Retry delay value (must be positive)
     */
    public void setRetryDelayMs(long retryDelayMs) {
        this.retryDelayMs = retryDelayMs;
    }

    /**
     * Checks if logging is enabled.
     * @return true if logging is enabled, false otherwise
     */
    public boolean isEnableLogging() {
        return enableLogging;
    }

    /**
     * Enables or disables logging.
     * @param enableLogging true to enable logging, false to disable
     */
    public void setEnableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }

    /**
     * Checks if mocking is enabled.
     * @return true if mocking is enabled, false otherwise
     */
    public boolean isEnableMocking() {
        return enableMocking;
    }

    /**
     * Enables or disables mocking functionality.
     * @param enableMocking true to enable mocking, false to disable
     */
    public void setEnableMocking(boolean enableMocking) {
        this.enableMocking = enableMocking;
    }

    /**
     * Gets the mock server URL.
     * @return Mock server URL
     */
    public String getMockServerUrl() {
        return mockServerUrl;
    }

    /**
     * Sets the mock server URL.
     * @param mockServerUrl Mock server URL (must be a valid URL)
     */
    public void setMockServerUrl(String mockServerUrl) {
        this.mockServerUrl = mockServerUrl;
    }
} 