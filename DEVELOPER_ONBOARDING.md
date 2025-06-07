# 🚀 Developer Onboarding Guide
**API Integration Framework v1.1.0**

Welcome to the API Integration Framework! This guide will get you up and running in 15 minutes or less.

## 📋 Prerequisites

Before you start, ensure you have:

- ✅ **Java 17+** (or Java 8+ minimum)
- ✅ **Maven 3.6+**
- ✅ **IDE** (IntelliJ IDEA, Eclipse, or VS Code with Java extensions)
- ✅ **Git** for version control

## 🏗️ Step 1: Environment Setup

### 1.1 Clone and Build the Project

```bash
# Clone the repository
git clone <repository-url>
cd APIFramwork

# Build the project
mvn clean compile

# Run all tests to verify setup
mvn test
```

**Expected Output:**
```
Tests run: 44, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 1.2 Import into Your IDE

**IntelliJ IDEA:**
1. File → Open → Select the `APIFramwork` folder
2. Wait for Maven to import dependencies
3. Verify project structure appears correctly

**Eclipse:**
1. File → Import → Existing Maven Projects
2. Browse to the `APIFramwork` folder
3. Select and import

### 1.3 Verify Spring Boot Application Starts

```bash
mvn spring-boot:run
```

**Expected Output:**
```
RestTemplate beans initialized with URL pattern mappings:
  https://payment.gateway.com/* -> paymentApiRestTemplate
  https://*/stream/* -> highVolumeApiRestTemplate
  ...
Started ApiFrameworkApplication in 2.5 seconds
```

## 🎯 Step 2: Understanding the Framework

### 2.1 Framework Architecture

```
┌─────────────────────────────────┐
│   Your Application             │
├─────────────────────────────────┤
│   ApiService (Entry Point)     │ ← Main service you'll use
├─────────────────────────────────┤
│   Spring Bean RestTemplates    │ ← Auto-configured for different APIs
├─────────────────────────────────┤
│   REST/SOAP Clients            │ ← Protocol handling
├─────────────────────────────────┤
│   Mock Framework               │ ← For testing
└─────────────────────────────────┘
```

### 2.2 Key Components

| Component | Purpose | When to Use |
|-----------|---------|-------------|
| `ApiService` | Main entry point for API calls | Always - your primary interface |
| `SpringBeanApiService` | Direct Spring bean usage | When you need specific RestTemplate beans |
| `MockApiService` | Testing and development | Unit tests and local development |
| `RestTemplateBeanConfiguration` | Bean configuration | When customizing RestTemplate settings |

### 2.3 Available RestTemplate Beans

The framework provides 5 pre-configured RestTemplate beans:

```java
@Autowired
@Qualifier("paymentApiRestTemplate")     // Fast: 2s/5s timeouts, 50 connections
private RestTemplate paymentTemplate;

@Autowired  
@Qualifier("batchApiRestTemplate")       // Slow: 10s/5min timeouts, 10 connections
private RestTemplate batchTemplate;

@Autowired
@Qualifier("externalApiRestTemplate")    // Balanced: 5s/30s timeouts, 20 connections  
private RestTemplate externalTemplate;

@Autowired
@Qualifier("highVolumeApiRestTemplate")  // High-throughput: 3s/15s timeouts, 100 connections
private RestTemplate highVolumeTemplate;

@Autowired
private RestTemplate defaultRestTemplate; // Default: 5s/30s timeouts, 100 connections
```

## 🔥 Step 3: Your First API Call

### 3.1 Create a Simple Service

Create `src/main/java/com/example/MyFirstApiService.java`:

```java
package com.example;

import com.company.apiframework.service.ApiService;
import com.company.apiframework.model.ApiRequest;
import com.company.apiframework.model.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MyFirstApiService {
    
    @Autowired
    private ApiService apiService;
    
    /**
     * Makes a simple GET request to JSONPlaceholder API
     */
    public String getUserById(int userId) {
        // Step 1: Build the request
        ApiRequest request = ApiRequest.builder()
            .url("https://jsonplaceholder.typicode.com/users/" + userId)
            .method("GET")
            .header("Accept", "application/json")
            .build();
        
        // Step 2: Execute the request
        ApiResponse<String> response = apiService.executeRest(request, String.class);
        
        // Step 3: Handle the response
        if (response.isSuccess()) {
            return response.getBody();
        } else {
            throw new RuntimeException("API call failed: " + response.getErrorMessage());
        }
    }
    
    /**
     * Makes a POST request with a body
     */
    public String createUser(String userData) {
        ApiRequest request = ApiRequest.builder()
            .url("https://jsonplaceholder.typicode.com/users")
            .method("POST")
            .header("Content-Type", "application/json")
            .body(userData)
            .build();
        
        ApiResponse<String> response = apiService.executeRest(request, String.class);
        
        if (response.isSuccess()) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to create user: " + response.getErrorMessage());
        }
    }
}
```

### 3.2 Test Your Service

Create `src/test/java/com/example/MyFirstApiServiceTest.java`:

```java
package com.example;

import com.company.apiframework.mock.MockApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {"api.framework.enableMocking=true"})
public class MyFirstApiServiceTest {
    
    @Autowired
    private MyFirstApiService myService;
    
    @Autowired
    private MockApiService mockApiService;
    
    @BeforeEach
    void setUp() {
        mockApiService.clearMockResponses();
    }
    
    @Test
    public void testGetUser() {
        // Setup mock response
        String mockUserData = "{\"id\":1,\"name\":\"John Doe\",\"email\":\"john@example.com\"}";
        mockApiService.registerMockResponse(
            "https://jsonplaceholder.typicode.com/users/1",
            200,
            mockUserData
        );
        
        // Test the service
        String result = myService.getUserById(1);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.contains("John Doe"));
        assertTrue(result.contains("john@example.com"));
    }
    
    @Test  
    public void testCreateUser() {
        // Setup mock response
        String mockResponse = "{\"id\":11,\"name\":\"New User\",\"email\":\"new@example.com\"}";
        mockApiService.registerMockResponse(
            "https://jsonplaceholder.typicode.com/users",
            201,
            mockResponse
        );
        
        // Test the service
        String userData = "{\"name\":\"New User\",\"email\":\"new@example.com\"}";
        String result = myService.createUser(userData);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.contains("New User"));
    }
}
```

### 3.3 Run Your Test

```bash
mvn test -Dtest=MyFirstApiServiceTest
```

**Expected Output:**
```
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
```

## 🎭 Step 4: Understanding URL Pattern Mapping

### 4.1 How URL Patterns Work

The framework automatically selects RestTemplate beans based on URL patterns:

```java
// These URLs automatically use paymentApiRestTemplate (fast settings)
"https://payment.gateway.com/process"     ✅
"https://stripe.com/payment/charge"       ✅  
"https://api.paypal.com/payment/execute"  ✅

// These URLs automatically use batchApiRestTemplate (slow, tolerant settings)  
"https://batch.processor.com/export"      ✅
"https://reports.internal.com/batch/run"  ✅

// These URLs use defaultRestTemplate (balanced settings)
"https://api.example.com/data"            ✅
"https://unknown-service.com/endpoint"    ✅
```

### 4.2 Create a Payment Service Example

```java
@Service
public class PaymentService {
    
    @Autowired
    private ApiService apiService;
    
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        ApiRequest request = ApiRequest.builder()
            .url("https://payment.gateway.com/process")  // 🎯 Uses paymentApiRestTemplate automatically
            .method("POST")
            .header("Authorization", "Bearer " + getApiKey())
            .header("Content-Type", "application/json")
            .body(paymentRequest)
            .build();
        
        ApiResponse<PaymentResponse> response = apiService.executeRest(request, PaymentResponse.class);
        
        if (response.isSuccess()) {
            return response.getBody();
        } else {
            throw new PaymentException("Payment failed: " + response.getErrorMessage());
        }
    }
    
    private String getApiKey() {
        return "your-api-key-here";
    }
}
```

## 🧪 Step 5: Advanced Testing with Custom Mocks

### 5.1 Using Built-in Payment API Mock

```java
@Test
public void testPaymentScenarios() {
    // Setup realistic payment mock
    PaymentApiMock paymentMock = new PaymentApiMock();
    mockApiService.registerCustomMock("payment-api", paymentMock);
    
    // Test successful payment
    paymentMock.setScenario("process_success");
    PaymentRequest request = new PaymentRequest("100.00", "USD", "4111111111111111");
    PaymentResponse response = paymentService.processPayment(request);
    assertEquals("SUCCESS", response.getStatus());
    
    // Test insufficient funds
    paymentMock.setScenario("insufficient_funds");
    assertThrows(PaymentException.class, () -> {
        paymentService.processPayment(request);
    });
    
    // Test network timeout
    paymentMock.setScenario("network_timeout");
    assertThrows(PaymentException.class, () -> {
        paymentService.processPayment(request);
    });
}
```

### 5.2 Creating Your Own Custom Mock

```java
public class WeatherApiMock implements CustomApiMock {
    
    private String currentScenario = "sunny";
    
    @Override
    public boolean matchesUrl(String url) {
        return url.contains("weather.api.com");
    }
    
    @Override
    public <T> ApiResponse<T> executeMock(ApiRequest request, Class<T> responseType) {
        ApiResponse<T> response = new ApiResponse<>();
        
        switch (currentScenario) {
            case "sunny":
                response.setStatusCode(200);
                response.setBody((T) "{\"temperature\":25,\"condition\":\"sunny\"}");
                break;
            case "rainy":
                response.setStatusCode(200);
                response.setBody((T) "{\"temperature\":18,\"condition\":\"rainy\"}");
                break;
            case "service_down":
                response.setStatusCode(503);
                response.setErrorMessage("Weather service temporarily unavailable");
                break;
        }
        
        response.setSuccess(response.getStatusCode() < 400);
        return response;
    }
    
    @Override
    public String getApiIdentifier() {
        return "weather-api";
    }
    
    public void setScenario(String scenario) {
        this.currentScenario = scenario;
    }
}
```

## ⚡ Step 6: Performance Best Practices

### 6.1 Use Appropriate RestTemplate Beans

```java
// ✅ GOOD: Use specific beans for better performance
@Autowired
@Qualifier("paymentApiRestTemplate")
private RestTemplate paymentTemplate; // Fast timeouts for critical payments

@Autowired  
@Qualifier("batchApiRestTemplate")
private RestTemplate batchTemplate;   // Long timeouts for batch operations

// ❌ AVOID: Using default for everything
@Autowired
private RestTemplate defaultTemplate; // OK for unknown APIs, but not optimal
```

### 6.2 Async Processing for Non-Critical APIs

```java
public void sendNotificationAsync(String message) {
    ApiRequest request = ApiRequest.builder()
        .url("https://notification.service.com/send")
        .method("POST")
        .body(message)
        .build();
    
    // Non-blocking async call
    apiService.executeAsync(request, String.class, new ApiCallback<String>() {
        @Override
        public void onSuccess(ApiResponse<String> response) {
            logger.info("Notification sent successfully");
        }
        
        @Override
        public void onError(ApiResponse<String> response) {
            logger.warn("Notification failed: {}", response.getErrorMessage());
        }
    });
}
```

### 6.3 Error Handling Patterns

```java
public class RobustApiService {
    
    @Autowired
    private ApiService apiService;
    
    public Optional<UserData> getUserWithFallback(String userId) {
        try {
            ApiRequest request = ApiRequest.builder()
                .url("https://user.service.com/users/" + userId)
                .method("GET")
                .build();
            
            ApiResponse<UserData> response = apiService.executeRest(request, UserData.class);
            
            if (response.isSuccess()) {
                return Optional.of(response.getBody());
            } else {
                logger.warn("User API failed: {}", response.getErrorMessage());
                return getUserFromCache(userId); // Fallback strategy
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error calling user API", e);
            return Optional.empty();
        }
    }
    
    private Optional<UserData> getUserFromCache(String userId) {
        // Implement cache fallback logic
        return Optional.empty();
    }
}
```

## 🔧 Step 7: Configuration and Customization

### 7.1 Application Properties

Create/update `src/main/resources/application.properties`:

```properties
# Framework Configuration
api.framework.enableMocking=false
api.framework.connectionTimeoutMs=5000
api.framework.readTimeoutMs=30000
api.framework.maxRetryAttempts=3

# Logging Configuration
logging.level.com.company.apiframework=DEBUG
logging.level.org.springframework.web.client=TRACE

# Server Configuration (if running as web app)
server.port=8080
management.endpoints.web.exposure.include=health,info
```

### 7.2 Custom RestTemplate Bean

```java
@Configuration
public class CustomApiConfig {
    
    @Bean
    @Qualifier("customApiRestTemplate")
    public RestTemplate customApiRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // Custom timeout configuration
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionTimeout(1000);  // 1 second
        factory.setReadTimeout(3000);        // 3 seconds
        
        restTemplate.setRequestFactory(factory);
        
        // Add custom interceptors
        restTemplate.getInterceptors().add(new CustomLoggingInterceptor());
        
        return restTemplate;
    }
}
```

### 7.3 Environment-Specific Configuration

**application-dev.properties:**
```properties
api.framework.enableMocking=true
api.framework.connectionTimeoutMs=1000
logging.level.com.company.apiframework=DEBUG
```

**application-prod.properties:**
```properties
api.framework.enableMocking=false
api.framework.connectionTimeoutMs=5000
logging.level.com.company.apiframework=WARN
```

## 🏥 Step 8: Health Monitoring

### 8.1 Health Check Endpoint

```java
@RestController
public class ApiHealthController {
    
    @Autowired
    private ApiService apiService;
    
    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        // Check framework health
        boolean frameworkHealthy = apiService.isHealthy();
        health.put("apiFramework", frameworkHealthy);
        
        // Get configuration summary
        Map<String, Object> config = apiService.getConfigurationSummary();
        health.put("configuration", config);
        
        // Check external services
        health.put("externalServices", checkExternalServices());
        
        HttpStatus status = frameworkHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(health);
    }
    
    private Map<String, String> checkExternalServices() {
        Map<String, String> services = new HashMap<>();
        
        // Add your external service health checks
        services.put("paymentGateway", checkPaymentGateway());
        services.put("userService", checkUserService());
        
        return services;
    }
    
    private String checkPaymentGateway() {
        try {
            ApiRequest request = ApiRequest.builder()
                .url("https://payment.gateway.com/health")
                .method("GET")
                .build();
            
            ApiResponse<String> response = apiService.executeRest(request, String.class);
            return response.isSuccess() ? "UP" : "DOWN";
        } catch (Exception e) {
            return "DOWN";
        }
    }
    
    private String checkUserService() {
        // Similar implementation
        return "UP";
    }
}
```

## 🚨 Step 9: Troubleshooting Common Issues

### 9.1 RestTemplate Bean Not Found

**Error:**
```
No qualifying bean of type 'RestTemplate' available
```

**Solution:**
```java
// Make sure you're importing the framework configuration
@SpringBootApplication
@ComponentScan(basePackages = {"com.company.apiframework", "your.package"})
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

### 9.2 Mocking Not Working

**Error:**
```
Real HTTP calls being made during tests
```

**Solution:**
```java
// Ensure mocking is enabled in test properties
@TestPropertySource(properties = {"api.framework.enableMocking=true"})
public class YourTest {
    
    @BeforeEach
    void setUp() {
        mockApiService.clearMockResponses(); // Clear between tests
    }
}
```

### 9.3 Connection Timeouts

**Error:**
```
java.net.SocketTimeoutException: Read timed out
```

**Solution:**
```java
// Use appropriate RestTemplate bean for your use case
@Qualifier("batchApiRestTemplate")  // For slow operations
private RestTemplate batchTemplate;

// Or create custom configuration
@Bean
public RestTemplate longTimeoutRestTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
    factory.setReadTimeout(60000); // 60 seconds
    restTemplate.setRequestFactory(factory);
    return restTemplate;
}
```

## 🎓 Step 10: Next Steps

### 10.1 Explore Advanced Features

1. **SOAP API Integration:**
   ```java
   ApiRequest soapRequest = ApiRequest.builder()
       .url("http://soap.service.com/endpoint")
       .method("POST")
       .soapAction("GetData")
       .body("<soap:Envelope>...</soap:Envelope>")
       .build();
   
   ApiResponse<String> response = apiService.executeSoap(soapRequest, String.class);
   ```

2. **Automatic Protocol Detection:**
   ```java
   // Framework automatically detects REST vs SOAP
   ApiResponse<Object> response = apiService.executeAuto(request, Object.class);
   ```

3. **Custom API Mocks for Complex Scenarios:**
   ```java
   // Create realistic mocks for your specific APIs
   MyCustomApiMock customMock = new MyCustomApiMock();
   mockApiService.registerCustomMock("my-api", customMock);
   ```

### 10.2 Read Additional Documentation

- 📖 **README.md** - Complete framework overview
- 🔍 **JavaDoc** - Detailed API documentation
- 🧪 **Test Examples** - Look at existing test files for patterns

### 10.3 Join the Community

- 💬 Ask questions in team chat
- 🐛 Report issues via team issue tracker
- 💡 Suggest improvements and new features

## ✅ Checklist: You're Ready!

After completing this guide, you should be able to:

- [ ] Build and run the project successfully
- [ ] Create a basic service using ApiService
- [ ] Write tests with mock responses
- [ ] Understand URL pattern mapping
- [ ] Use appropriate RestTemplate beans
- [ ] Handle errors gracefully
- [ ] Monitor API health
- [ ] Troubleshoot common issues

## 🎉 Welcome to the Team!

You're now ready to build robust, scalable API integrations using the framework. Remember:

- **Start Simple**: Begin with basic REST calls
- **Use Mocks**: Test everything with realistic mocks
- **Choose Right Beans**: Pick appropriate RestTemplate beans for performance
- **Handle Errors**: Always plan for API failures
- **Monitor Health**: Implement health checks for production

Happy coding! 🚀 