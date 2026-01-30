package com.enterprise.inventory.user.dto;

import lombok.AllArgsConstructor; // Import Lombok annotation for generating constructor with all parameters
import lombok.Builder; // Import Lombok annotation for builder pattern
import lombok.Data; // Import Lombok annotation to generate getters, setters, toString, equals, and hashCode
import lombok.NoArgsConstructor; // Import Lombok annotation for generating no-argument constructor

import java.time.LocalDateTime; // Import LocalDateTime for timestamp handling
import java.util.Set; // Import Set interface for roles

/**
 * Authentication Response DTO
 * This class is used for login responses containing JWT token and user information
 * 
 * @Data: Lombok annotation to generate getters, setters, toString, equals, and hashCode
 * @Builder: Lombok annotation to generate builder pattern
 * @NoArgsConstructor: Lombok annotation to generate no-argument constructor
 * @AllArgsConstructor: Lombok annotation to generate constructor with all parameters
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /**
     * JWT (JSON Web Token) for authentication
     * This token is used for subsequent API calls
     */
    private String token;

    /**
     * Token type (usually "Bearer")
     * Used for HTTP Authorization header
     */
    private String tokenType = "Bearer";

    /**
     * Token expiration time
     * Timestamp when the token expires
     */
    private LocalDateTime expiresAt;

    /**
     * User information
     * Basic user details for client application
     */
    private UserDto user;

    /**
     * User roles and permissions
     * Set of roles and permissions for authorization
     */
    private Set<String> roles;

    /**
     * Token refresh token (optional)
     * Used for obtaining new JWT tokens without re-authentication
     */
    private String refreshToken;

    /**
     * Message or status
     * Additional information about the authentication result
     */
    private String message;
}
