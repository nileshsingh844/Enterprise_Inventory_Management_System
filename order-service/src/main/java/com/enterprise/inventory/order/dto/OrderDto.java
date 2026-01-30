package com.enterprise.inventory.order.dto;

import com.enterprise.inventory.order.model.Order; // Import Order entity
import lombok.AllArgsConstructor; // Import Lombok annotation for generating constructor with all parameters
import lombok.Builder; // Import Lombok annotation for builder pattern
import lombok.Data; // Import Lombok annotation for generating getters, setters, toString, equals, and hashCode
import lombok.NoArgsConstructor; // Import Lombok annotation for generating no-argument constructor

import javax.validation.constraints.*; // Import validation annotations
import java.math.BigDecimal; // Import BigDecimal for precise monetary calculations
import java.time.LocalDateTime; // Import LocalDateTime for timestamp handling
import java.util.List; // Import List interface for order items

/**
 * Data Transfer Object (DTO) for Order entity
 * This class is used for transferring order data between layers and for API responses
 * DTOs help separate the internal domain model from the external API representation
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
public class OrderDto {

    /**
     * Unique order identifier
     * Primary key from the database
     */
    private Long orderId;

    /**
     * Unique order number
     * Generated automatically for order tracking
     */
    private String orderNumber;

    /**
     * Customer ID who placed the order
     * Must not be null for order creation
     */
    @NotNull(message = "Customer ID is required", groups = {CreateValidation.class})
    private Long customerId;

    /**
     * Customer name for order reference
     * Must not be null or empty for order creation
     */
    @NotBlank(message = "Customer name is required", groups = {CreateValidation.class})
    @Size(max = 200, message = "Customer name must not exceed 200 characters")
    private String customerName;

    /**
     * Customer email for order notifications
     * Must be valid email format if provided
     */
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String customerEmail;

    /**
     * Customer phone number for contact
     */
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String customerPhone;

    /**
     * Shipping address for the order
     * Must not be null or empty for order creation
     */
    @NotBlank(message = "Shipping address is required", groups = {CreateValidation.class})
    private String shippingAddress;

    /**
     * Billing address for the order
     * Optional field, defaults to shipping address if not provided
     */
    private String billingAddress;

    /**
     * Order status
     * Current state of the order in the fulfillment process
     */
    private String status;

    /**
     * Total order amount before taxes and discounts
     * Calculated as sum of all order items
     */
    @DecimalMin(value = "0.00", message = "Subtotal must be non-negative")
    private BigDecimal subtotal;

    /**
     * Tax amount applied to the order
     */
    @DecimalMin(value = "0.00", message = "Tax amount must be non-negative")
    private BigDecimal taxAmount;

    /**
     * Shipping charges for the order
     */
    @DecimalMin(value = "0.00", message = "Shipping amount must be non-negative")
    private BigDecimal shippingAmount;

    /**
     * Discount amount applied to the order
     */
    @DecimalMin(value = "0.00", message = "Discount amount must be non-negative")
    private BigDecimal discountAmount;

    /**
     * Total order amount including taxes, shipping, and discounts
     * Final amount charged to the customer
     */
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0", groups = {CreateValidation.class})
    private BigDecimal totalAmount;

    /**
     * Payment method used for the order
     */
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod;

    /**
     * Payment status for the order
     * Tracks whether payment has been processed
     */
    private String paymentStatus;

    /**
     * Transaction ID from payment gateway
     */
    @Size(max = 100, message = "Transaction ID must not exceed 100 characters")
    private String paymentTransactionId;

    /**
     * Order notes or special instructions
     */
    private String orderNotes;

    /**
     * Internal notes for order processing
     * Not visible to customers
     */
    private String internalNotes;

    /**
     * Expected delivery date
     * Estimated date when order will be delivered
     */
    private LocalDateTime expectedDeliveryDate;

    /**
     * Actual delivery date
     * Date when order was delivered to customer
     */
    private LocalDateTime actualDeliveryDate;

    /**
     * Timestamp when the order was created
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the order was last updated
     */
    private LocalDateTime updatedAt;

    /**
     * Order items associated with this order
     * List of individual products in the order
     */
    private List<OrderItemDto> orderItems;

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
     * Static factory method to convert Order entity to OrderDto
     * This method handles the mapping between domain model and DTO
     * 
     * @param order the Order entity to convert
     * @return OrderDto with mapped values
     */
    public static OrderDto fromEntity(Order order) {
        if (order == null) {
            return null;
        }

        return OrderDto.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomerId())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .customerPhone(order.getCustomerPhone())
                .shippingAddress(order.getShippingAddress())
                .billingAddress(order.getBillingAddress())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .shippingAmount(order.getShippingAmount())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null)
                .paymentTransactionId(order.getPaymentTransactionId())
                .orderNotes(order.getOrderNotes())
                .internalNotes(order.getInternalNotes())
                .expectedDeliveryDate(order.getExpectedDeliveryDate())
                .actualDeliveryDate(order.getActualDeliveryDate())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .orderItems(order.getOrderItems() != null ? 
                    order.getOrderItems().stream()
                        .map(OrderItemDto::fromEntity)
                        .collect(java.util.stream.Collectors.toList()) : null)
                .build();
    }
}
