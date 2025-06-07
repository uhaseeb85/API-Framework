package com.company.apiframework.client.soap;

import org.springframework.web.client.RestTemplate;

import com.company.apiframework.client.ApiClient;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * Factory for creating SOAP API clients
 */
public class SoapClientFactory {
    
    private final XmlMapper xmlMapper;
    
    public SoapClientFactory(XmlMapper xmlMapper) {
        this.xmlMapper = xmlMapper;
    }
    
    /**
     * Create a new SOAP API client with default RestTemplate
     * 
     * @return New SOAP API client instance
     */
    public ApiClient createClient() {
        return new SoapApiClient(new RestTemplate(), xmlMapper);
    }
    
    /**
     * Create a SOAP API client with custom RestTemplate
     * 
     * @param restTemplate Custom RestTemplate to use
     * @return New SOAP API client instance
     */
    public ApiClient createClient(RestTemplate restTemplate) {
        return new SoapApiClient(restTemplate, xmlMapper);
    }
} 