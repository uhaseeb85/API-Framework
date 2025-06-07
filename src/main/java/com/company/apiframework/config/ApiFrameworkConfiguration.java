package com.company.apiframework.config;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;

import com.company.apiframework.client.rest.RestClientFactory;
import com.company.apiframework.client.soap.SoapClientFactory;
import com.company.apiframework.interceptor.LoggingInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * Main Spring configuration class for the API Integration Framework.
 * 
 * <p>This configuration class sets up all the necessary beans and components
 * required for the framework to function properly, including:</p>
 * <ul>
 *   <li>HTTP client configuration with connection pooling</li>
 *   <li>RestTemplate with custom timeouts and interceptors</li>
 *   <li>JSON and XML mappers for data serialization</li>
 *   <li>Client factories for REST and SOAP protocols</li>
 *   <li>Retry mechanism and AOP support</li>
 * </ul>
 * 
 * <p><strong>Key Features Enabled:</strong></p>
 * <ul>
 *   <li>{@code @EnableRetry} - Enables Spring Retry for fault tolerance</li>
 *   <li>{@code @EnableAspectJAutoProxy} - Enables AOP for cross-cutting concerns</li>
 *   <li>{@code @EnableConfigurationProperties} - Binds configuration properties</li>
 * </ul>
 * 
 * @author API Framework Team
 * @version 1.0
 * @see ApiProperties
 * @since 1.0
 */
@Configuration
@EnableConfigurationProperties({ApiProperties.class})
@EnableRetry
@EnableAspectJAutoProxy
public class ApiFrameworkConfiguration {

    /**
     * Creates a Jackson ObjectMapper for JSON serialization/deserialization.
     * 
     * <p>This mapper is used for converting Java objects to/from JSON format
     * in REST API calls. It uses default Jackson configuration which handles
     * most common serialization scenarios.</p>
     * 
     * @return Configured ObjectMapper instance
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /**
     * Creates a Jackson XmlMapper for XML serialization/deserialization.
     * 
     * <p>This mapper is specifically used for SOAP API calls and any REST APIs
     * that work with XML format. It extends ObjectMapper with XML-specific
     * serialization capabilities.</p>
     * 
     * @return Configured XmlMapper instance
     */
    @Bean
    public XmlMapper xmlMapper() {
        return new XmlMapper();
    }

    /**
     * Creates an Apache HttpClient with connection pooling and timeout configuration.
     * 
     * <p>This HttpClient is configured with:</p>
     * <ul>
     *   <li>Connection timeout from properties</li>
     *   <li>Maximum total connections pool size</li>
     *   <li>Maximum connections per route</li>
     *   <li>Connection time-to-live management</li>
     * </ul>
     * 
     * <p>The connection pooling helps improve performance by reusing connections
     * and managing resource utilization efficiently.</p>
     * 
     * @param apiProperties Configuration properties for timeouts and connection limits
     * @return Configured HttpClient instance
     */
    @Bean
    public HttpClient httpClient(ApiProperties apiProperties) {
        return HttpClientBuilder.create()
                .setConnectionTimeToLive(apiProperties.getConnectionTimeoutMs(), java.util.concurrent.TimeUnit.MILLISECONDS)
                .setMaxConnTotal(apiProperties.getMaxConnections())
                .setMaxConnPerRoute(apiProperties.getMaxConnectionsPerRoute())
                .build();
    }

    /**
     * Creates the main RestTemplate with custom configuration and interceptors.
     * 
     * <p>This RestTemplate is configured with:</p>
     * <ul>
     *   <li>Custom HttpClient for connection pooling</li>
     *   <li>Connection and read timeouts from properties</li>
     *   <li>Logging interceptor for request/response monitoring</li>
     * </ul>
     * 
     * <p><strong>Note:</strong> This is the default RestTemplate. Individual API calls
     * can use custom RestTemplates registered via ApiService.</p>
     * 
     * @param httpClient The configured HttpClient
     * @param apiProperties Configuration properties for timeouts
     * @return Configured RestTemplate with interceptors
     * @see com.company.apiframework.service.ApiService#registerCustomRestTemplate(String, RestTemplate)
     */
    @Bean
    public RestTemplate restTemplate(HttpClient httpClient, ApiProperties apiProperties) {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        requestFactory.setConnectTimeout(apiProperties.getConnectionTimeoutMs());
        requestFactory.setReadTimeout(apiProperties.getReadTimeoutMs());
        
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.getInterceptors().add(new LoggingInterceptor());
        return restTemplate;
    }

    /**
     * Creates the REST client factory for creating REST API clients.
     * 
     * <p>This factory is responsible for creating ApiClient instances that handle
     * REST protocol communications. It uses the configured RestTemplate and
     * ObjectMapper for JSON processing.</p>
     * 
     * @param restTemplate The configured RestTemplate
     * @param objectMapper The JSON ObjectMapper
     * @return RestClientFactory instance
     */
    @Bean
    public RestClientFactory restClientFactory(RestTemplate restTemplate, ObjectMapper objectMapper) {
        return new RestClientFactory(restTemplate, objectMapper);
    }

    /**
     * Creates the SOAP client factory for creating SOAP API clients.
     * 
     * <p>This factory is responsible for creating ApiClient instances that handle
     * SOAP protocol communications. It uses the XmlMapper for XML processing
     * required by SOAP messages.</p>
     * 
     * @param xmlMapper The XML mapper for SOAP message processing
     * @return SoapClientFactory instance
     */
    @Bean
    public SoapClientFactory soapClientFactory(XmlMapper xmlMapper) {
        return new SoapClientFactory(xmlMapper);
    }

    /**
     * Creates the logging interceptor for HTTP request/response monitoring.
     * 
     * <p>This interceptor logs all HTTP requests and responses when debug logging
     * is enabled, or provides summary logging at info level. It helps with:</p>
     * <ul>
     *   <li>Debugging API integration issues</li>
     *   <li>Monitoring API call performance</li>
     *   <li>Auditing API usage</li>
     * </ul>
     * 
     * @return LoggingInterceptor instance
     * @see LoggingInterceptor
     */
    @Bean
    public LoggingInterceptor loggingInterceptor() {
        return new LoggingInterceptor();
    }
} 