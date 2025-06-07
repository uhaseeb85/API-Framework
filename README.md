# API Integration Framework

A comprehensive Spring-based framework for standardizing external API integrations across your team. This framework supports both REST and SOAP protocols with consistent request/response handling, centralized error management, and robust testing capabilities.

## Features

- **✅ REST and SOAP Support**: Unified interface for both REST and SOAP API calls
- **✅ Standardized Request/Response**: Consistent data models across all API integrations
- **✅ Error Handling**: Centralized error handling with customizable retry mechanisms
- **✅ Service Mocking**: Built-in mocking capabilities for testing and development
- **✅ Spring Integration**: Full Spring Boot integration with dependency injection
- **✅ Java 1.8 Compatible**: Fully compliant with Java 1.8 standards
- **✅ Configurable**: Extensive configuration options for timeouts, retries, and connection pooling
- **✅ Logging**: Comprehensive request/response logging with configurable levels
- **✅ Async Support**: Asynchronous API call capabilities with callbacks
- **✅ Custom RestTemplate Support**: Per-API integration custom RestTemplate configuration

## Quick Start

### 1. Add Dependencies

The framework is built with Maven. All necessary dependencies are included in the `pom.xml`.

### 2. Configuration

Configure the framework in your `application.yml`:

```yaml
api:
  framework:
    connection-timeout-ms: 5000
    read-timeout-ms: 30000
    max-retry-attempts: 3
    enable-logging: true
    enable-mocking: false
```

### 3. Basic Usage

#### REST API Call

```java
@Autowired
private ApiService apiService;

// Create REST request
ApiRequest request = ApiRequest.builder()
    .url("https://api.example.com/users/123")
    .method("GET")
    .header("Authorization", "Bearer your-token")
    .header("Accept", "application/json")
    .timeout(5000)
    .build();

// Execute synchronously
ApiResponse<UserDto> response = apiService.executeRest(request, UserDto.class);

if (response.isSuccess()) {
    UserDto user = response.getBody();
    System.out.println("User: " + user.getName());
} else {
    System.err.println("Error: " + response.getErrorMessage());
}
```

#### SOAP API Call

```java
// Create SOAP request
String soapBody = 
    "<GetWeatherRequest>" +
    "  <City>London</City>" +
    "  <Country>UK</Country>" +
    "</GetWeatherRequest>";

ApiRequest request = ApiRequest.builder()
    .url("http://weather.example.com/service")
    .method("POST")
    .soapAction("GetWeather")
    .body(soapBody)
    .build();

// Execute SOAP call
ApiResponse<String> response = apiService.executeSoap(request);

if (response.isSuccess()) {
    System.out.println("Weather data: " + response.getBody());
}
```

#### Auto-Detection

The framework can automatically detect whether to use REST or SOAP based on request characteristics:

```java
// Will be detected as SOAP due to soapAction
ApiRequest soapRequest = ApiRequest.builder()
    .url("http://soap.example.com/service")
    .soapAction("GetData")
    .body("<soap:Envelope>...</soap:Envelope>")
    .build();

// Will be detected as REST
ApiRequest restRequest = ApiRequest.builder()
    .url("https://api.example.com/data")
    .method("GET")
    .build();

// Use auto-detection
ApiResponse<String> response = apiService.executeAuto(request, String.class);
```

#### Asynchronous Calls

```java
apiService.executeAsync(request, UserDto.class, new ApiCallback<UserDto>() {
    @Override
    public void onSuccess(ApiResponse<UserDto> response) {
        System.out.println("Success: " + response.getBody().getName());
    }
    
    @Override
    public void onError(ApiResponse<UserDto> response) {
        System.err.println("Error: " + response.getErrorMessage());
    }
    
    @Override
    public void onException(Exception exception) {
        System.err.println("Exception: " + exception.getMessage());
    }
});
```

## Custom RestTemplate Support

The framework provides **full support** for custom RestTemplates per API integration, giving you maximum flexibility for different external services.

### Method 1: Custom RestTemplate per Call

```java
// Create custom RestTemplate with specific configuration
RestTemplate customRestTemplate = new RestTemplate();
customRestTemplate.setRequestFactory(customRequestFactory);

// Use it for a specific API call
ApiRequest request = ApiRequest.builder()
    .url("https://special-api.example.com/data")
    .method("GET")
    .build();

ApiResponse<DataDto> response = apiService.executeRest(request, DataDto.class, customRestTemplate);
```

### Method 2: Register Custom RestTemplates for URL Patterns

```java
// Register custom RestTemplates for different services
RestTemplate secureApiTemplate = createSecureRestTemplate();
RestTemplate legacyApiTemplate = createLegacyRestTemplate();
RestTemplate fastApiTemplate = createFastRestTemplate();

// Register patterns - supports wildcards
apiService.registerCustomRestTemplate("https://secure-api.example.com/*", secureApiTemplate);
apiService.registerCustomRestTemplate("https://legacy-api.example.com/*", legacyApiTemplate);
apiService.registerCustomRestTemplate("https://fast-api.example.com/*", fastApiTemplate);

// Now all calls to these URLs automatically use the custom RestTemplates
ApiRequest request = ApiRequest.builder()
    .url("https://secure-api.example.com/users/123")  // Uses secureApiTemplate
    .method("GET")
    .build();

ApiResponse<UserDto> response = apiService.executeRest(request, UserDto.class);
```

### Method 3: Different Configurations for Different APIs

```java
// Payment API - requires special SSL and timeouts
RestTemplate paymentTemplate = new RestTemplate();
HttpComponentsClientHttpRequestFactory paymentFactory = new HttpComponentsClientHttpRequestFactory();
paymentFactory.setConnectTimeout(5000);
paymentFactory.setReadTimeout(15000);
paymentTemplate.setRequestFactory(paymentFactory);

// Notification API - rate limited
RestTemplate notificationTemplate = new RestTemplate();
HttpClient rateLimitedClient = HttpClientBuilder.create()
    .setMaxConnTotal(5)
    .setMaxConnPerRoute(2)
    .build();
HttpComponentsClientHttpRequestFactory notificationFactory = new HttpComponentsClientHttpRequestFactory();
notificationFactory.setHttpClient(rateLimitedClient);
notificationTemplate.setRequestFactory(notificationFactory);

// Use them for specific calls
ApiResponse<PaymentDto> paymentResponse = apiService.executeRest(
    paymentRequest, PaymentDto.class, paymentTemplate);

ApiResponse<String> notificationResponse = apiService.executeRest(
    notificationRequest, String.class, notificationTemplate);
```

### SOAP with Custom RestTemplate

```java
// SOAP-optimized RestTemplate
RestTemplate soapTemplate = createSoapOptimizedRestTemplate();

ApiRequest soapRequest = ApiRequest.builder()
    .url("http://soap.example.com/service")
    .soapAction("GetData")
    .body("<soap:Envelope>...</soap:Envelope>")
    .build();

ApiResponse<String> soapResponse = apiService.executeSoap(soapRequest, soapTemplate);
```

### Management Methods

```java
// Register custom RestTemplate
apiService.registerCustomRestTemplate("https://api.example.com/*", customTemplate);

// Remove specific registration
apiService.removeCustomRestTemplate("https://api.example.com/*");

// Get all registered templates
Map<String, RestTemplate> registered = apiService.getCustomRestTemplates();

// Clear all registrations
apiService.clearCustomRestTemplates();
```

## Service Mocking

The framework includes powerful mocking capabilities for testing and development:

### Setting Up Mocks

```java
@Autowired
private MockApiService mockApiService;

// Register a simple mock
mockApiService.registerMockResponse(
    "https://api.example.com/users/123", 
    200, 
    new UserDto("123", "John Doe")
);

// Register mock with custom delay and headers
MockApiService.MockResponse mockResponse = new MockApiService.MockResponse(200, userData)
    .withDelay(500)  // 500ms delay
    .addHeader("X-Custom-Header", "test-value");

mockApiService.registerMockResponse("https://api.example.com/users/*", mockResponse);
```

### Pattern Matching

```java
// Use wildcards for flexible URL matching
mockApiService.registerMockResponse(
    "https://api.example.com/users/*",  // Matches any user ID
    200,
    new UserDto("any-id", "Mock User")
);

// Execute test
ApiRequest request = ApiRequest.builder()
    .url("https://api.example.com/users/456")  // Will match the pattern
    .method("GET")
    .build();

ApiResponse<UserDto> response = mockApiService.executeMock(request, UserDto.class);
```

## Custom API Mocks

The framework provides **sophisticated custom mocking capabilities** for different external APIs, allowing teams to create realistic, API-specific mock behaviors with complex scenarios.

### Creating Custom API Mocks

Custom API mocks allow you to create realistic, stateful mocks for specific external services:

```java
public class PaymentApiMock implements CustomApiMock {
    
    @Override
    public boolean matchesUrl(String url) {
        return url.contains("https://payment.gateway.com");
    }
    
    @Override
    public <T> ApiResponse<T> executeMock(ApiRequest request, Class<T> responseType) {
        // Determine scenario based on request content
        PaymentScenario scenario = determineScenario(request);
        
        // Create realistic response
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatusCode(scenario.statusCode);
        response.setSuccess(scenario.statusCode >= 200 && scenario.statusCode < 300);
        
        // Add payment-specific headers
        response.getHeaders().put("X-Payment-Gateway", "Mock-Gateway-v2.0");
        response.getHeaders().put("X-Transaction-Id", generateTransactionId());
        
        // Create response body
        Object responseBody = createPaymentResponse(scenario, request);
        response.setBody(convertResponse(responseBody, responseType));
        
        return response;
    }
    
    @Override
    public String getApiIdentifier() {
        return "payment-api";
    }
    
    @Override
    public void setupScenarios() {
        // Define payment-specific scenarios
        scenarios.put("insufficient_funds", new PaymentScenario(400, "INSUFFICIENT_FUNDS"));
        scenarios.put("invalid_card", new PaymentScenario(400, "INVALID_CARD"));
        scenarios.put("network_timeout", new PaymentScenario(504, "GATEWAY_TIMEOUT"));
        // ... more scenarios
    }
}
```

### Registering and Using Custom Mocks

```java
// Register custom API mocks for different services
PaymentApiMock paymentMock = new PaymentApiMock(objectMapper);
mockApiService.registerApiMock("payment-api", paymentMock);

UserServiceMock userServiceMock = new UserServiceMock(objectMapper);
mockApiService.registerApiMock("user-service", userServiceMock);

// Test API calls - automatically uses appropriate custom mock
ApiRequest paymentRequest = ApiRequest.builder()
    .url("https://payment.gateway.com/process")
    .method("POST")
    .body("{\"amount\":100.00,\"currency\":\"USD\"}")
    .build();

ApiResponse<String> response = mockApiService.executeMock(paymentRequest, String.class);
// Uses PaymentApiMock with realistic payment processing logic
```

### Scenario-Based Testing

Custom mocks support scenario-based testing using headers:

```java
// Test specific error scenarios
ApiRequest request = ApiRequest.builder()
    .url("https://payment.gateway.com/process")
    .method("POST")
    .header("X-Mock-Scenario", "insufficient_funds")  // Force specific scenario
    .body("{\"amount\":1000.00}")
    .build();

ApiResponse<String> response = mockApiService.executeMock(request, String.class);
// Returns: 400 status with "INSUFFICIENT_FUNDS" error

// Test different scenarios
String[] scenarios = {"network_timeout", "invalid_card", "rate_limit"};
for (String scenario : scenarios) {
    ApiRequest testRequest = ApiRequest.builder()
        .url("https://payment.gateway.com/process")
        .header("X-Mock-Scenario", scenario)
        .body("{\"amount\":50.00}")
        .build();
    
    ApiResponse<String> testResponse = mockApiService.executeMock(testRequest, String.class);
    // Each scenario returns appropriate error response
}
```

### Built-in Mock Implementations

The framework includes **production-ready custom mocks**:

#### Payment API Mock
- **Scenarios**: Success, insufficient funds, invalid card, expired card, network timeout, rate limiting
- **Features**: Transaction ID generation, realistic delays, payment-specific headers
- **Intelligence**: Automatically detects scenarios based on request content (card numbers, amounts)

```java
// Automatic scenario detection
ApiRequest request = ApiRequest.builder()
    .url("https://payment.gateway.com/process")
    .body("{\"cardNumber\":\"4000000000000002\"}")  // Triggers invalid card scenario
    .build();

ApiResponse<String> response = mockApiService.executeMock(request, String.class);
// Returns 400 with "INVALID_CARD" error
```

#### User Service Mock
- **Operations**: Full CRUD operations (GET, POST, PUT, DELETE)
- **Endpoints**: `/users`, `/users/{id}`, `/auth/login`, `/auth/logout`
- **Features**: Realistic user data, state management, validation errors
- **Intelligence**: Persistent in-memory user storage during test session

```java
// Create user
ApiRequest createRequest = ApiRequest.builder()
    .url("https://user-service.example.com/users")
    .method("POST")
    .body("{\"email\":\"test@example.com\",\"firstName\":\"Test\"}")
    .build();
mockApiService.executeMock(createRequest, String.class);

// Get user (will return the created user)
ApiRequest getRequest = ApiRequest.builder()
    .url("https://user-service.example.com/users/4")  // New user ID
    .method("GET")
    .build();
ApiResponse<String> response = mockApiService.executeMock(getRequest, String.class);
```

### Custom Mock Management

```java
// Check mock availability
boolean hasPaymentMock = mockApiService.hasApiMock("payment-api");
boolean hasCustomMockForUrl = mockApiService.hasCustomApiMock("https://payment.gateway.com/process");

// Get mock instances
CustomApiMock paymentMock = mockApiService.getApiMock("payment-api");

// Reset mock state (clears counters, regenerates IDs, etc.)
mockApiService.resetApiMock("payment-api");
mockApiService.resetAllApiMocks();

// Remove mocks
mockApiService.removeApiMock("payment-api");
mockApiService.clearCustomApiMocks();

// Get comprehensive mock summary
Map<String, Object> summary = mockApiService.getMockSummary();
System.out.println("Custom API Mocks: " + summary.get("customApiMocks"));
System.out.println("Request Counts: " + summary.get("requestCounts"));
```

### Combined Mocking Strategy

The framework intelligently combines custom and general mocks:

```java
// Custom mocks take priority
mockApiService.registerApiMock("payment-api", paymentMock);     // Custom mock
mockApiService.registerMockResponse("https://notification.service.com/*", 
    200, Map.of("status", "sent"));                             // General mock

// Framework automatically routes to appropriate mock type
ApiRequest paymentRequest = ApiRequest.builder()
    .url("https://payment.gateway.com/process")  // → Uses PaymentApiMock (custom)
    .build();

ApiRequest notificationRequest = ApiRequest.builder()
    .url("https://notification.service.com/send")  // → Uses general mock
    .build();

ApiRequest unknownRequest = ApiRequest.builder()
    .url("https://unknown.service.com/endpoint")  // → Returns 404 (no mock)
    .build();
```

## Configuration Options

### Connection Settings
- `connection-timeout-ms`: Connection timeout in milliseconds (default: 5000)
- `read-timeout-ms`: Read timeout in milliseconds (default: 30000)
- `max-connections`: Maximum total connections (default: 100)
- `max-connections-per-route`: Maximum connections per route (default: 20)

### Retry Settings
- `max-retry-attempts`: Maximum retry attempts (default: 3)
- `retry-delay-ms`: Initial retry delay in milliseconds (default: 1000)

### Logging Settings
- `enable-logging`: Enable request/response logging (default: true)

### Mocking Settings
- `enable-mocking`: Enable service mocking (default: false)
- `mock-server-url`: Mock server URL for WireMock integration

## Error Handling

The framework provides comprehensive error handling:

```java
ApiResponse<String> response = apiService.executeRest(request);

if (response.hasError()) {
    String errorCode = response.getErrorCode();
    String errorMessage = response.getErrorMessage();
    int statusCode = response.getStatusCode();
    
    // Handle specific error types
    switch (errorCode) {
        case "CONNECTION_TIMEOUT":
            // Handle timeout
            break;
        case "REST_ERROR":
            // Handle REST specific errors
            break;
        default:
            // Handle generic errors
            break;
    }
}
```

## Testing

The framework includes comprehensive test coverage. Run tests with:

```bash
mvn test
```

Example test structure:

```java
@SpringBootTest
@TestPropertySource(properties = {"api.framework.enableMocking=true"})
public class YourApiIntegrationTest {
    
    @Autowired
    private ApiService apiService;
    
    @Autowired
    private MockApiService mockApiService;
    
    @Test
    public void testUserApiIntegration() {
        // Setup mock
        mockApiService.registerMockResponse(
            "https://api.example.com/users/123",
            200,
            new UserDto("123", "Test User")
        );
        
        // Test your integration
        ApiRequest request = ApiRequest.builder()
            .url("https://api.example.com/users/123")
            .method("GET")
            .build();
        
        ApiResponse<UserDto> response = mockApiService.executeMock(request, UserDto.class);
        
        assertTrue(response.isSuccess());
        assertEquals("Test User", response.getBody().getName());
    }
}
```

## Best Practices

### 1. Use Builder Pattern
Always use the builder pattern for creating requests:

```java
ApiRequest request = ApiRequest.builder()
    .url("https://api.example.com/endpoint")
    .method("POST")
    .header("Content-Type", "application/json")
    .body(requestData)
    .timeout(10000)
    .build();
```

### 2. Handle Errors Gracefully
Always check for errors and handle them appropriately:

```java
ApiResponse<DataDto> response = apiService.executeRest(request, DataDto.class);

if (response.isSuccess()) {
    // Process successful response
    DataDto data = response.getBody();
} else {
    // Log error and handle gracefully
    logger.error("API call failed: {} - {}", response.getErrorCode(), response.getErrorMessage());
    // Implement fallback logic
}
```

### 3. Use Appropriate Timeouts
Set realistic timeouts based on the API's expected response time:

```java
ApiRequest request = ApiRequest.builder()
    .url("https://slow-api.example.com/data")
    .timeout(30000)  // 30 seconds for slow APIs
    .build();
```

### 4. Leverage Mocking in Tests
Use the mocking service extensively in your tests:

```java
@BeforeEach
void setUp() {
    mockApiService.clearMockResponses();
    // Setup test-specific mocks
}
```

## Architecture

The framework follows a layered architecture:

```
┌─────────────────┐
│   Service Layer │  (ApiService)
├─────────────────┤
│   Client Layer  │  (RestApiClient, SoapApiClient)
├─────────────────┤
│   Model Layer   │  (ApiRequest, ApiResponse)
├─────────────────┤
│  Infrastructure │  (Configuration, Interceptors)
└─────────────────┘
```

## Requirements

- Java 1.8 or higher
- Spring Boot 2.7.x
- Maven 3.6 or higher

## Building

Build the project with Maven:

```bash
mvn clean compile
mvn test
mvn package
```

## Contributing

1. Follow the established code style and patterns
2. Add comprehensive tests for new features
3. Update documentation for any API changes
4. Ensure Java 1.8 compatibility

## License

This API Integration Framework is designed for internal use and follows your organization's coding standards and best practices. 