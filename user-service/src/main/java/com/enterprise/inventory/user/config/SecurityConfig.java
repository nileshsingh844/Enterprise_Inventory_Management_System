package com.enterprise.inventory.user.config;

import com.enterprise.inventory.user.security.JwtAuthenticationFilter; // Import JWT authentication filter
import com.enterprise.inventory.user.security.JwtTokenProvider; // Import JWT token provider
import lombok.RequiredArgsConstructor; // Import Lombok's RequiredArgsConstructor annotation
import org.springframework.context.annotation.Bean; // Import Bean annotation for Spring component definition
import org.springframework.context.annotation.Configuration; // Import Configuration annotation for Spring configuration class
import org.springframework.security.authentication.AuthenticationManager; // Import AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; // Import AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity; // Import method security
import org.springframework.security.config.annotation.web.builders.HttpSecurity; // Import HttpSecurity configuration
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // Import WebSecurity configuration
import org.springframework.security.config.http.SessionCreationPolicy; // Import session creation policy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Import BCrypt password encoder
import org.springframework.security.crypto.password.PasswordEncoder; // Import PasswordEncoder interface
import org.springframework.security.web.SecurityFilterChain; // Import SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Import authentication filter

/**
 * Security Configuration class
 * This class configures Spring Security for JWT-based authentication and authorization
 * Defines security rules, password encoding, and authentication filters
 * 
 * @Configuration: Marks this class as a Spring configuration class
 * @EnableWebSecurity: Enables Spring Security web security
 * @EnableGlobalMethodSecurity: Enables method-level security with pre/post annotations
 * @RequiredArgsConstructor: Lombok annotation to generate constructor with required fields
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider tokenProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configure password encoder bean
     * Uses BCrypt for secure password hashing
     * 
     * @return PasswordEncoder bean using BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configure authentication manager bean
     * Used for authenticating users during login
     * 
     * @param config the authentication configuration
     * @return AuthenticationManager bean
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configure security filter chain
     * Defines HTTP security rules, authentication filters, and authorization
     * 
     * @param http the HttpSecurity configuration object
     * @return SecurityFilterChain bean
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF as we're using JWT tokens
            .csrf().disable()
            
            // Configure session management to stateless
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            
            // Configure authorization rules
            .authorizeHttpRequests(authz -> authz
                // Allow public endpoints without authentication
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/health").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/favicon.ico").permitAll()
                
                // Require authentication for all other endpoints
                .anyRequest().authenticated()
            )
            
            // Add JWT authentication filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // Configure exception handling
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"" + authException.getMessage() + "\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"" + accessDeniedException.getMessage() + "\"}");
                })
            );

        return http.build();
    }

    /**
     * Configure CORS (Cross-Origin Resource Sharing)
     * Allows cross-origin requests from specified origins
     * 
     * @return CORS configuration bean
     */
    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        
        // Allow specific origins (configure appropriately for production)
        configuration.setAllowedOrigins(java.util.Arrays.asList("http://localhost:3000", "http://localhost:4200", "http://localhost:8080"));
        
        // Allow specific HTTP methods
        configuration.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Allow specific headers
        configuration.setAllowedHeaders(java.util.Arrays.asList("*"));
        
        // Allow credentials
        configuration.setAllowCredentials(true);
        
        // Set max age for pre-flight requests
        configuration.setMaxAge(3600L);
        
        // Create CORS configuration source
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
