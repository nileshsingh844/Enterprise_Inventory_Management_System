package com.enterprise.inventory.config;

import org.springframework.boot.SpringApplication; // Import Spring Boot's main application class
import org.springframework.boot.autoconfigure.SpringBootApplication; // Import Spring Boot auto-configuration annotation
import org.springframework.cloud.config.server.EnableConfigServer; // Import Config Server enable annotation
import org.springframework.cloud.netflix.eureka.EnableEurekaClient; // Import Eureka Client enable annotation

/**
 * Main application class for Config Server
 * This class serves as the entry point for the centralized configuration server
 * 
 * @SpringBootApplication: Combines @Configuration, @EnableAutoConfiguration, and @ComponentScan
 * @EnableConfigServer: Enables Spring Cloud Config Server functionality
 * @EnableEurekaClient: Enables Eureka client functionality for service discovery
 */
@SpringBootApplication
@EnableConfigServer
@EnableEurekaClient
public class ConfigServerApplication {

    /**
     * Main method - entry point for the Spring Boot application
     * SpringApplication.run() boots up the Spring application context with Config Server capabilities
     * 
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        // Start the Spring Boot application with Config Server and Eureka Client capabilities
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
