# API Integration Framework

A comprehensive Spring Boot framework for integrating with external REST and SOAP APIs, providing a unified interface for API interactions with built-in mocking capabilities and Spring-managed RestTemplate beans.

## Features

- **Unified API Interface**: Single service for both REST and SOAP API calls
- **Spring Bean RestTemplates**: Pre-configured, optimized RestTemplate beans for different API types
- **URL Pattern Matching**: Automatic RestTemplate selection based on URL patterns
- **Protocol Auto-Detection**: Automatic detection of REST vs SOAP based on request characteristics
- **Mock Framework**: Built-in testing support with realistic API mocking
- **Spring Integration**: Full Spring Boot integration with dependency injection
- **Type Safety**: Generic response types with automatic deserialization
- **Async Support**: Non-blocking API calls with callback handling
- **Enterprise Features**: Health checks, configuration management, and monitoring support

## 🚀 Getting Started

**New to the framework?** Check out our comprehensive **[Developer Onboarding Guide](DEVELOPER_ONBOARDING.md)** with step-by-step instructions to get you up and running in 15 minutes!

## Quick Start

### 1. Add Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web-services</artifactId>
</dependency>
```

### 2. Basic Usage with Spring Bean Approach

```java
@Service
public class PaymentService {
    
    @Autowired
    private ApiService apiService;
    
    public PaymentResponse processPayment(PaymentRequest request) {
        // Create API request
        ApiRequest apiRequest = ApiRequest.builder()
            .url("https://payment.gateway.com/process")  // Uses paymentApiRestTemplate automatically
            .method("POST")
            .header("Authorization", "Bearer " + getAuthToken())
            .body(request)
            .build();
        
        // Execute with automatic RestTemplate selection
        ApiResponse<PaymentResponse> response = apiService.executeRest(apiRequest, PaymentResponse.class);
        
        if (response.isSuccess()) {
            return response.getBody();
        } else {
            throw new PaymentException("Payment failed: " + response.getErrorMessage());
        }
    }
}
```

## RestTemplate Configuration

The framework provides **three approaches** for RestTemplate management:

### Method 1: Spring Bean Approach (Recommended) ✅

The framework provides pre-configured RestTemplate beans optimized for different API types:

#### Available RestTemplate Beans:

- **`paymentApiRestTemplate`**: Optimized for payment processing (2s/5s timeouts, 50 connections)
- **`batchApiRestTemplate`**: Optimized for batch operations (10s/5min timeouts, 10 connections)
- **`externalApiRestTemplate`**: Optimized for external partners (5s/30s timeouts, 20 connections)
- **`highVolumeApiRestTemplate`**: Optimized for high-volume APIs (3s/15s timeouts, 100 connections)
- **`defaultRestTemplate`**: Default configuration (5s/30s timeouts, 100 connections)

#### URL Pattern Mappings:

The framework automatically maps URL patterns to appropriate RestTemplate beans:

```
https://payment.gateway.com/*       -> paymentApiRestTemplate
https://*/payment/*                 -> paymentApiRestTemplate
https://batch.processor.com/*       -> batchApiRestTemplate
https://*/batch/*                   -> batchApiRestTemplate
https://*.external.com/*            -> externalApiRestTemplate
https://partner-*.com/*             -> externalApiRestTemplate
https://high-volume.api.com/*       -> highVolumeApiRestTemplate
https://*/stream/*                  -> highVolumeApiRestTemplate
```

#### Direct Bean Injection:

```java
@Service
public class MyService {
    
    @Autowired
    @Qualifier("paymentApiRestTemplate")
    private RestTemplate paymentRestTemplate;
    
    @Autowired
    private SpringBeanApiService springBeanApiService;
    
    public void processPayment() {
        // Option 1: Direct bean usage
        ResponseEntity<String> response = paymentRestTemplate.postForEntity(
            "https://payment.gateway.com/process", request, String.class);
        
        // Option 2: Through SpringBeanApiService
        ApiResponse<PaymentDto> result = springBeanApiService.callPaymentApi(request, PaymentDto.class);
    }
}
```

### Method 2: Explicit RestTemplate per Call

```java
// Create custom RestTemplate for specific use case
RestTemplate customRestTemplate = new RestTemplate();
customRestTemplate.setRequestFactory(createCustomRequestFactory());

ApiResponse<String> response = apiService.executeRest(request, String.class, customRestTemplate);
```

### Method 3: Legacy Custom Registration (Backward Compatibility)

```java
// Register custom RestTemplate for URL patterns (legacy support)
RestTemplate legacyTemplate = new RestTemplate();
apiService.registerCustomRestTemplate("https://legacy-api.com/*", legacyTemplate);

// All calls to matching URLs will use this RestTemplate
ApiResponse<Data> response = apiService.executeRest(request, Data.class);
```

## Architecture

The framework follows a layered architecture:

1. **API Service Layer**: Main entry point (`ApiService`, `SpringBeanApiService`)
2. **Client Layer**: Protocol-specific implementations (`RestApiClient`, `SoapApiClient`)
3. **Factory Layer**: Client and RestTemplate creation (`RestClientFactory`, `SoapClientFactory`)
4. **Configuration Layer**: Spring bean configuration (`RestTemplateBeanConfiguration`)
5. **Model Layer**: Request/Response DTOs (`ApiRequest`, `ApiResponse`)
6. **Mock Layer**: Testing utilities (`MockApiService`, Custom API Mocks)

## Configuration

### Application Properties

```properties
# Framework Configuration
api.framework.enableMocking=false
api.framework.connectionTimeoutMs=5000
api.framework.readTimeoutMs=30000
api.framework.maxRetryAttempts=3

# SSL Configuration (if needed)
server.ssl.enabled=false
```

### Custom RestTemplate Bean Configuration

To customize or add new RestTemplate beans, extend `RestTemplateBeanConfiguration`:

```java
@Configuration
public class CustomRestTemplateConfig {
    
    @Bean
    @Qualifier("customApiRestTemplate")
    public RestTemplate customApiRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionTimeout(1000);
        factory.setReadTimeout(3000);
        restTemplate.setRequestFactory(factory);
        
        return restTemplate;
    }
}
```

## Usage Examples

### REST API Call

```java
ApiRequest request = ApiRequest.builder()
    .url("https://api.example.com/users/123")
    .method("GET")
    .header("Authorization", "Bearer token")
    .build();

ApiResponse<User> response = apiService.executeRest(request, User.class);

if (response.isSuccess()) {
    User user = response.getBody();
    System.out.println("User: " + user.getName());
} else {
    System.err.println("Error: " + response.getErrorMessage());
}
```

### SOAP API Call

```java
String soapBody = """
    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
        <soap:Body>
            <GetWeather>
                <City>New York</City>
            </GetWeather>
        </soap:Body>
    </soap:Envelope>
    """;

ApiRequest soapRequest = ApiRequest.builder()
    .url("http://soap.service.com/weather")
    .method("POST")
    .soapAction("GetWeather")
    .header("Content-Type", "text/xml")
    .body(soapBody)
    .build();

ApiResponse<String> response = apiService.executeSoap(soapRequest, String.class);
```

### Automatic Protocol Detection

```java
// Framework automatically detects protocol based on request characteristics
ApiResponse<Object> response = apiService.executeAuto(request, Object.class);
```

### Asynchronous Execution

```java
apiService.executeAsync(request, User.class, new ApiCallback<User>() {
    @Override
    public void onSuccess(ApiResponse<User> response) {
        System.out.println("Async success: " + response.getBody());
    }
    
    @Override
    public void onError(ApiResponse<User> response) {
        System.err.println("Async error: " + response.getErrorMessage());
    }
});
```

## Testing with Mock Framework

### Enable Mocking

```properties
api.framework.enableMocking=true
```

### Simple Mock Registration

```java
@Autowired
private MockApiService mockApiService;

@Test
public void testApiCall() {
    // Register mock response
    mockApiService.registerMockResponse(
        "https://api.example.com/users/123", 
        200, 
        new User("John Doe")
    );
    
    // Test your service
    ApiResponse<User> response = apiService.executeRest(request, User.class);
    assertEquals("John Doe", response.getBody().getName());
}
```

### Custom API Mocks

```java
// Register realistic payment API mock
PaymentApiMock paymentMock = new PaymentApiMock();
mockApiService.registerCustomMock("payment-api", paymentMock);

// Mock handles different scenarios automatically
paymentMock.setScenario("insufficient_funds");
ApiResponse<PaymentResponse> response = apiService.executeRest(paymentRequest, PaymentResponse.class);
assertEquals(402, response.getStatusCode());
```

## Best Practices

1. **Use Spring Bean Approach**: Prefer the pre-configured RestTemplate beans for production use
2. **URL Pattern Strategy**: Design your API URLs to match the framework's URL pattern mappings
3. **Bean Injection**: Use `@Qualifier` annotations when injecting specific RestTemplate beans
4. **Error Handling**: Always check `response.isSuccess()` before accessing response body
5. **Testing**: Use the mock framework for comprehensive testing scenarios
6. **Configuration**: Leverage Spring profiles for different environment configurations
7. **Monitoring**: Implement health checks using `apiService.isHealthy()`

## Health Monitoring

```java
@RestController
public class HealthController {
    
    @Autowired
    private ApiService apiService;
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("api-framework", apiService.isHealthy());
        health.put("configuration", apiService.getConfigurationSummary());
        
        return ResponseEntity.ok(health);
    }
}
```

## Documentation & Code Quality

The framework maintains comprehensive JavaDoc documentation across all classes:

- **Service Classes**: `ApiService`, `SpringBeanApiService`, `MockApiService`
- **Configuration Classes**: `RestTemplateBeanConfiguration`, `ApiProperties`
- **Model Classes**: `ApiRequest`, `ApiResponse`, `ApiCallback`
- **Client Classes**: `RestApiClient`, `SoapApiClient`
- **Factory Classes**: `RestClientFactory`, `SoapClientFactory`

All methods include detailed parameter descriptions, usage examples, and return value documentation to ensure developer productivity and maintainability.

## Migration Guide

If you're upgrading from the programmatic RestTemplate configuration approach:

### Before (Programmatic Configuration):
```java
RestTemplateConfig config = RestTemplateConfig.builder("payment-api")
    .connectionTimeoutMs(2000)
    .readTimeoutMs(5000)
    .build();
apiService.registerRestTemplateConfig("https://payment.gateway.com/*", config);
```

### After (Spring Bean Approach):
```java
// No configuration needed - beans are pre-configured
// URLs automatically map to appropriate beans
ApiResponse<PaymentDto> response = apiService.executeRest(request, PaymentDto.class);

// Or use direct bean injection
@Autowired
@Qualifier("paymentApiRestTemplate")
private RestTemplate paymentTemplate;
```

The framework maintains backward compatibility for legacy custom RestTemplate registrations through the `registerCustomRestTemplate()` method.

## Contributing

Please ensure all new features include:
- Comprehensive unit tests
- JavaDoc documentation
- Integration with the Spring bean approach
- Backward compatibility considerations

## License

This project is licensed under the MIT License. 