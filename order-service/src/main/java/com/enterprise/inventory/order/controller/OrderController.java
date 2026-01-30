package com.enterprise.inventory.order.controller;

import com.enterprise.inventory.order.dto.OrderDto; // Import Order DTO
import com.enterprise.inventory.order.service.OrderService; // Import Order service
import lombok.extern.slf4j.Slf4j; // Import Lombok's SLF4J annotation for logging
import org.springframework.beans.factory.annotation.Autowired; // Import Spring's Autowired annotation
import org.springframework.http.HttpStatus; // Import HTTP status codes
import org.springframework.http.ResponseEntity; // Import ResponseEntity for HTTP responses
import org.springframework.validation.annotation.Validated; // Import validation annotations
import org.springframework.web.bind.annotation.*; // Import REST controller annotations

import javax.validation.Valid; // Import validation annotation
import javax.validation.constraints.Min; // Import validation annotation
import javax.validation.constraints.NotBlank; // Import validation annotation
import java.util.List; // Import List interface

/**
 * REST Controller for Order operations
 * This class handles HTTP requests for order processing and management operations
 * 
 * @RestController: Combines @Controller and @ResponseBody for REST APIs
 * @RequestMapping: Base path for all endpoints in this controller
 * @Slf4j: Lombok annotation to add SLF4J logging capabilities
 * @Validated: Enables validation for method parameters
 */
@RestController
@RequestMapping("/api/orders")
@Validated
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * Constructor-based dependency injection
     * Spring automatically injects the OrderService bean
     * 
     * @param orderService the service for order business logic
     */
    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Create a new order
     * HTTP POST /api/orders
     * 
     * @param orderDto the order data to create
     * @return ResponseEntity with created order and HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderDto orderDto) {
        log.info("REST request to create order for customer: {}", orderDto.getCustomerId());
        
        OrderDto createdOrder = orderService.createOrder(orderDto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    /**
     * Update an existing order
     * HTTP PUT /api/orders/{id}
     * 
     * @param orderId the ID of the order to update
     * @param orderDto the updated order data
     * @return ResponseEntity with updated order and HTTP 200 status
     */
    @PutMapping("/{id}")
    public ResponseEntity<OrderDto> updateOrder(
            @PathVariable("id") @Min(value = 1, message = "Order ID must be positive") Long orderId,
            @Valid @RequestBody OrderDto orderDto) {
        log.info("REST request to update order with ID: {}", orderId);
        
        OrderDto updatedOrder = orderService.updateOrder(orderId, orderDto);
        
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * Get an order by ID
     * HTTP GET /api/orders/{id}
     * 
     * @param orderId the ID of the order to retrieve
     * @return ResponseEntity with order data and HTTP 200 status
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(
            @PathVariable("id") @Min(value = 1, message = "Order ID must be positive") Long orderId) {
        log.debug("REST request to get order with ID: {}", orderId);
        
        OrderDto order = orderService.getOrderById(orderId);
        
        return ResponseEntity.ok(order);
    }

    /**
     * Get an order by order number
     * HTTP GET /api/orders/number/{orderNumber}
     * 
     * @param orderNumber the order number of the order to retrieve
     * @return ResponseEntity with order data and HTTP 200 status
     */
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderDto> getOrderByOrderNumber(
            @PathVariable("orderNumber") @NotBlank(message = "Order number cannot be blank") String orderNumber) {
        log.debug("REST request to get order with order number: {}", orderNumber);
        
        OrderDto order = orderService.getOrderByOrderNumber(orderNumber);
        
        return ResponseEntity.ok(order);
    }

    /**
     * Get all orders
     * HTTP GET /api/orders
     * 
     * @return ResponseEntity with list of all orders and HTTP 200 status
     */
    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        log.debug("REST request to get all orders");
        
        List<OrderDto> orders = orderService.getAllOrders();
        
        return ResponseEntity.ok(orders);
    }

    /**
     * Get orders by customer ID
     * HTTP GET /api/orders/customer/{customerId}
     * 
     * @param customerId the customer ID to filter by
     * @return ResponseEntity with list of orders for the specified customer and HTTP 200 status
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderDto>> getOrdersByCustomerId(
            @PathVariable("customerId") @Min(value = 1, message = "Customer ID must be positive") Long customerId) {
        log.debug("REST request to get orders for customer ID: {}", customerId);
        
        List<OrderDto> orders = orderService.getOrdersByCustomerId(customerId);
        
        return ResponseEntity.ok(orders);
    }

    /**
     * Get orders by status
     * HTTP GET /api/orders/status/{status}
     * 
     * @param status the order status to filter by
     * @return ResponseEntity with list of orders with the specified status and HTTP 200 status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDto>> getOrdersByStatus(
            @PathVariable("status") @NotBlank(message = "Status cannot be blank") String status) {
        log.debug("REST request to get orders with status: {}", status);
        
        List<OrderDto> orders = orderService.getOrdersByStatus(status);
        
        return ResponseEntity.ok(orders);
    }

    /**
     * Update order status
     * HTTP PATCH /api/orders/{id}/status
     * 
     * @param orderId the ID of the order
     * @param status the new status
     * @return ResponseEntity with updated order and HTTP 200 status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable("id") @Min(value = 1, message = "Order ID must be positive") Long orderId,
            @RequestParam("status") @NotBlank(message = "Status cannot be blank") String status) {
        log.info("REST request to update status for order ID: {} to {}", orderId, status);
        
        OrderDto updatedOrder = orderService.updateOrderStatus(orderId, status);
        
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * Cancel an order
     * HTTP DELETE /api/orders/{id}/cancel
     * 
     * @param orderId the ID of the order to cancel
     * @return ResponseEntity with cancelled order and HTTP 200 status
     */
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(
            @PathVariable("id") @Min(value = 1, message = "Order ID must be positive") Long orderId) {
        log.info("REST request to cancel order with ID: {}", orderId);
        
        OrderDto cancelledOrder = orderService.cancelOrder(orderId);
        
        return ResponseEntity.ok(cancelledOrder);
    }

    /**
     * Get orders pending payment
     * HTTP GET /api/orders/pending-payment
     * 
     * @return ResponseEntity with list of orders pending payment and HTTP 200 status
     */
    @GetMapping("/pending-payment")
    public ResponseEntity<List<OrderDto>> getOrdersPendingPayment() {
        log.debug("REST request to get orders pending payment");
        
        List<OrderDto> orders = orderService.getOrdersByStatus("PENDING");
        
        return ResponseEntity.ok(orders);
    }

    /**
     * Get orders needing shipping
     * HTTP GET /api/orders/needs-shipping
     * 
     * @return ResponseEntity with list of orders needing shipping and HTTP 200 status
     */
    @GetMapping("/needs-shipping")
    public ResponseEntity<List<OrderDto>> getOrdersNeedingShipping() {
        log.debug("REST request to get orders needing shipping");
        
        List<OrderDto> orders = orderService.getOrdersByStatus("CONFIRMED");
        
        return ResponseEntity.ok(orders);
    }

    /**
     * Get shipped orders
     * HTTP GET /api/orders/shipped
     * 
     * @return ResponseEntity with list of shipped orders and HTTP 200 status
     */
    @GetMapping("/shipped")
    public ResponseEntity<List<OrderDto>> getShippedOrders() {
        log.debug("REST request to get shipped orders");
        
        List<OrderDto> orders = orderService.getOrdersByStatus("SHIPPED");
        
        return ResponseEntity.ok(orders);
    }

    /**
     * Get delivered orders
     * HTTP GET /api/orders/delivered
     * 
     * @return ResponseEntity with list of delivered orders and HTTP 200 status
     */
    @GetMapping("/delivered")
    public ResponseEntity<List<OrderDto>> getDeliveredOrders() {
        log.debug("REST request to get delivered orders");
        
        List<OrderDto> orders = orderService.getOrdersByStatus("DELIVERED");
        
        return ResponseEntity.ok(orders);
    }

    /**
     * Get cancelled orders
     * HTTP GET /api/orders/cancelled
     * 
     * @return ResponseEntity with list of cancelled orders and HTTP 200 status
     */
    @GetMapping("/cancelled")
    public ResponseEntity<List<OrderDto>> getCancelledOrders() {
        log.debug("REST request to get cancelled orders");
        
        List<OrderDto> orders = orderService.getOrdersByStatus("CANCELLED");
        
        return ResponseEntity.ok(orders);
    }

    /**
     * Health check endpoint
     * HTTP GET /api/orders/health
     * 
     * @return ResponseEntity with health status and HTTP 200 status
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.debug("Health check requested for Order Service");
        
        return ResponseEntity.ok("Order Service is healthy");
    }
}
