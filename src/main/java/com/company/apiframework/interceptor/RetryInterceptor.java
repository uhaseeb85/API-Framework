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
 * Aspect-based retry interceptor for API calls
 */
@Component
public class RetryInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(RetryInterceptor.class);
    
    @Autowired
    private ApiProperties apiProperties;
    
    /**
     * Retry mechanism for API calls with exponential backoff
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
     * Retry mechanism for API calls with custom configuration
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
     * Log retry attempts
     */
    public void logRetryAttempt(Exception ex, int attempt) {
        logger.warn("API call failed on attempt {}: {}", attempt, ex.getMessage());
        if (attempt >= apiProperties.getMaxRetryAttempts()) {
            logger.error("API call failed after {} attempts, giving up", attempt);
        }
    }
    
    /**
     * Functional interface for retryable API calls
     */
    @FunctionalInterface
    public interface RetryableApiCall<T> {
        T execute() throws Exception;
    }
} 