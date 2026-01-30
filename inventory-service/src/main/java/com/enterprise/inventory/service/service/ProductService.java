package com.enterprise.inventory.service.service;

import com.enterprise.inventory.service.dto.ProductDto; // Import Product DTO
import com.enterprise.inventory.service.exception.ResourceNotFoundException; // Import custom exception
import com.enterprise.inventory.service.exception.DuplicateResourceException; // Import custom exception
import com.enterprise.inventory.service.model.Product; // Import Product entity
import com.enterprise.inventory.service.repository.ProductRepository; // Import Product repository
import lombok.extern.slf4j.Slf4j; // Import Lombok's SLF4J annotation for logging
import org.springframework.beans.factory.annotation.Autowired; // Import Spring's Autowired annotation
import org.springframework.stereotype.Service; // Import Spring's Service annotation
import org.springframework.transaction.annotation.Transactional; // Import Transactional annotation

import java.math.BigDecimal; // Import BigDecimal for monetary calculations
import java.util.List; // Import List interface
import java.util.stream.Collectors; // Import Stream utilities for collection processing

/**
 * Service class for Product business logic
 * This class implements the core business operations for inventory management
 * 
 * @Service: Marks this class as a Spring service component
 * @Slf4j: Lombok annotation to add SLF4J logging capabilities
 * @Transactional: Enables transaction management for all methods
 */
@Service
@Transactional
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Constructor-based dependency injection
     * Spring automatically injects the ProductRepository bean
     * 
     * @param productRepository the repository for product data operations
     */
    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Create a new product in the inventory
     * Validates input and checks for duplicate SKU before creation
     * 
     * @param productDto the product data to create
     * @return ProductDto of the created product
     * @throws DuplicateResourceException if a product with the same SKU already exists
     */
    public ProductDto createProduct(ProductDto productDto) {
        log.info("Creating new product with SKU: {}", productDto.getSku());
        
        // Check if product with the same SKU already exists
        if (productRepository.existsBySku(productDto.getSku())) {
            log.warn("Product with SKU {} already exists", productDto.getSku());
            throw new DuplicateResourceException("Product with SKU " + productDto.getSku() + " already exists");
        }

        // Convert DTO to entity
        Product product = convertToEntity(productDto);
        
        // Save the product to database
        Product savedProduct = productRepository.save(product);
        
        log.info("Successfully created product with ID: {}", savedProduct.getProductId());
        
        // Convert back to DTO and return
        return ProductDto.fromEntity(savedProduct);
    }

    /**
     * Update an existing product
     * Validates that the product exists before updating
     * 
     * @param productId the ID of the product to update
     * @param productDto the updated product data
     * @return ProductDto of the updated product
     * @throws ResourceNotFoundException if the product is not found
     */
    public ProductDto updateProduct(Long productId, ProductDto productDto) {
        log.info("Updating product with ID: {}", productId);
        
        // Find existing product
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product with ID {} not found", productId);
                    return new ResourceNotFoundException("Product not found with id: " + productId);
                });

        // Check if SKU is being changed and if the new SKU already exists
        if (!existingProduct.getSku().equals(productDto.getSku()) && 
            productRepository.existsBySku(productDto.getSku())) {
            log.warn("Product with SKU {} already exists", productDto.getSku());
            throw new DuplicateResourceException("Product with SKU " + productDto.getSku() + " already exists");
        }

        // Update product fields
        updateProductFields(existingProduct, productDto);
        
        // Save the updated product
        Product updatedProduct = productRepository.save(existingProduct);
        
        log.info("Successfully updated product with ID: {}", updatedProduct.getProductId());
        
        return ProductDto.fromEntity(updatedProduct);
    }

    /**
     * Get a product by its ID
     * 
     * @param productId the ID of the product to retrieve
     * @return ProductDto of the found product
     * @throws ResourceNotFoundException if the product is not found
     */
    @Transactional(readOnly = true)
    public ProductDto getProductById(Long productId) {
        log.debug("Fetching product with ID: {}", productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product with ID {} not found", productId);
                    return new ResourceNotFoundException("Product not found with id: " + productId);
                });

        log.debug("Successfully fetched product with ID: {}", productId);
        return ProductDto.fromEntity(product);
    }

    /**
     * Get a product by its SKU
     * 
     * @param sku the SKU of the product to retrieve
     * @return ProductDto of the found product
     * @throws ResourceNotFoundException if the product is not found
     */
    @Transactional(readOnly = true)
    public ProductDto getProductBySku(String sku) {
        log.debug("Fetching product with SKU: {}", sku);
        
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> {
                    log.warn("Product with SKU {} not found", sku);
                    return new ResourceNotFoundException("Product not found with SKU: " + sku);
                });

        log.debug("Successfully fetched product with SKU: {}", sku);
        return ProductDto.fromEntity(product);
    }

    /**
     * Get all products in the inventory
     * 
     * @return List of all ProductDto objects
     */
    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts() {
        log.debug("Fetching all products");
        
        List<Product> products = productRepository.findAll();
        
        log.debug("Found {} products", products.size());
        return products.stream()
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get products by category
     * 
     * @param category the category to filter by
     * @return List of ProductDto objects in the specified category
     */
    @Transactional(readOnly = true)
    public List<ProductDto> getProductsByCategory(String category) {
        log.debug("Fetching products by category: {}", category);
        
        List<Product> products = productRepository.findByCategory(category);
        
        log.debug("Found {} products in category {}", products.size(), category);
        return products.stream()
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Search products by name or SKU
     * Performs case-insensitive search
     * 
     * @param searchTerm the search term
     * @return List of ProductDto objects matching the search criteria
     */
    @Transactional(readOnly = true)
    public List<ProductDto> searchProducts(String searchTerm) {
        log.debug("Searching products with term: {}", searchTerm);
        
        List<Product> products = productRepository.searchProductsByNameOrSku(searchTerm);
        
        log.debug("Found {} products matching search term: {}", products.size(), searchTerm);
        return products.stream()
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get products that need to be reordered
     * Products with quantity below reorder level
     * 
     * @return List of ProductDto objects that need reordering
     */
    @Transactional(readOnly = true)
    public List<ProductDto> getProductsNeedingReorder() {
        log.debug("Fetching products that need reordering");
        
        List<Product> products = productRepository.findProductsNeedingReorder();
        
        log.debug("Found {} products needing reordering", products.size());
        return products.stream()
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get products with low stock
     * Products with quantity at or below reorder level
     * 
     * @return List of ProductDto objects with low stock
     */
    @Transactional(readOnly = true)
    public List<ProductDto> getProductsWithLowStock() {
        log.debug("Fetching products with low stock");
        
        List<Product> products = productRepository.findProductsWithLowStock();
        
        log.debug("Found {} products with low stock", products.size());
        return products.stream()
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Update stock quantity for a product
     * 
     * @param productId the ID of the product
     * @param quantityChange the change in quantity (positive for addition, negative for subtraction)
     * @return ProductDto of the updated product
     * @throws ResourceNotFoundException if the product is not found
     * @throws IllegalArgumentException if the operation would result in negative stock
     */
    public ProductDto updateStockQuantity(Long productId, Integer quantityChange) {
        log.info("Updating stock quantity for product ID: {} by {}", productId, quantityChange);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product with ID {} not found", productId);
                    return new ResourceNotFoundException("Product not found with id: " + productId);
                });

        int newQuantity = product.getQuantityInStock() + quantityChange;
        
        if (newQuantity < 0) {
            log.warn("Invalid stock quantity update for product ID {}: would result in negative stock", productId);
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        product.setQuantityInStock(newQuantity);
        Product updatedProduct = productRepository.save(product);
        
        log.info("Successfully updated stock quantity for product ID: {} to {}", productId, newQuantity);
        
        return ProductDto.fromEntity(updatedProduct);
    }

    /**
     * Delete a product by its ID
     * 
     * @param productId the ID of the product to delete
     * @throws ResourceNotFoundException if the product is not found
     */
    public void deleteProduct(Long productId) {
        log.info("Deleting product with ID: {}", productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product with ID {} not found", productId);
                    return new ResourceNotFoundException("Product not found with id: " + productId);
                });

        productRepository.delete(product);
        
        log.info("Successfully deleted product with ID: {}", productId);
    }

    /**
     * Get total inventory value
     * Calculates the total value of all active inventory
     * 
     * @return Total inventory value
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalInventoryValue() {
        log.debug("Calculating total inventory value");
        
        BigDecimal totalValue = productRepository.getTotalInventoryValue();
        
        log.debug("Total inventory value: {}", totalValue);
        return totalValue != null ? totalValue : BigDecimal.ZERO;
    }

    /**
     * Convert ProductDto to Product entity
     * Helper method for entity conversion
     * 
     * @param productDto the DTO to convert
     * @return Product entity
     */
    private Product convertToEntity(ProductDto productDto) {
        return Product.builder()
                .sku(productDto.getSku())
                .name(productDto.getName())
                .description(productDto.getDescription())
                .category(productDto.getCategory())
                .unitPrice(productDto.getUnitPrice())
                .quantityInStock(productDto.getQuantityInStock() != null ? productDto.getQuantityInStock() : 0)
                .reorderLevel(productDto.getReorderLevel() != null ? productDto.getReorderLevel() : 10)
                .maxStockLevel(productDto.getMaxStockLevel())
                .location(productDto.getLocation())
                .supplier(productDto.getSupplier())
                .status(productDto.getStatus() != null ? 
                        Product.ProductStatus.valueOf(productDto.getStatus()) : Product.ProductStatus.ACTIVE)
                .build();
    }

    /**
     * Update fields of an existing product with data from DTO
     * Helper method for updating product entities
     * 
     * @param existingProduct the product to update
     * @param productDto the DTO with updated data
     */
    private void updateProductFields(Product existingProduct, ProductDto productDto) {
        existingProduct.setSku(productDto.getSku());
        existingProduct.setName(productDto.getName());
        existingProduct.setDescription(productDto.getDescription());
        existingProduct.setCategory(productDto.getCategory());
        existingProduct.setUnitPrice(productDto.getUnitPrice());
        existingProduct.setQuantityInStock(productDto.getQuantityInStock());
        existingProduct.setReorderLevel(productDto.getReorderLevel());
        existingProduct.setMaxStockLevel(productDto.getMaxStockLevel());
        existingProduct.setLocation(productDto.getLocation());
        existingProduct.setSupplier(productDto.getSupplier());
        if (productDto.getStatus() != null) {
            existingProduct.setStatus(Product.ProductStatus.valueOf(productDto.getStatus()));
        }
    }
}
