package com.enterprise.inventory.user.exception;

import org.springframework.http.HttpStatus; // Import HTTP status codes
import org.springframework.web.bind.annotation.ResponseStatus; // Import ResponseStatus annotation

/**
 * Custom exception for resource not found scenarios
 * This exception is thrown when a requested resource (like a user) cannot be found
 * 
 * @ResponseStatus: Automatically maps this exception to HTTP 404 NOT_FOUND status
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructor for ResourceNotFoundException with message
     * 
     * @param message the error message describing what resource was not found
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor for ResourceNotFoundException with message and cause
     * 
     * @param message the error message describing what resource was not found
     * @param cause the underlying cause of the exception
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
