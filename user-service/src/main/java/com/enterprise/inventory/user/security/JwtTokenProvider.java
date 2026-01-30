package com.enterprise.inventory.user.security;

import io.jsonwebtoken.*; // Import JWT library classes
import io.jsonwebtoken.security.Keys; // Import JWT key generation utilities
import lombok.extern.slf4j.Slf4j; // Import Lombok's SLF4J annotation for logging
import org.springframework.beans.factory.annotation.Value; // Import Spring's Value annotation for property injection
import org.springframework.security.core.Authentication; // Import Spring Security Authentication interface
import org.springframework.security.core.userdetails.UserDetails; // Import Spring Security UserDetails interface
import org.springframework.stereotype.Component; // Import Spring's Component annotation

import javax.crypto.SecretKey; // Import SecretKey interface for JWT signing
import java.util.Date; // Import Date class for token expiration
import java.util.HashMap; // Import HashMap for claims
import java.util.Map; // Import Map interface
import java.util.function.Function; // Import Function interface for claim extraction

/**
 * JWT Token Provider class
 * This class handles JWT token generation, validation, and extraction of claims
 * Provides secure token-based authentication for the User Service
 * 
 * @Component: Marks this class as a Spring component
 * @Slf4j: Lombok annotation to add SLF4J logging capabilities
 */
@Component
@Slf4j
public class JwtTokenProvider {

    /**
     * JWT secret key from application properties
     * Used for signing and validating JWT tokens
     */
    @Value("${app.jwt.secret:mySecretKey}")
    private String jwtSecret;

    /**
     * JWT expiration time in milliseconds from application properties
     * Determines how long the token is valid
     */
    @Value("${app.jwt.expiration:86400000}") // 24 hours in milliseconds
    private Long jwtExpirationInMs;

    /**
     * JWT refresh token expiration time in milliseconds from application properties
     * Determines how long the refresh token is valid
     */
    @Value("${app.jwt.refresh-expiration:604800000}") // 7 days in milliseconds
    private Long refreshExpirationInMs;

    /**
     * Get the signing key for JWT tokens
     * Uses the secret key to create a secure signing key
     * 
     * @return SecretKey for JWT signing
     */
    private SecretKey getSigningKey() {
        // Create a secure key from the secret string
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generate a JWT token for the given authentication
     * Creates a token containing user details and claims
     * 
     * @param authentication the Spring Security authentication object
     * @return JWT token string
     */
    public String generateToken(Authentication authentication) {
        // Get user details from authentication
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // Create claims for the token
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userDetails.getUsername());
        claims.put("roles", userDetails.getAuthorities());
        claims.put("iat", new Date(System.currentTimeMillis()));
        
        // Generate the token
        return generateToken(claims, userDetails.getUsername());
    }

    /**
     * Generate a JWT token with specific claims and subject
     * Creates a token with custom claims and expiration
     * 
     * @param claims the claims to include in the token
     * @param subject the subject (usually username) of the token
     * @return JWT token string
     */
    public String generateToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Generate a refresh token for the given username
     * Creates a long-lived token for obtaining new access tokens
     * 
     * @param username the username for the refresh token
     * @return Refresh token string
     */
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpirationInMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Extract the username (subject) from the JWT token
     * 
     * @param token the JWT token
     * @return Username extracted from the token
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Extract the expiration date from the JWT token
     * 
     * @param token the JWT token
     * @return Expiration date of the token
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Extract a specific claim from the JWT token
     * Uses the claims resolver function to extract the desired claim
     * 
     * @param token the JWT token
     * @param claimsResolver the function to extract the specific claim
     * @return The extracted claim
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from the JWT token
     * Parses the token and returns all claims
     * 
     * @param token the JWT token
     * @return All claims from the token
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Check if the JWT token is expired
     * 
     * @param token the JWT token
     * @return true if the token is expired, false otherwise
     */
    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (JwtException e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Validate the JWT token against the user details
     * Checks if the token is valid for the given user
     * 
     * @param token the JWT token
     * @param userDetails the user details to validate against
     * @return true if the token is valid, false otherwise
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException e) {
            log.error("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate the JWT token format and signature
     * Checks if the token is properly formatted and signed
     * 
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Get the token expiration time in milliseconds
     * 
     * @return JWT expiration time in milliseconds
     */
    public Long getJwtExpirationInMs() {
        return jwtExpirationInMs;
    }

    /**
     * Get the refresh token expiration time in milliseconds
     * 
     * @return Refresh token expiration time in milliseconds
     */
    public Long getRefreshExpirationInMs() {
        return refreshExpirationInMs;
    }

    /**
     * Extract the token from the Authorization header
     * Removes the "Bearer " prefix if present
     * 
     * @param authHeader the Authorization header value
     * @return The JWT token without the "Bearer " prefix
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Check if the token is close to expiration
     * Returns true if the token expires within the next 30 minutes
     * 
     * @param token the JWT token
     * @return true if the token is close to expiration, false otherwise
     */
    public Boolean isTokenCloseToExpiration(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            final Date thirtyMinutesFromNow = new Date(System.currentTimeMillis() + (30 * 60 * 1000));
            return expiration.before(thirtyMinutesFromNow);
        } catch (JwtException e) {
            log.error("Error checking token expiration proximity: {}", e.getMessage());
            return true;
        }
    }
}
