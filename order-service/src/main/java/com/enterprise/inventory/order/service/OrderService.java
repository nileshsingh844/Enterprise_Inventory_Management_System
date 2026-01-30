package com.enterprise.inventory.order.service;

import com.enterprise.inventory.order.client.InventoryServiceClient; // Import Inventory Service client
import com.enterprise.inventory.order.dto.OrderDto; // Import Order DTO
import com.enterprise.inventory.order.dto.OrderItemDto; // Import OrderItem DTO
import com.enterprise.inventory.order.exception.InsufficientStockException; // Import custom exception
import com.enterprise.inventory.order.exception.ResourceNotFoundException; // Import custom exception
import com.enterprise.inventory.order.model.Order; // Import Order entity
import com.enterprise.inventory.order.model.OrderItem; // Import OrderItem entity
import com.enterprise.inventory.order.repository.OrderRepository; // Import Order repository
import lombok.extern.slf4j.Slf4j; // Import Lombok's SLF4J annotation for logging
import org.springframework.beans.factory.annotation.Autowired; // Import Spring's Autowired annotation
import org.springframework.stereotype.Service; // Import Spring's Service annotation
import org.springframework.transaction.annotation.Transactional; // Import Transactional annotation

import java.math.BigDecimal; // Import BigDecimal for monetary calculations
import java.time.LocalDateTime; // Import LocalDateTime for timestamp handling
import java.util.ArrayList; // Import ArrayList for list operations
import java.util.List; // Import List interface
import java.util.stream.Collectors; // Import Stream utilities for collection processing

/**
 * Service class for Order business logic
 * This class implements the core business operations for order processing and management
 * 
 * @Service: Marks this class as a Spring service component
 * @Slf4j: Lombok annotation to add SLF4J logging capabilities
 * @Transactional: Enables transaction management for all methods
 */
@Service
@Transactional
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryServiceClient inventoryServiceClient;

    /**
     * Constructor-based dependency injection
     * Spring automatically injects the required beans
     * 
     * @param orderRepository the repository for order data operations
     * @param inventoryServiceClient the client for inventory service communication
     */
    @Autowired
    public OrderService(OrderRepository orderRepository, InventoryServiceClient inventoryServiceClient) {
        this.orderRepository = orderRepository;
        this.inventoryServiceClient = inventoryServiceClient;
    }

    /**
     * Create a new order
     * Validates inventory availability and calculates totals before creating the order
     * 
     * @param orderDto the order data to create
     * @return OrderDto of the created order
     * @throws ResourceNotFoundException if any product is not found
     * @throws InsufficientStockException if any product has insufficient stock
     */
    public OrderDto createOrder(OrderDto orderDto) {
        log.info("Creating new order for customer: {}", orderDto.getCustomerId());
        
        // Validate order items and check inventory availability
        validateOrderItems(orderDto.getOrderItems());
        
        // Convert DTO to entity
        Order order = convertToEntity(orderDto);
        
        // Calculate order totals
        calculateOrderTotals(order);
        
        // Reserve inventory for the order
        reserveInventory(order);
        
        // Save the order to database
        Order savedOrder = orderRepository.save(order);
        
        log.info("Successfully created order with ID: {} and order number: {}", 
                savedOrder.getOrderId(), savedOrder.getOrderNumber());
        
        // Convert back to DTO and return
        return OrderDto.fromEntity(savedOrder);
    }

    /**
     * Update an existing order
     * Validates that the order exists before updating
     * 
     * @param orderId the ID of the order to update
     * @param orderDto the updated order data
     * @return OrderDto of the updated order
     * @throws ResourceNotFoundException if the order is not found
     */
    public OrderDto updateOrder(Long orderId, OrderDto orderDto) {
        log.info("Updating order with ID: {}", orderId);
        
        // Find existing order
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order with ID {} not found", orderId);
                    return new ResourceNotFoundException("Order not found with id: " + orderId);
                });

        // Update order fields
        updateOrderFields(existingOrder, orderDto);
        
        // Recalculate totals if order items changed
        if (orderDto.getOrderItems() != null) {
            validateOrderItems(orderDto.getOrderItems());
            updateOrderItems(existingOrder, orderDto.getOrderItems());
            calculateOrderTotals(existingOrder);
        }
        
        // Save the updated order
        Order updatedOrder = orderRepository.save(existingOrder);
        
        log.info("Successfully updated order with ID: {}", updatedOrder.getOrderId());
        
        return OrderDto.fromEntity(updatedOrder);
    }

    /**
     * Get an order by its ID
     * 
     * @param orderId the ID of the order to retrieve
     * @return OrderDto of the found order
     * @throws ResourceNotFoundException if the order is not found
     */
    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long orderId) {
        log.debug("Fetching order with ID: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order with ID {} not found", orderId);
                    return new ResourceNotFoundException("Order not found with id: " + orderId);
                });

        log.debug("Successfully fetched order with ID: {}", orderId);
        return OrderDto.fromEntity(order);
    }

    /**
     * Get an order by its order number
     * 
     * @param orderNumber the order number of the order to retrieve
     * @return OrderDto of the found order
     * @throws ResourceNotFoundException if the order is not found
     */
    @Transactional(readOnly = true)
    public OrderDto getOrderByOrderNumber(String orderNumber) {
        log.debug("Fetching order with order number: {}", orderNumber);
        
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> {
                    log.warn("Order with order number {} not found", orderNumber);
                    return new ResourceNotFoundException("Order not found with order number: " + orderNumber);
                });

        log.debug("Successfully fetched order with order number: {}", orderNumber);
        return OrderDto.fromEntity(order);
    }

    /**
     * Get all orders
     * 
     * @return List of all OrderDto objects
     */
    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        log.debug("Fetching all orders");
        
        List<Order> orders = orderRepository.findAll();
        
        log.debug("Found {} orders", orders.size());
        return orders.stream()
                .map(OrderDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get orders by customer ID
     * 
     * @param customerId the customer ID to filter by
     * @return List of OrderDto objects for the specified customer
     */
    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByCustomerId(Long customerId) {
        log.debug("Fetching orders for customer ID: {}", customerId);
        
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        
        log.debug("Found {} orders for customer ID: {}", orders.size(), customerId);
        return orders.stream()
                .map(OrderDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get orders by status
     * 
     * @param status the order status to filter by
     * @return List of OrderDto objects with the specified status
     */
    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByStatus(String status) {
        log.debug("Fetching orders with status: {}", status);
        
        Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        List<Order> orders = orderRepository.findByStatus(orderStatus);
        
        log.debug("Found {} orders with status: {}", orders.size(), status);
        return orders.stream()
                .map(OrderDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Update order status
     * 
     * @param orderId the ID of the order
     * @param status the new status
     * @return OrderDto of the updated order
     * @throws ResourceNotFoundException if the order is not found
     */
    public OrderDto updateOrderStatus(Long orderId, String status) {
        log.info("Updating status for order ID: {} to {}", orderId, status);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order with ID {} not found", orderId);
                    return new ResourceNotFoundException("Order not found with id: " + orderId);
                });

        Order.OrderStatus newStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        
        // Handle status-specific logic
        handleStatusChange(order, newStatus);
        
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        
        log.info("Successfully updated status for order ID: {} to {}", orderId, status);
        
        return OrderDto.fromEntity(updatedOrder);
    }

    /**
     * Cancel an order
     * Releases reserved inventory and updates order status
     * 
     * @param orderId the ID of the order to cancel
     * @return OrderDto of the cancelled order
     * @throws ResourceNotFoundException if the order is not found
     */
    public OrderDto cancelOrder(Long orderId) {
        log.info("Cancelling order with ID: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order with ID {} not found", orderId);
                    return new ResourceNotFoundException("Order not found with id: " + orderId);
                });

        // Check if order can be cancelled
        if (order.getStatus() == Order.OrderStatus.SHIPPED || 
            order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel order that has been shipped or delivered");
        }

        // Release reserved inventory
        releaseInventory(order);
        
        // Update order status
        order.setStatus(Order.OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);
        
        log.info("Successfully cancelled order with ID: {}", orderId);
        
        return OrderDto.fromEntity(cancelledOrder);
    }

    /**
     * Validate order items and check inventory availability
     * 
     * @param orderItems list of order items to validate
     * @throws ResourceNotFoundException if any product is not found
     * @throws InsufficientStockException if any product has insufficient stock
     */
    private void validateOrderItems(List<OrderItemDto> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        for (OrderItemDto item : orderItems) {
            try {
                // Check if product exists
                ProductDto product = inventoryServiceClient.getProductById(item.getProductId());
                
                // Check if sufficient stock is available
                if (product.getQuantityInStock() < item.getQuantity()) {
                    log.warn("Insufficient stock for product ID: {}. Available: {}, Required: {}", 
                            item.getProductId(), product.getQuantityInStock(), item.getQuantity());
                    throw new InsufficientStockException(
                            "Insufficient stock for product: " + product.getName());
                }
                
                // Update item details from product
                item.setProductSku(product.getSku());
                item.setProductName(product.getName());
                item.setProductDescription(product.getDescription());
                item.setProductCategory(product.getCategory());
                item.setUnitPrice(product.getUnitPrice());
                
            } catch (Exception e) {
                log.error("Error validating product ID: {}", item.getProductId(), e);
                throw new ResourceNotFoundException("Product not found with id: " + item.getProductId());
            }
        }
    }

    /**
     * Reserve inventory for an order
     * 
     * @param order the order to reserve inventory for
     */
    private void reserveInventory(Order order) {
        List<InventoryServiceClient.ProductReservation> reservations = new ArrayList<>();
        
        for (OrderItem item : order.getOrderItems()) {
            InventoryServiceClient.ProductReservation reservation = 
                new InventoryServiceClient.ProductReservation(
                    item.getProductId(), 
                    item.getQuantity(), 
                    order.getOrderNumber(), 
                    order.getOrderNumber()
                );
            reservations.add(reservation);
        }
        
        try {
            inventoryServiceClient.reserveProducts(reservations);
            log.info("Successfully reserved inventory for order: {}", order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to reserve inventory for order: {}", order.getOrderNumber(), e);
            throw new RuntimeException("Failed to reserve inventory", e);
        }
    }

    /**
     * Release inventory for a cancelled order
     * 
     * @param order the order to release inventory for
     */
    private void releaseInventory(Order order) {
        List<InventoryServiceClient.ProductReservation> reservations = new ArrayList<>();
        
        for (OrderItem item : order.getOrderItems()) {
            InventoryServiceClient.ProductReservation reservation = 
                new InventoryServiceClient.ProductReservation(
                    item.getProductId(), 
                    item.getQuantity(), 
                    order.getOrderNumber(), 
                    order.getOrderNumber()
                );
            reservations.add(reservation);
        }
        
        try {
            inventoryServiceClient.releaseProductReservations(reservations);
            log.info("Successfully released inventory for order: {}", order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to release inventory for order: {}", order.getOrderNumber(), e);
            // Don't throw exception here as order is already cancelled
        }
    }

    /**
     * Calculate order totals (subtotal, tax, shipping, total)
     * 
     * @param order the order to calculate totals for
     */
    private void calculateOrderTotals(Order order) {
        // Calculate subtotal from order items
        BigDecimal subtotal = order.getOrderItems().stream()
                .map(item -> item.getTotalPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        order.setSubtotal(subtotal);
        
        // Calculate tax (assuming 10% tax rate for simplicity)
        BigDecimal taxAmount = subtotal.multiply(BigDecimal.valueOf(0.10));
        order.setTaxAmount(taxAmount);
        
        // Calculate shipping (assuming flat rate of $10 for simplicity)
        BigDecimal shippingAmount = BigDecimal.valueOf(10.00);
        order.setShippingAmount(shippingAmount);
        
        // Calculate total amount
        BigDecimal totalAmount = subtotal.add(taxAmount).add(shippingAmount)
                .subtract(order.getDiscountAmount());
        order.setTotalAmount(totalAmount);
    }

    /**
     * Convert OrderDto to Order entity
     * 
     * @param orderDto the DTO to convert
     * @return Order entity
     */
    private Order convertToEntity(OrderDto orderDto) {
        Order order = Order.builder()
                .customerId(orderDto.getCustomerId())
                .customerName(orderDto.getCustomerName())
                .customerEmail(orderDto.getCustomerEmail())
                .customerPhone(orderDto.getCustomerPhone())
                .shippingAddress(orderDto.getShippingAddress())
                .billingAddress(orderDto.getBillingAddress())
                .paymentMethod(orderDto.getPaymentMethod())
                .orderNotes(orderDto.getOrderNotes())
                .internalNotes(orderDto.getInternalNotes())
                .expectedDeliveryDate(orderDto.getExpectedDeliveryDate())
                .build();

        // Convert order items
        if (orderDto.getOrderItems() != null) {
            List<OrderItem> orderItems = orderDto.getOrderItems().stream()
                    .map(this::convertOrderItemToEntity)
                    .collect(Collectors.toList());
            order.setOrderItems(orderItems);
        }

        return order;
    }

    /**
     * Convert OrderItemDto to OrderItem entity
     * 
     * @param itemDto the DTO to convert
     * @return OrderItem entity
     */
    private OrderItem convertOrderItemToEntity(OrderItemDto itemDto) {
        return OrderItem.builder()
                .productId(itemDto.getProductId())
                .productSku(itemDto.getProductSku())
                .productName(itemDto.getProductName())
                .productDescription(itemDto.getProductDescription())
                .productCategory(itemDto.getProductCategory())
                .unitPrice(itemDto.getUnitPrice())
                .quantity(itemDto.getQuantity())
                .discountAmount(itemDto.getDiscountAmount() != null ? itemDto.getDiscountAmount() : BigDecimal.ZERO)
                .taxAmount(itemDto.getTaxAmount() != null ? itemDto.getTaxAmount() : BigDecimal.ZERO)
                .weight(itemDto.getWeight())
                .lengthCm(itemDto.getLengthCm())
                .widthCm(itemDto.getWidthCm())
                .heightCm(itemDto.getHeightCm())
                .specialInstructions(itemDto.getSpecialInstructions())
                .build();
    }

    /**
     * Update fields of an existing order with data from DTO
     * 
     * @param existingOrder the order to update
     * @param orderDto the DTO with updated data
     */
    private void updateOrderFields(Order existingOrder, OrderDto orderDto) {
        existingOrder.setCustomerName(orderDto.getCustomerName());
        existingOrder.setCustomerEmail(orderDto.getCustomerEmail());
        existingOrder.setCustomerPhone(orderDto.getCustomerPhone());
        existingOrder.setShippingAddress(orderDto.getShippingAddress());
        existingOrder.setBillingAddress(orderDto.getBillingAddress());
        existingOrder.setPaymentMethod(orderDto.getPaymentMethod());
        existingOrder.setOrderNotes(orderDto.getOrderNotes());
        existingOrder.setInternalNotes(orderDto.getInternalNotes());
        existingOrder.setExpectedDeliveryDate(orderDto.getExpectedDeliveryDate());
        
        if (orderDto.getStatus() != null) {
            existingOrder.setStatus(Order.OrderStatus.valueOf(orderDto.getStatus()));
        }
        if (orderDto.getPaymentStatus() != null) {
            existingOrder.setPaymentStatus(Order.PaymentStatus.valueOf(orderDto.getPaymentStatus()));
        }
    }

    /**
     * Update order items
     * 
     * @param existingOrder the order to update items for
     * @param updatedItems the updated order items
     */
    private void updateOrderItems(Order existingOrder, List<OrderItemDto> updatedItems) {
        // Clear existing items
        existingOrder.getOrderItems().clear();
        
        // Add updated items
        List<OrderItem> newItems = updatedItems.stream()
                .map(this::convertOrderItemToEntity)
                .collect(Collectors.toList());
        
        existingOrder.getOrderItems().addAll(newItems);
    }

    /**
     * Handle status-specific logic
     * 
     * @param order the order being updated
     * @param newStatus the new status
     */
    private void handleStatusChange(Order order, Order.OrderStatus newStatus) {
        switch (newStatus) {
            case SHIPPED:
                // Confirm inventory reservations
                confirmInventoryReservations(order);
                break;
            case DELIVERED:
                // Set actual delivery date
                order.setActualDeliveryDate(LocalDateTime.now());
                break;
            case CANCELLED:
                // Release inventory
                releaseInventory(order);
                break;
            default:
                // No special handling for other statuses
                break;
        }
    }

    /**
     * Confirm inventory reservations when order is shipped
     * 
     * @param order the order being shipped
     */
    private void confirmInventoryReservations(Order order) {
        List<InventoryServiceClient.ProductReservation> reservations = new ArrayList<>();
        
        for (OrderItem item : order.getOrderItems()) {
            InventoryServiceClient.ProductReservation reservation = 
                new InventoryServiceClient.ProductReservation(
                    item.getProductId(), 
                    item.getQuantity(), 
                    order.getOrderNumber(), 
                    order.getOrderNumber()
                );
            reservations.add(reservation);
        }
        
        try {
            inventoryServiceClient.confirmProductReservations(reservations);
            log.info("Successfully confirmed inventory reservations for order: {}", order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to confirm inventory reservations for order: {}", order.getOrderNumber(), e);
            // Don't throw exception here as order is already shipped
        }
    }
}
