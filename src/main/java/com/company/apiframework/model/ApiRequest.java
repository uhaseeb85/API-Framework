package com.company.apiframework.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Standardized API request model for both REST and SOAP calls
 */
public class ApiRequest {
    
    private String url;
    private String method;
    private Map<String, String> headers;
    private Object body;
    private Map<String, Object> parameters;
    private String soapAction;
    
    public ApiRequest() {
        this.headers = new HashMap<>();
        this.parameters = new HashMap<>();
    }
    
    public ApiRequest(String url, String method) {
        this();
        this.url = url;
        this.method = method;
    }
    
    // Getters and Setters
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    
    public ApiRequest addHeader(String name, String value) {
        this.headers.put(name, value);
        return this;
    }
    
    public Object getBody() {
        return body;
    }
    
    public void setBody(Object body) {
        this.body = body;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    public ApiRequest addParameter(String name, Object value) {
        this.parameters.put(name, value);
        return this;
    }
    
    public String getSoapAction() {
        return soapAction;
    }
    
    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }
    

    
    // Builder pattern for fluent API creation
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private ApiRequest request;
        
        public Builder() {
            this.request = new ApiRequest();
        }
        
        public Builder url(String url) {
            request.setUrl(url);
            return this;
        }
        
        public Builder method(String method) {
            request.setMethod(method);
            return this;
        }
        
        public Builder header(String name, String value) {
            request.addHeader(name, value);
            return this;
        }
        
        public Builder body(Object body) {
            request.setBody(body);
            return this;
        }
        
        public Builder parameter(String name, Object value) {
            request.addParameter(name, value);
            return this;
        }
        
        public Builder soapAction(String soapAction) {
            request.setSoapAction(soapAction);
            return this;
        }
        

        
        public ApiRequest build() {
            return request;
        }
    }
} 