package com.company.apiframework.client;

import com.company.apiframework.model.ApiRequest;
import com.company.apiframework.model.ApiResponse;

/**
 * Main interface for API clients supporting both REST and SOAP protocols.
 * 
 * <p>This interface defines the contract for all API client implementations in the
 * framework. It provides a unified API for making HTTP requests regardless of the
 * underlying protocol (REST or SOAP), enabling protocol-agnostic client code.</p>
 * 
 * <p><strong>Key Design Principles:</strong></p>
 * <ul>
 *   <li><strong>Protocol Abstraction:</strong> Same interface for REST and SOAP</li>
 *   <li><strong>Type Safety:</strong> Generic return types for strongly-typed responses</li>
 *   <li><strong>Async Support:</strong> Non-blocking operations with callbacks</li>
 *   <li><strong>Flexibility:</strong> Support for both typed and string responses</li>
 * </ul>
 * 
 * <p><strong>Implementation Classes:</strong></p>
 * <ul>
 *   <li>{@code RestApiClient} - For REST/HTTP API calls</li>
 *   <li>{@code SoapApiClient} - For SOAP/XML API calls</li>
 * </ul>
 * 
 * <p><strong>Usage Examples:</strong></p>
 * <pre>
 * // Synchronous typed response
 * ApiClient client = clientFactory.createClient();
 * ApiResponse&lt;User&gt; response = client.execute(request, User.class);
 * 
 * // Synchronous string response
 * ApiResponse&lt;String&gt; response = client.execute(request);
 * 
 * // Asynchronous with callback
 * client.executeAsync(request, User.class, new ApiCallback&lt;User&gt;() {
 *     public void onSuccess(ApiResponse&lt;User&gt; response) {
 *         // Handle success
 *     }
 *     public void onError(ApiResponse&lt;User&gt; response) {
 *         // Handle error
 *     }
 * });
 * </pre>
 * 
 * @author API Framework Team
 * @version 1.0
 * @since 1.0
 * @see ApiRequest
 * @see ApiResponse
 * @see ApiCallback
 */
public interface ApiClient {
    
    /**
     * Executes an API request synchronously and returns a typed response.
     * 
     * <p>This is the primary method for making API calls with strongly-typed
     * response handling. The response body will be automatically deserialized
     * to the specified type using the appropriate mapper (JSON for REST,
     * XML for SOAP).</p>
     * 
     * <p><strong>Error Handling:</strong></p>
     * <ul>
     *   <li>Network errors are wrapped in ApiResponse with error details</li>
     *   <li>HTTP error status codes (4xx, 5xx) are preserved in the response</li>
     *   <li>Deserialization errors are captured and reported</li>
     * </ul>
     * 
     * @param <T> The expected response body type
     * @param request The API request to execute
     * @param responseType The class representing the expected response type
     * @return ApiResponse containing the result, status, and metadata
     * @throws ApiException If a critical error occurs that prevents execution
     */
    <T> ApiResponse<T> execute(ApiRequest request, Class<T> responseType);
    
    /**
     * Executes an API request synchronously and returns a string response.
     * 
     * <p>This is a convenience method for cases where you want the raw response
     * content as a string without any deserialization. Useful for:</p>
     * <ul>
     *   <li>Debugging and logging raw responses</li>
     *   <li>Working with non-standard response formats</li>
     *   <li>Custom parsing logic in the calling code</li>
     *   <li>Proxying responses without modification</li>
     * </ul>
     * 
     * @param request The API request to execute
     * @return ApiResponse containing the raw response as a string
     * @throws ApiException If a critical error occurs that prevents execution
     */
    ApiResponse<String> execute(ApiRequest request);
    
    /**
     * Executes an API request asynchronously with a callback for result handling.
     * 
     * <p>This method enables non-blocking API calls, allowing the calling thread
     * to continue processing while the API request is executed in the background.
     * The callback will be invoked when the request completes (successfully or
     * with an error).</p>
     * 
     * <p><strong>Callback Behavior:</strong></p>
     * <ul>
     *   <li>{@code onSuccess()} - Called for successful responses (2xx status)</li>
     *   <li>{@code onError()} - Called for HTTP errors (4xx, 5xx status)</li>
     *   <li>{@code onException()} - Called for network/system exceptions</li>
     * </ul>
     * 
     * <p><strong>Thread Safety:</strong> Callbacks may be invoked on different
     * threads than the calling thread. Ensure thread-safe handling in callbacks.</p>
     * 
     * @param <T> The expected response body type
     * @param request The API request to execute
     * @param responseType The class representing the expected response type
     * @param callback The callback to handle the response or error
     */
    <T> void executeAsync(ApiRequest request, Class<T> responseType, ApiCallback<T> callback);
    
    /**
     * Checks if this client implementation supports the specified protocol.
     * 
     * <p>This method allows runtime protocol detection and client selection.
     * It's used by the framework to route requests to the appropriate client
     * implementation based on the detected or specified protocol.</p>
     * 
     * <p><strong>Standard Protocol Values:</strong></p>
     * <ul>
     *   <li>"REST" - For REST/HTTP APIs</li>
     *   <li>"SOAP" - For SOAP/XML APIs</li>
     * </ul>
     * 
     * @param protocol The protocol to check (case-insensitive)
     * @return true if this client can handle the specified protocol
     */
    boolean supportsProtocol(String protocol);
    
    /**
     * Returns the primary protocol type that this client handles.
     * 
     * <p>This method provides introspection capability, allowing code to
     * determine what type of client they're working with. Useful for:</p>
     * <ul>
     *   <li>Logging and monitoring</li>
     *   <li>Protocol-specific configuration</li>
     *   <li>Debugging and troubleshooting</li>
     *   <li>Metrics collection by protocol type</li>
     * </ul>
     * 
     * @return The protocol type string ("REST", "SOAP", etc.)
     */
    String getProtocolType();
} 