package com.enterprise.inventory.user.dto;

import lombok.AllArgsConstructor; // Import Lombok annotation for generating constructor with all parameters
import lombok.Builder; // Import Lombok annotation for builder pattern
import lombok.Data; // Import Lombok annotation for generating getters, setters, toString, equals, and hashCode
import lombok.NoArgsConstructor; // Import Lombok annotation for generating no-argument constructor

import javax.validation.constraints.NotBlank; // Import validation annotation

/**
 * Authentication Request DTO
 * This class is used for login requests containing user credentials
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
public class AuthRequest {

    /**
     * Username or email for authentication
     * Can be either username or email address
     */
    @NotBlank(message = "Username or email is required")
    private String username;

    /**
     * User's password
     * Plain text password that will be validated against stored hash
     */
    @NotBlank(message = "Password is required")
    private String password;
}
