package com.enterprise.inventory.user.security;

import lombok.extern.slf4j.Slf4j; // Import Lombok's SLF4J annotation for logging
import org.springframework.beans.factory.annotation.Autowired; // Import Spring's Autowired annotation
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Import authentication token
import org.springframework.security.core.context.SecurityContextHolder; // Import security context holder
import org.springframework.security.core.userdetails.UserDetails; // Import Spring Security UserDetails
import org.springframework.security.core.userdetails.UserDetailsService; // Import Spring Security UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource; // Import web authentication details
import org.springframework.stereotype.Component; // Import Spring's Component annotation
import org.springframework.util.StringUtils; // Import Spring's StringUtils utility
import org.springframework.web.filter.OncePerRequestFilter; // Import Spring's OncePerRequestFilter

import javax.servlet.FilterChain; // Import servlet FilterChain
import javax.servlet.ServletException; // Import ServletException
import javax.servlet.http.HttpServletRequest; // Import HttpServletRequest
import javax.servlet.http.HttpServletResponse; // Import HttpServletResponse
import java.io.IOException; // Import IOException

/**
 * JWT Authentication Filter class
 * This filter intercepts incoming requests and validates JWT tokens
 * Sets the authentication context if the token is valid
 * 
 * @Component: Marks this class as a Spring component
 * @Slf4j: Lombok annotation to add SLF4J logging capabilities
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    /**
     * Constructor-based dependency injection
     * Spring automatically injects the required beans
     * 
     * @param tokenProvider the JWT token provider for token operations
     * @param userDetailsService the user details service for loading user details
     */
    @Autowired
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Filter method that processes each incoming request
     * Extracts JWT token from request, validates it, and sets authentication
     * 
     * @param request the incoming HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain for processing the request
     * @throws ServletException if servlet-related errors occur
     * @throws IOException if I/O errors occur
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Extract JWT token from the request
            String jwt = getJwtFromRequest(request);
            
            // Validate token and set authentication if valid
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // Extract username from token
                String username = tokenProvider.getUsernameFromToken(jwt);
                
                // Load user details
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // Create authentication token
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, 
                        null, 
                        userDetails.getAuthorities()
                    );
                
                // Set authentication details
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("Set authentication for user: {}", username);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }
        
        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from the HTTP request
     * Looks for the token in the Authorization header
     * 
     * @param request the HTTP request
     * @return JWT token if found, null otherwise
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // Get Authorization header
        String bearerToken = request.getHeader("Authorization");
        
        // Check if header contains Bearer token
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Extract token (remove "Bearer " prefix)
            return bearerToken.substring(7);
        }
        
        // No token found
        return null;
    }

    /**
     * Determine if the filter should be applied to the request
     * Skip authentication for public endpoints like login and register
     * 
     * @param request the incoming HTTP request
     * @return true if the filter should be applied, false otherwise
     * @throws ServletException if servlet-related errors occur
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // Skip authentication for public endpoints
        return path.startsWith("/api/auth/") || 
               path.startsWith("/api/public/") ||
               path.startsWith("/actuator/") ||
               path.startsWith("/h2-console/") ||
               path.equals("/health") ||
               path.equals("/favicon.ico");
    }
}
