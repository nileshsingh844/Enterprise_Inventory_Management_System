package com.enterprise.inventory.user.service;

import com.enterprise.inventory.user.dto.AuthRequest; // Import AuthRequest DTO
import com.enterprise.inventory.user.dto.AuthResponse; // Import AuthResponse DTO
import com.enterprise.inventory.user.dto.UserDto; // Import UserDto
import com.enterprise.inventory.user.model.User; // Import User entity
import com.enterprise.inventory.user.repository.UserRepository; // Import User repository
import lombok.extern.slf4j.Slf4j; // Import Lombok's SLF4J annotation for logging
import org.springframework.beans.factory.annotation.Autowired; // Import Spring's Autowired annotation
import org.springframework.security.authentication.AuthenticationManager; // Import AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException; // Import BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Import authentication token
import org.springframework.security.core.Authentication; // Import Authentication interface
import org.springframework.security.core.AuthenticationException; // Import AuthenticationException
import org.springframework.security.core.userdetails.UserDetails; // Import UserDetails
import org.springframework.security.crypto.password.PasswordEncoder; // Import PasswordEncoder
import org.springframework.stereotype.Service; // Import Spring's Service annotation
import org.springframework.transaction.annotation.Transactional; // Import Transactional annotation

import java.time.LocalDateTime; // Import LocalDateTime for timestamp handling
import java.util.HashSet; // Import HashSet for roles
import java.util.Set; // Import Set interface
import java.util.stream.Collectors; // Import Stream utilities

/**
 * Authentication Service class
 * This class handles authentication operations including login, token generation, and user validation
 * Integrates with Spring Security for secure authentication
 * 
 * @Service: Marks this class as a Spring service component
 * @Slf4j: Lombok annotation to add SLF4J logging capabilities
 * @Transactional: Enables transaction management for all methods
 */
@Service
@Transactional
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    /**
     * Constructor-based dependency injection
     * Spring automatically injects the required beans
     * 
     * @param authenticationManager the Spring Security authentication manager
     * @param userService the user service for user operations
     * @param tokenProvider the JWT token provider for token operations
     * @param userRepository the user repository for data operations
     */
    @Autowired
    public AuthService(AuthenticationManager authenticationManager, 
                     UserService userService, 
                     JwtTokenProvider tokenProvider,
                     UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    /**
     * Authenticate a user and generate JWT token
     * Validates credentials and returns authentication response with token
     * 
     * @param authRequest the authentication request containing username and password
     * @return AuthResponse containing JWT token and user information
     * @throws BadCredentialsException if authentication fails
     */
    public AuthResponse authenticateUser(AuthRequest authRequest) {
        log.info("Authenticating user: {}", authRequest.getUsername());
        
        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    authRequest.getUsername(), 
                    authRequest.getPassword()
                )
            );

            // Get user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            // Get user entity for additional information
            User user = userRepository.findByUsernameOrEmail(authRequest.getUsername(), authRequest.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            // Check if user account is active and not locked
            if (!user.isActive()) {
                log.warn("User account is not active: {}", authRequest.getUsername());
                throw new BadCredentialsException("User account is not active");
            }

            if (!user.getAccountNonLocked()) {
                log.warn("User account is locked: {}", authRequest.getUsername());
                throw new BadCredentialsException("User account is locked");
            }

            // Generate JWT token
            String token = tokenProvider.generateToken(authentication);
            
            // Generate refresh token
            String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());
            
            // Update last login timestamp
            userService.updateLastLogin(user.getUsername());
            
            // Get user roles
            Set<String> roles = getRoles(user);
            
            // Build authentication response
            AuthResponse response = AuthResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .expiresAt(LocalDateTime.now().plusSeconds(tokenProvider.getJwtExpirationInMs() / 1000))
                    .user(UserDto.fromEntity(user))
                    .roles(roles)
                    .refreshToken(refreshToken)
                    .message("Authentication successful")
                    .build();
            
            log.info("Successfully authenticated user: {}", authRequest.getUsername());
            
            return response;
            
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user: {}", authRequest.getUsername());
            
            // Increment failed login attempts
            try {
                userService.incrementFailedLoginAttempts(authRequest.getUsername());
            } catch (Exception ex) {
                log.error("Error incrementing failed login attempts for user: {}", authRequest.getUsername(), ex);
            }
            
            throw new BadCredentialsException("Invalid username or password", e);
        }
    }

    /**
     * Refresh JWT token using refresh token
     * Validates refresh token and generates new access token
     * 
     * @param refreshToken the refresh token
     * @return AuthResponse containing new JWT token
     * @throws BadCredentialsException if refresh token is invalid
     */
    public AuthResponse refreshToken(String refreshToken) {
        log.debug("Refreshing token");
        
        try {
            // Validate refresh token
            if (!tokenProvider.validateToken(refreshToken)) {
                log.warn("Invalid refresh token");
                throw new BadCredentialsException("Invalid refresh token");
            }

            // Extract username from refresh token
            String username = tokenProvider.getUsernameFromToken(refreshToken);
            
            // Load user details
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            // Check if user account is still active
            if (!user.isActive()) {
                log.warn("User account is not active: {}", username);
                throw new BadCredentialsException("User account is not active");
            }

            // Generate new access token
            String newToken = tokenProvider.generateToken(username);
            
            // Generate new refresh token
            String newRefreshToken = tokenProvider.generateRefreshToken(username);
            
            // Get user roles
            Set<String> roles = getRoles(user);
            
            // Build authentication response
            AuthResponse response = AuthResponse.builder()
                    .token(newToken)
                    .tokenType("Bearer")
                    .expiresAt(LocalDateTime.now().plusSeconds(tokenProvider.getJwtExpirationInMs() / 1000))
                    .user(UserDto.fromEntity(user))
                    .roles(roles)
                    .refreshToken(newRefreshToken)
                    .message("Token refreshed successfully")
                    .build();
            
            log.debug("Successfully refreshed token for user: {}", username);
            
            return response;
            
        } catch (Exception e) {
            log.warn("Token refresh failed", e);
            throw new BadCredentialsException("Failed to refresh token", e);
        }
    }

    /**
     * Validate JWT token
     * Checks if the token is valid and not expired
     * 
     * @param token the JWT token to validate
     * @return true if token is valid, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        log.debug("Validating token");
        
        try {
            // Validate token format and signature
            if (!tokenProvider.validateToken(token)) {
                log.debug("Token validation failed: invalid format or signature");
                return false;
            }

            // Extract username from token
            String username = tokenProvider.getUsernameFromToken(token);
            
            // Check if user still exists and is active
            User user = userRepository.findByUsername(username)
                    .orElse(null);
            
            if (user == null || !user.isActive()) {
                log.debug("Token validation failed: user not found or inactive");
                return false;
            }

            log.debug("Token validation successful for user: {}", username);
            return true;
            
        } catch (Exception e) {
            log.debug("Token validation failed", e);
            return false;
        }
    }

    /**
     * Get user information from JWT token
     * Extracts user details from valid token without authentication
     * 
     * @param token the JWT token
     * @return UserDto containing user information
     * @throws BadCredentialsException if token is invalid
     */
    @Transactional(readOnly = true)
    public UserDto getUserFromToken(String token) {
        log.debug("Getting user from token");
        
        try {
            // Validate token
            if (!validateToken(token)) {
                throw new BadCredentialsException("Invalid token");
            }

            // Extract username from token
            String username = tokenProvider.getUsernameFromToken(token);
            
            // Get user details
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BadCredentialsException("User not found"));
            
            log.debug("Successfully retrieved user from token: {}", username);
            
            return UserDto.fromEntity(user);
            
        } catch (Exception e) {
            log.warn("Failed to get user from token", e);
            throw new BadCredentialsException("Failed to get user from token", e);
        }
    }

    /**
     * Logout user
     * In a real implementation, this would invalidate the token on the server side
     * For JWT, this is typically handled by the client discarding the token
     * 
     * @param token the JWT token to logout
     * @return success message
     */
    public String logout(String token) {
        log.debug("Logging out user");
        
        try {
            // Extract username from token for logging
            String username = tokenProvider.getUsernameFromToken(token);
            log.info("User logged out: {}", username);
            
            // In a real implementation, you might:
            // 1. Add the token to a blacklist
            // 2. Invalidate the token in a token store
            // 3. Record the logout event
            
            return "Logout successful";
            
        } catch (Exception e) {
            log.warn("Logout failed", e);
            return "Logout failed";
        }
    }

    /**
     * Get roles for a user
     * Extracts roles and permissions from user entity
     * 
     * @param user the user to get roles for
     * @return Set of role strings
     */
    private Set<String> getRoles(User user) {
        Set<String> roles = new HashSet<>();
        
        // Add primary role
        roles.add("ROLE_" + user.getRole().name());
        
        // Add permissions if available
        if (user.getPermissions() != null) {
            roles.addAll(user.getPermissions());
        }
        
        return roles;
    }

    /**
     * Check if token is close to expiration
     * Used by client to determine when to refresh token
     * 
     * @param token the JWT token
     * @return true if token is close to expiration, false otherwise
     */
    public boolean isTokenCloseToExpiration(String token) {
        try {
            return tokenProvider.isTokenCloseToExpiration(token);
        } catch (Exception e) {
            log.debug("Error checking token expiration proximity", e);
            return true;
        }
    }
}
