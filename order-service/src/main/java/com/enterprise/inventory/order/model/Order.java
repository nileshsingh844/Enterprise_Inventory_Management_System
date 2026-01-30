package com.enterprise.inventory.order.model;

import lombok.AllArgsConstructor; // Import Lombok annotation for generating constructor with all parameters
import lombok.Builder; // Import Lombok annotation for builder pattern
import lombok.Data; // Import Lombok annotation for generating getters, setters, toString, equals, and hashCode
import lombok.NoArgsConstructor; // Import Lombok annotation for generating no-argument constructor

import javax.persistence.*; // Import JPA annotations for entity mapping
import javax.validation.constraints.*; // Import validation annotations
import java.math.BigDecimal; // Import BigDecimal for precise monetary calculations
import java.time.LocalDateTime; // Import LocalDateTime for timestamp handling
import java.util.List; // Import List interface for order items

/**
 * Order entity representing customer orders
 * This JPA entity maps to the 'orders' table in the database
 * 
 * @Entity: Marks this class as a JPA entity
 * @Table: Specifies the database table name
 * @Data: Lombok annotation to generate getters, setters, toString, equals, and hashCode
 * @Builder: Lombok annotation to generate builder pattern
 * @NoArgsConstructor: Lombok annotation to generate no-argument constructor
 * @AllArgsConstructor: Lombok annotation to generate constructor with all parameters
 */
@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    /**
     * Primary key for the Order entity
     * Auto-generated sequence value for unique identification
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    /**
     * Unique order number
     * Generated automatically for order tracking
     */
    @Column(name = "order_number", unique = true, nullable = false, length = 50)
    private String orderNumber;

    /**
     * Customer ID who placed the order
     * References the customer in the user management system
     */
    @Column(name = "customer_id", nullable = false)
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    /**
     * Customer name for order reference
     * Denormalized for quick access and reporting
     */
    @Column(name = "customer_name", nullable = false, length = 200)
    @NotBlank(message = "Customer name is required")
    @Size(max = 200, message = "Customer name must not exceed 200 characters")
    private String customerName;

    /**
     * Customer email for order notifications
     */
    @Column(name = "customer_email", length = 100)
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String customerEmail;

    /**
     * Customer phone number for contact
     */
    @Column(name = "customer_phone", length = 20)
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String customerPhone;

    /**
     * Shipping address for the order
     * Complete address including street, city, state, and zip code
     */
    @Column(name = "shipping_address", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    /**
     * Billing address for the order
     * Can be same as shipping address or different
     */
    @Column(name = "billing_address", columnDefinition = "TEXT")
    private String billingAddress;

    /**
     * Order status
     * Tracks the current state of the order in the fulfillment process
     */
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    /**
     * Total order amount before taxes and discounts
     * Calculated as sum of all order items
     */
    @Column(name = "subtotal", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Subtotal is required")
    @DecimalMin(value = "0.00", message = "Subtotal must be non-negative")
    private BigDecimal subtotal;

    /**
     * Tax amount applied to the order
     * Calculated based on tax rates and location
     */
    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Tax amount is required")
    @DecimalMin(value = "0.00", message = "Tax amount must be non-negative")
    private BigDecimal taxAmount;

    /**
     * Shipping charges for the order
     * Calculated based on shipping method and weight/distance
     */
    @Column(name = "shipping_amount", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Shipping amount is required")
    @DecimalMin(value = "0.00", message = "Shipping amount must be non-negative")
    private BigDecimal shippingAmount;

    /**
     * Discount amount applied to the order
     * From promotional codes or bulk discounts
     */
    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Discount amount is required")
    @DecimalMin(value = "0.00", message = "Discount amount must be non-negative")
    private BigDecimal discountAmount;

    /**
     * Total order amount including taxes, shipping, and discounts
     * Final amount charged to the customer
     */
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    private BigDecimal totalAmount;

    /**
     * Payment method used for the order
     * Credit card, PayPal, bank transfer, etc.
     */
    @Column(name = "payment_method", length = 50)
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod;

    /**
     * Payment status for the order
     * Tracks whether payment has been processed
     */
    @Column(name = "payment_status", length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    /**
     * Transaction ID from payment gateway
     * Reference for payment tracking and refunds
     */
    @Column(name = "payment_transaction_id", length = 100)
    @Size(max = 100, message = "Transaction ID must not exceed 100 characters")
    private String paymentTransactionId;

    /**
     * Order notes or special instructions
     * Customer comments or internal notes
     */
    @Column(name = "order_notes", columnDefinition = "TEXT")
    private String orderNotes;

    /**
     * Internal notes for order processing
     * Not visible to customers
     */
    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;

    /**
     * Expected delivery date
     * Estimated date when order will be delivered
     */
    @Column(name = "expected_delivery_date")
    private LocalDateTime expectedDeliveryDate;

    /**
     * Actual delivery date
     * Date when order was delivered to customer
     */
    @Column(name = "actual_delivery_date")
    private LocalDateTime actualDeliveryDate;

    /**
     * Timestamp when the order was created
     * Automatically set when the record is created
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the order was last updated
     * Automatically updated when the record is modified
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * User who last modified the order
     * Tracks changes for audit purposes
     */
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    /**
     * Order items associated with this order
     * One-to-many relationship with OrderItem entity
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    /**
     * JPA lifecycle callback - called before entity is persisted
     * Sets the creation timestamp and generates order number
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // Generate unique order number if not set
        if (orderNumber == null) {
            orderNumber = generateOrderNumber();
        }
        // Set default status if not specified
        if (status == null) {
            status = OrderStatus.PENDING;
        }
        // Set default payment status if not specified
        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.PENDING;
        }
        // Initialize amounts to zero if not specified
        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (taxAmount == null) taxAmount = BigDecimal.ZERO;
        if (shippingAmount == null) shippingAmount = BigDecimal.ZERO;
        if (discountAmount == null) discountAmount = BigDecimal.ZERO;
        if (totalAmount == null) totalAmount = BigDecimal.ZERO;
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
     * Generate a unique order number
     * Format: ORD-YYYYMMDD-HHMMSS-Sequence
     * 
     * @return Unique order number string
     */
    private String generateOrderNumber() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("ORD-%d%02d%02d-%02d%02d%02d-%d",
                now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
                now.getHour(), now.getMinute(), now.getSecond(),
                System.currentTimeMillis() % 1000);
    }

    /**
     * Enum for order status
     * Defines the possible states of an order in the fulfillment process
     */
    public enum OrderStatus {
        PENDING("Pending"),           // Order received, awaiting processing
        CONFIRMED("Confirmed"),       // Order confirmed, inventory allocated
        PROCESSING("Processing"),     // Order being processed/picked
        SHIPPED("Shipped"),          // Order shipped, on the way
        DELIVERED("Delivered"),      // Order delivered to customer
        CANCELLED("Cancelled"),       // Order cancelled by customer or system
        RETURNED("Returned"),        // Order returned by customer
        REFUNDED("Refunded");        // Order refunded

        private final String displayName;

        OrderStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Enum for payment status
     * Defines the possible states of payment processing
     */
    public enum PaymentStatus {
        PENDING("Pending"),           // Payment awaiting processing
        PROCESSING("Processing"),     // Payment being processed
        COMPLETED("Completed"),       // Payment successfully processed
        FAILED("Failed"),            // Payment processing failed
        REFUNDED("Refunded"),         // Payment refunded
        PARTIALLY_REFUNDED("Partially Refunded"); // Partial refund processed

        private final String displayName;

        PaymentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
