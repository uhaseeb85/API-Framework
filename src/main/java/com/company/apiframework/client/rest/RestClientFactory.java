package com.company.apiframework.client.rest;

import org.springframework.web.client.RestTemplate;

import com.company.apiframework.client.ApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Factory for creating REST API clients
 */
public class RestClientFactory {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public RestClientFactory(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Create a new REST API client
     * 
     * @return New REST API client instance
     */
    public ApiClient createClient() {
        return new RestApiClient(restTemplate, objectMapper);
    }
    
    /**
     * Create a REST API client with custom RestTemplate
     * 
     * @param customRestTemplate Custom RestTemplate to use
     * @return New REST API client instance
     */
    public ApiClient createClient(RestTemplate customRestTemplate) {
        return new RestApiClient(customRestTemplate, objectMapper);
    }
} 