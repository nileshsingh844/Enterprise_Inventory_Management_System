package com.enterprise.inventory.order;

import org.springframework.boot.SpringApplication; // Import Spring Boot's main application class
import org.springframework.boot.autoconfigure.SpringBootApplication; // Import Spring Boot auto-configuration annotation
import org.springframework.cloud.netflix.eureka.EnableEurekaClient; // Import Eureka Client enable annotation
import org.springframework.cloud.openfeign.EnableFeignClients; // Import Feign clients enable annotation

/**
 * Main application class for Order Service
 * This class serves as the entry point for the order processing microservice
 * 
 * @SpringBootApplication: Combines @Configuration, @EnableAutoConfiguration, and @ComponentScan
 * @EnableEurekaClient: Enables Eureka client functionality for service discovery
 * @EnableFeignClients: Enables Feign clients for inter-service communication
 */
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
public class OrderServiceApplication {

    /**
     * Main method - entry point for the Spring Boot application
     * SpringApplication.run() boots up the Spring application context with all configured components
     * 
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        // Start the Spring Boot application with Order Service capabilities
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
