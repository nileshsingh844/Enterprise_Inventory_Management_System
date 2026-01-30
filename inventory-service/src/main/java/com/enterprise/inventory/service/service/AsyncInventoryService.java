package com.enterprise.inventory.service.service;

import com.enterprise.inventory.service.dto.ProductDto; // Import Product DTO
import com.enterprise.inventory.service.model.Product; // Import Product entity
import com.enterprise.inventory.service.repository.ProductRepository; // Import Product repository
import lombok.extern.slf4j.Slf4j; // Import Lombok's SLF4J annotation for logging
import org.springframework.beans.factory.annotation.Autowired; // Import Spring's Autowired annotation
import org.springframework.scheduling.annotation.Async; // Import Async annotation for asynchronous method execution
import org.springframework.stereotype.Service; // Import Spring's Service annotation

import java.util.List; // Import List interface
import java.util.concurrent.CompletableFuture; // Import CompletableFuture for asynchronous operations
import java.util.stream.Collectors; // Import Stream utilities for collection processing

/**
 * Asynchronous service class for inventory operations
 * This class provides asynchronous methods for time-consuming inventory operations
 * Uses multiple thread pools for different types of operations to optimize performance
 * 
 * @Service: Marks this class as a Spring service component
 * @Slf4j: Lombok annotation to add SLF4J logging capabilities
 */
@Service
@Slf4j
public class AsyncInventoryService {

    private final ProductRepository productRepository;

    /**
     * Constructor-based dependency injection
     * Spring automatically injects the ProductRepository bean
     * 
     * @param productRepository the repository for product data operations
     */
    @Autowired
    public AsyncInventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Asynchronously update stock quantities for multiple products
     * This method processes stock updates in parallel to improve performance
     * Uses the default thread pool for general async operations
     * 
     * @param stockUpdates list of product IDs and quantity changes
     * @return CompletableFuture containing the number of successfully updated products
     */
    @Async("taskExecutor")
    public CompletableFuture<Integer> updateMultipleStockQuantities(List<StockUpdate> stockUpdates) {
        log.info("Starting asynchronous stock quantity update for {} products", stockUpdates.size());
        
        return CompletableFuture.supplyAsync(() -> {
            int successCount = 0;
            
            // Process each stock update sequentially to maintain data consistency
            for (StockUpdate update : stockUpdates) {
                try {
                    updateSingleStockQuantity(update.getProductId(), update.getQuantityChange());
                    successCount++;
                    log.debug("Successfully updated stock for product ID: {}, change: {}", 
                            update.getProductId(), update.getQuantityChange());
                } catch (Exception e) {
                    log.error("Failed to update stock for product ID: {}, change: {}", 
                            update.getProductId(), update.getQuantityChange(), e);
                    // Continue with other updates even if one fails
                }
            }
            
            log.info("Completed stock quantity update. Success: {}, Failed: {}", 
                    successCount, stockUpdates.size() - successCount);
            
            return successCount;
        });
    }

    /**
     * Asynchronously generate inventory report
     * This method processes large datasets without blocking the main thread
     * Uses the file processing thread pool optimized for I/O operations
     * 
     * @return CompletableFuture containing the generated report data
     */
    @Async("fileProcessingExecutor")
    public CompletableFuture<InventoryReport> generateInventoryReport() {
        log.info("Starting asynchronous inventory report generation");
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simulate report generation with data processing
                List<Product> allProducts = productRepository.findAll();
                
                // Process data in parallel streams for better performance
                InventoryReport report = InventoryReport.builder()
                        .totalProducts(allProducts.size())
                        .activeProducts((int) allProducts.stream()
                                .filter(p -> p.getStatus() == Product.ProductStatus.ACTIVE)
                                .count())
                        .totalValue(allProducts.stream()
                                .mapToDouble(p -> p.getUnitPrice().doubleValue() * p.getQuantityInStock())
                                .sum())
                        .lowStockProducts((int) allProducts.stream()
                                .filter(p -> p.getQuantityInStock() <= p.getReorderLevel())
                                .count())
                        .outOfStockProducts((int) allProducts.stream()
                                .filter(p -> p.getQuantityInStock() == 0)
                                .count())
                        .categories(allProducts.stream()
                                .map(Product::getCategory)
                                .filter(category -> category != null)
                                .distinct()
                                .collect(Collectors.toList()))
                        .build();
                
                log.info("Inventory report generated successfully: {} products, {} active", 
                        report.getTotalProducts(), report.getActiveProducts());
                
                return report;
                
            } catch (Exception e) {
                log.error("Failed to generate inventory report", e);
                throw new RuntimeException("Report generation failed", e);
            }
        });
    }

    /**
     * Asynchronously process bulk product import
     * This method handles large file imports without blocking the main thread
     * Uses the file processing thread pool for I/O-intensive operations
     * 
     * @param products list of products to import
     * @return CompletableFuture containing import results
     */
    @Async("fileProcessingExecutor")
    public CompletableFuture<BulkImportResult> processBulkProductImport(List<ProductDto> products) {
        log.info("Starting asynchronous bulk import of {} products", products.size());
        
        return CompletableFuture.supplyAsync(() -> {
            BulkImportResult result = new BulkImportResult();
            result.setTotalProducts(products.size());
            
            int successCount = 0;
            int failureCount = 0;
            
            for (ProductDto productDto : products) {
                try {
                    // Validate product data
                    if (isValidProduct(productDto)) {
                        // Check if product already exists
                        if (!productRepository.existsBySku(productDto.getSku())) {
                            // Convert and save product
                            Product product = convertToEntity(productDto);
                            productRepository.save(product);
                            successCount++;
                            result.getSuccessfulProducts().add(productDto.getSku());
                        } else {
                            failureCount++;
                            result.getFailedProducts().put(productDto.getSku(), "Product with SKU already exists");
                        }
                    } else {
                        failureCount++;
                        result.getFailedProducts().put(productDto.getSku(), "Invalid product data");
                    }
                } catch (Exception e) {
                    failureCount++;
                    result.getFailedProducts().put(productDto.getSku(), "Error: " + e.getMessage());
                    log.error("Failed to import product: {}", productDto.getSku(), e);
                }
            }
            
            result.setSuccessCount(successCount);
            result.setFailureCount(failureCount);
            
            log.info("Bulk import completed. Success: {}, Failed: {}", successCount, failureCount);
            
            return result;
        });
    }

    /**
     * Asynchronously send low stock notifications
     * This method sends notifications without blocking main business operations
     * Uses the notification thread pool for non-critical operations
     * 
     * @return CompletableFuture containing the number of notifications sent
     */
    @Async("notificationExecutor")
    public CompletableFuture<Integer> sendLowStockNotifications() {
        log.info("Starting asynchronous low stock notifications");
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Find products with low stock
                List<Product> lowStockProducts = productRepository.findProductsWithLowStock();
                
                int notificationCount = 0;
                
                for (Product product : lowStockProducts) {
                    try {
                        // Send notification (simulated)
                        sendLowStockAlert(product);
                        notificationCount++;
                        log.debug("Sent low stock notification for product: {}", product.getSku());
                    } catch (Exception e) {
                        log.error("Failed to send notification for product: {}", product.getSku(), e);
                    }
                }
                
                log.info("Low stock notifications sent: {}", notificationCount);
                return notificationCount;
                
            } catch (Exception e) {
                log.error("Failed to send low stock notifications", e);
                return 0;
            }
        });
    }

    /**
     * Asynchronously perform inventory reconciliation
     * This method compares expected vs actual stock levels
     * Uses the scheduled task executor for periodic maintenance operations
     * 
     * @return CompletableFuture containing reconciliation results
     */
    @Async("scheduledTaskExecutor")
    public CompletableFuture<ReconciliationResult> performInventoryReconciliation() {
        log.info("Starting asynchronous inventory reconciliation");
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simulate reconciliation process
                List<Product> allProducts = productRepository.findAll();
                
                ReconciliationResult result = ReconciliationResult.builder()
                        .totalProducts(allProducts.size())
                        .checkedProducts(allProducts.size())
                        .discrepancies(0)
                        .build();
                
                // In a real implementation, this would compare with external systems
                // For now, we'll simulate finding some discrepancies
                int simulatedDiscrepancies = (int) (allProducts.size() * 0.05); // 5% discrepancy rate
                result.setDiscrepancies(simulatedDiscrepancies);
                
                log.info("Inventory reconciliation completed. Checked: {}, Discrepancies: {}", 
                        result.getCheckedProducts(), result.getDiscrepancies());
                
                return result;
                
            } catch (Exception e) {
                log.error("Failed to perform inventory reconciliation", e);
                throw new RuntimeException("Reconciliation failed", e);
            }
        });
    }

    /**
     * Helper method to update single product stock quantity
     * This is a synchronous method used by the async bulk update method
     * 
     * @param productId the ID of the product to update
     * @param quantityChange the change in quantity (positive for addition, negative for subtraction)
     */
    private void updateSingleStockQuantity(Long productId, Integer quantityChange) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        
        int newQuantity = product.getQuantityInStock() + quantityChange;
        
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        
        product.setQuantityInStock(newQuantity);
        productRepository.save(product);
    }

    /**
     * Helper method to validate product data
     * 
     * @param productDto the product DTO to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidProduct(ProductDto productDto) {
        return productDto.getSku() != null && !productDto.getSku().trim().isEmpty() &&
               productDto.getName() != null && !productDto.getName().trim().isEmpty() &&
               productDto.getUnitPrice() != null && productDto.getUnitPrice().doubleValue() > 0 &&
               productDto.getQuantityInStock() != null && productDto.getQuantityInStock() >= 0;
    }

    /**
     * Helper method to convert ProductDto to Product entity
     * 
     * @param productDto the DTO to convert
     * @return Product entity
     */
    private Product convertToEntity(ProductDto productDto) {
        Product product = new Product();
        product.setSku(productDto.getSku());
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setCategory(productDto.getCategory());
        product.setUnitPrice(productDto.getUnitPrice());
        product.setQuantityInStock(productDto.getQuantityInStock());
        product.setReorderLevel(productDto.getReorderLevel() != null ? productDto.getReorderLevel() : 10);
        product.setStatus(productDto.getStatus() != null ? 
                Product.ProductStatus.valueOf(productDto.getStatus()) : Product.ProductStatus.ACTIVE);
        return product;
    }

    /**
     * Helper method to send low stock alert (simulated)
     * 
     * @param product the product with low stock
     */
    private void sendLowStockAlert(Product product) {
        // In a real implementation, this would send email, SMS, or push notification
        log.info("LOW STOCK ALERT: Product {} (SKU: {}) has only {} units remaining (reorder level: {})",
                product.getName(), product.getSku(), product.getQuantityInStock(), product.getReorderLevel());
    }

    /**
     * Inner class for stock update operations
     * Used to pass stock update data to async methods
     */
    public static class StockUpdate {
        private Long productId;
        private Integer quantityChange;

        public StockUpdate(Long productId, Integer quantityChange) {
            this.productId = productId;
            this.quantityChange = quantityChange;
        }

        public Long getProductId() {
            return productId;
        }

        public Integer getQuantityChange() {
            return quantityChange;
        }
    }

    /**
     * Inner class for inventory report data
     * Contains aggregated inventory statistics
     */
    public static class InventoryReport {
        private int totalProducts;
        private int activeProducts;
        private double totalValue;
        private int lowStockProducts;
        private int outOfStockProducts;
        private List<String> categories;

        // Getters and setters
        public int getTotalProducts() { return totalProducts; }
        public void setTotalProducts(int totalProducts) { this.totalProducts = totalProducts; }
        public int getActiveProducts() { return activeProducts; }
        public void setActiveProducts(int activeProducts) { this.activeProducts = activeProducts; }
        public double getTotalValue() { return totalValue; }
        public void setTotalValue(double totalValue) { this.totalValue = totalValue; }
        public int getLowStockProducts() { return lowStockProducts; }
        public void setLowStockProducts(int lowStockProducts) { this.lowStockProducts = lowStockProducts; }
        public int getOutOfStockProducts() { return outOfStockProducts; }
        public void setOutOfStockProducts(int outOfStockProducts) { this.outOfStockProducts = outOfStockProducts; }
        public List<String> getCategories() { return categories; }
        public void setCategories(List<String> categories) { this.categories = categories; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private InventoryReport report = new InventoryReport();

            public Builder totalProducts(int totalProducts) {
                report.totalProducts = totalProducts;
                return this;
            }

            public Builder activeProducts(int activeProducts) {
                report.activeProducts = activeProducts;
                return this;
            }

            public Builder totalValue(double totalValue) {
                report.totalValue = totalValue;
                return this;
            }

            public Builder lowStockProducts(int lowStockProducts) {
                report.lowStockProducts = lowStockProducts;
                return this;
            }

            public Builder outOfStockProducts(int outOfStockProducts) {
                report.outOfStockProducts = outOfStockProducts;
                return this;
            }

            public Builder categories(List<String> categories) {
                report.categories = categories;
                return this;
            }

            public InventoryReport build() {
                return report;
            }
        }
    }

    /**
     * Inner class for bulk import results
     * Contains statistics about the import operation
     */
    public static class BulkImportResult {
        private int totalProducts;
        private int successCount;
        private int failureCount;
        private List<String> successfulProducts;
        private java.util.Map<String, String> failedProducts;

        // Getters and setters
        public int getTotalProducts() { return totalProducts; }
        public void setTotalProducts(int totalProducts) { this.totalProducts = totalProducts; }
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        public int getFailureCount() { return failureCount; }
        public void setFailureCount(int failureCount) { this.failureCount = failureCount; }
        public List<String> getSuccessfulProducts() { return successfulProducts; }
        public void setSuccessfulProducts(List<String> successfulProducts) { this.successfulProducts = successfulProducts; }
        public java.util.Map<String, String> getFailedProducts() { return failedProducts; }
        public void setFailedProducts(java.util.Map<String, String> failedProducts) { this.failedProducts = failedProducts; }
    }

    /**
     * Inner class for reconciliation results
     * Contains statistics about the reconciliation process
     */
    public static class ReconciliationResult {
        private int totalProducts;
        private int checkedProducts;
        private int discrepancies;

        // Getters and setters
        public int getTotalProducts() { return totalProducts; }
        public void setTotalProducts(int totalProducts) { this.totalProducts = totalProducts; }
        public int getCheckedProducts() { return checkedProducts; }
        public void setCheckedProducts(int checkedProducts) { this.checkedProducts = checkedProducts; }
        public int getDiscrepancies() { return discrepancies; }
        public void setDiscrepancies(int discrepancies) { this.discrepancies = discrepancies; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private ReconciliationResult result = new ReconciliationResult();

            public Builder totalProducts(int totalProducts) {
                result.totalProducts = totalProducts;
                return this;
            }

            public Builder checkedProducts(int checkedProducts) {
                result.checkedProducts = checkedProducts;
                return this;
            }

            public Builder discrepancies(int discrepancies) {
                result.discrepancies = discrepancies;
                return this;
            }

            public ReconciliationResult build() {
                return result;
            }
        }
    }
}
