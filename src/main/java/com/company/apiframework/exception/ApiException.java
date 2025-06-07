package com.company.apiframework.exception;

/**
 * Custom runtime exception for API-related errors in the framework.
 * 
 * <p>This exception is thrown when API calls fail due to various reasons such as:</p>
 * <ul>
 *   <li>Network connectivity issues</li>
 *   <li>HTTP errors (4xx, 5xx status codes)</li>
 *   <li>Request/response parsing failures</li>
 *   <li>Authentication/authorization failures</li>
 *   <li>Timeout exceptions</li>
 *   <li>Configuration errors</li>
 * </ul>
 * 
 * <p>The exception provides additional context through error codes and HTTP status codes
 * to help with programmatic error handling and debugging.</p>
 * 
 * <p><strong>Usage Examples:</strong></p>
 * <pre>
 * // Throwing with just a message
 * throw new ApiException("Failed to connect to API endpoint");
 * 
 * // Throwing with error code for programmatic handling
 * throw new ApiException("AUTH_FAILED", "Invalid API key provided");
 * 
 * // Throwing with HTTP status code context
 * throw new ApiException("VALIDATION_ERROR", "Request validation failed", 400);
 * 
 * // Wrapping another exception
 * try {
 *     // API call
 * } catch (IOException e) {
 *     throw new ApiException("NETWORK_ERROR", "Connection failed", e);
 * }
 * </pre>
 * 
 * @author API Framework Team
 * @version 1.0
 * @since 1.0
 */
public class ApiException extends RuntimeException {
    
    /**
     * Application-specific error code for programmatic error handling.
     * Examples: "TIMEOUT", "AUTH_FAILED", "VALIDATION_ERROR", "NETWORK_ERROR"
     */
    private String errorCode;
    
    /**
     * HTTP status code associated with the error, if applicable.
     * Useful when the exception is related to an HTTP response error.
     */
    private int statusCode;
    
    /**
     * Creates an ApiException with just an error message.
     * 
     * @param message Human-readable error description
     */
    public ApiException(String message) {
        super(message);
    }
    
    /**
     * Creates an ApiException with a message and underlying cause.
     * 
     * <p>Use this constructor when wrapping another exception that caused
     * the API failure, such as IOException, SocketTimeoutException, etc.</p>
     * 
     * @param message Human-readable error description
     * @param cause The underlying exception that caused this error
     */
    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Creates an ApiException with an error code and message.
     * 
     * <p>The error code enables programmatic error handling by allowing
     * applications to respond differently to different types of errors.</p>
     * 
     * @param errorCode Application-specific error code
     * @param message Human-readable error description
     */
    public ApiException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * Creates an ApiException with error code, message, and underlying cause.
     * 
     * <p>This is the most comprehensive constructor, providing both programmatic
     * error handling capability and exception chaining.</p>
     * 
     * @param errorCode Application-specific error code
     * @param message Human-readable error description
     * @param cause The underlying exception that caused this error
     */
    public ApiException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * Creates an ApiException with error code, message, and HTTP status code.
     * 
     * <p>Use this constructor when the error is related to an HTTP response
     * with a specific status code (4xx, 5xx).</p>
     * 
     * @param errorCode Application-specific error code
     * @param message Human-readable error description
     * @param statusCode HTTP status code associated with the error
     */
    public ApiException(String errorCode, String message, int statusCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }
    
    /**
     * Creates an ApiException with all available context information.
     * 
     * <p>This constructor provides the most complete error context, including
     * error code, HTTP status code, and exception chaining.</p>
     * 
     * @param errorCode Application-specific error code
     * @param message Human-readable error description
     * @param statusCode HTTP status code associated with the error
     * @param cause The underlying exception that caused this error
     */
    public ApiException(String errorCode, String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }
    
    /**
     * Gets the application-specific error code.
     * 
     * @return Error code for programmatic handling, or null if not set
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Sets the application-specific error code.
     * 
     * @param errorCode Error code for programmatic handling
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    /**
     * Gets the HTTP status code associated with the error.
     * 
     * @return HTTP status code, or 0 if not set
     */
    public int getStatusCode() {
        return statusCode;
    }
    
    /**
     * Sets the HTTP status code associated with the error.
     * 
     * @param statusCode HTTP status code (typically 4xx or 5xx)
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
} 