package com.enterprise.inventory.service.controller;

import com.enterprise.inventory.service.dto.ProductDto; // Import Product DTO
import com.enterprise.inventory.service.service.ProductService; // Import Product service
import lombok.extern.slf4j.Slf4j; // Import Lombok's SLF4J annotation for logging
import org.springframework.beans.factory.annotation.Autowired; // Import Spring's Autowired annotation
import org.springframework.http.HttpStatus; // Import HTTP status codes
import org.springframework.http.ResponseEntity; // Import ResponseEntity for HTTP responses
import org.springframework.validation.annotation.Validated; // Import validation annotations
import org.springframework.web.bind.annotation.*; // Import REST controller annotations

import javax.validation.Valid; // Import validation annotation
import javax.validation.constraints.Min; // Import validation annotation
import javax.validation.constraints.NotBlank; // Import validation annotation
import java.math.BigDecimal; // Import BigDecimal for monetary calculations
import java.util.List; // Import List interface

/**
 * REST Controller for Product operations
 * This class handles HTTP requests for inventory management operations
 * 
 * @RestController: Combines @Controller and @ResponseBody for REST APIs
 * @RequestMapping: Base path for all endpoints in this controller
 * @Slf4j: Lombok annotation to add SLF4J logging capabilities
 * @Validated: Enables validation for method parameters
 */
@RestController
@RequestMapping("/api/products")
@Validated
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * Constructor-based dependency injection
     * Spring automatically injects the ProductService bean
     * 
     * @param productService the service for product business logic
     */
    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Create a new product
     * HTTP POST /api/products
     * 
     * @param productDto the product data to create
     * @return ResponseEntity with created product and HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto productDto) {
        log.info("REST request to create product: {}", productDto.getSku());
        
        ProductDto createdProduct = productService.createProduct(productDto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    /**
     * Update an existing product
     * HTTP PUT /api/products/{id}
     * 
     * @param productId the ID of the product to update
     * @param productDto the updated product data
     * @return ResponseEntity with updated product and HTTP 200 status
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable("id") @Min(value = 1, message = "Product ID must be positive") Long productId,
            @Valid @RequestBody ProductDto productDto) {
        log.info("REST request to update product with ID: {}", productId);
        
        ProductDto updatedProduct = productService.updateProduct(productId, productDto);
        
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Get a product by ID
     * HTTP GET /api/products/{id}
     * 
     * @param productId the ID of the product to retrieve
     * @return ResponseEntity with product data and HTTP 200 status
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(
            @PathVariable("id") @Min(value = 1, message = "Product ID must be positive") Long productId) {
        log.debug("REST request to get product with ID: {}", productId);
        
        ProductDto product = productService.getProductById(productId);
        
        return ResponseEntity.ok(product);
    }

    /**
     * Get a product by SKU
     * HTTP GET /api/products/sku/{sku}
     * 
     * @param sku the SKU of the product to retrieve
     * @return ResponseEntity with product data and HTTP 200 status
     */
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductDto> getProductBySku(
            @PathVariable("sku") @NotBlank(message = "SKU cannot be blank") String sku) {
        log.debug("REST request to get product with SKU: {}", sku);
        
        ProductDto product = productService.getProductBySku(sku);
        
        return ResponseEntity.ok(product);
    }

    /**
     * Get all products
     * HTTP GET /api/products
     * 
     * @return ResponseEntity with list of all products and HTTP 200 status
     */
    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        log.debug("REST request to get all products");
        
        List<ProductDto> products = productService.getAllProducts();
        
        return ResponseEntity.ok(products);
    }

    /**
     * Get products by category
     * HTTP GET /api/products/category/{category}
     * 
     * @param category the category to filter by
     * @return ResponseEntity with list of products in category and HTTP 200 status
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductDto>> getProductsByCategory(
            @PathVariable("category") @NotBlank(message = "Category cannot be blank") String category) {
        log.debug("REST request to get products by category: {}", category);
        
        List<ProductDto> products = productService.getProductsByCategory(category);
        
        return ResponseEntity.ok(products);
    }

    /**
     * Search products by name or SKU
     * HTTP GET /api/products/search?term={searchTerm}
     * 
     * @param searchTerm the search term to look for in name or SKU
     * @return ResponseEntity with list of matching products and HTTP 200 status
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductDto>> searchProducts(
            @RequestParam("term") @NotBlank(message = "Search term cannot be blank") String searchTerm) {
        log.debug("REST request to search products with term: {}", searchTerm);
        
        List<ProductDto> products = productService.searchProducts(searchTerm);
        
        return ResponseEntity.ok(products);
    }

    /**
     * Get products that need to be reordered
     * HTTP GET /api/products/reorder-needed
     * 
     * @return ResponseEntity with list of products needing reorder and HTTP 200 status
     */
    @GetMapping("/reorder-needed")
    public ResponseEntity<List<ProductDto>> getProductsNeedingReorder() {
        log.debug("REST request to get products needing reordering");
        
        List<ProductDto> products = productService.getProductsNeedingReorder();
        
        return ResponseEntity.ok(products);
    }

    /**
     * Get products with low stock
     * HTTP GET /api/products/low-stock
     * 
     * @return ResponseEntity with list of products with low stock and HTTP 200 status
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductDto>> getProductsWithLowStock() {
        log.debug("REST request to get products with low stock");
        
        List<ProductDto> products = productService.getProductsWithLowStock();
        
        return ResponseEntity.ok(products);
    }

    /**
     * Update stock quantity for a product
     * HTTP PATCH /api/products/{id}/stock?change={quantityChange}
     * 
     * @param productId the ID of the product
     * @param quantityChange the change in quantity (positive for addition, negative for subtraction)
     * @return ResponseEntity with updated product and HTTP 200 status
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductDto> updateStockQuantity(
            @PathVariable("id") @Min(value = 1, message = "Product ID must be positive") Long productId,
            @RequestParam("change") Integer quantityChange) {
        log.info("REST request to update stock quantity for product ID: {} by {}", productId, quantityChange);
        
        ProductDto updatedProduct = productService.updateStockQuantity(productId, quantityChange);
        
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Delete a product
     * HTTP DELETE /api/products/{id}
     * 
     * @param productId the ID of the product to delete
     * @return ResponseEntity with HTTP 204 NO_CONTENT status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable("id") @Min(value = 1, message = "Product ID must be positive") Long productId) {
        log.info("REST request to delete product with ID: {}", productId);
        
        productService.deleteProduct(productId);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Get total inventory value
     * HTTP GET /api/products/inventory-value
     * 
     * @return ResponseEntity with total inventory value and HTTP 200 status
     */
    @GetMapping("/inventory-value")
    public ResponseEntity<BigDecimal> getTotalInventoryValue() {
        log.debug("REST request to get total inventory value");
        
        BigDecimal totalValue = productService.getTotalInventoryValue();
        
        return ResponseEntity.ok(totalValue);
    }

    /**
     * Health check endpoint
     * HTTP GET /api/products/health
     * 
     * @return ResponseEntity with health status and HTTP 200 status
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.debug("Health check requested for Inventory Service");
        
        return ResponseEntity.ok("Inventory Service is healthy");
    }
}
