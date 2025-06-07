package com.company.apiframework.mock.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.company.apiframework.mock.ApiMockRegistry.CustomApiMock;
import com.company.apiframework.model.ApiRequest;
import com.company.apiframework.model.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Custom mock implementation for Payment API
 * Simulates various payment scenarios including success, failures, and edge cases
 */
public class PaymentApiMock implements CustomApiMock {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentApiMock.class);
    private static final String API_IDENTIFIER = "payment-api";
    private static final String BASE_URL_PATTERN = "https://payment.gateway.com";
    
    private final ObjectMapper objectMapper;
    private final AtomicInteger transactionCounter = new AtomicInteger(1000);
    private final Map<String, PaymentScenario> scenarios = new HashMap<>();
    
    public PaymentApiMock(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        setupScenarios();
    }
    
    @Override
    public boolean matchesUrl(String url) {
        return url.contains(BASE_URL_PATTERN);
    }
    
    @Override
    public <T> ApiResponse<T> executeMock(ApiRequest request, Class<T> responseType) {
        String endpoint = extractEndpoint(request.getUrl());
        PaymentScenario scenario = determineScenario(request, endpoint);
        
        logger.debug("Payment API mock executing scenario: {} for endpoint: {}", scenario.name, endpoint);
        
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatusCode(scenario.statusCode);
        response.setSuccess(scenario.statusCode >= 200 && scenario.statusCode < 300);
        response.setResponseTimeMs(scenario.delay);
        
        // Simulate delay
        if (scenario.delay > 0) {
            try {
                Thread.sleep(scenario.delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Create response body based on scenario
        Object responseBody = createResponseBody(scenario, request, endpoint);
        
        try {
            if (responseType == String.class) {
                response.setBody(responseType.cast(responseBody.toString()));
            } else {
                String json = objectMapper.writeValueAsString(responseBody);
                T convertedBody = objectMapper.readValue(json, responseType);
                response.setBody(convertedBody);
            }
        } catch (Exception e) {
            response.markAsError("PAYMENT_MOCK_ERROR", "Failed to create mock response: " + e.getMessage());
        }
        
        // Add payment-specific headers
        response.getHeaders().put("X-Payment-Gateway", "Mock-Gateway-v2.0");
        response.getHeaders().put("X-Transaction-Id", String.valueOf(transactionCounter.incrementAndGet()));
        
        if (!response.isSuccess()) {
            response.markAsError(scenario.errorCode, scenario.errorMessage);
        }
        
        return response;
    }
    
    @Override
    public String getApiIdentifier() {
        return API_IDENTIFIER;
    }
    
    @Override
    public void setupScenarios() {
        // Success scenarios
        scenarios.put("process_success", new PaymentScenario(
            "process_success", 200, 0, null, null
        ));
        
        scenarios.put("process_slow", new PaymentScenario(
            "process_slow", 200, 3000, null, null
        ));
        
        // Error scenarios
        scenarios.put("insufficient_funds", new PaymentScenario(
            "insufficient_funds", 400, 100, "INSUFFICIENT_FUNDS", "Insufficient funds in account"
        ));
        
        scenarios.put("invalid_card", new PaymentScenario(
            "invalid_card", 400, 50, "INVALID_CARD", "Invalid card number"
        ));
        
        scenarios.put("expired_card", new PaymentScenario(
            "expired_card", 400, 50, "EXPIRED_CARD", "Card has expired"
        ));
        
        scenarios.put("network_timeout", new PaymentScenario(
            "network_timeout", 504, 5000, "GATEWAY_TIMEOUT", "Payment gateway timeout"
        ));
        
        scenarios.put("service_unavailable", new PaymentScenario(
            "service_unavailable", 503, 100, "SERVICE_UNAVAILABLE", "Payment service temporarily unavailable"
        ));
        
        scenarios.put("rate_limit", new PaymentScenario(
            "rate_limit", 429, 100, "RATE_LIMIT_EXCEEDED", "Too many payment requests"
        ));
        
        logger.info("Payment API mock scenarios setup complete. Available scenarios: {}", scenarios.keySet());
    }
    
    @Override
    public void reset() {
        transactionCounter.set(1000);
        logger.info("Payment API mock state reset");
    }
    
    private PaymentScenario determineScenario(ApiRequest request, String endpoint) {
        // Check for scenario hint in headers
        String scenarioHint = request.getHeaders().get("X-Mock-Scenario");
        if (scenarioHint != null && scenarios.containsKey(scenarioHint)) {
            return scenarios.get(scenarioHint);
        }
        
        // Determine scenario based on request content
        if (request.getBody() != null) {
            String body = request.getBody().toString();
            
            // Simulate different scenarios based on amount
            if (body.contains("\"amount\":1")) {
                return scenarios.get("insufficient_funds");
            } else if (body.contains("\"cardNumber\":\"4000000000000002\"")) {
                return scenarios.get("invalid_card");
            } else if (body.contains("\"cardNumber\":\"4000000000000069\"")) {
                return scenarios.get("expired_card");
            } else if (body.contains("\"amount\":999999")) {
                return scenarios.get("network_timeout");
            } else if (body.contains("\"test\":\"slow\"")) {
                return scenarios.get("process_slow");
            }
        }
        
        // Default to success
        return scenarios.get("process_success");
    }
    
    private Object createResponseBody(PaymentScenario scenario, ApiRequest request, String endpoint) {
        Map<String, Object> responseBody = new HashMap<>();
        
        if (scenario.statusCode >= 200 && scenario.statusCode < 300) {
            // Success response
            responseBody.put("transactionId", "txn_" + transactionCounter.get());
            responseBody.put("status", "completed");
            responseBody.put("amount", extractAmountFromRequest(request));
            responseBody.put("currency", "USD");
            responseBody.put("timestamp", System.currentTimeMillis());
            responseBody.put("message", "Payment processed successfully");
        } else {
            // Error response
            responseBody.put("error", scenario.errorCode);
            responseBody.put("message", scenario.errorMessage);
            responseBody.put("timestamp", System.currentTimeMillis());
            if (scenario.statusCode == 429) {
                responseBody.put("retryAfter", 60);
            }
        }
        
        return responseBody;
    }
    
    private String extractEndpoint(String url) {
        return url.replace(BASE_URL_PATTERN, "");
    }
    
    private double extractAmountFromRequest(ApiRequest request) {
        try {
            if (request.getBody() != null) {
                String body = request.getBody().toString();
                // Simple extraction - in real implementation, parse JSON properly
                if (body.contains("\"amount\":")) {
                    String amountStr = body.substring(body.indexOf("\"amount\":") + 9);
                    amountStr = amountStr.substring(0, amountStr.indexOf(",") > 0 ? amountStr.indexOf(",") : amountStr.indexOf("}"));
                    return Double.parseDouble(amountStr.trim());
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to extract amount from request: {}", e.getMessage());
        }
        return 100.0; // Default amount
    }
    
    /**
     * Payment scenario configuration
     */
    private static class PaymentScenario {
        final String name;
        final int statusCode;
        final long delay;
        final String errorCode;
        final String errorMessage;
        
        PaymentScenario(String name, int statusCode, long delay, String errorCode, String errorMessage) {
            this.name = name;
            this.statusCode = statusCode;
            this.delay = delay;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }
    }
} 