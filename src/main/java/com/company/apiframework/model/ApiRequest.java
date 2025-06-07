package com.company.apiframework.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Standardized API request model for both REST and SOAP calls.
 * 
 * <p>This class represents a unified request structure that can be used for both
 * REST and SOAP API calls. It provides a consistent interface regardless of the
 * underlying protocol, making it easier to switch between different API types
 * or support multiple protocols in the same application.</p>
 * 
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li>Protocol-agnostic design (works with REST and SOAP)</li>
 *   <li>Fluent builder pattern for easy request construction</li>
 *   <li>Support for headers, parameters, and request body</li>
 *   <li>SOAP-specific fields (soapAction) when needed</li>
 * </ul>
 * 
 * <p><strong>Usage Examples:</strong></p>
 * <pre>
 * // REST API request
 * ApiRequest restRequest = ApiRequest.builder()
 *     .url("https://api.example.com/users")
 *     .method("GET")
 *     .header("Authorization", "Bearer token")
 *     .parameter("page", 1)
 *     .build();
 * 
 * // SOAP API request
 * ApiRequest soapRequest = ApiRequest.builder()
 *     .url("https://soap.example.com/service")
 *     .method("POST")
 *     .soapAction("getUserInfo")
 *     .body(soapEnvelope)
 *     .build();
 * </pre>
 * 
 * @author API Framework Team
 * @version 1.0
 * @since 1.0
 */
public class ApiRequest {
    
    /**
     * The target URL for the API call.
     * This should be a complete URL including protocol, host, and path.
     */
    private String url;
    
    /**
     * HTTP method for the request (GET, POST, PUT, DELETE, etc.).
     * For SOAP calls, this is typically POST.
     */
    private String method;
    
    /**
     * HTTP headers to be sent with the request.
     * Common headers include Authorization, Content-Type, Accept, etc.
     */
    private Map<String, String> headers;
    
    /**
     * Request body content.
     * Can be a String, JSON object, XML string, or any serializable object.
     * For REST APIs, this is typically JSON. For SOAP, this is the SOAP envelope.
     */
    private Object body;
    
    /**
     * Query parameters or form parameters for the request.
     * For GET requests, these become query string parameters.
     * For POST requests, these can become form data or query parameters.
     */
    private Map<String, Object> parameters;
    
    /**
     * SOAP Action header value for SOAP requests.
     * This is used to identify the specific SOAP operation being called.
     * Only relevant for SOAP API calls, ignored for REST calls.
     */
    private String soapAction;
    
    /**
     * Default constructor that initializes empty collections.
     */
    public ApiRequest() {
        this.headers = new HashMap<>();
        this.parameters = new HashMap<>();
    }
    
    /**
     * Constructor with URL and method.
     * 
     * @param url The target URL for the API call
     * @param method The HTTP method (GET, POST, etc.)
     */
    public ApiRequest(String url, String method) {
        this();
        this.url = url;
        this.method = method;
    }
    
    // Getters and Setters with documentation
    
    /**
     * Gets the target URL for the API call.
     * @return The complete URL including protocol, host, and path
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * Sets the target URL for the API call.
     * @param url The complete URL including protocol, host, and path
     */
    public void setUrl(String url) {
        this.url = url;
    }
    
    /**
     * Gets the HTTP method for the request.
     * @return HTTP method (GET, POST, PUT, DELETE, etc.)
     */
    public String getMethod() {
        return method;
    }
    
    /**
     * Sets the HTTP method for the request.
     * @param method HTTP method (GET, POST, PUT, DELETE, etc.)
     */
    public void setMethod(String method) {
        this.method = method;
    }
    
    /**
     * Gets the HTTP headers map.
     * @return Mutable map of header name-value pairs
     */
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    /**
     * Sets the HTTP headers map.
     * @param headers Map of header name-value pairs
     */
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    
    /**
     * Adds a single HTTP header to the request.
     * 
     * @param name Header name (e.g., "Authorization", "Content-Type")
     * @param value Header value
     * @return This ApiRequest instance for method chaining
     */
    public ApiRequest addHeader(String name, String value) {
        this.headers.put(name, value);
        return this;
    }
    
    /**
     * Gets the request body content.
     * @return Request body (can be String, JSON object, XML, etc.)
     */
    public Object getBody() {
        return body;
    }
    
    /**
     * Sets the request body content.
     * @param body Request body (can be String, JSON object, XML, etc.)
     */
    public void setBody(Object body) {
        this.body = body;
    }
    
    /**
     * Gets the parameters map.
     * @return Mutable map of parameter name-value pairs
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    /**
     * Sets the parameters map.
     * @param parameters Map of parameter name-value pairs
     */
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    /**
     * Adds a single parameter to the request.
     * 
     * @param name Parameter name
     * @param value Parameter value
     * @return This ApiRequest instance for method chaining
     */
    public ApiRequest addParameter(String name, Object value) {
        this.parameters.put(name, value);
        return this;
    }
    
    /**
     * Gets the SOAP Action header value.
     * @return SOAP Action string, or null if not a SOAP request
     */
    public String getSoapAction() {
        return soapAction;
    }
    
    /**
     * Sets the SOAP Action header value.
     * @param soapAction SOAP Action string (only used for SOAP requests)
     */
    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }

    /**
     * Creates a new builder instance for fluent API request construction.
     * 
     * <p>The builder pattern provides a clean and readable way to construct
     * API requests with method chaining.</p>
     * 
     * @return New Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder class for fluent API request construction.
     * 
     * <p>This builder provides a clean, readable way to construct ApiRequest objects
     * using method chaining. It follows the builder pattern to make request creation
     * more intuitive and less error-prone.</p>
     * 
     * <p><strong>Example Usage:</strong></p>
     * <pre>
     * ApiRequest request = ApiRequest.builder()
     *     .url("https://api.example.com/users")
     *     .method("POST")
     *     .header("Content-Type", "application/json")
     *     .header("Authorization", "Bearer " + token)
     *     .body(userObject)
     *     .parameter("validate", true)
     *     .build();
     * </pre>
     */
    public static class Builder {
        /**
         * The ApiRequest instance being built.
         */
        private ApiRequest request;
        
        /**
         * Creates a new builder with a fresh ApiRequest instance.
         */
        public Builder() {
            this.request = new ApiRequest();
        }
        
        /**
         * Sets the target URL for the API call.
         * 
         * @param url Complete URL including protocol, host, and path
         * @return This builder instance for method chaining
         */
        public Builder url(String url) {
            request.setUrl(url);
            return this;
        }
        
        /**
         * Sets the HTTP method for the request.
         * 
         * @param method HTTP method (GET, POST, PUT, DELETE, etc.)
         * @return This builder instance for method chaining
         */
        public Builder method(String method) {
            request.setMethod(method);
            return this;
        }
        
        /**
         * Adds an HTTP header to the request.
         * 
         * @param name Header name (e.g., "Authorization", "Content-Type")
         * @param value Header value
         * @return This builder instance for method chaining
         */
        public Builder header(String name, String value) {
            request.addHeader(name, value);
            return this;
        }
        
        /**
         * Sets the request body content.
         * 
         * @param body Request body (String, JSON object, XML, etc.)
         * @return This builder instance for method chaining
         */
        public Builder body(Object body) {
            request.setBody(body);
            return this;
        }
        
        /**
         * Adds a parameter to the request.
         * 
         * @param name Parameter name
         * @param value Parameter value
         * @return This builder instance for method chaining
         */
        public Builder parameter(String name, Object value) {
            request.addParameter(name, value);
            return this;
        }
        
        /**
         * Sets the SOAP Action header for SOAP requests.
         * 
         * @param soapAction SOAP Action string
         * @return This builder instance for method chaining
         */
        public Builder soapAction(String soapAction) {
            request.setSoapAction(soapAction);
            return this;
        }
        
        /**
         * Builds and returns the configured ApiRequest instance.
         * 
         * @return The fully configured ApiRequest
         */
        public ApiRequest build() {
            return request;
        }
    }
} 