package com.company.apiframework.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.company.apiframework.client.ApiCallback;
import com.company.apiframework.model.ApiRequest;
import com.company.apiframework.model.ApiResponse;
import com.company.apiframework.service.ApiService;

/**
 * Example usage of the API Integration Framework
 */
@Component
public class ExampleApiUsage {
    
    @Autowired
    private ApiService apiService;
    
    /**
     * Example REST API call
     */
    public void exampleRestCall() {
        // Create REST request
        ApiRequest request = ApiRequest.builder()
                .url("https://jsonplaceholder.typicode.com/posts/1")
                .method("GET")
                .header("Accept", "application/json")
                .build();
        
        // Execute synchronously
        ApiResponse<PostDto> response = apiService.executeRest(request, PostDto.class);
        
        if (response.isSuccess()) {
            System.out.println("REST call successful: " + response.getBody().getTitle());
        } else {
            System.err.println("REST call failed: " + response.getErrorMessage());
        }
    }
    
    /**
     * Example REST POST with body
     */
    public void exampleRestPost() {
        PostDto newPost = new PostDto();
        newPost.setTitle("New Post");
        newPost.setBody("This is a new post");
        newPost.setUserId(1);
        
        ApiRequest request = ApiRequest.builder()
                .url("https://jsonplaceholder.typicode.com/posts")
                .method("POST")
                .header("Content-Type", "application/json")
                .body(newPost)
                .build();
        
        ApiResponse<PostDto> response = apiService.executeRest(request, PostDto.class);
        
        if (response.isSuccess()) {
            System.out.println("POST successful, created post with ID: " + response.getBody().getId());
        } else {
            System.err.println("POST failed: " + response.getErrorMessage());
        }
    }
    
    /**
     * Example SOAP API call
     */
    public void exampleSoapCall() {
        String soapBody = 
            "<GetWeatherRequest>" +
            "  <City>London</City>" +
            "  <Country>UK</Country>" +
            "</GetWeatherRequest>";
        
        ApiRequest request = ApiRequest.builder()
                .url("http://example.com/weather-service")
                .method("POST")
                .soapAction("GetWeather")
                .body(soapBody)
                .build();
        
        ApiResponse<String> response = apiService.executeSoap(request);
        
        if (response.isSuccess()) {
            System.out.println("SOAP call successful: " + response.getBody());
        } else {
            System.err.println("SOAP call failed: " + response.getErrorMessage());
        }
    }
    
    /**
     * Example asynchronous API call
     */
    public void exampleAsyncCall() {
        ApiRequest request = ApiRequest.builder()
                .url("https://jsonplaceholder.typicode.com/posts/1")
                .method("GET")
                .build();
        
        apiService.executeAsync(request, PostDto.class, new ApiCallback<PostDto>() {
            @Override
            public void onSuccess(ApiResponse<PostDto> response) {
                System.out.println("Async call successful: " + response.getBody().getTitle());
            }
            
            @Override
            public void onError(ApiResponse<PostDto> response) {
                System.err.println("Async call failed: " + response.getErrorMessage());
            }
            
            @Override
            public void onException(Exception exception) {
                System.err.println("Async call exception: " + exception.getMessage());
            }
        });
    }
    
    /**
     * Example using auto-detection
     */
    public void exampleAutoDetection() {
        // This will be detected as REST
        ApiRequest restRequest = ApiRequest.builder()
                .url("https://api.example.com/users")
                .method("GET")
                .build();
        
        ApiResponse<String> restResponse = apiService.executeAuto(restRequest, String.class);
        
        // This will be detected as SOAP
        ApiRequest soapRequest = ApiRequest.builder()
                .url("http://soap.example.com/service")
                .method("POST")
                .soapAction("GetData")
                .body("<soap:Envelope>...</soap:Envelope>")
                .build();
        
        ApiResponse<String> soapResponse = apiService.executeAuto(soapRequest, String.class);
    }
    
    /**
     * Example DTO class
     */
    public static class PostDto {
        private int id;
        private int userId;
        private String title;
        private String body;
        
        // Getters and Setters
        public int getId() {
            return id;
        }
        
        public void setId(int id) {
            this.id = id;
        }
        
        public int getUserId() {
            return userId;
        }
        
        public void setUserId(int userId) {
            this.userId = userId;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getBody() {
            return body;
        }
        
        public void setBody(String body) {
            this.body = body;
        }
    }
} 