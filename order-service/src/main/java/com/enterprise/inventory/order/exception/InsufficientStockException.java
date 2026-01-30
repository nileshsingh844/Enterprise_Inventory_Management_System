package com.enterprise.inventory.order.exception;

import org.springframework.http.HttpStatus; // Import HTTP status codes
import org.springframework.web.bind.annotation.ResponseStatus; // Import ResponseStatus annotation

/**
 * Custom exception for insufficient stock scenarios
 * This exception is thrown when there is not enough inventory to fulfill an order
 * 
 * @ResponseStatus: Automatically maps this exception to HTTP 409 CONFLICT status
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class InsufficientStockException extends RuntimeException {

    /**
     * Constructor for InsufficientStockException with message
     * 
     * @param message the error message describing the stock shortage
     */
    public InsufficientStockException(String message) {
        super(message);
    }

    /**
     * Constructor for InsufficientStockException with message and cause
     * 
     * @param message the error message describing the stock shortage
     * @param cause the underlying cause of the exception
     */
    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }
}
