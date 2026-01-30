package com.enterprise.inventory.order.client;

import com.enterprise.inventory.order.dto.ProductDto; // Import Product DTO
import org.springframework.cloud.openfeign.FeignClient; // Import Feign client annotation
import org.springframework.web.bind.annotation.GetMapping; // Import GET mapping annotation
import org.springframework.web.bind.annotation.PathVariable; // Import PathVariable annotation
import org.springframework.web.bind.annotation.PostMapping; // Import POST mapping annotation
import org.springframework.web.bind.annotation.RequestBody; // Import RequestBody annotation
import org.springframework.web.bind.annotation.RequestParam; // Import RequestParam annotation

import javax.validation.Valid; // Import validation annotation
import java.util.List; // Import List interface

/**
 * Feign client for communicating with Inventory Service
 * This interface defines the REST API calls to the Inventory Service
 * Feign automatically implements the HTTP client based on these method definitions
 * 
 * @FeignClient: Marks this interface as a Feign client
 * name: Logical name of the service for load balancing
 * url: Direct URL (fallback when service discovery is not available)
 */
@FeignClient(name = "inventory-service", url = "${inventory.service.url:http://localhost:8081}")
public interface InventoryServiceClient {

    /**
     * Get a product by its ID
     * Calls the Inventory Service to retrieve product information
     * 
     * @param productId the ID of the product to retrieve
     * @return ProductDto containing product information
     */
    @GetMapping("/api/products/{id}")
    ProductDto getProductById(@PathVariable("id") Long productId);

    /**
     * Get a product by its SKU
     * Calls the Inventory Service to retrieve product information by SKU
     * 
     * @param sku the SKU of the product to retrieve
     * @return ProductDto containing product information
     */
    @GetMapping("/api/products/sku/{sku}")
    ProductDto getProductBySku(@PathVariable("sku") String sku);

    /**
     * Update stock quantity for a product
     * Calls the Inventory Service to adjust stock levels when orders are placed
     * 
     * @param productId the ID of the product
     * @param quantityChange the change in quantity (negative for orders)
     * @return ProductDto with updated stock information
     */
    @PostMapping("/api/products/{id}/stock")
    ProductDto updateStockQuantity(@PathVariable("id") Long productId, 
                                  @RequestParam("change") Integer quantityChange);

    /**
     * Check if a product exists and has sufficient stock
     * Calls the Inventory Service to validate product availability
     * 
     * @param productId the ID of the product to check
     * @param requiredQuantity the quantity needed
     * @return ProductDto if product exists and has sufficient stock
     */
    @GetMapping("/api/products/{id}/availability")
    ProductDto checkProductAvailability(@PathVariable("id") Long productId, 
                                      @RequestParam("quantity") Integer requiredQuantity);

    /**
     * Get multiple products by their IDs
     * Calls the Inventory Service to retrieve multiple products at once
     * Used for order validation and batch operations
     * 
     * @param productIds list of product IDs to retrieve
     * @return List of ProductDto objects
     */
    @PostMapping("/api/products/batch")
    List<ProductDto> getProductsByIds(@RequestBody List<Long> productIds);

    /**
     * Reserve products for an order
     * Calls the Inventory Service to reserve stock for a pending order
     * 
     * @param productReservations list of product reservations
     * @return List of ProductDto with updated reservation status
     */
    @PostMapping("/api/products/reserve")
    List<ProductDto> reserveProducts(@RequestBody List<ProductReservation> productReservations);

    /**
     * Release product reservations
     * Calls the Inventory Service to release reserved stock when order is cancelled
     * 
     * @param productReservations list of product reservations to release
     * @return List of ProductDto with updated reservation status
     */
    @PostMapping("/api/products/release")
    List<ProductDto> releaseProductReservations(@RequestBody List<ProductReservation> productReservations);

    /**
     * Confirm product reservations (convert to actual stock reduction)
     * Calls the Inventory Service to confirm reservations when order is shipped
     * 
     * @param productReservations list of product reservations to confirm
     * @return List of ProductDto with updated stock levels
     */
    @PostMapping("/api/products/confirm-reservation")
    List<ProductDto> confirmProductReservations(@RequestBody List<ProductReservation> productReservations);

    /**
     * Inner class for product reservation requests
     * Used for batch reservation operations
     */
    class ProductReservation {
        private Long productId;
        private Integer quantity;
        private String reservationId;
        private String orderNumber;

        // Default constructor
        public ProductReservation() {}

        // Parameterized constructor
        public ProductReservation(Long productId, Integer quantity, String reservationId, String orderNumber) {
            this.productId = productId;
            this.quantity = quantity;
            this.reservationId = reservationId;
            this.orderNumber = orderNumber;
        }

        // Getters and setters
        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public String getReservationId() {
            return reservationId;
        }

        public void setReservationId(String reservationId) {
            this.reservationId = reservationId;
        }

        public String getOrderNumber() {
            return orderNumber;
        }

        public void setOrderNumber(String orderNumber) {
            this.orderNumber = orderNumber;
        }
    }
}
