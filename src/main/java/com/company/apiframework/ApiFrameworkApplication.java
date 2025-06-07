package com.company.apiframework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main Spring Boot application class for the API Integration Framework.
 * 
 * <p>This framework provides a unified interface for making REST and SOAP API calls
 * with built-in features including:</p>
 * <ul>
 *   <li>Automatic protocol detection (REST/SOAP)</li>
 *   <li>Request/response logging and monitoring</li>
 *   <li>Retry mechanisms with exponential backoff</li>
 *   <li>Custom RestTemplate support per API endpoint</li>
 *   <li>Mock API capabilities for testing</li>
 *   <li>Configurable timeouts and connection pooling</li>
 * </ul>
 * 
 * <p><strong>Usage:</strong></p>
 * <p>This application serves as a client-side library and does not expose REST endpoints.
 * It can be embedded in other Spring Boot applications or run standalone for testing purposes.</p>
 * 
 * <p><strong>Configuration:</strong></p>
 * <p>Configure the framework using application properties with prefix 'api.framework'.
 * See {@link com.company.apiframework.config.ApiProperties} for available options.</p>
 * 
 * <p><strong>Main Entry Points:</strong></p>
 * <ul>
 *   <li>{@link com.company.apiframework.service.ApiService} - Main service for API calls</li>
 *   <li>{@link com.company.apiframework.mock.MockApiService} - Mock service for testing</li>
 * </ul>
 * 
 * @author API Framework Team
 * @version 1.0
 * @since 1.0
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.company.apiframework")
public class ApiFrameworkApplication {
    
    /**
     * Main method to start the Spring Boot application.
     * 
     * <p>When run standalone, this starts the application with management endpoints
     * available for monitoring (health, metrics, etc.).</p>
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ApiFrameworkApplication.class, args);
    }
} 