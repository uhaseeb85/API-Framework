package com.company.apiframework.client;

import com.company.apiframework.model.ApiResponse;

/**
 * Callback interface for asynchronous API calls
 */
public interface ApiCallback<T> {
    
    /**
     * Called when the API call completes successfully
     * 
     * @param response The successful response
     */
    void onSuccess(ApiResponse<T> response);
    
    /**
     * Called when the API call fails
     * 
     * @param response The error response
     */
    void onError(ApiResponse<T> response);
    
    /**
     * Called when an exception occurs during the API call
     * 
     * @param exception The exception that occurred
     */
    void onException(Exception exception);
} 