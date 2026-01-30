package com.enterprise.inventory.user.model;

import lombok.AllArgsConstructor; // Import Lombok annotation for generating constructor with all parameters
import lombok.Builder; // Import Lombok annotation for builder pattern
import lombok.Data; // Import Lombok annotation for generating getters, setters, toString, equals, and hashCode
import lombok.NoArgsConstructor; // Import Lombok annotation for generating no-argument constructor

import javax.persistence.*; // Import JPA annotations for entity mapping
import javax.validation.constraints.*; // Import validation annotations
import java.time.LocalDateTime; // Import LocalDateTime for timestamp handling
import java.util.Set; // Import Set interface for roles

/**
 * User entity representing system users
 * This JPA entity maps to the 'users' table in the database
 * 
 * @Entity: Marks this class as a JPA entity
 * @Table: Specifies the database table name
 * @Data: Lombok annotation to generate getters, setters, toString, equals, and hashCode
 * @Builder: Lombok annotation to generate builder pattern
 * @NoArgsConstructor: Lombok annotation to generate no-argument constructor
 * @AllArgsConstructor: Lombok annotation to generate constructor with all parameters
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * Primary key for the User entity
     * Auto-generated sequence value for unique identification
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    /**
     * Unique username for authentication
     * Must be unique across all users
     */
    @Column(name = "username", unique = true, nullable = false, length = 50)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    /**
     * User's email address
     * Must be unique and valid email format
     */
    @Column(name = "email", unique = true, nullable = false, length = 100)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    /**
     * User's password (encrypted)
     * Stored as encrypted hash, not plain text
     */
    @Column(name = "password", nullable = false)
    @NotBlank(message = "Password is required")
    private String password;

    /**
     * User's first name
     */
    @Column(name = "first_name", nullable = false, length = 50)
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    /**
     * User's last name
     */
    @Column(name = "last_name", nullable = false, length = 50)
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    /**
     * User's phone number
     * Optional field for contact information
     */
    @Column(name = "phone_number", length = 20)
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    /**
     * User's address
     * Optional field for shipping and billing
     */
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    /**
     * User's city
     */
    @Column(name = "city", length = 50)
    @Size(max = 50, message = "City must not exceed 50 characters")
    private String city;

    /**
     * User's state or province
     */
    @Column(name = "state", length = 50)
    @Size(max = 50, message = "State must not exceed 50 characters")
    private String state;

    /**
     * User's postal code
     */
    @Column(name = "postal_code", length = 20)
    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    /**
     * User's country
     */
    @Column(name = "country", length = 50)
    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;

    /**
     * User's account status
     * Determines if the user account is active, inactive, or suspended
     */
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    /**
     * User's role in the system
     * Determines permissions and access levels
     */
    @Column(name = "role", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    /**
     * Flag indicating if user account is locked
     * Used for security purposes after multiple failed login attempts
     */
    @Column(name = "account_non_locked", nullable = false)
    private Boolean accountNonLocked;

    /**
     * Flag indicating if user account is expired
     * Used for temporary accounts or subscription-based access
     */
    @Column(name = "account_non_expired", nullable = false)
    private Boolean accountNonExpired;

    /**
     * Flag indicating if user credentials are expired
     * Used for password expiration policies
     */
    @Column(name = "credentials_non_expired", nullable = false)
    private Boolean credentialsNonExpired;

    /**
     * Flag indicating if user account is enabled
     * Used for enabling/disabling accounts without deleting them
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    /**
     * Timestamp when the user account was created
     * Automatically set when the record is created
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the user account was last updated
     * Automatically updated when the record is modified
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Timestamp of the last successful login
     * Used for tracking user activity and security monitoring
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * Timestamp when the password was last changed
     * Used for password expiration policies
     */
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    /**
     * Number of failed login attempts
     * Used for account lockout security feature
     */
    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts;

    /**
     * Timestamp when the account was locked
     * Used for temporary account lockouts
     */
    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    /**
     * User's permissions or authorities
     * Additional permissions beyond role-based access
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_permissions", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "permission", length = 100)
    private Set<String> permissions;

    /**
     * JPA lifecycle callback - called before entity is persisted
     * Sets the creation timestamp and default values
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        passwordChangedAt = LocalDateTime.now();
        
        // Set default values if not specified
        if (status == null) {
            status = UserStatus.ACTIVE;
        }
        if (role == null) {
            role = UserRole.CUSTOMER;
        }
        if (accountNonLocked == null) {
            accountNonLocked = true;
        }
        if (accountNonExpired == null) {
            accountNonExpired = true;
        }
        if (credentialsNonExpired == null) {
            credentialsNonExpired = true;
        }
        if (enabled == null) {
            enabled = true;
        }
        if (failedLoginAttempts == null) {
            failedLoginAttempts = 0;
        }
    }

    /**
     * JPA lifecycle callback - called before entity is updated
     * Updates the modification timestamp
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Get user's full name
     * Convenience method to get first and last name combined
     * 
     * @return Full name as "First Last"
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Check if user account is currently locked
     * Convenience method to check lock status
     * 
     * @return true if account is locked, false otherwise
     */
    public boolean isAccountLocked() {
        return !accountNonLocked;
    }

    /**
     * Check if user account is currently active
     * Convenience method to check if account is active and enabled
     * 
     * @return true if account is active and enabled, false otherwise
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE && enabled;
    }

    /**
     * Enum for user status
     * Defines the possible states of a user account
     */
    public enum UserStatus {
        ACTIVE("Active"),           // User account is active and can be used
        INACTIVE("Inactive"),       // User account is inactive (temporary)
        SUSPENDED("Suspended"),     // User account is suspended (administrative action)
        TERMINATED("Terminated");   // User account is terminated (permanent)

        private final String displayName;

        UserStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Enum for user roles
     * Defines the possible roles and their associated permissions
     */
    public enum UserRole {
        CUSTOMER("Customer"),       // Regular customer with basic permissions
        MANAGER("Manager"),         // Manager with elevated permissions
        ADMIN("Administrator"),     // Administrator with full system access
        SUPER_ADMIN("Super Admin"); // Super administrator with all permissions

        private final String displayName;

        UserRole(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
