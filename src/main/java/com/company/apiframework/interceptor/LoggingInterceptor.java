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
 * Interceptor for logging HTTP requests and responses
 */
public class LoggingInterceptor implements ClientHttpRequestInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    
    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, 
            byte[] body, 
            ClientHttpRequestExecution execution) throws IOException {
        
        // Log request
        logRequest(request, body);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Execute request
            ClientHttpResponse response = execution.execute(request, body);
            
            // Log response
            logResponse(response, System.currentTimeMillis() - startTime);
            
            return response;
        } catch (Exception e) {
            logger.error("Request failed: {} {}, Error: {}", 
                    request.getMethod(), request.getURI(), e.getMessage());
            throw e;
        }
    }
    
    private void logRequest(HttpRequest request, byte[] body) {
        if (logger.isDebugEnabled()) {
            logger.debug("=== HTTP REQUEST ===");
            logger.debug("Method: {}", request.getMethod());
            logger.debug("URI: {}", request.getURI());
            logger.debug("Headers: {}", request.getHeaders());
            
            if (body != null && body.length > 0) {
                String bodyStr = new String(body, StandardCharsets.UTF_8);
                logger.debug("Body: {}", bodyStr);
            }
            logger.debug("==================");
        } else if (logger.isInfoEnabled()) {
            logger.info("HTTP {} {}", request.getMethod(), request.getURI());
        }
    }
    
    private void logResponse(ClientHttpResponse response, long duration) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("=== HTTP RESPONSE ===");
            logger.debug("Status: {} {}", response.getStatusCode(), response.getStatusText());
            logger.debug("Headers: {}", response.getHeaders());
            logger.debug("Duration: {}ms", duration);
            
            // Read response body for logging (this might affect performance)
            byte[] bodyBytes = StreamUtils.copyToByteArray(response.getBody());
            if (bodyBytes.length > 0) {
                String bodyStr = new String(bodyBytes, determineCharset(response));
                logger.debug("Body: {}", bodyStr);
            }
            logger.debug("===================");
        } else if (logger.isInfoEnabled()) {
            logger.info("HTTP Response: {} in {}ms", response.getStatusCode(), duration);
        }
    }
    
    private Charset determineCharset(ClientHttpResponse response) {
        String contentType = response.getHeaders().getFirst("Content-Type");
        if (contentType != null && contentType.contains("charset=")) {
            try {
                String charset = contentType.substring(contentType.indexOf("charset=") + 8);
                if (charset.contains(";")) {
                    charset = charset.substring(0, charset.indexOf(";"));
                }
                return Charset.forName(charset.trim());
            } catch (Exception e) {
                logger.debug("Failed to parse charset from Content-Type: {}", contentType);
            }
        }
        return StandardCharsets.UTF_8;
    }
} 