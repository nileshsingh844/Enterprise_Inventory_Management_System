package com.enterprise.inventory.user.service;

import com.enterprise.inventory.user.dto.UserDto; // Import User DTO
import com.enterprise.inventory.user.exception.ResourceNotFoundException; // Import custom exception
import com.enterprise.inventory.user.exception.DuplicateResourceException; // Import custom exception
import com.enterprise.inventory.user.model.User; // Import User entity
import com.enterprise.inventory.user.repository.UserRepository; // Import User repository
import lombok.extern.slf4j.Slf4j; // Import Lombok's SLF4J annotation for logging
import org.springframework.beans.factory.annotation.Autowired; // Import Spring's Autowired annotation
import org.springframework.security.core.userdetails.UserDetails; // Import Spring Security UserDetails
import org.springframework.security.core.userdetails.UserDetailsService; // Import UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Import UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder; // Import PasswordEncoder
import org.springframework.stereotype.Service; // Import Spring's Service annotation
import org.springframework.transaction.annotation.Transactional; // Import Transactional annotation

import java.time.LocalDateTime; // Import LocalDateTime for timestamp handling
import java.util.List; // Import List interface
import java.util.stream.Collectors; // Import Stream utilities for collection processing

/**
 * Service class for User business logic
 * This class implements the core business operations for user management and authentication
 * Also implements UserDetailsService for Spring Security integration
 * 
 * @Service: Marks this class as a Spring service component
 * @Slf4j: Lombok annotation to add SLF4J logging capabilities
 * @Transactional: Enables transaction management for all methods
 */
@Service
@Transactional
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor-based dependency injection
     * Spring automatically injects the required beans
     * 
     * @param userRepository the repository for user data operations
     * @param passwordEncoder the password encoder for secure password hashing
     */
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create a new user
     * Validates input and checks for duplicate username/email before creation
     * 
     * @param userDto the user data to create
     * @return UserDto of the created user
     * @throws DuplicateResourceException if a user with the same username or email already exists
     */
    public UserDto createUser(UserDto userDto) {
        log.info("Creating new user with username: {}", userDto.getUsername());
        
        // Check if username already exists
        if (userRepository.existsByUsername(userDto.getUsername())) {
            log.warn("Username {} already exists", userDto.getUsername());
            throw new DuplicateResourceException("Username " + userDto.getUsername() + " already exists");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(userDto.getEmail())) {
            log.warn("Email {} already exists", userDto.getEmail());
            throw new DuplicateResourceException("Email " + userDto.getEmail() + " already exists");
        }

        // Convert DTO to entity
        User user = convertToEntity(userDto);
        
        // Encode password
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        
        // Save the user to database
        User savedUser = userRepository.save(user);
        
        log.info("Successfully created user with ID: {}", savedUser.getUserId());
        
        // Convert back to DTO and return
        return UserDto.fromEntity(savedUser);
    }

    /**
     * Update an existing user
     * Validates that the user exists before updating
     * 
     * @param userId the ID of the user to update
     * @param userDto the updated user data
     * @return UserDto of the updated user
     * @throws ResourceNotFoundException if the user is not found
     */
    public UserDto updateUser(Long userId, UserDto userDto) {
        log.info("Updating user with ID: {}", userId);
        
        // Find existing user
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with ID {} not found", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });

        // Check if username is being changed and if the new username already exists
        if (!existingUser.getUsername().equals(userDto.getUsername()) && 
            userRepository.existsByUsername(userDto.getUsername())) {
            log.warn("Username {} already exists", userDto.getUsername());
            throw new DuplicateResourceException("Username " + userDto.getUsername() + " already exists");
        }

        // Check if email is being changed and if the new email already exists
        if (!existingUser.getEmail().equals(userDto.getEmail()) && 
            userRepository.existsByEmail(userDto.getEmail())) {
            log.warn("Email {} already exists", userDto.getEmail());
            throw new DuplicateResourceException("Email " + userDto.getEmail() + " already exists");
        }

        // Update user fields
        updateUserFields(existingUser, userDto);
        
        // Update password if provided
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
            existingUser.setPasswordChangedAt(LocalDateTime.now());
        }
        
        // Save the updated user
        User updatedUser = userRepository.save(existingUser);
        
        log.info("Successfully updated user with ID: {}", updatedUser.getUserId());
        
        return UserDto.fromEntity(updatedUser);
    }

    /**
     * Get a user by their ID
     * 
     * @param userId the ID of the user to retrieve
     * @return UserDto of the found user
     * @throws ResourceNotFoundException if the user is not found
     */
    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        log.debug("Fetching user with ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with ID {} not found", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });

        log.debug("Successfully fetched user with ID: {}", userId);
        return UserDto.fromEntity(user);
    }

    /**
     * Get a user by their username
     * 
     * @param username the username of the user to retrieve
     * @return UserDto of the found user
     * @throws ResourceNotFoundException if the user is not found
     */
    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        log.debug("Fetching user with username: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User with username {} not found", username);
                    return new ResourceNotFoundException("User not found with username: " + username);
                });

        log.debug("Successfully fetched user with username: {}", username);
        return UserDto.fromEntity(user);
    }

    /**
     * Get a user by their email
     * 
     * @param email the email of the user to retrieve
     * @return UserDto of the found user
     * @throws ResourceNotFoundException if the user is not found
     */
    @Transactional(readOnly = true)
    public UserDto getUserByEmail(String email) {
        log.debug("Fetching user with email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User with email {} not found", email);
                    return new ResourceNotFoundException("User not found with email: " + email);
                });

        log.debug("Successfully fetched user with email: {}", email);
        return UserDto.fromEntity(user);
    }

    /**
     * Get all users
     * 
     * @return List of all UserDto objects
     */
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        log.debug("Fetching all users");
        
        List<User> users = userRepository.findAll();
        
        log.debug("Found {} users", users.size());
        return users.stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Search users by term
     * Searches in username, email, first name, and last name
     * 
     * @param searchTerm the search term
     * @return List of UserDto objects matching the search criteria
     */
    @Transactional(readOnly = true)
    public List<UserDto> searchUsers(String searchTerm) {
        log.debug("Searching users with term: {}", searchTerm);
        
        List<User> users = userRepository.searchUsers(searchTerm);
        
        log.debug("Found {} users matching search term: {}", users.size(), searchTerm);
        return users.stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Update user password
     * 
     * @param userId the ID of the user
     * @param currentPassword the current password
     * @param newPassword the new password
     * @return UserDto of the updated user
     * @throws ResourceNotFoundException if the user is not found
     * @throws IllegalArgumentException if the current password is incorrect
     */
    public UserDto updatePassword(Long userId, String currentPassword, String newPassword) {
        log.info("Updating password for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with ID {} not found", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.warn("Invalid current password for user ID: {}", userId);
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        
        log.info("Successfully updated password for user ID: {}", userId);
        
        return UserDto.fromEntity(updatedUser);
    }

    /**
     * Enable or disable a user account
     * 
     * @param userId the ID of the user
     * @param enabled the enabled status to set
     * @return UserDto of the updated user
     * @throws ResourceNotFoundException if the user is not found
     */
    public UserDto updateUserStatus(Long userId, boolean enabled) {
        log.info("Updating status for user ID: {} to enabled: {}", userId, enabled);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with ID {} not found", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });

        user.setEnabled(enabled);
        User updatedUser = userRepository.save(user);
        
        log.info("Successfully updated status for user ID: {}", userId);
        
        return UserDto.fromEntity(updatedUser);
    }

    /**
     * Lock or unlock a user account
     * 
     * @param userId the ID of the user
     * @param locked the locked status to set
     * @return UserDto of the updated user
     * @throws ResourceNotFoundException if the user is not found
     */
    public UserDto updateUserLockStatus(Long userId, boolean locked) {
        log.info("Updating lock status for user ID: {} to locked: {}", userId, locked);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with ID {} not found", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });

        user.setAccountNonLocked(!locked);
        if (locked) {
            user.setLockedAt(LocalDateTime.now());
        } else {
            user.setLockedAt(null);
            user.setFailedLoginAttempts(0);
        }
        
        User updatedUser = userRepository.save(user);
        
        log.info("Successfully updated lock status for user ID: {}", userId);
        
        return UserDto.fromEntity(updatedUser);
    }

    /**
     * Update last login timestamp for a user
     * 
     * @param username the username of the user
     */
    public void updateLastLogin(String username) {
        log.debug("Updating last login for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User with username {} not found", username);
                    return new ResourceNotFoundException("User not found with username: " + username);
                });

        user.setLastLoginAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        
        userRepository.save(user);
        
        log.debug("Successfully updated last login for user: {}", username);
    }

    /**
     * Increment failed login attempts for a user
     * 
     * @param username the username of the user
     * @return UserDto of the updated user
     */
    public UserDto incrementFailedLoginAttempts(String username) {
        log.debug("Incrementing failed login attempts for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User with username {} not found", username);
                    return new ResourceNotFoundException("User not found with username: " + username);
                });

        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        
        // Lock account after 5 failed attempts
        if (user.getFailedLoginAttempts() >= 5) {
            user.setAccountNonLocked(false);
            user.setLockedAt(LocalDateTime.now());
            log.warn("Account locked for user: {} due to too many failed login attempts", username);
        }
        
        User updatedUser = userRepository.save(user);
        
        log.debug("Successfully incremented failed login attempts for user: {}", username);
        
        return UserDto.fromEntity(updatedUser);
    }

    /**
     * Load user by username for Spring Security
     * This method is required by the UserDetailsService interface
     * 
     * @param username the username to load
     * @return UserDetails object for Spring Security
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> {
                    log.warn("User not found with username or email: {}", username);
                    return new UsernameNotFoundException("User not found with username or email: " + username);
                });

        log.debug("Successfully loaded user by username: {}", username);
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(getAuthorities(user))
                .accountExpired(!user.getAccountNonExpired())
                .accountLocked(!user.getAccountNonLocked())
                .credentialsExpired(!user.getCredentialsNonExpired())
                .disabled(!user.getEnabled())
                .build();
    }

    /**
     * Get authorities for a user
     * Combines role and permissions for Spring Security
     * 
     * @param user the user to get authorities for
     * @return Array of GrantedAuthority objects
     */
    private org.springframework.security.core.authority.SimpleGrantedAuthority[] getAuthorities(User user) {
        java.util.List<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
        
        // Add role
        authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        
        // Add permissions
        if (user.getPermissions() != null) {
            user.getPermissions().forEach(permission -> 
                authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(permission)));
        }
        
        return authorities.toArray(new org.springframework.security.core.authority.SimpleGrantedAuthority[0]);
    }

    /**
     * Convert UserDto to User entity
     * 
     * @param userDto the DTO to convert
     * @return User entity
     */
    private User convertToEntity(UserDto userDto) {
        return User.builder()
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .phoneNumber(userDto.getPhoneNumber())
                .address(userDto.getAddress())
                .city(userDto.getCity())
                .state(userDto.getState())
                .postalCode(userDto.getPostalCode())
                .country(userDto.getCountry())
                .status(userDto.getStatus() != null ? 
                        User.UserStatus.valueOf(userDto.getStatus()) : User.UserStatus.ACTIVE)
                .role(userDto.getRole() != null ? 
                        User.UserRole.valueOf(userDto.getRole()) : User.UserRole.CUSTOMER)
                .accountNonLocked(userDto.getAccountNonLocked() != null ? userDto.getAccountNonLocked() : true)
                .accountNonExpired(userDto.getAccountNonExpired() != null ? userDto.getAccountNonExpired() : true)
                .credentialsNonExpired(userDto.getCredentialsNonExpired() != null ? userDto.getCredentialsNonExpired() : true)
                .enabled(userDto.getEnabled() != null ? userDto.getEnabled() : true)
                .permissions(userDto.getPermissions())
                .build();
    }

    /**
     * Update fields of an existing user with data from DTO
     * 
     * @param existingUser the user to update
     * @param userDto the DTO with updated data
     */
    private void updateUserFields(User existingUser, UserDto userDto) {
        existingUser.setUsername(userDto.getUsername());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setFirstName(userDto.getFirstName());
        existingUser.setLastName(userDto.getLastName());
        existingUser.setPhoneNumber(userDto.getPhoneNumber());
        existingUser.setAddress(userDto.getAddress());
        existingUser.setCity(userDto.getCity());
        existingUser.setState(userDto.getState());
        existingUser.setPostalCode(userDto.getPostalCode());
        existingUser.setCountry(userDto.getCountry());
        
        if (userDto.getStatus() != null) {
            existingUser.setStatus(User.UserStatus.valueOf(userDto.getStatus()));
        }
        if (userDto.getRole() != null) {
            existingUser.setRole(User.UserRole.valueOf(userDto.getRole()));
        }
        if (userDto.getAccountNonLocked() != null) {
            existingUser.setAccountNonLocked(userDto.getAccountNonLocked());
        }
        if (userDto.getAccountNonExpired() != null) {
            existingUser.setAccountNonExpired(userDto.getAccountNonExpired());
        }
        if (userDto.getCredentialsNonExpired() != null) {
            existingUser.setCredentialsNonExpired(userDto.getCredentialsNonExpired());
        }
        if (userDto.getEnabled() != null) {
            existingUser.setEnabled(userDto.getEnabled());
        }
        if (userDto.getPermissions() != null) {
            existingUser.setPermissions(userDto.getPermissions());
        }
    }
}
