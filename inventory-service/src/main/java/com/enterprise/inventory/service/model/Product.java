package com.enterprise.inventory.service.model;

import lombok.AllArgsConstructor; // Import Lombok annotation for generating constructor with all parameters
import lombok.Builder; // Import Lombok annotation for builder pattern
import lombok.Data; // Import Lombok annotation for generating getters, setters, toString, equals, and hashCode
import lombok.NoArgsConstructor; // Import Lombok annotation for generating no-argument constructor

import javax.persistence.*; // Import JPA annotations for entity mapping
import javax.validation.constraints.*; // Import validation annotations
import java.math.BigDecimal; // Import BigDecimal for precise monetary calculations
import java.time.LocalDateTime; // Import LocalDateTime for timestamp handling

/**
 * Product entity representing inventory items
 * This JPA entity maps to the 'products' table in the database
 * 
 * @Entity: Marks this class as a JPA entity
 * @Table: Specifies the database table name
 * @Data: Lombok annotation to generate getters, setters, toString, equals, and hashCode
 * @Builder: Lombok annotation to generate builder pattern
 * @NoArgsConstructor: Lombok annotation to generate no-argument constructor
 * @AllArgsConstructor: Lombok annotation to generate constructor with all parameters
 */
@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    /**
     * Primary key for the Product entity
     * Auto-generated sequence value for unique identification
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    /**
     * Unique product identifier/SKU
     * Must not be null or empty
     */
    @Column(name = "sku", unique = true, nullable = false, length = 50)
    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU must not exceed 50 characters")
    private String sku;

    /**
     * Product name
     * Must not be null or empty
     */
    @Column(name = "name", nullable = false, length = 200)
    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String name;

    /**
     * Detailed product description
     * Optional field that can be null
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Product category
     * Used for categorization and filtering
     */
    @Column(name = "category", length = 100)
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    /**
     * Unit price of the product
     * Uses BigDecimal for precise monetary calculations
     * Must be positive
     */
    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    private BigDecimal unitPrice;

    /**
     * Current stock quantity available
     * Must be zero or positive
     */
    @Column(name = "quantity_in_stock", nullable = false)
    @Min(value = 0, message = "Quantity in stock must be non-negative")
    private Integer quantityInStock;

    /**
     * Minimum stock level threshold
     * When stock falls below this level, reorder is triggered
     */
    @Column(name = "reorder_level", nullable = false)
    @Min(value = 0, message = "Reorder level must be non-negative")
    private Integer reorderLevel;

    /**
     * Maximum stock level capacity
     * Helps prevent overstocking
     */
    @Column(name = "max_stock_level")
    @Min(value = 0, message = "Max stock level must be non-negative")
    private Integer maxStockLevel;

    /**
     * Warehouse or storage location
     * Physical location of the product in the warehouse
     */
    @Column(name = "location", length = 100)
    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    /**
     * Supplier information
     * Identifies the supplier for this product
     */
    @Column(name = "supplier", length = 200)
    @Size(max = 200, message = "Supplier must not exceed 200 characters")
    private String supplier;

    /**
     * Product status
     * Indicates whether product is active, inactive, or discontinued
     */
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    /**
     * Timestamp when the product was created
     * Automatically set when the record is created
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the product was last updated
     * Automatically updated when the record is modified
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * User who last modified the product
     * Tracks changes for audit purposes
     */
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    /**
     * JPA lifecycle callback - called before entity is persisted
     * Sets the creation timestamp
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // Set default status if not specified
        if (status == null) {
            status = ProductStatus.ACTIVE;
        }
        // Set default reorder level if not specified
        if (reorderLevel == null) {
            reorderLevel = 10;
        }
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
     * Enum for product status
     * Defines the possible states of a product
     */
    public enum ProductStatus {
        ACTIVE("Active"),        // Product is available for sale
        INACTIVE("Inactive"),    // Product is temporarily unavailable
        DISCONTINUED("Discontinued"), // Product is no longer sold
        OUT_OF_STOCK("Out of Stock"); // Product is temporarily out of stock

        private final String displayName;

        ProductStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
