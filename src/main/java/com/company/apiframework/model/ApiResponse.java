package com.company.apiframework.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Standardized API response model for both REST and SOAP calls.
 * 
 * <p>This class represents a unified response structure that encapsulates the result
 * of API calls regardless of the underlying protocol (REST or SOAP). It provides
 * a consistent interface for handling responses, errors, and metadata.</p>
 * 
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li>Generic type support for strongly-typed response bodies</li>
 *   <li>Comprehensive error handling with error codes and messages</li>
 *   <li>Response metadata (status codes, headers, timing)</li>
 *   <li>Success/failure determination logic</li>
 *   <li>Raw response preservation for debugging</li>
 * </ul>
 * 
 * <p><strong>Usage Examples:</strong></p>
 * <pre>
 * // Handling a successful response
 * ApiResponse&lt;User&gt; response = apiService.executeRest(request, User.class);
 * if (response.isSuccess()) {
 *     User user = response.getBody();
 *     // Process user data
 * } else {
 *     logger.error("API call failed: {}", response.getErrorMessage());
 * }
 * 
 * // Checking for specific error conditions
 * if (response.hasError()) {
 *     String errorCode = response.getErrorCode();
 *     if ("VALIDATION_ERROR".equals(errorCode)) {
 *         // Handle validation errors
 *     }
 * }
 * </pre>
 * 
 * @param <T> The type of the response body
 * @author API Framework Team
 * @version 1.0
 * @since 1.0
 */
public class ApiResponse<T> {
    
    /**
     * HTTP status code returned by the API.
     * Standard HTTP status codes (200, 404, 500, etc.)
     */
    private int statusCode;
    
    /**
     * HTTP status message corresponding to the status code.
     * Examples: "OK", "Not Found", "Internal Server Error"
     */
    private String statusMessage;
    
    /**
     * HTTP response headers returned by the API.
     * Contains metadata like Content-Type, Cache-Control, etc.
     */
    private Map<String, String> headers;
    
    /**
     * The parsed response body content.
     * Type is determined by the generic parameter T.
     */
    private T body;
    
    /**
     * Indicates whether the API call was successful.
     * Automatically set based on HTTP status code (2xx = success).
     */
    private boolean success;
    
    /**
     * Human-readable error message when the call fails.
     * Provides details about what went wrong.
     */
    private String errorMessage;
    
    /**
     * Application-specific error code for programmatic error handling.
     * Examples: "VALIDATION_ERROR", "TIMEOUT", "AUTHENTICATION_FAILED"
     */
    private String errorCode;
    
    /**
     * Time taken to complete the API call in milliseconds.
     * Useful for performance monitoring and debugging.
     */
    private long responseTimeMs;
    
    /**
     * Raw response content as received from the API.
     * Preserved for debugging and troubleshooting purposes.
     */
    private String rawResponse;
    
    /**
     * Default constructor that initializes empty headers map.
     */
    public ApiResponse() {
        this.headers = new HashMap<>();
    }
    
    /**
     * Constructor with status code and response body.
     * Automatically determines success based on status code.
     * 
     * @param statusCode HTTP status code
     * @param body Response body content
     */
    public ApiResponse(int statusCode, T body) {
        this();
        this.statusCode = statusCode;
        this.body = body;
        this.success = statusCode >= 200 && statusCode < 300;
    }
    
    // Getters and Setters with documentation
    
    /**
     * Gets the HTTP status code.
     * @return HTTP status code (200, 404, 500, etc.)
     */
    public int getStatusCode() {
        return statusCode;
    }
    
    /**
     * Sets the HTTP status code and automatically updates success flag.
     * 
     * @param statusCode HTTP status code
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        this.success = statusCode >= 200 && statusCode < 300;
    }
    
    /**
     * Gets the HTTP status message.
     * @return Status message corresponding to the status code
     */
    public String getStatusMessage() {
        return statusMessage;
    }
    
    /**
     * Sets the HTTP status message.
     * @param statusMessage Status message (e.g., "OK", "Not Found")
     */
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
    
    /**
     * Gets the response headers map.
     * @return Mutable map of header name-value pairs
     */
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    /**
     * Sets the response headers map.
     * @param headers Map of header name-value pairs
     */
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    
    /**
     * Gets the parsed response body.
     * @return Response body of type T, or null if no body
     */
    public T getBody() {
        return body;
    }
    
    /**
     * Sets the response body content.
     * @param body Response body of type T
     */
    public void setBody(T body) {
        this.body = body;
    }
    
    /**
     * Checks if the API call was successful.
     * @return true if status code is 2xx, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Manually sets the success flag.
     * Note: This is automatically set when status code is updated.
     * 
     * @param success true if successful, false otherwise
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    /**
     * Gets the error message.
     * @return Human-readable error description, or null if no error
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Sets the error message.
     * @param errorMessage Human-readable error description
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    /**
     * Gets the application-specific error code.
     * @return Error code for programmatic handling, or null if no error
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Sets the application-specific error code.
     * @param errorCode Error code for programmatic handling
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    /**
     * Gets the response time in milliseconds.
     * @return Time taken to complete the API call
     */
    public long getResponseTimeMs() {
        return responseTimeMs;
    }
    
    /**
     * Sets the response time in milliseconds.
     * @param responseTimeMs Time taken to complete the API call
     */
    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }
    
    /**
     * Gets the raw response content.
     * @return Raw response as received from the API
     */
    public String getRawResponse() {
        return rawResponse;
    }
    
    /**
     * Sets the raw response content.
     * @param rawResponse Raw response as received from the API
     */
    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }
    
    // Helper methods with documentation
    
    /**
     * Checks if the response contains an error.
     * 
     * <p>A response is considered to have an error if:</p>
     * <ul>
     *   <li>The success flag is false, OR</li>
     *   <li>An error message has been set</li>
     * </ul>
     * 
     * @return true if there's an error, false otherwise
     */
    public boolean hasError() {
        return !success || errorMessage != null;
    }
    
    /**
     * Marks the response as failed with an error message.
     * 
     * <p>This is a convenience method that sets both the success flag to false
     * and provides an error message for the failure.</p>
     * 
     * @param errorMessage Human-readable error description
     */
    public void markAsError(String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
    }
    
    /**
     * Marks the response as failed with both error code and message.
     * 
     * <p>This is a convenience method that sets the success flag to false
     * and provides both programmatic error code and human-readable message.</p>
     * 
     * @param errorCode Application-specific error code
     * @param errorMessage Human-readable error description
     */
    public void markAsError(String errorCode, String errorMessage) {
        this.success = false;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
    /**
     * Returns a string representation of the response for debugging.
     * 
     * <p>Includes key information like status code, success flag, error details,
     * and response time. Does not include the full response body to avoid
     * cluttering logs with large data.</p>
     * 
     * @return String representation of the response
     */
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