package com.company.apiframework.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Standardized API response model for both REST and SOAP calls
 */
public class ApiResponse<T> {
    
    private int statusCode;
    private String statusMessage;
    private Map<String, String> headers;
    private T body;
    private boolean success;
    private String errorMessage;
    private String errorCode;
    private long responseTimeMs;
    private String rawResponse;
    
    public ApiResponse() {
        this.headers = new HashMap<>();
    }
    
    public ApiResponse(int statusCode, T body) {
        this();
        this.statusCode = statusCode;
        this.body = body;
        this.success = statusCode >= 200 && statusCode < 300;
    }
    
    // Getters and Setters
    public int getStatusCode() {
        return statusCode;
    }
    
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        this.success = statusCode >= 200 && statusCode < 300;
    }
    
    public String getStatusMessage() {
        return statusMessage;
    }
    
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    
    public T getBody() {
        return body;
    }
    
    public void setBody(T body) {
        this.body = body;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public long getResponseTimeMs() {
        return responseTimeMs;
    }
    
    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }
    
    public String getRawResponse() {
        return rawResponse;
    }
    
    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }
    
    // Helper methods
    public boolean hasError() {
        return !success || errorMessage != null;
    }
    
    public void markAsError(String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
    }
    
    public void markAsError(String errorCode, String errorMessage) {
        this.success = false;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
    @Override
    public String toString() {
        return "ApiResponse{" +
                "statusCode=" + statusCode +
                ", statusMessage='" + statusMessage + '\'' +
                ", success=" + success +
                ", errorMessage='" + errorMessage + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", responseTimeMs=" + responseTimeMs +
                '}';
    }
} 