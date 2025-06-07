package com.company.apiframework.client.soap;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.company.apiframework.client.ApiCallback;
import com.company.apiframework.client.ApiClient;
import com.company.apiframework.exception.ApiException;
import com.company.apiframework.model.ApiRequest;
import com.company.apiframework.model.ApiResponse;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * SOAP API client implementation
 */
public class SoapApiClient implements ApiClient {
    
    private static final Logger logger = LoggerFactory.getLogger(SoapApiClient.class);
    private static final String PROTOCOL_TYPE = "SOAP";
    
    private final RestTemplate restTemplate;
    private final XmlMapper xmlMapper;
    private final Executor asyncExecutor;
    
    public SoapApiClient(RestTemplate restTemplate, XmlMapper xmlMapper) {
        this.restTemplate = restTemplate;
        this.xmlMapper = xmlMapper;
        this.asyncExecutor = Executors.newCachedThreadPool();
    }
    
    @Override
    public <T> ApiResponse<T> execute(ApiRequest request, Class<T> responseType) {
        long startTime = System.currentTimeMillis();
        ApiResponse<T> apiResponse = new ApiResponse<>();
        
        try {
            // Build SOAP headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            headers.add("SOAPAction", request.getSoapAction() != null ? request.getSoapAction() : "");
            
            // Add custom headers
            request.getHeaders().forEach(headers::add);
            
            // Create SOAP envelope
            String soapEnvelope = createSoapEnvelope(request);
            HttpEntity<String> entity = new HttpEntity<>(soapEnvelope, headers);
            
            // Execute SOAP request
            ResponseEntity<String> response = restTemplate.exchange(
                    request.getUrl(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            
            // Parse response
            T parsedResponse = parseSoapResponse(response.getBody(), responseType);
            
            // Map response
            apiResponse.setStatusCode(response.getStatusCodeValue());
            apiResponse.setStatusMessage(response.getStatusCode().getReasonPhrase());
            apiResponse.setBody(parsedResponse);
            apiResponse.setRawResponse(response.getBody());
            apiResponse.setSuccess(true);
            
            // Map response headers
            response.getHeaders().forEach((key, values) -> {
                if (!values.isEmpty()) {
                    apiResponse.getHeaders().put(key, values.get(0));
                }
            });
            
        } catch (RestClientException e) {
            logger.error("SOAP API call failed: {}", e.getMessage(), e);
            apiResponse.markAsError("SOAP_ERROR", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during SOAP API call: {}", e.getMessage(), e);
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
                        callback.onException(new ApiException("Async SOAP execution failed", throwable));
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
    
    /**
     * Create SOAP envelope from request
     */
    private String createSoapEnvelope(ApiRequest request) {
        try {
            StringBuilder envelope = new StringBuilder();
            envelope.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            envelope.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">");
            envelope.append("<soap:Header/>");
            envelope.append("<soap:Body>");
            
            if (request.getBody() != null) {
                if (request.getBody() instanceof String) {
                    envelope.append(request.getBody());
                } else {
                    String xmlBody = xmlMapper.writeValueAsString(request.getBody());
                    envelope.append(xmlBody);
                }
            }
            
            envelope.append("</soap:Body>");
            envelope.append("</soap:Envelope>");
            
            return envelope.toString();
        } catch (Exception e) {
            throw new ApiException("SOAP_ENVELOPE_ERROR", "Failed to create SOAP envelope", e);
        }
    }
    
    /**
     * Parse SOAP response
     */
    private <T> T parseSoapResponse(String soapResponse, Class<T> responseType) {
        try {
            if (responseType == String.class) {
                return responseType.cast(soapResponse);
            }
            
            // Extract body from SOAP envelope
            String bodyContent = extractSoapBody(soapResponse);
            
            if (bodyContent != null && !bodyContent.trim().isEmpty()) {
                return xmlMapper.readValue(bodyContent, responseType);
            }
            
            return null;
        } catch (Exception e) {
            logger.error("Failed to parse SOAP response: {}", e.getMessage(), e);
            throw new ApiException("SOAP_PARSE_ERROR", "Failed to parse SOAP response", e);
        }
    }
    
    /**
     * Extract body content from SOAP envelope
     */
    private String extractSoapBody(String soapResponse) {
        try {
            int bodyStart = soapResponse.indexOf("<soap:Body>");
            int bodyEnd = soapResponse.indexOf("</soap:Body>");
            
            if (bodyStart == -1 || bodyEnd == -1) {
                // Try alternative namespace
                bodyStart = soapResponse.indexOf("<Body>");
                bodyEnd = soapResponse.indexOf("</Body>");
            }
            
            if (bodyStart != -1 && bodyEnd != -1) {
                bodyStart = soapResponse.indexOf('>', bodyStart) + 1;
                return soapResponse.substring(bodyStart, bodyEnd).trim();
            }
            
            return soapResponse;
        } catch (Exception e) {
            logger.warn("Failed to extract SOAP body, returning full response: {}", e.getMessage());
            return soapResponse;
        }
    }
} 