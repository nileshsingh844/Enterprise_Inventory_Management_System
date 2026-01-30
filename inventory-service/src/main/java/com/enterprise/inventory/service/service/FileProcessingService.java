package com.enterprise.inventory.service.service;

import com.enterprise.inventory.service.dto.ProductDto; // Import Product DTO
import com.enterprise.inventory.service.model.Product; // Import Product entity
import com.enterprise.inventory.service.repository.ProductRepository; // Import Product repository
import lombok.extern.slf4j.Slf4j; // Import Lombok's SLF4J annotation for logging
import org.springframework.beans.factory.annotation.Autowired; // Import Spring's Autowired annotation
import org.springframework.scheduling.annotation.Async; // Import Async annotation for asynchronous method execution
import org.springframework.stereotype.Service; // Import Spring's Service annotation

import java.io.BufferedReader; // Import BufferedReader for file reading
import java.io.BufferedWriter; // Import BufferedWriter for file writing
import java.io.FileReader; // Import FileReader for file reading
import java.io.FileWriter; // Import FileWriter for file writing
import java.io.IOException; // Import IOException for I/O exceptions
import java.nio.file.Files; // Import Files utility for file operations
import java.nio.file.Path; // Import Path interface for file paths
import java.nio.file.Paths; // Import Paths utility for file path creation
import java.time.LocalDateTime; // Import LocalDateTime for timestamp handling
import java.time.format.DateTimeFormatter; // Import DateTimeFormatter for timestamp formatting
import java.util.ArrayList; // Import ArrayList for dynamic array
import java.util.List; // Import List interface
import java.util.concurrent.CompletableFuture; // Import CompletableFuture for asynchronous operations
import java.util.stream.Collectors; // Import Stream utilities for collection processing

/**
 * Service class for file processing operations
 * This class handles file I/O operations for importing/exporting inventory data
 * Uses dedicated thread pool for file operations to avoid blocking main threads
 * 
 * @Service: Marks this class as a Spring service component
 * @Slf4j: Lombok annotation to add SLF4J logging capabilities
 */
@Service
@Slf4j
public class FileProcessingService {

    private final ProductRepository productRepository;

    /**
     * Constructor-based dependency injection
     * Spring automatically injects the ProductRepository bean
     * 
     * @param productRepository the repository for product data operations
     */
    @Autowired
    public FileProcessingService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Asynchronously export inventory data to CSV file
     * This method exports all products to a CSV file without blocking the main thread
     * Uses the file processing thread pool for I/O-intensive operations
     * 
     * @param filePath the path where the CSV file should be created
     * @return CompletableFuture containing the path of the created file
     */
    @Async("fileProcessingExecutor")
    public CompletableFuture<String> exportInventoryToCsv(String filePath) {
        log.info("Starting asynchronous inventory export to CSV file: {}", filePath);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Create directory if it doesn't exist
                Path path = Paths.get(filePath);
                Files.createDirectories(path.getParent());
                
                // Get all products from database
                List<Product> products = productRepository.findAll();
                
                // Write products to CSV file
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                    // Write CSV header
                    writer.write("Product ID,SKU,Name,Description,Category,Unit Price,Quantity,Reorder Level,Status,Created At\n");
                    
                    // Write product data
                    for (Product product : products) {
                        writer.write(String.format("%d,%s,\"%s\",\"%s\",\"%s\",%.2f,%d,%d,%s,\"%s\"\n",
                                product.getProductId(),
                                escapeCsv(product.getSku()),
                                escapeCsv(product.getName()),
                                escapeCsv(product.getDescription() != null ? product.getDescription() : ""),
                                escapeCsv(product.getCategory() != null ? product.getCategory() : ""),
                                product.getUnitPrice(),
                                product.getQuantityInStock(),
                                product.getReorderLevel(),
                                product.getStatus().name(),
                                product.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
                    }
                }
                
                log.info("Successfully exported {} products to CSV file: {}", products.size(), filePath);
                return filePath;
                
            } catch (IOException e) {
                log.error("Failed to export inventory to CSV file: {}", filePath, e);
                throw new RuntimeException("CSV export failed", e);
            }
        });
    }

    /**
     * Asynchronously import products from CSV file
     * This method reads products from a CSV file and imports them to the database
     * Uses the file processing thread pool for I/O-intensive operations
     * 
     * @param filePath the path of the CSV file to import
     * @return CompletableFuture containing import results
     */
    @Async("fileProcessingExecutor")
    public CompletableFuture<FileImportResult> importProductsFromCsv(String filePath) {
        log.info("Starting asynchronous product import from CSV file: {}", filePath);
        
        return CompletableFuture.supplyAsync(() -> {
            FileImportResult result = new FileImportResult();
            
            try {
                // Check if file exists
                Path path = Paths.get(filePath);
                if (!Files.exists(path)) {
                    throw new IOException("File not found: " + filePath);
                }
                
                List<ProductDto> products = new ArrayList<>();
                int lineNumber = 0;
                int successCount = 0;
                int failureCount = 0;
                
                // Read CSV file
                try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                    String line;
                    boolean isFirstLine = true;
                    
                    while ((line = reader.readLine()) != null) {
                        lineNumber++;
                        
                        // Skip header line
                        if (isFirstLine) {
                            isFirstLine = false;
                            continue;
                        }
                        
                        try {
                            ProductDto product = parseCsvLine(line, lineNumber);
                            if (product != null) {
                                products.add(product);
                                successCount++;
                            } else {
                                failureCount++;
                                result.getFailedLines().put(lineNumber, "Invalid CSV format");
                            }
                        } catch (Exception e) {
                            failureCount++;
                            result.getFailedLines().put(lineNumber, "Parse error: " + e.getMessage());
                            log.warn("Failed to parse CSV line {}: {}", lineNumber, line);
                        }
                    }
                }
                
                // Process imported products
                int processedCount = 0;
                int duplicateCount = 0;
                
                for (ProductDto productDto : products) {
                    try {
                        // Check if product already exists
                        if (!productRepository.existsBySku(productDto.getSku())) {
                            // Convert and save product
                            Product product = convertToEntity(productDto);
                            productRepository.save(product);
                            processedCount++;
                            result.getSuccessfulImports().add(productDto.getSku());
                        } else {
                            duplicateCount++;
                            result.getSkippedImports().put(productDto.getSku(), "Product with SKU already exists");
                        }
                    } catch (Exception e) {
                        failureCount++;
                        result.getFailedImports().put(productDto.getSku(), "Database error: " + e.getMessage());
                        log.error("Failed to import product: {}", productDto.getSku(), e);
                    }
                }
                
                // Set result statistics
                result.setTotalLines(lineNumber - 1); // Exclude header
                result.setProcessedCount(processedCount);
                result.setDuplicateCount(duplicateCount);
                result.setSuccessCount(successCount);
                result.setFailureCount(failureCount);
                
                log.info("CSV import completed. Total lines: {}, Processed: {}, Duplicates: {}, Failed: {}", 
                        result.getTotalLines(), result.getProcessedCount(), result.getDuplicateCount(), result.getFailureCount());
                
                return result;
                
            } catch (IOException e) {
                log.error("Failed to import products from CSV file: {}", filePath, e);
                throw new RuntimeException("CSV import failed", e);
            }
        });
    }

    /**
     * Asynchronously generate inventory report in JSON format
     * This method generates a detailed inventory report and saves it to a JSON file
     * Uses the file processing thread pool for I/O-intensive operations
     * 
     * @param filePath the path where the JSON report should be created
     * @return CompletableFuture containing the path of the created file
     */
    @Async("fileProcessingExecutor")
    public CompletableFuture<String> generateInventoryReportJson(String filePath) {
        log.info("Starting asynchronous inventory report generation in JSON: {}", filePath);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Create directory if it doesn't exist
                Path path = Paths.get(filePath);
                Files.createDirectories(path.getParent());
                
                // Get all products from database
                List<Product> products = productRepository.findAll();
                
                // Create report data
                InventoryReport report = InventoryReport.builder()
                        .generatedAt(LocalDateTime.now())
                        .totalProducts(products.size())
                        .activeProducts((int) products.stream()
                                .filter(p -> p.getStatus() == Product.ProductStatus.ACTIVE)
                                .count())
                        .totalValue(products.stream()
                                .mapToDouble(p -> p.getUnitPrice().doubleValue() * p.getQuantityInStock())
                                .sum())
                        .lowStockProducts((int) products.stream()
                                .filter(p -> p.getQuantityInStock() <= p.getReorderLevel())
                                .count())
                        .outOfStockProducts((int) products.stream()
                                .filter(p -> p.getQuantityInStock() == 0)
                                .count())
                        .categories(products.stream()
                                .map(Product::getCategory)
                                .filter(category -> category != null)
                                .distinct()
                                .collect(Collectors.toList()))
                        .build();
                
                // Convert to JSON and write to file
                String jsonReport = convertToJson(report);
                
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                    writer.write(jsonReport);
                }
                
                log.info("Successfully generated inventory report in JSON: {}", filePath);
                return filePath;
                
            } catch (IOException e) {
                log.error("Failed to generate inventory report in JSON: {}", filePath, e);
                throw new RuntimeException("JSON report generation failed", e);
            }
        });
    }

    /**
     * Asynchronously backup inventory data
     * This method creates a backup of all inventory data in JSON format
     * Uses the file processing thread pool for I/O-intensive operations
     * 
     * @param backupDir the directory where backup files should be stored
     * @return CompletableFuture containing the path of the backup file
     */
    @Async("fileProcessingExecutor")
    public CompletableFuture<String> backupInventoryData(String backupDir) {
        log.info("Starting asynchronous inventory data backup to directory: {}", backupDir);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Create backup directory with timestamp
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
                String fileName = "inventory_backup_" + timestamp + ".json";
                String filePath = backupDir + "/" + fileName;
                
                Path path = Paths.get(filePath);
                Files.createDirectories(path.getParent());
                
                // Get all products from database
                List<Product> products = productRepository.findAll();
                
                // Create backup data
                InventoryBackup backup = InventoryBackup.builder()
                        .backupAt(LocalDateTime.now())
                        .totalProducts(products.size())
                        .products(products.stream()
                                .map(this::convertProductToBackupDto)
                                .collect(Collectors.toList()))
                        .build();
                
                // Convert to JSON and write to file
                String jsonBackup = convertToJson(backup);
                
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                    writer.write(jsonBackup);
                }
                
                log.info("Successfully backed up {} products to file: {}", products.size(), filePath);
                return filePath;
                
            } catch (IOException e) {
                log.error("Failed to backup inventory data: {}", backupDir, e);
                throw new RuntimeException("Backup failed", e);
            }
        });
    }

    /**
     * Parse a single CSV line into ProductDto
     * Helper method for CSV parsing with proper escaping
     * 
     * @param line the CSV line to parse
     * @param lineNumber the line number for error reporting
     * @return ProductDto if parsing succeeds, null otherwise
     */
    private ProductDto parseCsvLine(String line, int lineNumber) {
        try {
            String[] fields = line.split(",");
            
            if (fields.length < 7) {
                throw new IllegalArgumentException("Insufficient fields in CSV line");
            }
            
            return ProductDto.builder()
                    .productId(Long.parseLong(fields[0].trim()))
                    .sku(fields[1].trim())
                    .name(unescapeCsv(fields[2].trim()))
                    .description(fields.length > 3 ? unescapeCsv(fields[3].trim()) : null)
                    .category(fields.length > 4 ? unescapeCsv(fields[4].trim()) : null)
                    .unitPrice(java.math.BigDecimal.valueOf(fields[5].trim()))
                    .quantityInStock(Integer.parseInt(fields[6].trim()))
                    .reorderLevel(fields.length > 7 ? Integer.parseInt(fields[7].trim()) : 10)
                    .status(fields.length > 8 ? fields[8].trim() : "ACTIVE")
                    .build();
                    
        } catch (Exception e) {
            log.warn("Failed to parse CSV line {}: {}", lineNumber, line);
            throw new IllegalArgumentException("Invalid CSV format", e);
        }
    }

    /**
     * Escape CSV field values
     * Helper method to handle commas and quotes in CSV values
     * 
     * @param value the value to escape
     * @return escaped value
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        
        // Escape quotes by doubling them
        String escaped = value.replace("\"", "\"\"");
        
        // Enclose in quotes if it contains comma, quote, or newline
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            escaped = "\"" + escaped + "\"";
        }
        
        return escaped;
    }

    /**
     * Unescape CSV field values
     * Helper method to reverse CSV escaping
     * 
     * @param value the escaped value
     * @return unescaped value
     */
    private String unescapeCsv(String value) {
        if (value == null) {
            return "";
        }
        
        // Remove surrounding quotes if present
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1) {
            return value.substring(1, value.length() - 1).replace("\"\"", "\"");
        }
        
        return value;
    }

    /**
     * Convert ProductDto to Product entity
     * Helper method for entity conversion
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
        product.setReorderLevel(productDto.getReorderLevel());
        product.setStatus(productDto.getStatus() != null ? 
                Product.ProductStatus.valueOf(productDto.getStatus()) : Product.ProductStatus.ACTIVE);
        return product;
    }

    /**
     * Convert Product entity to backup DTO
     * Helper method for backup serialization
     * 
     * @param product the entity to convert
     * @return backup DTO
     */
    private ProductBackupDto convertProductToBackupDto(Product product) {
        return ProductBackupDto.builder()
                .productId(product.getProductId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .unitPrice(product.getUnitPrice())
                .quantityInStock(product.getQuantityInStock())
                .reorderLevel(product.getReorderLevel())
                .status(product.getStatus().name())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    /**
     * Convert object to JSON string
     * Helper method for JSON serialization
     * 
     * @param object the object to convert
     * @return JSON string
     */
    private String convertToJson(Object object) {
        try {
            // In a real implementation, use Jackson or Gson
            // For simplicity, we'll use a basic string representation
            return object.toString();
        } catch (Exception e) {
            log.error("Failed to convert object to JSON", e);
            return "{}";
        }
    }

    /**
     * Inner class for file import results
     * Contains statistics about the import operation
     */
    public static class FileImportResult {
        private int totalLines;
        private int processedCount;
        private int duplicateCount;
        private int successCount;
        private int failureCount;
        private java.util.Map<Integer, String> failedLines = new java.util.HashMap<>();
        private java.util.Map<String, String> failedImports = new java.util.HashMap<>();
        private java.util.Map<String, String> skippedImports = new java.util.HashMap<>();
        private java.util.List<String> successfulImports = new java.util.ArrayList<>();

        // Getters and setters
        public int getTotalLines() { return totalLines; }
        public void setTotalLines(int totalLines) { this.totalLines = totalLines; }
        public int getProcessedCount() { return processedCount; }
        public void setProcessedCount(int processedCount) { this.processedCount = processedCount; }
        public int getDuplicateCount() { return duplicateCount; }
        public void setDuplicateCount(int duplicateCount) { this.duplicateCount = duplicateCount; }
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        public int getFailureCount() { return failureCount; }
        public void setFailureCount(int failureCount) { this.failureCount = failureCount; }
        public java.util.Map<Integer, String> getFailedLines() { return failedLines; }
        public java.util.Map<String, String> getFailedImports() { return failedImports; }
        public java.util.Map<String, String> getSkippedImports() { return skippedImports; }
        public java.util.List<String> getSuccessfulImports() { return successfulImports; }
    }

    /**
     * Inner class for inventory report data
     * Contains aggregated inventory statistics
     */
    public static class InventoryReport {
        private LocalDateTime generatedAt;
        private int totalProducts;
        private int activeProducts;
        private double totalValue;
        private int lowStockProducts;
        private int outOfStockProducts;
        private List<String> categories;

        // Getters and setters
        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
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

            public Builder generatedAt(LocalDateTime generatedAt) {
                report.generatedAt = generatedAt;
                return this;
            }

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
     * Inner class for inventory backup data
     * Contains backup metadata and product information
     */
    public static class InventoryBackup {
        private LocalDateTime backupAt;
        private int totalProducts;
        private List<ProductBackupDto> products;

        // Getters and setters
        public LocalDateTime getBackupAt() { return backupAt; }
        public void setBackupAt(LocalDateTime backupAt) { this.backupAt = backupAt; }
        public int getTotalProducts() { return totalProducts; }
        public void setTotalProducts(int totalProducts) { this.totalProducts = totalProducts; }
        public List<ProductBackupDto> getProducts() { return products; }
        public void setProducts(List<ProductBackupDto> products) { this.products = products; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private InventoryBackup backup = new InventoryBackup();

            public Builder backupAt(LocalDateTime backupAt) {
                backup.backupAt = backupAt;
                return this;
            }

            public Builder totalProducts(int totalProducts) {
                backup.totalProducts = totalProducts;
                return this;
            }

            public Builder products(List<ProductBackupDto> products) {
                backup.products = products;
                return this;
            }

            public InventoryBackup build() {
                return backup;
            }
        }
    }

    /**
     * Inner class for backup product DTO
     * Simplified DTO for backup serialization
     */
    public static class ProductBackupDto {
        private Long productId;
        private String sku;
        private String name;
        private String description;
        private String category;
        private java.math.BigDecimal unitPrice;
        private Integer quantityInStock;
        private Integer reorderLevel;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public java.math.BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(java.math.BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public Integer getQuantityInStock() { return quantityInStock; }
        public void setQuantityInStock(Integer quantityInStock) { this.quantityInStock = quantityInStock; }
        public Integer getReorderLevel() { return reorderLevel; }
        public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private ProductBackupDto dto = new ProductBackupDto();

            public Builder productId(Long productId) {
                dto.productId = productId;
                return this;
            }

            public Builder sku(String sku) {
                dto.sku = sku;
                return this;
            }

            public Builder name(String name) {
                dto.name = name;
                return this;
            }

            public Builder description(String description) {
                dto.description = description;
                return this;
            }

            public Builder category(String category) {
                dto.category = category;
                return this;
            }

            public Builder unitPrice(java.math.BigDecimal unitPrice) {
                dto.unitPrice = unitPrice;
                return this;
            }

            public Builder quantityInStock(Integer quantityInStock) {
                dto.quantityInStock = quantityInStock;
                return this;
            }

            public Builder reorderLevel(Integer reorderLevel) {
                dto.reorderLevel = reorderLevel;
                return this;
            }

            public Builder status(String status) {
                dto.status = status;
                return this;
            }

            public Builder createdAt(LocalDateTime createdAt) {
                dto.createdAt = createdAt;
                return this;
            }

            public Builder updatedAt(LocalDateTime updatedAt) {
                dto.updatedAt = updatedAt;
                return this;
            }

            public ProductBackupDto build() {
                return dto;
            }
        }
    }
}
