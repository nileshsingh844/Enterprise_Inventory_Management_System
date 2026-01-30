package com.enterprise.inventory.gateway;

import org.springframework.boot.SpringApplication; // Import Spring Boot's main application class
import org.springframework.boot.autoconfigure.SpringBootApplication; // Import Spring Boot auto-configuration annotation
import org.springframework.cloud.netflix.eureka.EnableEurekaClient; // Import Eureka Client enable annotation

/**
 * Main application class for API Gateway
 * This class serves as the entry point for the API Gateway service
 * 
 * @SpringBootApplication: Combines @Configuration, @EnableAutoConfiguration, and @ComponentScan
 * @EnableEurekaClient: Enables Eureka client functionality for service discovery
 */
@SpringBootApplication
@EnableEurekaClient
public class GatewayApplication {

    /**
     * Main method - entry point for the Spring Boot application
     * SpringApplication.run() boots up the Spring application context with Gateway capabilities
     * 
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        // Start the Spring Boot application with API Gateway and Eureka Client capabilities
        SpringApplication.run(GatewayApplication.class, args);
    }
}
