package com.company.apiframework.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import com.company.apiframework.config.ApiProperties;

/**
 * Aspect-based retry interceptor for API calls with configurable retry strategies.
 * 
 * <p>This component provides automatic retry functionality for failed API calls using
 * Spring Retry framework. It implements exponential backoff strategy to avoid
 * overwhelming target services while providing resilience against transient failures.</p>
 * 
 * <p><strong>Retry Triggers:</strong></p>
 * <ul>
 *   <li>Network connectivity issues (RestClientException)</li>
 *   <li>Timeout exceptions</li>
 *   <li>Server errors (5xx HTTP status codes)</li>
 *   <li>Other runtime exceptions during API calls</li>
 * </ul>
 * 
 * <p><strong>Retry Strategies:</strong></p>
 * <ul>
 *   <li><strong>Fixed Configuration:</strong> Uses hardcoded retry settings (3 attempts, 1s base delay)</li>
 *   <li><strong>Dynamic Configuration:</strong> Uses settings from ApiProperties for flexibility</li>
 * </ul>
 * 
 * <p><strong>Exponential Backoff:</strong></p>
 * <p>The retry delay increases exponentially with each attempt:</p>
 * <ul>
 *   <li>Attempt 1: Base delay (e.g., 1000ms)</li>
 *   <li>Attempt 2: Base delay × 2 (e.g., 2000ms)</li>
 *   <li>Attempt 3: Base delay × 4 (e.g., 4000ms)</li>
 * </ul>
 * 
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * // Using the retry mechanism
 * ApiResponse&lt;String&gt; response = retryInterceptor.retryApiCall(() -&gt; {
 *     return apiService.executeRest(request, String.class);
 * });
 * 
 * // With custom configuration
 * ApiResponse&lt;User&gt; response = retryInterceptor.retryApiCallWithConfig(() -&gt; {
 *     return apiService.executeRest(userRequest, User.class);
 * });
 * </pre>
 * 
 * <p><strong>Configuration:</strong></p>
 * <p>Retry behavior can be configured via ApiProperties:</p>
 * <pre>
 * api.framework.max-retry-attempts=5
 * api.framework.retry-delay-ms=2000
 * </pre>
 * 
 * @author API Framework Team
 * @version 1.0
 * @since 1.0
 * @see ApiProperties
 * @see org.springframework.retry.annotation.Retryable
 */
@Component
public class RetryInterceptor {
    
    /**
     * Logger for tracking retry attempts and failures.
     */
    private static final Logger logger = LoggerFactory.getLogger(RetryInterceptor.class);
    
    /**
     * Configuration properties for retry behavior.
     */
    @Autowired
    private ApiProperties apiProperties;
    
    /**
     * Executes API calls with fixed retry configuration and exponential backoff.
     * 
     * <p>This method uses hardcoded retry settings:</p>
     * <ul>
     *   <li>Maximum attempts: 3</li>
     *   <li>Base delay: 1000ms</li>
     *   <li>Backoff multiplier: 2</li>
     * </ul>
     * 
     * <p>The method will retry on RestClientException and RuntimeException,
     * which covers most transient API failures including network issues,
     * timeouts, and server errors.</p>
     * 
     * @param <T> The return type of the API call
     * @param apiCall The API call to execute with retry logic
     * @return The result of the successful API call
     * @throws Exception If all retry attempts fail
     */
    @Retryable(
            value = {RestClientException.class, RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public <T> T retryApiCall(RetryableApiCall<T> apiCall) throws Exception {
        logger.debug("Executing API call with retry mechanism");
        return apiCall.execute();
    }
    
    /**
     * Executes API calls with configurable retry settings from ApiProperties.
     * 
     * <p>This method uses dynamic configuration from ApiProperties:</p>
     * <ul>
     *   <li>Maximum attempts: {@code apiProperties.maxRetryAttempts}</li>
     *   <li>Base delay: {@code apiProperties.retryDelayMs}</li>
     *   <li>Backoff multiplier: 2 (fixed)</li>
     * </ul>
     * 
     * <p>This approach allows runtime configuration changes without code
     * modifications, making it suitable for different environments with
     * varying retry requirements.</p>
     * 
     * @param <T> The return type of the API call
     * @param apiCall The API call to execute with retry logic
     * @return The result of the successful API call
     * @throws Exception If all retry attempts fail
     */
    @Retryable(
            value = {RestClientException.class, RuntimeException.class},
            maxAttemptsExpression = "#{@apiProperties.maxRetryAttempts}",
            backoff = @Backoff(delayExpression = "#{@apiProperties.retryDelayMs}", multiplier = 2)
    )
    public <T> T retryApiCallWithConfig(RetryableApiCall<T> apiCall) throws Exception {
        logger.debug("Executing API call with configured retry mechanism");
        return apiCall.execute();
    }
    
    /**
     * Logs retry attempt information for monitoring and debugging.
     * 
     * <p>This method is typically called by the retry framework or can be
     * used manually to track retry attempts. It provides visibility into
     * retry behavior and helps with troubleshooting persistent failures.</p>
     * 
     * <p>Log levels used:</p>
     * <ul>
     *   <li><strong>WARN:</strong> For individual retry attempts</li>
     *   <li><strong>ERROR:</strong> When all retry attempts are exhausted</li>
     * </ul>
     * 
     * @param ex The exception that caused the retry
     * @param attempt The current attempt number (1-based)
     */
    public void logRetryAttempt(Exception ex, int attempt) {
        logger.warn("API call failed on attempt {}: {}", attempt, ex.getMessage());
        if (attempt >= apiProperties.getMaxRetryAttempts()) {
            logger.error("API call failed after {} attempts, giving up", attempt);
        }
    }
    
    /**
     * Functional interface for retryable API calls.
     * 
     * <p>This interface allows any API call to be wrapped with retry logic
     * using lambda expressions or method references. It provides a clean
     * way to apply retry behavior to different types of API operations.</p>
     * 
     * <p><strong>Usage Examples:</strong></p>
     * <pre>
     * // Lambda expression
     * RetryableApiCall&lt;String&gt; call = () -&gt; apiService.executeRest(request, String.class);
     * 
     * // Method reference
     * RetryableApiCall&lt;User&gt; call = () -&gt; userService.getUser(userId);
     * 
     * // Complex operation
     * RetryableApiCall&lt;List&lt;Order&gt;&gt; call = () -&gt; {
     *     ApiRequest request = buildOrderRequest();
     *     return apiService.executeRest(request, new TypeReference&lt;List&lt;Order&gt;&gt;() {});
     * };
     * </pre>
     * 
     * @param <T> The return type of the API call
     */
    @FunctionalInterface
    public interface RetryableApiCall<T> {
        /**
         * Executes the API call that may need to be retried.
         * 
         * @return The result of the API call
         * @throws Exception If the API call fails
         */
        T execute() throws Exception;
    }
} 