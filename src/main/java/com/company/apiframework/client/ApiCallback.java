package com.company.apiframework.client;

import com.company.apiframework.model.ApiResponse;

/**
 * Callback interface for handling asynchronous API call results.
 * 
 * <p>This interface defines the contract for handling the results of asynchronous
 * API calls. It provides separate methods for handling successful responses,
 * HTTP errors, and system exceptions, allowing for fine-grained error handling
 * and response processing.</p>
 * 
 * <p><strong>Callback Flow:</strong></p>
 * <ol>
 *   <li>API call is initiated asynchronously</li>
 *   <li>Calling thread continues execution</li>
 *   <li>When API call completes, one of the callback methods is invoked</li>
 *   <li>Callback handles the result appropriately</li>
 * </ol>
 * 
 * <p><strong>Method Selection Logic:</strong></p>
 * <ul>
 *   <li>{@code onSuccess()} - HTTP 2xx status codes</li>
 *   <li>{@code onError()} - HTTP 4xx/5xx status codes</li>
 *   <li>{@code onException()} - Network errors, timeouts, parsing failures</li>
 * </ul>
 * 
 * <p><strong>Usage Examples:</strong></p>
 * <pre>
 * // Lambda-based callback
 * apiClient.executeAsync(request, User.class, new ApiCallback&lt;User&gt;() {
 *     &#64;Override
 *     public void onSuccess(ApiResponse&lt;User&gt; response) {
 *         User user = response.getBody();
 *         // Process successful response
 *     }
 *     
 *     &#64;Override
 *     public void onError(ApiResponse&lt;User&gt; response) {
 *         logger.error("API error: {} - {}", 
 *             response.getStatusCode(), response.getErrorMessage());
 *     }
 *     
 *     &#64;Override
 *     public void onException(Exception exception) {
 *         logger.error("API call failed", exception);
 *     }
 * });
 * 
 * // Using default methods for simplified handling
 * apiClient.executeAsync(request, String.class, new ApiCallback&lt;String&gt;() {
 *     &#64;Override
 *     public void onSuccess(ApiResponse&lt;String&gt; response) {
 *         processResponse(response.getBody());
 *     }
 *     // onError and onException use default implementations
 * });
 * </pre>
 * 
 * <p><strong>Thread Safety:</strong> Callback methods may be invoked on different
 * threads than the calling thread. Implementations should be thread-safe and
 * avoid blocking operations that could impact the async execution pool.</p>
 * 
 * @param <T> The type of the response body
 * @author API Framework Team
 * @version 1.0
 * @since 1.0
 * @see ApiClient#executeAsync(com.company.apiframework.model.ApiRequest, Class, ApiCallback)
 * @see ApiResponse
 */
public interface ApiCallback<T> {
    
    /**
     * Called when the API call completes successfully with a 2xx HTTP status code.
     * 
     * <p>This method is invoked for all successful HTTP responses, regardless of
     * the specific 2xx status code (200, 201, 204, etc.). The response object
     * contains the parsed response body, headers, and metadata.</p>
     * 
     * <p><strong>Implementation Guidelines:</strong></p>
     * <ul>
     *   <li>Keep processing lightweight to avoid blocking the async thread pool</li>
     *   <li>Handle potential null response bodies gracefully</li>
     *   <li>Consider offloading heavy processing to separate threads</li>
     *   <li>Ensure thread-safe access to shared resources</li>
     * </ul>
     * 
     * <p><strong>Common Use Cases:</strong></p>
     * <ul>
     *   <li>Updating UI components with received data</li>
     *   <li>Caching successful responses</li>
     *   <li>Triggering follow-up API calls</li>
     *   <li>Logging successful operations</li>
     * </ul>
     * 
     * @param response The successful API response containing data and metadata
     */
    void onSuccess(ApiResponse<T> response);
    
    /**
     * Called when the API call returns an HTTP error status (4xx or 5xx).
     * 
     * <p>This method handles HTTP-level errors where the server responded but
     * indicated an error condition. The response object contains error details,
     * status codes, and any error response body returned by the server.</p>
     * 
     * <p><strong>Common HTTP Error Scenarios:</strong></p>
     * <ul>
     *   <li><strong>4xx Client Errors:</strong> Bad request, unauthorized, not found, etc.</li>
     *   <li><strong>5xx Server Errors:</strong> Internal server error, service unavailable, etc.</li>
     * </ul>
     * 
     * <p><strong>Error Handling Strategies:</strong></p>
     * <ul>
     *   <li>Check {@code response.getStatusCode()} for specific error types</li>
     *   <li>Use {@code response.getErrorCode()} for application-specific error handling</li>
     *   <li>Parse error response body for detailed error information</li>
     *   <li>Implement retry logic for transient server errors (5xx)</li>
     * </ul>
     * 
     * <p><strong>Default Implementation:</strong> Logs the error at WARN level.
     * Override to provide custom error handling logic.</p>
     * 
     * @param response The error response containing status code and error details
     */
    default void onError(ApiResponse<T> response) {
        // Default implementation - can be overridden
        System.err.println("API call failed with status: " + response.getStatusCode() + 
                          " - " + response.getErrorMessage());
    }
    
    /**
     * Called when an exception occurs during the API call execution.
     * 
     * <p>This method handles system-level failures that prevent the API call
     * from completing normally. These are typically network-related issues,
     * configuration problems, or parsing failures.</p>
     * 
     * <p><strong>Common Exception Scenarios:</strong></p>
     * <ul>
     *   <li><strong>Network Issues:</strong> Connection timeouts, DNS failures, network unreachable</li>
     *   <li><strong>Configuration Errors:</strong> Invalid URLs, SSL certificate issues</li>
     *   <li><strong>Parsing Failures:</strong> JSON/XML deserialization errors</li>
     *   <li><strong>Security Issues:</strong> Authentication failures, SSL handshake errors</li>
     * </ul>
     * 
     * <p><strong>Exception Handling Strategies:</strong></p>
     * <ul>
     *   <li>Check exception type for specific handling (SocketTimeoutException, etc.)</li>
     *   <li>Implement exponential backoff for transient network issues</li>
     *   <li>Log detailed error information for debugging</li>
     *   <li>Provide fallback mechanisms or cached responses</li>
     * </ul>
     * 
     * <p><strong>Default Implementation:</strong> Logs the exception at ERROR level.
     * Override to provide custom exception handling logic.</p>
     * 
     * @param exception The exception that occurred during API call execution
     */
    default void onException(Exception exception) {
        // Default implementation - can be overridden
        System.err.println("API call failed with exception: " + exception.getMessage());
        exception.printStackTrace();
    }
} 