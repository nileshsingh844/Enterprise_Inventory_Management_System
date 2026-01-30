package com.enterprise.inventory.user.repository;

import com.enterprise.inventory.user.model.User; // Import User entity
import org.springframework.data.jpa.repository.JpaRepository; // Import Spring Data JPA repository
import org.springframework.data.jpa.repository.Query; // Import Query annotation for custom queries
import org.springframework.data.repository.query.Param; // Import Param annotation for named parameters
import org.springframework.stereotype.Repository; // Import Repository annotation

import java.time.LocalDateTime; // Import LocalDateTime for date filtering
import java.util.List; // Import List interface
import java.util.Optional; // Import Optional for nullable results

/**
 * Spring Data JPA repository for User entity
 * This interface provides database operations for User entities
 * Spring Data automatically implements the methods defined in this interface
 * 
 * @Repository: Marks this interface as a Spring repository component
 * JpaRepository: Provides CRUD operations and pagination support
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their username
     * Used for authentication and user lookup
     * 
     * @param username the username to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by their email address
     * Used for authentication and user lookup
     * 
     * @param email the email address to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Find a user by username or email
     * Used for flexible login with either username or email
     * 
     * @param username the username to search for
     * @param email the email address to search for
     * @return Optional containing the user if found, empty otherwise
     */
    @Query("SELECT u FROM User u WHERE u.username = :username OR u.email = :email")
    Optional<User> findByUsernameOrEmail(@Param("username") String username, @Param("email") String email);

    /**
     * Check if a user exists with the given username
     * Used for validation during user creation
     * 
     * @param username the username to check
     * @return true if a user with the username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Check if a user exists with the given email
     * Used for validation during user creation
     * 
     * @param email the email to check
     * @return true if a user with the email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find users by their status
     * Used for user management and reporting
     * 
     * @param status the user status to filter by
     * @return List of users with the specified status
     */
    List<User> findByStatus(User.UserStatus status);

    /**
     * Find users by their role
     * Used for role-based access control and reporting
     * 
     * @param role the user role to filter by
     * @return List of users with the specified role
     */
    List<User> findByRole(User.UserRole role);

    /**
     * Find users by their first name
     * Used for user search and filtering
     * 
     * @param firstName the first name to search for
     * @return List of users with the specified first name
     */
    List<User> findByFirstName(String firstName);

    /**
     * Find users by their last name
     * Used for user search and filtering
     * 
     * @param lastName the last name to search for
     * @return List of users with the specified last name
     */
    List<User> findByLastName(String lastName);

    /**
     * Find users by their full name (first and last name)
     * Uses case-insensitive search for better user experience
     * 
     * @param firstName the first name to search for
     * @param lastName the last name to search for
     * @return List of users matching the full name criteria
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) AND LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    List<User> findByFullName(@Param("firstName") String firstName, @Param("lastName") String lastName);

    /**
     * Search users by username, email, first name, or last name
     * Uses case-insensitive search for better user experience
     * 
     * @param searchTerm the search term to look for
     * @return List of users matching the search criteria
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);

    /**
     * Find users who are currently locked
     * Used for security monitoring and account management
     * 
     * @return List of locked users
     */
    @Query("SELECT u FROM User u WHERE u.accountNonLocked = false")
    List<User> findLockedUsers();

    /**
     * Find users who are currently enabled
     * Used for filtering active users
     * 
     * @return List of enabled users
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true")
    List<User> findEnabledUsers();

    /**
     * Find users who are currently disabled
     * Used for filtering inactive users
     * 
     * @return List of disabled users
     */
    @Query("SELECT u FROM User u WHERE u.enabled = false")
    List<User> findDisabledUsers();

    /**
     * Find users who have not logged in since a specific date
     * Used for identifying inactive accounts
     * 
     * @param lastLoginDate the cutoff date for last login
     * @return List of users who haven't logged in since the specified date
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :lastLoginDate OR u.lastLoginAt IS NULL")
    List<User> findUsersWithLastLoginBefore(@Param("lastLoginDate") LocalDateTime lastLoginDate);

    /**
     * Find users who have failed login attempts
     * Used for security monitoring and account lockout management
     * 
     * @param maxFailedAttempts the maximum number of failed attempts to filter by
     * @return List of users with failed login attempts greater than the specified threshold
     */
    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts > :maxFailedAttempts")
    List<User> findUsersWithFailedLoginAttempts(@Param("maxFailedAttempts") Integer maxFailedAttempts);

    /**
     * Find users created within a date range
     * Used for reporting and analytics
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return List of users created within the date range
     */
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersByCreationDateRange(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Count users by status
     * Used for reporting and analytics
     * 
     * @param status the user status to count
     * @return Number of users with the specified status
     */
    long countByStatus(User.UserStatus status);

    /**
     * Count users by role
     * Used for reporting and analytics
     * 
     * @param role the user role to count
     * @return Number of users with the specified role
     */
    long countByRole(User.UserRole role);

    /**
     * Count enabled users
     * Used for reporting and analytics
     * 
     * @return Number of enabled users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true")
    long countEnabledUsers();

    /**
     * Count disabled users
     * Used for reporting and analytics
     * 
     * @return Number of disabled users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = false")
    long countDisabledUsers();

    /**
     * Count locked users
     * Used for security reporting
     * 
     * @return Number of locked users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.accountNonLocked = false")
    long countLockedUsers();

    /**
     * Find users with specific permission
     * Used for permission-based access control
     * 
     * @param permission the permission to search for
     * @return List of users with the specified permission
     */
    @Query("SELECT u FROM User u JOIN u.permissions p WHERE p = :permission")
    List<User> findUsersWithPermission(@Param("permission") String permission);

    /**
     * Find users by city and country
     * Used for location-based user management
     * 
     * @param city the city to filter by
     * @param country the country to filter by
     * @return List of users in the specified city and country
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.city) = LOWER(:city) AND LOWER(u.country) = LOWER(:country)")
    List<User> findUsersByCityAndCountry(@Param("city") String city, @Param("country") String country);
}
