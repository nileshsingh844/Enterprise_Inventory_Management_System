package com.enterprise.inventory.service;

import org.springframework.boot.SpringApplication; // Import Spring Boot's main application class
import org.springframework.boot.autoconfigure.SpringBootApplication; // Import Spring Boot auto-configuration annotation
import org.springframework.cloud.netflix.eureka.EnableEurekaClient; // Import Eureka Client enable annotation

/**
 * Main application class for Inventory Service
 * This class serves as the entry point for the inventory management microservice
 * 
 * @SpringBootApplication: Combines @Configuration, @EnableAutoConfiguration, and @ComponentScan
 * @EnableEurekaClient: Enables Eureka client functionality for service discovery
 */
@SpringBootApplication
@EnableEurekaClient
public class InventoryServiceApplication {

    /**
     * Main method - entry point for the Spring Boot application
     * SpringApplication.run() boots up the Spring application context with all configured components
     * 
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        // Start the Spring Boot application with Inventory Service capabilities
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
