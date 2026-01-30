package com.enterprise.inventory.service.repository;

import com.enterprise.inventory.service.model.Product; // Import Product entity
import org.springframework.data.jpa.repository.JpaRepository; // Import Spring Data JPA repository
import org.springframework.data.jpa.repository.Query; // Import Query annotation for custom queries
import org.springframework.data.repository.query.Param; // Import Param annotation for named parameters
import org.springframework.stereotype.Repository; // Import Repository annotation

import java.util.List; // Import List interface
import java.util.Optional; // Import Optional for nullable results

/**
 * Spring Data JPA repository for Product entity
 * This interface provides database operations for Product entities
 * Spring Data automatically implements the methods defined in this interface
 * 
 * @Repository: Marks this interface as a Spring repository component
 * JpaRepository: Provides CRUD operations and pagination support
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find a product by its unique SKU (Stock Keeping Unit)
     * 
     * @param sku the SKU to search for
     * @return Optional containing the product if found, empty otherwise
     */
    Optional<Product> findBySku(String sku);

    /**
     * Find products by their category
     * 
     * @param category the category to filter by
     * @return List of products in the specified category
     */
    List<Product> findByCategory(String category);

    /**
     * Find products by their supplier
     * 
     * @param supplier the supplier to filter by
     * @return List of products from the specified supplier
     */
    List<Product> findBySupplier(String supplier);

    /**
     * Find products by their status
     * 
     * @param status the product status to filter by
     * @return List of products with the specified status
     */
    List<Product> findByStatus(Product.ProductStatus status);

    /**
     * Find products that need to be reordered
     * Products with quantity in stock less than their reorder level
     * 
     * @return List of products that need reordering
     */
    @Query("SELECT p FROM Product p WHERE p.quantityInStock < p.reorderLevel AND p.status = 'ACTIVE'")
    List<Product> findProductsNeedingReorder();

    /**
     * Find products with low stock
     * Products with quantity in stock less than or equal to their reorder level
     * 
     * @return List of products with low stock
     */
    @Query("SELECT p FROM Product p WHERE p.quantityInStock <= p.reorderLevel AND p.status = 'ACTIVE'")
    List<Product> findProductsWithLowStock();

    /**
     * Find products that are out of stock
     * Products with quantity in stock equal to 0
     * 
     * @return List of out-of-stock products
     */
    @Query("SELECT p FROM Product p WHERE p.quantityInStock = 0 AND p.status = 'ACTIVE'")
    List<Product> findOutOfStockProducts();

    /**
     * Search products by name or SKU
     * Uses case-insensitive search for better user experience
     * 
     * @param searchTerm the search term to look for in name or SKU
     * @return List of products matching the search criteria
     */
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Product> searchProductsByNameOrSku(@Param("searchTerm") String searchTerm);

    /**
     * Find products within a price range
     * Useful for filtering products by price
     * 
     * @param minPrice the minimum price (inclusive)
     * @param maxPrice the maximum price (inclusive)
     * @return List of products within the specified price range
     */
    @Query("SELECT p FROM Product p WHERE p.unitPrice BETWEEN :minPrice AND :maxPrice AND p.status = 'ACTIVE'")
    List<Product> findProductsByPriceRange(@Param("minPrice") java.math.BigDecimal minPrice, 
                                          @Param("maxPrice") java.math.BigDecimal maxPrice);

    /**
     * Find products by their location in the warehouse
     * 
     * @param location the warehouse location to search for
     * @return List of products at the specified location
     */
    List<Product> findByLocation(String location);

    /**
     * Count products by category
     * Useful for reporting and analytics
     * 
     * @param category the category to count products for
     * @return Number of products in the specified category
     */
    long countByCategory(String category);

    /**
     * Count products by status
     * Useful for reporting and analytics
     * 
     * @param status the product status to count
     * @return Number of products with the specified status
     */
    long countByStatus(Product.ProductStatus status);

    /**
     * Check if a product exists with the given SKU
     * Useful for validation before creating new products
     * 
     * @param sku the SKU to check
     * @return true if a product with the SKU exists, false otherwise
     */
    boolean existsBySku(String sku);

    /**
     * Find active products with quantity below a specific threshold
     * Custom threshold for low stock alerts
     * 
     * @param threshold the stock quantity threshold
     * @return List of products with quantity below the threshold
     */
    @Query("SELECT p FROM Product p WHERE p.quantityInStock < :threshold AND p.status = 'ACTIVE'")
    List<Product> findProductsWithStockBelowThreshold(@Param("threshold") Integer threshold);

    /**
     * Get total value of inventory for a specific category
     * Calculates sum of (quantity * unit price) for all products in category
     * 
     * @param category the category to calculate inventory value for
     * @return Total inventory value for the category
     */
    @Query("SELECT SUM(p.quantityInStock * p.unitPrice) FROM Product p WHERE p.category = :category AND p.status = 'ACTIVE'")
    java.math.BigDecimal getTotalInventoryValueByCategory(@Param("category") String category);

    /**
     * Get total value of all inventory
     * Calculates sum of (quantity * unit price) for all active products
     * 
     * @return Total inventory value across all products
     */
    @Query("SELECT SUM(p.quantityInStock * p.unitPrice) FROM Product p WHERE p.status = 'ACTIVE'")
    java.math.BigDecimal getTotalInventoryValue();
}
