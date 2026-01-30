package com.enterprise.inventory.user.controller;

import com.enterprise.inventory.user.dto.AuthRequest; // Import AuthRequest DTO
import com.enterprise.inventory.user.dto.AuthResponse; // Import AuthResponse DTO
import com.enterprise.inventory.user.dto.UserDto; // Import UserDto
import com.enterprise.inventory.user.service.AuthService; // Import AuthService
import lombok.extern.slf4j.Slf4j; // Import Lombok's SLF4J annotation for logging
import org.springframework.beans.factory.annotation.Autowired; // Import Spring's Autowired annotation
import org.springframework.http.HttpStatus; // Import HTTP status codes
import org.springframework.http.ResponseEntity; // Import ResponseEntity for HTTP responses
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Import AuthenticationPrincipal annotation
import org.springframework.security.core.userdetails.UserDetails; // Import UserDetails
import org.springframework.validation.annotation.Validated; // Import validation annotations
import org.springframework.web.bind.annotation.*; // Import REST controller annotations

import javax.servlet.http.HttpServletRequest; // Import HttpServletRequest
import javax.validation.Valid; // Import validation annotation
import javax.validation.constraints.NotBlank; // Import validation annotation

/**
 * REST Controller for Authentication operations
 * This class handles HTTP requests for user authentication, token management, and authorization
 * 
 * @RestController: Combines @Controller and @ResponseBody for REST APIs
 * @RequestMapping: Base path for all endpoints in this controller
 * @Slf4j: Lombok annotation to add SLF4J logging capabilities
 * @Validated: Enables validation for method parameters
 */
@RestController
@RequestMapping("/api/auth")
@Validated
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Constructor-based dependency injection
     * Spring automatically injects the AuthService bean
     * 
     * @param authService the service for authentication business logic
     */
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authenticate a user and generate JWT token
     * HTTP POST /api/auth/login
     * 
     * @param authRequest the authentication request containing username and password
     * @return ResponseEntity with authentication response and HTTP 200 status
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody AuthRequest authRequest) {
        log.info("REST request to authenticate user: {}", authRequest.getUsername());
        
        AuthResponse authResponse = authService.authenticateUser(authRequest);
        
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Refresh JWT token using refresh token
     * HTTP POST /api/auth/refresh
     * 
     * @param refreshToken the refresh token
     * @return ResponseEntity with new authentication response and HTTP 200 status
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody String refreshToken) {
        log.info("REST request to refresh token");
        
        AuthResponse authResponse = authService.refreshToken(refreshToken);
        
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Validate JWT token
     * HTTP POST /api/auth/validate
     * 
     * @param request the HTTP request containing the token in Authorization header
     * @return ResponseEntity with validation result and HTTP 200 status
     */
    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateToken(HttpServletRequest request) {
        log.debug("REST request to validate token");
        
        // Extract token from Authorization header
        String token = extractTokenFromRequest(request);
        
        if (token == null) {
            return ResponseEntity.badRequest().body(false);
        }
        
        boolean isValid = authService.validateToken(token);
        
        return ResponseEntity.ok(isValid);
    }

    /**
     * Get current user information from JWT token
     * HTTP GET /api/auth/me
     * 
     * @param userDetails the authenticated user details
     * @return ResponseEntity with user information and HTTP 200 status
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        log.debug("REST request to get current user: {}", userDetails.getUsername());
        
        UserDto user = authService.getUserFromToken(extractTokenFromRequest(null));
        
        return ResponseEntity.ok(user);
    }

    /**
     * Get current user information using token
     * HTTP GET /api/auth/user
     * 
     * @param request the HTTP request containing the token in Authorization header
     * @return ResponseEntity with user information and HTTP 200 status
     */
    @GetMapping("/user")
    public ResponseEntity<UserDto> getUserFromToken(HttpServletRequest request) {
        log.debug("REST request to get user from token");
        
        String token = extractTokenFromRequest(request);
        
        if (token == null) {
            return ResponseEntity.badRequest().build();
        }
        
        UserDto user = authService.getUserFromToken(token);
        
        return ResponseEntity.ok(user);
    }

    /**
     * Check if token is close to expiration
     * HTTP GET /api/auth/token-expiry
     * 
     * @param request the HTTP request containing the token in Authorization header
     * @return ResponseEntity with expiry status and HTTP 200 status
     */
    @GetMapping("/token-expiry")
    public ResponseEntity<Boolean> isTokenCloseToExpiration(HttpServletRequest request) {
        log.debug("REST request to check token expiry");
        
        String token = extractTokenFromRequest(request);
        
        if (token == null) {
            return ResponseEntity.badRequest().build();
        }
        
        boolean isCloseToExpiry = authService.isTokenCloseToExpiration(token);
        
        return ResponseEntity.ok(isCloseToExpiry);
    }

    /**
     * Logout user
     * HTTP POST /api/auth/logout
     * 
     * @param request the HTTP request containing the token in Authorization header
     * @return ResponseEntity with logout message and HTTP 200 status
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        log.info("REST request to logout user");
        
        String token = extractTokenFromRequest(request);
        
        if (token == null) {
            return ResponseEntity.badRequest().body("No token provided");
        }
        
        String message = authService.logout(token);
        
        return ResponseEntity.ok(message);
    }

    /**
     * Health check endpoint for authentication service
     * HTTP GET /api/auth/health
     * 
     * @return ResponseEntity with health status and HTTP 200 status
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.debug("Health check requested for Auth Service");
        
        return ResponseEntity.ok("Auth Service is healthy");
    }

    /**
     * Check if user is authenticated
     * HTTP GET /api/auth/authenticated
     * 
     * @param userDetails the authenticated user details (optional)
     * @return ResponseEntity with authentication status and HTTP 200 status
     */
    @GetMapping("/authenticated")
    public ResponseEntity<Boolean> isAuthenticated(@AuthenticationPrincipal UserDetails userDetails) {
        log.debug("REST request to check authentication status");
        
        boolean isAuthenticated = userDetails != null;
        
        return ResponseEntity.ok(isAuthenticated);
    }

    /**
     * Get user roles and permissions
     * HTTP GET /api/auth/roles
     * 
     * @param userDetails the authenticated user details
     * @return ResponseEntity with user roles and HTTP 200 status
     */
    @GetMapping("/roles")
    public ResponseEntity<java.util.Set<String>> getUserRoles(@AuthenticationPrincipal UserDetails userDetails) {
        log.debug("REST request to get user roles for: {}", userDetails.getUsername());
        
        java.util.Set<String> roles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(java.util.stream.Collectors.toSet());
        
        return ResponseEntity.ok(roles);
    }

    /**
     * Check if user has specific role
     * HTTP GET /api/auth/has-role
     * 
     * @param userDetails the authenticated user details
     * @param role the role to check
     * @return ResponseEntity with role check result and HTTP 200 status
     */
    @GetMapping("/has-role")
    public ResponseEntity<Boolean> hasRole(@AuthenticationPrincipal UserDetails userDetails,
                                          @RequestParam @NotBlank(message = "Role cannot be blank") String role) {
        log.debug("REST request to check role '{}' for user: {}", role, userDetails.getUsername());
        
        boolean hasRole = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role) || 
                                     authority.getAuthority().equals("ROLE_" + role));
        
        return ResponseEntity.ok(hasRole);
    }

    /**
     * Check if user has specific permission
     * HTTP GET /api/auth/has-permission
     * 
     * @param userDetails the authenticated user details
     * @param permission the permission to check
     * @return ResponseEntity with permission check result and HTTP 200 status
     */
    @GetMapping("/has-permission")
    public ResponseEntity<Boolean> hasPermission(@AuthenticationPrincipal UserDetails userDetails,
                                                @RequestParam @NotBlank(message = "Permission cannot be blank") String permission) {
        log.debug("REST request to check permission '{}' for user: {}", permission, userDetails.getUsername());
        
        boolean hasPermission = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(permission));
        
        return ResponseEntity.ok(hasPermission);
    }

    /**
     * Get token information (for debugging purposes)
     * HTTP GET /api/auth/token-info
     * 
     * @param request the HTTP request containing the token in Authorization header
     * @return ResponseEntity with token information and HTTP 200 status
     */
    @GetMapping("/token-info")
    public ResponseEntity<java.util.Map<String, Object>> getTokenInfo(HttpServletRequest request) {
        log.debug("REST request to get token information");
        
        String token = extractTokenFromRequest(request);
        
        if (token == null) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            // Create a simple map with token information
            java.util.Map<String, Object> tokenInfo = new java.util.HashMap<>();
            tokenInfo.put("valid", authService.validateToken(token));
            tokenInfo.put("closeToExpiry", authService.isTokenCloseToExpiration(token));
            tokenInfo.put("tokenLength", token.length());
            
            return ResponseEntity.ok(tokenInfo);
            
        } catch (Exception e) {
            log.error("Error getting token information", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Extract JWT token from HTTP request
     * Looks for the token in the Authorization header
     * 
     * @param request the HTTP request
     * @return JWT token if found, null otherwise
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        
        String bearerToken = request.getHeader("Authorization");
        
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }
}
