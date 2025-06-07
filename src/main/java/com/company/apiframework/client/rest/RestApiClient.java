package com.company.apiframework.client.rest;

import com.company.apiframework.client.ApiCallback;
import com.company.apiframework.client.ApiClient;
import com.company.apiframework.exception.ApiException;
import com.company.apiframework.model.ApiRequest;
import com.company.apiframework.model.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * REST API client implementation
 */
public class RestApiClient implements ApiClient {
    
    private static final Logger logger = LoggerFactory.getLogger(RestApiClient.class);
    private static final String PROTOCOL_TYPE = "REST";
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Executor asyncExecutor;
    
    public RestApiClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.asyncExecutor = Executors.newCachedThreadPool();
    }
    
    @Override
    public <T> ApiResponse<T> execute(ApiRequest request, Class<T> responseType) {
        long startTime = System.currentTimeMillis();
        ApiResponse<T> apiResponse = new ApiResponse<>();
        
        try {
            // Build HTTP headers
            HttpHeaders headers = new HttpHeaders();
            request.getHeaders().forEach(headers::add);
            
            // Set content type if not specified
            if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
                headers.setContentType(MediaType.APPLICATION_JSON);
            }
            
            // Create HTTP entity
            Object requestBody = request.getBody();
            HttpEntity<?> entity = new HttpEntity<>(requestBody, headers);
            
            // Execute request
            ResponseEntity<T> response = restTemplate.exchange(
                    request.getUrl(),
                    HttpMethod.valueOf(request.getMethod().toUpperCase()),
                    entity,
                    responseType
            );
            
            // Map response
            apiResponse.setStatusCode(response.getStatusCodeValue());
            apiResponse.setStatusMessage(response.getStatusCode().getReasonPhrase());
            apiResponse.setBody(response.getBody());
            apiResponse.setSuccess(true);
            
            // Map response headers
            response.getHeaders().forEach((key, values) -> {
                if (!values.isEmpty()) {
                    apiResponse.getHeaders().put(key, values.get(0));
                }
            });
            
        } catch (RestClientException e) {
            logger.error("REST API call failed: {}", e.getMessage(), e);
            apiResponse.markAsError("REST_ERROR", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during REST API call: {}", e.getMessage(), e);
            apiResponse.markAsError("UNEXPECTED_ERROR", e.getMessage());
        } finally {
            apiResponse.setResponseTimeMs(System.currentTimeMillis() - startTime);
        }
        
        return apiResponse;
    }
    
    @Override
    public ApiResponse<String> execute(ApiRequest request) {
        return execute(request, String.class);
    }
    
    @Override
    public <T> void executeAsync(ApiRequest request, Class<T> responseType, ApiCallback<T> callback) {
        CompletableFuture.supplyAsync(() -> execute(request, responseType), asyncExecutor)
                .whenComplete((response, throwable) -> {
                    if (throwable != null) {
                        callback.onException(new ApiException("Async execution failed", throwable));
                    } else if (response.hasError()) {
                        callback.onError(response);
                    } else {
                        callback.onSuccess(response);
                    }
                });
    }
    
    @Override
    public boolean supportsProtocol(String protocol) {
        return PROTOCOL_TYPE.equalsIgnoreCase(protocol);
    }
    
    @Override
    public String getProtocolType() {
        return PROTOCOL_TYPE;
    }
} 