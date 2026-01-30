package com.enterprise.inventory.order.model;

import lombok.AllArgsConstructor; // Import Lombok annotation for generating constructor with all parameters
import lombok.Builder; // Import Lombok annotation for builder pattern
import lombok.Data; // Import Lombok annotation for generating getters, setters, toString, equals, and hashCode
import lombok.NoArgsConstructor; // Import Lombok annotation for generating no-argument constructor

import javax.persistence.*; // Import JPA annotations for entity mapping
import javax.validation.constraints.*; // Import validation annotations
import java.math.BigDecimal; // Import BigDecimal for precise monetary calculations
import java.time.LocalDateTime; // Import LocalDateTime for timestamp handling

/**
 * OrderItem entity representing individual items within an order
 * This JPA entity maps to the 'order_items' table in the database
 * Each order item represents a specific product and quantity ordered
 * 
 * @Entity: Marks this class as a JPA entity
 * @Table: Specifies the database table name
 * @Data: Lombok annotation to generate getters, setters, toString, equals, and hashCode
 * @Builder: Lombok annotation to generate builder pattern
 * @NoArgsConstructor: Lombok annotation to generate no-argument constructor
 * @AllArgsConstructor: Lombok annotation to generate constructor with all parameters
 */
@Entity
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    /**
     * Primary key for the OrderItem entity
     * Auto-generated sequence value for unique identification
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    /**
     * Reference to the parent order
     * Many-to-one relationship with Order entity
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Product ID from the inventory system
     * References the product being ordered
     */
    @Column(name = "product_id", nullable = false)
    @NotNull(message = "Product ID is required")
    private Long productId;

    /**
     * Product SKU for reference
     * Denormalized for quick access and reporting
     */
    @Column(name = "product_sku", nullable = false, length = 50)
    @NotBlank(message = "Product SKU is required")
    @Size(max = 50, message = "Product SKU must not exceed 50 characters")
    private String productSku;

    /**
     * Product name for reference
     * Denormalized for quick access and reporting
     */
    @Column(name = "product_name", nullable = false, length = 200)
    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String productName;

    /**
     * Product description for reference
     * Denormalized for order details
     */
    @Column(name = "product_description", columnDefinition = "TEXT")
    private String productDescription;

    /**
     * Product category for reference
     * Denormalized for reporting and analytics
     */
    @Column(name = "product_category", length = 100)
    @Size(max = 100, message = "Product category must not exceed 100 characters")
    private String productCategory;

    /**
     * Unit price of the product at the time of ordering
     * Important for maintaining historical price data
     */
    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    private BigDecimal unitPrice;

    /**
     * Quantity of the product ordered
     * Must be positive
     */
    @Column(name = "quantity", nullable = false)
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    /**
     * Total price for this order item (unit_price * quantity)
     * Calculated automatically
     */
    @Column(name = "total_price", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Total price is required")
    @DecimalMin(value = "0.01", message = "Total price must be greater than 0")
    private BigDecimal totalPrice;

    /**
     * Discount amount applied to this specific item
     * Per-item discount or promotional discount
     */
    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Discount amount is required")
    @DecimalMin(value = "0.00", message = "Discount amount must be non-negative")
    private BigDecimal discountAmount;

    /**
     * Tax amount applied to this specific item
     * Calculated based on tax rates and item price
     */
    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Tax amount is required")
    @DecimalMin(value = "0.00", message = "Tax amount must be non-negative")
    private BigDecimal taxAmount;

    /**
     * Final price after discounts and taxes
     * Total price minus discount plus tax
     */
    @Column(name = "final_price", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Final price is required")
    @DecimalMin(value = "0.01", message = "Final price must be greater than 0")
    private BigDecimal finalPrice;

    /**
     * Weight of the item for shipping calculations
     * Used for determining shipping costs
     */
    @Column(name = "weight", precision = 10, scale = 3)
    @DecimalMin(value = "0.001", message = "Weight must be positive")
    private BigDecimal weight;

    /**
     * Dimensions of the item for shipping calculations
     * Length in centimeters
     */
    @Column(name = "length_cm", precision = 8, scale = 2)
    @DecimalMin(value = "0.01", message = "Length must be positive")
    private BigDecimal lengthCm;

    /**
     * Dimensions of the item for shipping calculations
     * Width in centimeters
     */
    @Column(name = "width_cm", precision = 8, scale = 2)
    @DecimalMin(value = "0.01", message = "Width must be positive")
    private BigDecimal widthCm;

    /**
     * Dimensions of the item for shipping calculations
     * Height in centimeters
     */
    @Column(name = "height_cm", precision = 8, scale = 2)
    @DecimalMin(value = "0.01", message = "Height must be positive")
    private BigDecimal heightCm;

    /**
     * Special instructions for this item
     * Gift wrapping, special handling, etc.
     */
    @Column(name = "special_instructions", length = 500)
    @Size(max = 500, message = "Special instructions must not exceed 500 characters")
    private String specialInstructions;

    /**
     * Status of this specific order item
     * Can be different from overall order status
     */
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private OrderItemStatus status;

    /**
     * Reason for return or cancellation (if applicable)
     * Used for customer service and analytics
     */
    @Column(name = "return_reason", length = 200)
    @Size(max = 200, message = "Return reason must not exceed 200 characters")
    private String returnReason;

    /**
     * Timestamp when this order item was created
     * Automatically set when the record is created
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when this order item was last updated
     * Automatically updated when the record is modified
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * JPA lifecycle callback - called before entity is persisted
     * Sets the creation timestamp and calculates prices
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // Calculate total price if not set
        if (totalPrice == null && unitPrice != null && quantity != null) {
            totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
        // Initialize amounts to zero if not specified
        if (discountAmount == null) discountAmount = BigDecimal.ZERO;
        if (taxAmount == null) taxAmount = BigDecimal.ZERO;
        // Calculate final price
        calculateFinalPrice();
        // Set default status if not specified
        if (status == null) {
            status = OrderItemStatus.ORDERED;
        }
    }

    /**
     * JPA lifecycle callback - called before entity is updated
     * Updates the modification timestamp and recalculates prices
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // Recalculate total price if unit price or quantity changed
        if (unitPrice != null && quantity != null) {
            totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
        // Recalculate final price
        calculateFinalPrice();
    }

    /**
     * Calculate the final price for this order item
     * Final price = total price - discount amount + tax amount
     */
    private void calculateFinalPrice() {
        if (totalPrice != null && discountAmount != null && taxAmount != null) {
            finalPrice = totalPrice.subtract(discountAmount).add(taxAmount);
        }
    }

    /**
     * Enum for order item status
     * Defines the possible states of an individual order item
     */
    public enum OrderItemStatus {
        ORDERED("Ordered"),           // Item ordered, awaiting processing
        CONFIRMED("Confirmed"),       // Item confirmed, inventory allocated
        PROCESSING("Processing"),     // Item being processed/picked
        SHIPPED("Shipped"),          // Item shipped, on the way
        DELIVERED("Delivered"),      // Item delivered to customer
        CANCELLED("Cancelled"),       // Item cancelled
        RETURNED("Returned"),        // Item returned by customer
        REFUNDED("Refunded"),        // Item refunded
        BACKORDERED("Backordered");   // Item on backorder

        private final String displayName;

        OrderItemStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
