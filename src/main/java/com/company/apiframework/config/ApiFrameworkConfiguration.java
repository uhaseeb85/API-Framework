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
 * Main configuration class for the API Integration Framework
 */
@Configuration
@EnableConfigurationProperties({ApiProperties.class})
@EnableRetry
@EnableAspectJAutoProxy
public class ApiFrameworkConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public XmlMapper xmlMapper() {
        return new XmlMapper();
    }

    @Bean
    public HttpClient httpClient(ApiProperties apiProperties) {
        return HttpClientBuilder.create()
                .setConnectionTimeToLive(apiProperties.getConnectionTimeoutMs(), java.util.concurrent.TimeUnit.MILLISECONDS)
                .setMaxConnTotal(apiProperties.getMaxConnections())
                .setMaxConnPerRoute(apiProperties.getMaxConnectionsPerRoute())
                .build();
    }

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

    @Bean
    public RestClientFactory restClientFactory(RestTemplate restTemplate, ObjectMapper objectMapper) {
        return new RestClientFactory(restTemplate, objectMapper);
    }

    @Bean
    public SoapClientFactory soapClientFactory(XmlMapper xmlMapper) {
        return new SoapClientFactory(xmlMapper);
    }

    @Bean
    public LoggingInterceptor loggingInterceptor() {
        return new LoggingInterceptor();
    }
} 