package com.company.apiframework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main application class for the API Integration Framework
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.company.apiframework")
public class ApiFrameworkApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ApiFrameworkApplication.class, args);
    }
} 