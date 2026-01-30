package com.enterprise.inventory.service.controller;

import com.enterprise.inventory.service.dto.ProductDto; // Import Product DTO
import com.enterprise.inventory.service.service.AsyncInventoryService; // Import AsyncInventoryService
import lombok.extern.slf4j.Slf4j; // Import Lombok's SLF4J annotation for logging
import org.springframework.beans.factory.annotation.Autowired; // Import Spring's Autowired annotation
import org.springframework.http.HttpStatus; // Import HTTP status codes
import org.springframework.http.ResponseEntity; // Import ResponseEntity for HTTP responses
import org.springframework.validation.annotation.Validated; // Import validation annotations
import org.springframework.web.bind.annotation.*; // Import REST controller annotations

import javax.validation.Valid; // Import validation annotation
import javax.validation.constraints.Min; // Import validation annotation
import java.util.List; // Import List interface
import java.util.concurrent.CompletableFuture; // Import CompletableFuture for asynchronous operations

/**
 * REST Controller for asynchronous inventory operations
 * This controller handles HTTP requests for asynchronous inventory management operations
 * Provides endpoints for bulk operations, report generation, and background processing
 * 
 * @RestController: Combines @Controller and @ResponseBody for REST APIs
 * @RequestMapping: Base path for all endpoints in this controller
 * @Validated: Enables validation for method parameters
 * @Slf4j: Lombok annotation to add SLF4J logging capabilities
 */
@RestController
@RequestMapping("/api/inventory/async")
@Validated
@Slf4j
public class AsyncInventoryController {

    private final AsyncInventoryService asyncInventoryService;

    /**
     * Constructor-based dependency injection
     * Spring automatically injects the AsyncInventoryService bean
     * 
     * @param asyncInventoryService the service for asynchronous inventory operations
     */
    @Autowired
    public AsyncInventoryController(AsyncInventoryService asyncInventoryService) {
        this.asyncInventoryService = asyncInventoryService;
    }

    /**
     * Asynchronously update stock quantities for multiple products
     * HTTP POST /api/inventory/async/stock/bulk-update
     * This endpoint processes stock updates in parallel for better performance
     * 
     * @param stockUpdates list of product IDs and quantity changes
     * @return CompletableFuture containing the number of successfully updated products
     */
    @PostMapping("/stock/bulk-update")
    public CompletableFuture<ResponseEntity<Integer>> bulkUpdateStockQuantities(
            @Valid @RequestBody List<AsyncInventoryService.StockUpdate> stockUpdates) {
        
        log.info("Received request to bulk update stock quantities for {} products", stockUpdates.size());
        
        try {
            CompletableFuture<Integer> result = asyncInventoryService.updateMultipleStockQuantities(stockUpdates);
            
            return result.thenApply(successCount -> {
                log.info("Bulk stock update initiated. Expected success count: {}", stockUpdates.size());
                return ResponseEntity.ok(successCount);
            }).exceptionally(throwable -> {
                log.error("Failed to initiate bulk stock update", throwable);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            });
            
        } catch (Exception e) {
            log.error("Error processing bulk stock update request", e);
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        }
    }

    /**
     * Asynchronously generate inventory report
     * HTTP GET /api/inventory/async/reports/inventory
     * This endpoint generates comprehensive inventory reports without blocking the main thread
     * 
     * @return CompletableFuture containing the generated inventory report
     */
    @GetMapping("/reports/inventory")
    public CompletableFuture<ResponseEntity<AsyncInventoryService.InventoryReport>> generateInventoryReport() {
        
        log.info("Received request to generate inventory report");
        
        try {
            CompletableFuture<AsyncInventoryService.InventoryReport> result = asyncInventoryService.generateInventoryReport();
            
            return result.thenApply(report -> {
                log.info("Inventory report generation initiated");
                return ResponseEntity.ok(report);
            }).exceptionally(throwable -> {
                log.error("Failed to generate inventory report", throwable);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            });
            
        } catch (Exception e) {
            log.error("Error processing inventory report request", e);
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        }
    }

    /**
     * Asynchronously process bulk product import
     * HTTP POST /api/inventory/async/import/bulk
     * This endpoint handles large file imports without blocking the main thread
     * 
     * @param products list of products to import
     * @return CompletableFuture containing import results
     */
    @PostMapping("/import/bulk")
    public CompletableFuture<ResponseEntity<AsyncInventoryService.BulkImportResult>> processBulkImport(
            @Valid @RequestBody List<ProductDto> products) {
        
        log.info("Received request to bulk import {} products", products.size());
        
        try {
            CompletableFuture<AsyncInventoryService.BulkImportResult> result = asyncInventoryService.processBulkProductImport(products);
            
            return result.thenApply(importResult -> {
                log.info("Bulk import initiated. Total products: {}", products.size());
                return ResponseEntity.ok(importResult);
            }).exceptionally(throwable -> {
                log.error("Failed to initiate bulk import", throwable);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            });
            
        } catch (Exception e) {
            log.error("Error processing bulk import request", e);
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        }
    }

    /**
     * Asynchronously send low stock notifications
     * HTTP POST /api/inventory/async/notifications/low-stock
     * This endpoint sends notifications without blocking main business operations
     * 
     * @return CompletableFuture containing the number of notifications sent
     */
    @PostMapping("/notifications/low-stock")
    public CompletableFuture<ResponseEntity<Integer>> sendLowStockNotifications() {
        
        log.info("Received request to send low stock notifications");
        
        try {
            CompletableFuture<Integer> result = asyncInventoryService.sendLowStockNotifications();
            
            return result.thenApply(notificationCount -> {
                log.info("Low stock notifications initiated");
                return ResponseEntity.ok(notificationCount);
            }).exceptionally(throwable -> {
                log.error("Failed to send low stock notifications", throwable);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            });
            
        } catch (Exception e) {
            log.error("Error processing low stock notification request", e);
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        }
    }

    /**
     * Asynchronously perform inventory reconciliation
     * HTTP POST /api/inventory/async/reconciliation/inventory
     * This endpoint performs periodic inventory reconciliation without blocking operations
     * 
     * @return CompletableFuture containing reconciliation results
     */
    @PostMapping("/reconciliation/inventory")
    public CompletableFuture<ResponseEntity<AsyncInventoryService.ReconciliationResult>> performInventoryReconciliation() {
        
        log.info("Received request to perform inventory reconciliation");
        
        try {
            CompletableFuture<AsyncInventoryService.ReconciliationResult> result = asyncInventoryService.performInventoryReconciliation();
            
            return result.thenApply(reconciliationResult -> {
                log.info("Inventory reconciliation initiated");
                return ResponseEntity.ok(reconciliationResult);
            }).exceptionally(throwable -> {
                log.error("Failed to perform inventory reconciliation", throwable);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            });
            
        } catch (Exception e) {
            log.error("Error processing inventory reconciliation request", e);
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        }
    }

    /**
     * Get status of asynchronous operations
     * HTTP GET /api/inventory/async/status
     * This endpoint provides status information about ongoing async operations
     * 
     * @return ResponseEntity with status information
     */
    @GetMapping("/status")
    public ResponseEntity<String> getAsyncOperationStatus() {
        
        log.debug("Received request for async operation status");
        
        // In a real implementation, this would query a status store or cache
        // For now, we'll return a simple status message
        String status = "Async operations are running. Check logs for detailed information.";
        
        return ResponseEntity.ok(status);
    }

    /**
     * Health check endpoint for async operations
     * HTTP GET /api/inventory/async/health
     * This endpoint provides health status of the async processing system
     * 
     * @return ResponseEntity with health status
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        
        log.debug("Health check requested for async inventory operations");
        
        // Check if thread pools are healthy
        // In a real implementation, this would check thread pool metrics
        String health = "Async Inventory Operations - Healthy";
        
        return ResponseEntity.ok(health);
    }
}
