package com.enterprise.inventory.order.dto;

import com.enterprise.inventory.order.model.OrderItem; // Import OrderItem entity
import lombok.AllArgsConstructor; // Import Lombok annotation for generating constructor with all parameters
import lombok.Builder; // Import Lombok annotation for builder pattern
import lombok.Data; // Import Lombok annotation for generating getters, setters, toString, equals, and hashCode
import lombok.NoArgsConstructor; // Import Lombok annotation for generating no-argument constructor

import javax.validation.constraints.*; // Import validation annotations
import java.math.BigDecimal; // Import BigDecimal for precise monetary calculations
import java.time.LocalDateTime; // Import LocalDateTime for timestamp handling

/**
 * Data Transfer Object (DTO) for OrderItem entity
 * This class is used for transferring order item data between layers and for API responses
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
public class OrderItemDto {

    /**
     * Unique order item identifier
     * Primary key from the database
     */
    private Long orderItemId;

    /**
     * Reference to the parent order
     * Order ID this item belongs to
     */
    private Long orderId;

    /**
     * Product ID from the inventory system
     * Must not be null for order item creation
     */
    @NotNull(message = "Product ID is required", groups = {CreateValidation.class})
    private Long productId;

    /**
     * Product SKU for reference
     * Must not be null or empty for order item creation
     */
    @NotBlank(message = "Product SKU is required", groups = {CreateValidation.class})
    @Size(max = 50, message = "Product SKU must not exceed 50 characters")
    private String productSku;

    /**
     * Product name for reference
     * Must not be null or empty for order item creation
     */
    @NotBlank(message = "Product name is required", groups = {CreateValidation.class})
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String productName;

    /**
     * Product description for reference
     * Optional field
     */
    private String productDescription;

    /**
     * Product category for reference
     */
    @Size(max = 100, message = "Product category must not exceed 100 characters")
    private String productCategory;

    /**
     * Unit price of the product at the time of ordering
     * Must be positive for order item creation
     */
    @NotNull(message = "Unit price is required", groups = {CreateValidation.class})
    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0", groups = {CreateValidation.class})
    private BigDecimal unitPrice;

    /**
     * Quantity of the product ordered
     * Must be positive for order item creation
     */
    @NotNull(message = "Quantity is required", groups = {CreateValidation.class})
    @Min(value = 1, message = "Quantity must be at least 1", groups = {CreateValidation.class})
    private Integer quantity;

    /**
     * Total price for this order item (unit_price * quantity)
     * Calculated automatically
     */
    @DecimalMin(value = "0.01", message = "Total price must be greater than 0")
    private BigDecimal totalPrice;

    /**
     * Discount amount applied to this specific item
     */
    @DecimalMin(value = "0.00", message = "Discount amount must be non-negative")
    private BigDecimal discountAmount;

    /**
     * Tax amount applied to this specific item
     */
    @DecimalMin(value = "0.00", message = "Tax amount must be non-negative")
    private BigDecimal taxAmount;

    /**
     * Final price after discounts and taxes
     * Total price minus discount plus tax
     */
    @DecimalMin(value = "0.01", message = "Final price must be greater than 0")
    private BigDecimal finalPrice;

    /**
     * Weight of the item for shipping calculations
     */
    @DecimalMin(value = "0.001", message = "Weight must be positive")
    private BigDecimal weight;

    /**
     * Dimensions of the item for shipping calculations
     * Length in centimeters
     */
    @DecimalMin(value = "0.01", message = "Length must be positive")
    private BigDecimal lengthCm;

    /**
     * Dimensions of the item for shipping calculations
     * Width in centimeters
     */
    @DecimalMin(value = "0.01", message = "Width must be positive")
    private BigDecimal widthCm;

    /**
     * Dimensions of the item for shipping calculations
     * Height in centimeters
     */
    @DecimalMin(value = "0.01", message = "Height must be positive")
    private BigDecimal heightCm;

    /**
     * Special instructions for this item
     * Gift wrapping, special handling, etc.
     */
    @Size(max = 500, message = "Special instructions must not exceed 500 characters")
    private String specialInstructions;

    /**
     * Status of this specific order item
     * Can be different from overall order status
     */
    private String status;

    /**
     * Reason for return or cancellation (if applicable)
     */
    @Size(max = 200, message = "Return reason must not exceed 200 characters")
    private String returnReason;

    /**
     * Timestamp when this order item was created
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when this order item was last updated
     */
    private LocalDateTime updatedAt;

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
     * Static factory method to convert OrderItem entity to OrderItemDto
     * This method handles the mapping between domain model and DTO
     * 
     * @param orderItem the OrderItem entity to convert
     * @return OrderItemDto with mapped values
     */
    public static OrderItemDto fromEntity(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }

        return OrderItemDto.builder()
                .orderItemId(orderItem.getOrderItemId())
                .orderId(orderItem.getOrder() != null ? orderItem.getOrder().getOrderId() : null)
                .productId(orderItem.getProductId())
                .productSku(orderItem.getProductSku())
                .productName(orderItem.getProductName())
                .productDescription(orderItem.getProductDescription())
                .productCategory(orderItem.getProductCategory())
                .unitPrice(orderItem.getUnitPrice())
                .quantity(orderItem.getQuantity())
                .totalPrice(orderItem.getTotalPrice())
                .discountAmount(orderItem.getDiscountAmount())
                .taxAmount(orderItem.getTaxAmount())
                .finalPrice(orderItem.getFinalPrice())
                .weight(orderItem.getWeight())
                .lengthCm(orderItem.getLengthCm())
                .widthCm(orderItem.getWidthCm())
                .heightCm(orderItem.getHeightCm())
                .specialInstructions(orderItem.getSpecialInstructions())
                .status(orderItem.getStatus() != null ? orderItem.getStatus().name() : null)
                .returnReason(orderItem.getReturnReason())
                .createdAt(orderItem.getCreatedAt())
                .updatedAt(orderItem.getUpdatedAt())
                .build();
    }
}
