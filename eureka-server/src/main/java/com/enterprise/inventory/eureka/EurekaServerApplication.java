package com.enterprise.inventory.eureka;

import org.springframework.boot.SpringApplication; // Import Spring Boot's main application class
import org.springframework.boot.autoconfigure.SpringBootApplication; // Import Spring Boot auto-configuration annotation
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer; // Import Eureka Server enable annotation

/**
 * Main application class for Eureka Server
 * This class serves as the entry point for the service discovery server
 * 
 * @SpringBootApplication: Combines @Configuration, @EnableAutoConfiguration, and @ComponentScan
 * @EnableEurekaServer: Enables Eureka server functionality for service discovery
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    /**
     * Main method - entry point for the Spring Boot application
     * SpringApplication.run() boots up the Spring application context
     * 
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        // Start the Spring Boot application with Eureka Server capabilities
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
