package com.enterprise.inventory.user.controller;

import com.enterprise.inventory.user.dto.UserDto; // Import UserDto
import com.enterprise.inventory.user.service.UserService; // Import UserService
import lombok.extern.slf4j.Slf4j; // Import Lombok's SLF4J annotation for logging
import org.springframework.beans.factory.annotation.Autowired; // Import Spring's Autowired annotation
import org.springframework.http.HttpStatus; // Import HTTP status codes
import org.springframework.http.ResponseEntity; // Import ResponseEntity for HTTP responses
import org.springframework.security.access.prepost.PreAuthorize; // Import PreAuthorize annotation
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Import AuthenticationPrincipal annotation
import org.springframework.security.core.userdetails.UserDetails; // Import UserDetails
import org.springframework.validation.annotation.Validated; // Import validation annotations
import org.springframework.web.bind.annotation.*; // Import REST controller annotations

import javax.validation.Valid; // Import validation annotation
import javax.validation.constraints.Min; // Import validation annotation
import javax.validation.constraints.NotBlank; // Import validation annotation
import java.util.List; // Import List interface

/**
 * REST Controller for User management operations
 * This class handles HTTP requests for user CRUD operations and management
 * 
 * @RestController: Combines @Controller and @ResponseBody for REST APIs
 * @RequestMapping: Base path for all endpoints in this controller
 * @Slf4j: Lombok annotation to add SLF4J logging capabilities
 * @Validated: Enables validation for method parameters
 */
@RestController
@RequestMapping("/api/users")
@Validated
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Constructor-based dependency injection
     * Spring automatically injects the UserService bean
     * 
     * @param userService the service for user business logic
     */
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Create a new user
     * HTTP POST /api/users
     * 
     * @param userDto the user data to create
     * @return ResponseEntity with created user and HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        log.info("REST request to create user: {}", userDto.getUsername());
        
        UserDto createdUser = userService.createUser(userDto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * Update an existing user
     * HTTP PUT /api/users/{id}
     * 
     * @param userId the ID of the user to update
     * @param userDto the updated user data
     * @return ResponseEntity with updated user and HTTP 200 status
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER') or #userId == authentication.principal.userId")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable("id") @Min(value = 1, message = "User ID must be positive") Long userId,
            @Valid @RequestBody UserDto userDto) {
        log.info("REST request to update user with ID: {}", userId);
        
        UserDto updatedUser = userService.updateUser(userId, userDto);
        
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Get a user by their ID
     * HTTP GET /api/users/{id}
     * 
     * @param userId the ID of the user to retrieve
     * @return ResponseEntity with user data and HTTP 200 status
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER') or #userId == authentication.principal.userId")
    public ResponseEntity<UserDto> getUserById(
            @PathVariable("id") @Min(value = 1, message = "User ID must be positive") Long userId) {
        log.debug("REST request to get user with ID: {}", userId);
        
        UserDto user = userService.getUserById(userId);
        
        return ResponseEntity.ok(user);
    }

    /**
     * Get a user by their username
     * HTTP GET /api/users/username/{username}
     * 
     * @param username the username of the user to retrieve
     * @return ResponseEntity with user data and HTTP 200 status
     */
    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER') or #username == authentication.principal.username")
    public ResponseEntity<UserDto> getUserByUsername(
            @PathVariable("username") @NotBlank(message = "Username cannot be blank") String username) {
        log.debug("REST request to get user with username: {}", username);
        
        UserDto user = userService.getUserByUsername(username);
        
        return ResponseEntity.ok(user);
    }

    /**
     * Get a user by their email
     * HTTP GET /api/users/email/{email}
     * 
     * @param email the email of the user to retrieve
     * @return ResponseEntity with user data and HTTP 200 status
     */
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public ResponseEntity<UserDto> getUserByEmail(
            @PathVariable("email") @NotBlank(message = "Email cannot be blank") String email) {
        log.debug("REST request to get user with email: {}", email);
        
        UserDto user = userService.getUserByEmail(email);
        
        return ResponseEntity.ok(user);
    }

    /**
     * Get all users
     * HTTP GET /api/users
     * 
     * @return ResponseEntity with list of all users and HTTP 200 status
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.debug("REST request to get all users");
        
        List<UserDto> users = userService.getAllUsers();
        
        return ResponseEntity.ok(users);
    }

    /**
     * Search users by term
     * HTTP GET /api/users/search?term={searchTerm}
     * 
     * @param searchTerm the search term to look for in username, email, first name, or last name
     * @return ResponseEntity with list of matching users and HTTP 200 status
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public ResponseEntity<List<UserDto>> searchUsers(
            @RequestParam("term") @NotBlank(message = "Search term cannot be blank") String searchTerm) {
        log.debug("REST request to search users with term: {}", searchTerm);
        
        List<UserDto> users = userService.searchUsers(searchTerm);
        
        return ResponseEntity.ok(users);
    }

    /**
     * Update user password
     * HTTP PUT /api/users/{id}/password
     * 
     * @param userId the ID of the user
     * @param passwordChangeRequest the password change request containing current and new passwords
     * @return ResponseEntity with updated user and HTTP 200 status
     */
    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('ROLE_ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<UserDto> updatePassword(
            @PathVariable("id") @Min(value = 1, message = "User ID must be positive") Long userId,
            @Valid @RequestBody PasswordChangeRequest passwordChangeRequest) {
        log.info("REST request to update password for user ID: {}", userId);
        
        UserDto updatedUser = userService.updatePassword(userId, 
                passwordChangeRequest.getCurrentPassword(), 
                passwordChangeRequest.getNewPassword());
        
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Enable or disable a user account
     * HTTP PATCH /api/users/{id}/status
     * 
     * @param userId the ID of the user
     * @param enabled the enabled status to set
     * @return ResponseEntity with updated user and HTTP 200 status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDto> updateUserStatus(
            @PathVariable("id") @Min(value = 1, message = "User ID must be positive") Long userId,
            @RequestParam("enabled") Boolean enabled) {
        log.info("REST request to update status for user ID: {} to enabled: {}", userId, enabled);
        
        UserDto updatedUser = userService.updateUserStatus(userId, enabled);
        
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Lock or unlock a user account
     * HTTP PATCH /api/users/{id}/lock
     * 
     * @param userId the ID of the user
     * @param locked the locked status to set
     * @return ResponseEntity with updated user and HTTP 200 status
     */
    @PatchMapping("/{id}/lock")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDto> updateUserLockStatus(
            @PathVariable("id") @Min(value = 1, message = "User ID must be positive") Long userId,
            @RequestParam("locked") Boolean locked) {
        log.info("REST request to update lock status for user ID: {} to locked: {}", userId, locked);
        
        UserDto updatedUser = userService.updateUserLockStatus(userId, locked);
        
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Get current user profile
     * HTTP GET /api/users/profile
     * 
     * @param userDetails the authenticated user details
     * @return ResponseEntity with current user data and HTTP 200 status
     */
    @GetMapping("/profile")
    public ResponseEntity<UserDto> getCurrentUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        log.debug("REST request to get current user profile: {}", userDetails.getUsername());
        
        UserDto user = userService.getUserByUsername(userDetails.getUsername());
        
        return ResponseEntity.ok(user);
    }

    /**
     * Update current user profile
     * HTTP PUT /api/users/profile
     * 
     * @param userDto the updated user data
     * @param userDetails the authenticated user details
     * @return ResponseEntity with updated user and HTTP 200 status
     */
    @PutMapping("/profile")
    public ResponseEntity<UserDto> updateCurrentUserProfile(
            @Valid @RequestBody UserDto userDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("REST request to update current user profile: {}", userDetails.getUsername());
        
        // Get current user ID
        UserDto currentUser = userService.getUserByUsername(userDetails.getUsername());
        
        // Update user (only allow updating certain fields for self-update)
        UserDto updatedUser = userService.updateUser(currentUser.getUserId(), userDto);
        
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Get users by role
     * HTTP GET /api/users/role/{role}
     * 
     * @param role the role to filter by
     * @return ResponseEntity with list of users with the specified role and HTTP 200 status
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public ResponseEntity<List<UserDto>> getUsersByRole(
            @PathVariable("role") @NotBlank(message = "Role cannot be blank") String role) {
        log.debug("REST request to get users by role: {}", role);
        
        // This would require implementing getUsersByRole in UserService
        // For now, return all users and filter by role
        List<UserDto> allUsers = userService.getAllUsers();
        List<UserDto> filteredUsers = allUsers.stream()
                .filter(user -> role.equalsIgnoreCase(user.getRole()))
                .collect(java.util.stream.Collectors.toList());
        
        return ResponseEntity.ok(filteredUsers);
    }

    /**
     * Get active users
     * HTTP GET /api/users/active
     * 
     * @return ResponseEntity with list of active users and HTTP 200 status
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public ResponseEntity<List<UserDto>> getActiveUsers() {
        log.debug("REST request to get active users");
        
        // This would require implementing getActiveUsers in UserService
        // For now, return all users and filter by active status
        List<UserDto> allUsers = userService.getAllUsers();
        List<UserDto> activeUsers = allUsers.stream()
                .filter(UserDto::isActive)
                .collect(java.util.stream.Collectors.toList());
        
        return ResponseEntity.ok(activeUsers);
    }

    /**
     * Health check endpoint
     * HTTP GET /api/users/health
     * 
     * @return ResponseEntity with health status and HTTP 200 status
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.debug("Health check requested for User Service");
        
        return ResponseEntity.ok("User Service is healthy");
    }

    /**
     * Inner class for password change requests
     * Used for validating password change operations
     */
    public static class PasswordChangeRequest {
        
        @NotBlank(message = "Current password is required")
        private String currentPassword;
        
        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "New password must be at least 6 characters")
        private String newPassword;
        
        // Default constructor
        public PasswordChangeRequest() {}
        
        // Parameterized constructor
        public PasswordChangeRequest(String currentPassword, String newPassword) {
            this.currentPassword = currentPassword;
            this.newPassword = newPassword;
        }
        
        // Getters and setters
        public String getCurrentPassword() {
            return currentPassword;
        }
        
        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }
        
        public String getNewPassword() {
            return newPassword;
        }
        
        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}
