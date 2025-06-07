package com.company.apiframework.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for API Framework
 */
@ConfigurationProperties(prefix = "api.framework")
public class ApiProperties {
    
    private int connectionTimeoutMs = 5000;
    private int readTimeoutMs = 30000;
    private int maxConnections = 100;
    private int maxConnectionsPerRoute = 20;
    private int maxRetryAttempts = 3;
    private long retryDelayMs = 1000;
    private boolean enableLogging = true;
    private boolean enableMocking = false;
    private String mockServerUrl = "http://localhost:8089";

    // Getters and Setters
    public int getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public void setConnectionTimeoutMs(int connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getMaxConnectionsPerRoute() {
        return maxConnectionsPerRoute;
    }

    public void setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
    }

    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    public void setMaxRetryAttempts(int maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
    }

    public long getRetryDelayMs() {
        return retryDelayMs;
    }

    public void setRetryDelayMs(long retryDelayMs) {
        this.retryDelayMs = retryDelayMs;
    }

    public boolean isEnableLogging() {
        return enableLogging;
    }

    public void setEnableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }

    public boolean isEnableMocking() {
        return enableMocking;
    }

    public void setEnableMocking(boolean enableMocking) {
        this.enableMocking = enableMocking;
    }

    public String getMockServerUrl() {
        return mockServerUrl;
    }

    public void setMockServerUrl(String mockServerUrl) {
        this.mockServerUrl = mockServerUrl;
    }
} 