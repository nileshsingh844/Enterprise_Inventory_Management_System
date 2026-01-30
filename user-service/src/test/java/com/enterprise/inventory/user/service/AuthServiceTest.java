package com.enterprise.inventory.user.service;

import com.enterprise.inventory.user.dto.AuthRequest; // Import AuthRequest DTO
import com.enterprise.inventory.user.dto.AuthResponse; // Import AuthResponse DTO
import com.enterprise.inventory.user.dto.UserDto; // Import UserDto
import com.enterprise.inventory.user.exception.ResourceNotFoundException; // Import custom exception
import com.enterprise.inventory.user.model.User; // Import User entity
import com.enterprise.inventory.user.repository.UserRepository; // Import User repository
import org.junit.jupiter.api.BeforeEach; // Import JUnit 5 BeforeEach annotation
import org.junit.jupiter.api.DisplayName; // Import JUnit 5 DisplayName annotation
import org.junit.jupiter.api.Test; // Import JUnit 5 Test annotation
import org.junit.jupiter.api.extension.ExtendWith; // Import JUnit 5 ExtendWith annotation
import org.mockito.InjectMocks; // Import Mockito InjectMocks annotation
import org.mockito.Mock; // Import Mockito Mock annotation
import org.mockito.junit.jupiter.MockitoExtension; // Import Mockito JUnit 5 extension
import org.springframework.security.authentication.AuthenticationManager; // Import AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException; // Import BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Import authentication token
import org.springframework.security.core.Authentication; // Import Authentication interface
import org.springframework.security.core.userdetails.UserDetails; // Import UserDetails
import org.springframework.security.crypto.password.PasswordEncoder; // Import PasswordEncoder

import java.time.LocalDateTime; // Import LocalDateTime for timestamp handling
import java.util.HashSet; // Import HashSet for roles
import java.util.Set; // Import Set interface

import static org.junit.jupiter.api.Assertions.*; // Import JUnit 5 assertions
import static org.mockito.ArgumentMatchers.any; // Import Mockito any matcher
import static org.mockito.ArgumentMatchers.anyString; // Import Mockito anyString matcher
import static org.mockito.Mockito.*; // Import Mockito methods

/**
 * Unit tests for AuthService class
 * This test class validates the authentication business logic of the AuthService
 * Uses Mockito for mocking dependencies and JUnit 5 for testing framework
 * 
 * @ExtendWith: Enables Mockito extension for JUnit 5
 * @DisplayName: Provides descriptive test names
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Service Tests")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager; // Mocked AuthenticationManager dependency

    @Mock
    private UserService userService; // Mocked UserService dependency

    @Mock
    private JwtTokenProvider tokenProvider; // Mocked JwtTokenProvider dependency

    @Mock
    private UserRepository userRepository; // Mocked UserRepository dependency

    @InjectMocks
    private AuthService authService; // AuthService instance with mocked dependencies

    private AuthRequest authRequest; // Test authentication request
    private User testUser; // Test user entity
    private UserDetails userDetails; // Test user details
    private Authentication authentication; // Test authentication object

    /**
     * Setup method executed before each test
     * Initializes test data and common mock behaviors
     */
    @BeforeEach
    void setUp() {
        // Create test authentication request
        authRequest = AuthRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        // Create test user entity
        testUser = User.builder()
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .password("$2a$10$rQZ8kHWKqYQJzKjXzqzN/e9Z8G8r8Z8r8Z8r8Z8r8Z8r8Z8r8Z8r8Z8r8Z8")
                .firstName("Test")
                .lastName("User")
                .status(User.UserStatus.ACTIVE)
                .role(User.UserRole.CUSTOMER)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .failedLoginAttempts(0)
                .build();

        // Create test user details
        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("testuser")
                .password("password123")
                .authorities("ROLE_CUSTOMER")
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();

        // Create test authentication object
        authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }

    /**
     * Test successful user authentication
     * Validates that authentication succeeds with valid credentials
     */
    @Test
    @DisplayName("Should authenticate user successfully with valid credentials")
    void shouldAuthenticateUserSuccessfullyWithValidCredentials() {
        // Arrange: Set up mock behaviors
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsernameOrEmail(anyString(), anyString()))
                .thenReturn(java.util.Optional.of(testUser));
        when(tokenProvider.generateToken(any(Authentication.class)))
                .thenReturn("jwt-token");
        when(tokenProvider.generateRefreshToken(anyString()))
                .thenReturn("refresh-token");
        when(tokenProvider.getJwtExpirationInMs())
                .thenReturn(86400000L); // 24 hours
        doNothing().when(userService).updateLastLogin(anyString());

        // Act: Call the method under test
        AuthResponse result = authService.authenticateUser(authRequest);

        // Assert: Verify the result
        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals("refresh-token", result.getRefreshToken());
        assertEquals("Authentication successful", result.getMessage());
        assertNotNull(result.getUser());
        assertEquals("testuser", result.getUser().getUsername());
        assertNotNull(result.getRoles());

        // Verify: Check that service methods were called
        verify(authenticationManager).authenticate(any(Authentication.class));
        verify(userRepository).findByUsernameOrEmail("testuser", "testuser");
        verify(tokenProvider).generateToken(any(Authentication.class));
        verify(tokenProvider).generateRefreshToken("testuser");
        verify(userService).updateLastLogin("testuser");
    }

    /**
     * Test authentication with invalid credentials
     * Validates that BadCredentialsException is thrown for invalid credentials
     */
    @Test
    @DisplayName("Should throw BadCredentialsException for invalid credentials")
    void shouldThrowBadCredentialsExceptionForInvalidCredentials() {
        // Arrange: Set up mock to throw exception
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));
        when(userRepository.findByUsernameOrEmail(anyString(), anyString()))
                .thenReturn(java.util.Optional.of(testUser));
        when(userService.incrementFailedLoginAttempts(anyString()))
                .thenReturn(UserDto.fromEntity(testUser));

        // Act & Assert: Verify exception is thrown
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.authenticateUser(authRequest)
        );

        // Assert: Verify exception message
        assertEquals("Invalid username or password", exception.getMessage());

        // Verify: Check that failed login attempts were incremented
        verify(userService).incrementFailedLoginAttempts("testuser");
    }

    /**
     * Test authentication with non-existent user
     * Validates that BadCredentialsException is thrown for non-existent user
     */
    @Test
    @DisplayName("Should throw BadCredentialsException for non-existent user")
    void shouldThrowBadCredentialsExceptionForNonExistentUser() {
        // Arrange: Set up mock to throw exception
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("User not found"));

        // Act & Assert: Verify exception is thrown
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.authenticateUser(authRequest)
        );

        // Assert: Verify exception message
        assertEquals("Invalid username or password", exception.getMessage());
    }

    /**
     * Test authentication with inactive user
     * Validates that BadCredentialsException is thrown for inactive user
     */
    @Test
    @DisplayName("Should throw BadCredentialsException for inactive user")
    void shouldThrowBadCredentialsExceptionForInactiveUser() {
        // Arrange: Create inactive user
        User inactiveUser = User.builder()
                .userId(1L)
                .username("testuser")
                .status(User.UserStatus.INACTIVE)
                .enabled(false)
                .build();

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsernameOrEmail(anyString(), anyString()))
                .thenReturn(java.util.Optional.of(inactiveUser));

        // Act & Assert: Verify exception is thrown
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.authenticateUser(authRequest)
        );

        // Assert: Verify exception message
        assertEquals("User account is not active", exception.getMessage());
    }

    /**
     * Test authentication with locked user
     * Validates that BadCredentialsException is thrown for locked user
     */
    @Test
    @DisplayName("Should throw BadCredentialsException for locked user")
    void shouldThrowBadCredentialsExceptionForLockedUser() {
        // Arrange: Create locked user
        User lockedUser = User.builder()
                .userId(1L)
                .username("testuser")
                .status(User.UserStatus.ACTIVE)
                .enabled(true)
                .accountNonLocked(false)
                .build();

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsernameOrEmail(anyString(), anyString()))
                .thenReturn(java.util.Optional.of(lockedUser));

        // Act & Assert: Verify exception is thrown
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.authenticateUser(authRequest)
        );

        // Assert: Verify exception message
        assertEquals("User account is locked", exception.getMessage());
    }

    /**
     * Test successful token refresh
     * Validates that token is refreshed successfully
     */
    @Test
    @DisplayName("Should refresh token successfully")
    void shouldRefreshTokenSuccessfully() {
        // Arrange: Set up mock behaviors
        when(tokenProvider.validateToken("refresh-token")).thenReturn(true);
        when(tokenProvider.getUsernameFromToken("refresh-token")).thenReturn("testuser");
        when(userRepository.findByUsername("testuser"))
                .thenReturn(java.util.Optional.of(testUser));
        when(tokenProvider.generateToken("testuser")).thenReturn("new-jwt-token");
        when(tokenProvider.generateRefreshToken("testuser")).thenReturn("new-refresh-token");
        when(tokenProvider.getJwtExpirationInMs())
                .thenReturn(86400000L); // 24 hours

        // Act: Call the method under test
        AuthResponse result = authService.refreshToken("refresh-token");

        // Assert: Verify the result
        assertNotNull(result);
        assertEquals("new-jwt-token", result.getToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals("new-refresh-token", result.getRefreshToken());
        assertEquals("Token refreshed successfully", result.getMessage());
        assertNotNull(result.getUser());

        // Verify: Check that token provider methods were called
        verify(tokenProvider).validateToken("refresh-token");
        verify(tokenProvider).getUsernameFromToken("refresh-token");
        verify(userRepository).findByUsername("testuser");
        verify(tokenProvider).generateToken("testuser");
        verify(tokenProvider).generateRefreshToken("testuser");
    }

    /**
     * Test token refresh with invalid token
     * Validates that BadCredentialsException is thrown for invalid refresh token
     */
    @Test
    @DisplayName("Should throw BadCredentialsException for invalid refresh token")
    void shouldThrowBadCredentialsExceptionForInvalidRefreshToken() {
        // Arrange: Set up mock to return false for validation
        when(tokenProvider.validateToken("invalid-token")).thenReturn(false);

        // Act & Assert: Verify exception is thrown
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.refreshToken("invalid-token")
        );

        // Assert: Verify exception message
        assertEquals("Invalid refresh token", exception.getMessage());

        // Verify: Check that validation was called
        verify(tokenProvider).validateToken("invalid-token");
    }

    /**
     * Test token validation
     * Validates that token validation returns true for valid token
     */
    @Test
    @DisplayName("Should return true for valid token")
    void shouldReturnTrueForValidToken() {
        // Arrange: Set up mock behaviors
        when(tokenProvider.validateToken("valid-token")).thenReturn(true);
        when(tokenProvider.getUsernameFromToken("valid-token")).thenReturn("testuser");
        when(userRepository.findByUsername("testuser"))
                .thenReturn(java.util.Optional.of(testUser));

        // Act: Call the method under test
        boolean result = authService.validateToken("valid-token");

        // Assert: Verify the result
        assertTrue(result);

        // Verify: Check that methods were called
        verify(tokenProvider).validateToken("valid-token");
        verify(tokenProvider).getUsernameFromToken("valid-token");
        verify(userRepository).findByUsername("testuser");
    }

    /**
     * Test token validation with invalid token
     * Validates that token validation returns false for invalid token
     */
    @Test
    @DisplayName("Should return false for invalid token")
    void shouldReturnFalseForInvalidToken() {
        // Arrange: Set up mock to return false for validation
        when(tokenProvider.validateToken("invalid-token")).thenReturn(false);

        // Act: Call the method under test
        boolean result = authService.validateToken("invalid-token");

        // Assert: Verify the result
        assertFalse(result);

        // Verify: Check that validation was called
        verify(tokenProvider).validateToken("invalid-token");
    }

    /**
     * Test token validation with non-existent user
     * Validates that token validation returns false for non-existent user
     */
    @Test
    @DisplayName("Should return false for token with non-existent user")
    void shouldReturnFalseForTokenWithNonExistentUser() {
        // Arrange: Set up mock behaviors
        when(tokenProvider.validateToken("valid-token")).thenReturn(true);
        when(tokenProvider.getUsernameFromToken("valid-token")).thenReturn("nonexistent");
        when(userRepository.findByUsername("nonexistent"))
                .thenReturn(java.util.Optional.empty());

        // Act: Call the method under test
        boolean result = authService.validateToken("valid-token");

        // Assert: Verify the result
        assertFalse(result);

        // Verify: Check that methods were called
        verify(tokenProvider).validateToken("valid-token");
        verify(tokenProvider).getUsernameFromToken("valid-token");
        verify(userRepository).findByUsername("nonexistent");
    }

    /**
     * Test getting user from token
     * Validates that user is returned for valid token
     */
    @Test
    @DisplayName("Should return user for valid token")
    void shouldReturnUserForValidToken() {
        // Arrange: Set up mock behaviors
        when(tokenProvider.validateToken("valid-token")).thenReturn(true);
        when(tokenProvider.getUsernameFromToken("valid-token")).thenReturn("testuser");
        when(userRepository.findByUsername("testuser"))
                .thenReturn(java.util.Optional.of(testUser));

        // Act: Call the method under test
        UserDto result = authService.getUserFromToken("valid-token");

        // Assert: Verify the result
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());

        // Verify: Check that methods were called
        verify(tokenProvider).validateToken("valid-token");
        verify(tokenProvider).getUsernameFromToken("valid-token");
        verify(userRepository).findByUsername("testuser");
    }

    /**
     * Test getting user from invalid token
     * Validates that BadCredentialsException is thrown for invalid token
     */
    @Test
    @DisplayName("Should throw BadCredentialsException for invalid token when getting user")
    void shouldThrowBadCredentialsExceptionForInvalidTokenWhenGettingUser() {
        // Arrange: Set up mock to return false for validation
        when(tokenProvider.validateToken("invalid-token")).thenReturn(false);

        // Act & Assert: Verify exception is thrown
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.getUserFromToken("invalid-token")
        );

        // Assert: Verify exception message
        assertEquals("Invalid token", exception.getMessage());
    }

    /**
     * Test logout
     * Validates that logout returns success message
     */
    @Test
    @DisplayName("Should return success message for logout")
    void shouldReturnSuccessMessageForLogout() {
        // Arrange: Set up mock behavior
        when(tokenProvider.getUsernameFromToken("valid-token")).thenReturn("testuser");

        // Act: Call the method under test
        String result = authService.logout("valid-token");

        // Assert: Verify the result
        assertEquals("Logout successful", result);

        // Verify: Check that method was called
        verify(tokenProvider).getUsernameFromToken("valid-token");
    }

    /**
     * Test logout with invalid token
     * Validates that logout returns failure message for invalid token
     */
    @Test
    @DisplayName("Should return failure message for logout with invalid token")
    void shouldReturnFailureMessageForLogoutWithInvalidToken() {
        // Arrange: Set up mock to throw exception
        when(tokenProvider.getUsernameFromToken("invalid-token"))
                .thenThrow(new RuntimeException("Invalid token"));

        // Act: Call the method under test
        String result = authService.logout("invalid-token");

        // Assert: Verify the result
        assertEquals("Logout failed", result);
    }

    /**
     * Test checking if token is close to expiration
     * Validates that token expiration check returns correct result
     */
    @Test
    @DisplayName("Should return correct result for token expiration check")
    void shouldReturnCorrectResultForTokenExpirationCheck() {
        // Arrange: Set up mock behavior
        when(tokenProvider.isTokenCloseToExpiration("valid-token")).thenReturn(true);

        // Act: Call the method under test
        boolean result = authService.isTokenCloseToExpiration("valid-token");

        // Assert: Verify the result
        assertTrue(result);

        // Verify: Check that method was called
        verify(tokenProvider).isTokenCloseToExpiration("valid-token");
    }

    /**
     * Test checking if token is close to expiration with exception
     * Validates that token expiration check returns true when exception occurs
     */
    @Test
    @DisplayName("Should return true for token expiration check when exception occurs")
    void shouldReturnTrueForTokenExpirationCheckWhenExceptionOccurs() {
        // Arrange: Set up mock to throw exception
        when(tokenProvider.isTokenCloseToExpiration("invalid-token"))
                .thenThrow(new RuntimeException("Invalid token"));

        // Act: Call the method under test
        boolean result = authService.isTokenCloseToExpiration("invalid-token");

        // Assert: Verify the result
        assertTrue(result);

        // Verify: Check that method was called
        verify(tokenProvider).isTokenCloseToExpiration("invalid-token");
    }
}
