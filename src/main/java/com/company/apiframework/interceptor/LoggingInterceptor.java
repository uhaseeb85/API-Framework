package com.company.apiframework.interceptor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

/**
 * HTTP request/response logging interceptor for the API Integration Framework.
 * 
 * <p>This interceptor automatically logs all HTTP requests and responses made through
 * the framework's RestTemplate instances. It provides different levels of detail
 * based on the configured logging level:</p>
 * 
 * <p><strong>Logging Levels:</strong></p>
 * <ul>
 *   <li><strong>DEBUG:</strong> Full request/response details including:
 *       <ul>
 *         <li>HTTP method and URL</li>
 *         <li>Request/response headers</li>
 *         <li>Request/response body content</li>
 *         <li>Response status and timing</li>
 *       </ul>
 *   </li>
 *   <li><strong>INFO:</strong> Summary information including:
 *       <ul>
 *         <li>HTTP method and URL</li>
 *         <li>Response status code</li>
 *         <li>Request duration</li>
 *       </ul>
 *   </li>
 * </ul>
 * 
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Automatic charset detection for response body logging</li>
 *   <li>Performance timing measurement</li>
 *   <li>Error logging for failed requests</li>
 *   <li>Configurable through standard SLF4J logging configuration</li>
 * </ul>
 * 
 * <p><strong>Configuration Example (logback.xml):</strong></p>
 * <pre>
 * &lt;logger name="com.company.apiframework.interceptor.LoggingInterceptor" level="DEBUG"/&gt;
 * </pre>
 * 
 * <p><strong>Security Note:</strong> When DEBUG logging is enabled, request and response
 * bodies are logged in full. Ensure sensitive data (passwords, tokens, PII) is not
 * logged in production environments.</p>
 * 
 * @author API Framework Team
 * @version 1.0
 * @since 1.0
 */
public class LoggingInterceptor implements ClientHttpRequestInterceptor {
    
    /**
     * Logger instance for this interceptor.
     * Logging level controls the amount of detail logged.
     */
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    
    /**
     * Intercepts HTTP requests and responses to provide logging.
     * 
     * <p>This method is called for every HTTP request made through RestTemplate
     * instances that have this interceptor configured. It logs the request,
     * executes the actual HTTP call, measures timing, and logs the response.</p>
     * 
     * @param request The HTTP request being made
     * @param body The request body content
     * @param execution The execution chain to continue the request
     * @return The HTTP response from the target server
     * @throws IOException If an I/O error occurs during request execution
     */
    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, 
            byte[] body, 
            ClientHttpRequestExecution execution) throws IOException {
        
        // Log the outgoing request
        logRequest(request, body);
        
        // Record start time for performance measurement
        long startTime = System.currentTimeMillis();
        
        try {
            // Execute the actual HTTP request
            ClientHttpResponse response = execution.execute(request, body);
            
            // Log the received response with timing information
            logResponse(response, System.currentTimeMillis() - startTime);
            
            return response;
        } catch (Exception e) {
            // Log any exceptions that occur during request execution
            logger.error("Request failed: {} {}, Error: {}", 
                    request.getMethod(), request.getURI(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Logs the outgoing HTTP request details.
     * 
     * <p>The amount of detail logged depends on the configured logging level:</p>
     * <ul>
     *   <li>DEBUG: Full request details including headers and body</li>
     *   <li>INFO: Summary with method and URL only</li>
     * </ul>
     * 
     * @param request The HTTP request to log
     * @param body The request body content as byte array
     */
    private void logRequest(HttpRequest request, byte[] body) {
        if (logger.isDebugEnabled()) {
            logger.debug("=== HTTP REQUEST ===");
            logger.debug("Method: {}", request.getMethod());
            logger.debug("URI: {}", request.getURI());
            logger.debug("Headers: {}", request.getHeaders());
            
            // Log request body if present
            if (body != null && body.length > 0) {
                String bodyStr = new String(body, StandardCharsets.UTF_8);
                logger.debug("Body: {}", bodyStr);
            }
            logger.debug("==================");
        } else if (logger.isInfoEnabled()) {
            // Provide summary logging at INFO level
            logger.info("HTTP {} {}", request.getMethod(), request.getURI());
        }
    }
    
    /**
     * Logs the received HTTP response details.
     * 
     * <p>The amount of detail logged depends on the configured logging level:</p>
     * <ul>
     *   <li>DEBUG: Full response details including headers and body</li>
     *   <li>INFO: Summary with status code and duration</li>
     * </ul>
     * 
     * <p><strong>Note:</strong> Reading the response body for logging purposes
     * may impact performance, especially for large responses. This only occurs
     * when DEBUG logging is enabled.</p>
     * 
     * @param response The HTTP response to log
     * @param duration The request duration in milliseconds
     * @throws IOException If an error occurs reading the response body
     */
    private void logResponse(ClientHttpResponse response, long duration) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("=== HTTP RESPONSE ===");
            logger.debug("Status: {} {}", response.getStatusCode(), response.getStatusText());
            logger.debug("Headers: {}", response.getHeaders());
            logger.debug("Duration: {}ms", duration);
            
            // Read and log response body (may impact performance)
            byte[] bodyBytes = StreamUtils.copyToByteArray(response.getBody());
            if (bodyBytes.length > 0) {
                String bodyStr = new String(bodyBytes, determineCharset(response));
                logger.debug("Body: {}", bodyStr);
            }
            logger.debug("===================");
        } else if (logger.isInfoEnabled()) {
            // Provide summary logging at INFO level
            logger.info("HTTP Response: {} in {}ms", response.getStatusCode(), duration);
        }
    }
    
    /**
     * Determines the character encoding for the response body.
     * 
     * <p>This method attempts to extract the charset from the Content-Type header.
     * If no charset is specified or if parsing fails, it defaults to UTF-8.</p>
     * 
     * <p><strong>Supported Content-Type examples:</strong></p>
     * <ul>
     *   <li>application/json; charset=UTF-8</li>
     *   <li>text/xml; charset=ISO-8859-1</li>
     *   <li>application/xml; charset=UTF-16</li>
     * </ul>
     * 
     * @param response The HTTP response containing Content-Type header
     * @return The determined Charset, defaulting to UTF-8 if not determinable
     */
    private Charset determineCharset(ClientHttpResponse response) {
        String contentType = response.getHeaders().getFirst("Content-Type");
        if (contentType != null && contentType.contains("charset=")) {
            try {
                // Extract charset value from Content-Type header
                String charset = contentType.substring(contentType.indexOf("charset=") + 8);
                
                // Remove any trailing parameters (e.g., "; boundary=...")
                if (charset.contains(";")) {
                    charset = charset.substring(0, charset.indexOf(";"));
                }
                
                return Charset.forName(charset.trim());
            } catch (Exception e) {
                // Log charset parsing failure but continue with default
                logger.debug("Failed to parse charset from Content-Type: {}", contentType);
            }
        }
        
        // Default to UTF-8 if charset cannot be determined
        return StandardCharsets.UTF_8;
    }
} 