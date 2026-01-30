package com.enterprise.inventory.user.exception;

import org.springframework.http.HttpStatus; // Import HTTP status codes
import org.springframework.web.bind.annotation.ResponseStatus; // Import ResponseStatus annotation

/**
 * Custom exception for duplicate resource scenarios
 * This exception is thrown when attempting to create a resource that already exists
 * 
 * @ResponseStatus: Automatically maps this exception to HTTP 409 CONFLICT status
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {

    /**
     * Constructor for DuplicateResourceException with message
     * 
     * @param message the error message describing what resource already exists
     */
    public DuplicateResourceException(String message) {
        super(message);
    }

    /**
     * Constructor for DuplicateResourceException with message and cause
     * 
     * @param message the error message describing what resource already exists
     * @param cause the underlying cause of the exception
     */
    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
