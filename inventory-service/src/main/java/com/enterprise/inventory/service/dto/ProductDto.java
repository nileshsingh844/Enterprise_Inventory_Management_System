package com.enterprise.inventory.service.dto;

import com.enterprise.inventory.service.model.Product; // Import Product entity for conversion
import lombok.AllArgsConstructor; // Import Lombok annotation for generating constructor with all parameters
import lombok.Builder; // Import Lombok annotation for builder pattern
import lombok.Data; // Import Lombok annotation for generating getters, setters, toString, equals, and hashCode
import lombok.NoArgsConstructor; // Import Lombok annotation for generating no-argument constructor

import javax.validation.constraints.*; // Import validation annotations
import java.math.BigDecimal; // Import BigDecimal for precise monetary calculations

/**
 * Data Transfer Object (DTO) for Product entity
 * This class is used for transferring product data between layers and for API responses
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
public class ProductDto {

    /**
     * Unique product identifier
     * Primary key from the database
     */
    private Long productId;

    /**
     * Unique product identifier/SKU
     * Must not be null or empty for creation
     */
    @NotBlank(message = "SKU is required", groups = {CreateValidation.class})
    @Size(max = 50, message = "SKU must not exceed 50 characters")
    private String sku;

    /**
     * Product name
     * Must not be null or empty for creation
     */
    @NotBlank(message = "Product name is required", groups = {CreateValidation.class})
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String name;

    /**
     * Detailed product description
     * Optional field
     */
    private String description;

    /**
     * Product category
     * Used for categorization and filtering
     */
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    /**
     * Unit price of the product
     * Must be positive for creation
     */
    @NotNull(message = "Unit price is required", groups = {CreateValidation.class})
    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0", groups = {CreateValidation.class})
    private BigDecimal unitPrice;

    /**
     * Current stock quantity available
     * Must be zero or positive
     */
    @Min(value = 0, message = "Quantity in stock must be non-negative")
    private Integer quantityInStock;

    /**
     * Minimum stock level threshold
     * When stock falls below this level, reorder is triggered
     */
    @Min(value = 0, message = "Reorder level must be non-negative")
    private Integer reorderLevel;

    /**
     * Maximum stock level capacity
     * Helps prevent overstocking
     */
    @Min(value = 0, message = "Max stock level must be non-negative")
    private Integer maxStockLevel;

    /**
     * Warehouse or storage location
     * Physical location of the product in the warehouse
     */
    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    /**
     * Supplier information
     * Identifies the supplier for this product
     */
    @Size(max = 200, message = "Supplier must not exceed 200 characters")
    private String supplier;

    /**
     * Product status
     * Indicates whether product is active, inactive, or discontinued
     */
    private String status;

    /**
     * Flag indicating if stock is low
     * Computed field based on quantity and reorder level
     */
    private Boolean lowStock;

    /**
     * Flag indicating if stock needs to be reordered
     * Computed field based on quantity and reorder level
     */
    private Boolean needsReorder;

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
     * Static factory method to convert Product entity to ProductDto
     * This method handles the mapping between domain model and DTO
     * 
     * @param product the Product entity to convert
     * @return ProductDto with mapped values
     */
    public static ProductDto fromEntity(Product product) {
        if (product == null) {
            return null;
        }

        return ProductDto.builder()
                .productId(product.getProductId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .unitPrice(product.getUnitPrice())
                .quantityInStock(product.getQuantityInStock())
                .reorderLevel(product.getReorderLevel())
                .maxStockLevel(product.getMaxStockLevel())
                .location(product.getLocation())
                .supplier(product.getSupplier())
                .status(product.getStatus() != null ? product.getStatus().name() : null)
                .lowStock(product.getQuantityInStock() != null && product.getReorderLevel() != null 
                        && product.getQuantityInStock() <= product.getReorderLevel())
                .needsReorder(product.getQuantityInStock() != null && product.getReorderLevel() != null 
                        && product.getQuantityInStock() < product.getReorderLevel())
                .build();
    }
}
