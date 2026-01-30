package com.enterprise.inventory.gateway.config;

import org.springframework.context.annotation.Bean; // Import Bean annotation for Spring component definition
import org.springframework.context.annotation.Configuration; // Import Configuration annotation for Spring configuration class
import org.springframework.web.cors.CorsConfiguration; // Import CORS configuration class
import org.springframework.web.cors.reactive.CorsWebFilter; // Import reactive CORS web filter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource; // Import URL-based CORS configuration source

import java.util.Arrays; // Import Arrays utility class
import java.util.Collections; // Import Collections utility class

/**
 * Configuration class for API Gateway
 * This class defines additional configuration beans for the gateway, including CORS settings
 * 
 * @Configuration: Marks this class as a Spring configuration class
 */
@Configuration
public class GatewayConfig {

    /**
     * Bean definition for CORS (Cross-Origin Resource Sharing) web filter
     * This filter enables cross-origin requests to the API Gateway
     * 
     * @return CorsWebFilter configured with allowed origins, methods, and headers
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        // Create CORS configuration object
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Allow specific origins (configure appropriately for production security)
        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:4200"));
        
        // Allow all HTTP methods (GET, POST, PUT, DELETE, etc.)
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        
        // Allow all headers including custom headers
        corsConfig.setAllowedHeaders(Collections.singletonList("*"));
        
        // Allow credentials in requests (cookies, authorization headers)
        corsConfig.setAllowCredentials(true);
        
        // Set max age for pre-flight requests (in seconds)
        corsConfig.setMaxAge(3600L);
        
        // Create URL-based CORS configuration source
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // Apply CORS configuration to all paths
        source.registerCorsConfiguration("/**", corsConfig);
        
        // Return the CORS web filter with the configuration
        return new CorsWebFilter(source);
    }
}
