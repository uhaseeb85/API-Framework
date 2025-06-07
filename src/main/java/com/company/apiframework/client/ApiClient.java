package com.company.apiframework.client;

import com.company.apiframework.model.ApiRequest;
import com.company.apiframework.model.ApiResponse;

/**
 * Main interface for API clients supporting both REST and SOAP protocols
 */
public interface ApiClient {
    
    /**
     * Execute API request and return response
     * 
     * @param request The API request to execute
     * @param responseType The expected response type
     * @return ApiResponse containing the result
     */
    <T> ApiResponse<T> execute(ApiRequest request, Class<T> responseType);
    
    /**
     * Execute API request with string response
     * 
     * @param request The API request to execute
     * @return ApiResponse containing string result
     */
    ApiResponse<String> execute(ApiRequest request);
    
    /**
     * Execute API request asynchronously
     * 
     * @param request The API request to execute
     * @param responseType The expected response type
     * @param callback Callback to handle the response
     */
    <T> void executeAsync(ApiRequest request, Class<T> responseType, ApiCallback<T> callback);
    
    /**
     * Check if the client supports the given protocol
     * 
     * @param protocol The protocol to check (REST, SOAP)
     * @return true if supported, false otherwise
     */
    boolean supportsProtocol(String protocol);
    
    /**
     * Get the protocol type this client handles
     * 
     * @return The protocol type (REST, SOAP)
     */
    String getProtocolType();
} 