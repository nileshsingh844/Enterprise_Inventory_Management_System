package com.enterprise.inventory.user.dto;

import com.enterprise.inventory.user.model.User; // Import User entity
import lombok.AllArgsConstructor; // Import Lombok annotation for generating constructor with all parameters
import lombok.Builder; // Import Lombok annotation for builder pattern
import lombok.Data; // Import Lombok annotation for generating getters, setters, toString, equals, and hashCode
import lombok.NoArgsConstructor; // Import Lombok annotation for generating no-argument constructor

import javax.validation.constraints.*; // Import validation annotations
import java.time.LocalDateTime; // Import LocalDateTime for timestamp handling
import java.util.Set; // Import Set interface for permissions

/**
 * Data Transfer Object (DTO) for User entity
 * This class is used for transferring user data between layers and for API responses
 * DTOs help separate the internal domain model from the external API representation
 * Password field is excluded for security reasons
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
public class UserDto {

    /**
     * Unique user identifier
     * Primary key from the database
     */
    private Long userId;

    /**
     * Unique username for authentication
     * Must be unique across all users
     */
    @NotBlank(message = "Username is required", groups = {CreateValidation.class, UpdateValidation.class})
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters", groups = {CreateValidation.class, UpdateValidation.class})
    private String username;

    /**
     * User's email address
     * Must be unique and valid email format
     */
    @NotBlank(message = "Email is required", groups = {CreateValidation.class, UpdateValidation.class})
    @Email(message = "Invalid email format", groups = {CreateValidation.class, UpdateValidation.class})
    @Size(max = 100, message = "Email must not exceed 100 characters", groups = {CreateValidation.class, UpdateValidation.class})
    private String email;

    /**
     * User's password (for create/update operations only)
     * Excluded from responses for security
     */
    @Size(min = 6, message = "Password must be at least 6 characters", groups = {CreateValidation.class, PasswordChangeValidation.class})
    private String password;

    /**
     * User's first name
     */
    @NotBlank(message = "First name is required", groups = {CreateValidation.class, UpdateValidation.class})
    @Size(max = 50, message = "First name must not exceed 50 characters", groups = {CreateValidation.class, UpdateValidation.class})
    private String firstName;

    /**
     * User's last name
     */
    @NotBlank(message = "Last name is required", groups = {CreateValidation.class, UpdateValidation.class})
    @Size(max = 50, message = "Last name must not exceed 50 characters", groups = {CreateValidation.class, UpdateValidation.class})
    private String lastName;

    /**
     * User's phone number
     */
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    /**
     * User's address
     */
    private String address;

    /**
     * User's city
     */
    @Size(max = 50, message = "City must not exceed 50 characters")
    private String city;

    /**
     * User's state or province
     */
    @Size(max = 50, message = "State must not exceed 50 characters")
    private String state;

    /**
     * User's postal code
     */
    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    /**
     * User's country
     */
    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;

    /**
     * User's account status
     */
    private String status;

    /**
     * User's role in the system
     */
    private String role;

    /**
     * Flag indicating if user account is locked
     */
    private Boolean accountNonLocked;

    /**
     * Flag indicating if user account is expired
     */
    private Boolean accountNonExpired;

    /**
     * Flag indicating if user credentials are expired
     */
    private Boolean credentialsNonExpired;

    /**
     * Flag indicating if user account is enabled
     */
    private Boolean enabled;

    /**
     * Timestamp when the user account was created
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the user account was last updated
     */
    private LocalDateTime updatedAt;

    /**
     * Timestamp of the last successful login
     */
    private LocalDateTime lastLoginAt;

    /**
     * Timestamp when the password was last changed
     */
    private LocalDateTime passwordChangedAt;

    /**
     * Number of failed login attempts
     */
    private Integer failedLoginAttempts;

    /**
     * Timestamp when the account was locked
     */
    private LocalDateTime lockedAt;

    /**
     * User's permissions or authorities
     */
    private Set<String> permissions;

    /**
     * Validation group for create operations
     * Used to specify which validations should be applied during creation
     */
    public interface CreateValidation {}

    /**
     * Validation group for update operations
     * Used to specify which validations should be applied during updates
     */
    public interface UpdateValidation {}

    /**
     * Validation group for password change operations
     * Used to specify which validations should be applied during password changes
     */
    public interface PasswordChangeValidation {}

    /**
     * Static factory method to convert User entity to UserDto
     * This method handles the mapping between domain model and DTO
     * Excludes sensitive information like password
     * 
     * @param user the User entity to convert
     * @return UserDto with mapped values
     */
    public static UserDto fromEntity(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .city(user.getCity())
                .state(user.getState())
                .postalCode(user.getPostalCode())
                .country(user.getCountry())
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .role(user.getRole() != null ? user.getRole().name() : null)
                .accountNonLocked(user.getAccountNonLocked())
                .accountNonExpired(user.getAccountNonExpired())
                .credentialsNonExpired(user.getCredentialsNonExpired())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .passwordChangedAt(user.getPasswordChangedAt())
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .lockedAt(user.getLockedAt())
                .permissions(user.getPermissions())
                .build();
    }

    /**
     * Get user's full name
     * Convenience method to get first and last name combined
     * 
     * @return Full name as "First Last"
     */
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return "";
    }

    /**
     * Check if user account is currently active
     * Convenience method to check if account is active and enabled
     * 
     * @return true if account is active and enabled, false otherwise
     */
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status) && Boolean.TRUE.equals(enabled);
    }
}
