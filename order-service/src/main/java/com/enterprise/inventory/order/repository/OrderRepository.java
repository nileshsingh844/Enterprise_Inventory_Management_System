package com.enterprise.inventory.order.repository;

import com.enterprise.inventory.order.model.Order; // Import Order entity
import org.springframework.data.domain.Page; // Import Page interface for pagination
import org.springframework.data.domain.Pageable; // Import Pageable interface for pagination parameters
import org.springframework.data.jpa.repository.JpaRepository; // Import Spring Data JPA repository
import org.springframework.data.jpa.repository.Query; // Import Query annotation for custom queries
import org.springframework.data.repository.query.Param; // Import Param annotation for named parameters
import org.springframework.stereotype.Repository; // Import Repository annotation

import java.math.BigDecimal; // Import BigDecimal for monetary calculations
import java.time.LocalDateTime; // Import LocalDateTime for date filtering
import java.util.List; // Import List interface
import java.util.Optional; // Import Optional for nullable results

/**
 * Spring Data JPA repository for Order entity
 * This interface provides database operations for Order entities
 * Spring Data automatically implements the methods defined in this interface
 * 
 * @Repository: Marks this interface as a Spring repository component
 * JpaRepository: Provides CRUD operations and pagination support
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find an order by its unique order number
     * 
     * @param orderNumber the order number to search for
     * @return Optional containing the order if found, empty otherwise
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Find orders by customer ID
     * Used for customer order history
     * 
     * @param customerId the customer ID to filter by
     * @return List of orders for the specified customer
     */
    List<Order> findByCustomerId(Long customerId);

    /**
     * Find orders by customer ID with pagination
     * Used for customer order history with pagination
     * 
     * @param customerId the customer ID to filter by
     * @param pageable pagination parameters
     * @return Page of orders for the specified customer
     */
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    /**
     * Find orders by their status
     * Used for order management and reporting
     * 
     * @param status the order status to filter by
     * @return List of orders with the specified status
     */
    List<Order> findByStatus(Order.OrderStatus status);

    /**
     * Find orders by their status with pagination
     * Used for order management with pagination
     * 
     * @param status the order status to filter by
     * @param pageable pagination parameters
     * @return Page of orders with the specified status
     */
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);

    /**
     * Find orders by payment status
     * Used for payment processing and reporting
     * 
     * @param paymentStatus the payment status to filter by
     * @return List of orders with the specified payment status
     */
    List<Order> findByPaymentStatus(Order.PaymentStatus paymentStatus);

    /**
     * Find orders created within a date range
     * Used for reporting and analytics
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return List of orders created within the date range
     */
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersByDateRange(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);

    /**
     * Find orders created within a date range with pagination
     * Used for reporting with pagination
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param pageable pagination parameters
     * @return Page of orders created within the date range
     */
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    Page<Order> findOrdersByDateRange(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate,
                                     Pageable pageable);

    /**
     * Find orders by customer email
     * Used for customer service and notifications
     * 
     * @param customerEmail the customer email to search for
     * @return List of orders for the specified email
     */
    List<Order> findByCustomerEmail(String customerEmail);

    /**
     * Find orders by total amount range
     * Used for reporting and analytics
     * 
     * @param minAmount the minimum total amount (inclusive)
     * @param maxAmount the maximum total amount (inclusive)
     * @return List of orders within the specified amount range
     */
    @Query("SELECT o FROM Order o WHERE o.totalAmount BETWEEN :minAmount AND :maxAmount")
    List<Order> findOrdersByAmountRange(@Param("minAmount") BigDecimal minAmount, 
                                       @Param("maxAmount") BigDecimal maxAmount);

    /**
     * Find orders that are pending payment
     * Orders with payment status PENDING or PROCESSING
     * 
     * @return List of orders pending payment
     */
    @Query("SELECT o FROM Order o WHERE o.paymentStatus IN ('PENDING', 'PROCESSING')")
    List<Order> findOrdersPendingPayment();

    /**
     * Find orders that need to be shipped
     * Orders with status CONFIRMED or PROCESSING
     * 
     * @return List of orders that need shipping
     */
    @Query("SELECT o FROM Order o WHERE o.status IN ('CONFIRMED', 'PROCESSING')")
    List<Order> findOrdersNeedingShipping();

    /**
     * Find overdue orders
     * Orders that are past expected delivery date but not delivered
     * 
     * @return List of overdue orders
     */
    @Query("SELECT o FROM Order o WHERE o.expectedDeliveryDate < :currentDate AND o.status NOT IN ('DELIVERED', 'CANCELLED', 'RETURNED')")
    List<Order> findOverdueOrders(@Param("currentDate") LocalDateTime currentDate);

    /**
     * Search orders by customer name or order number
     * Uses case-insensitive search for better user experience
     * 
     * @param searchTerm the search term to look for in customer name or order number
     * @return List of orders matching the search criteria
     */
    @Query("SELECT o FROM Order o WHERE " +
           "LOWER(o.customerName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Order> searchOrders(@Param("searchTerm") String searchTerm);

    /**
     * Search orders by customer name or order number with pagination
     * Uses case-insensitive search with pagination
     * 
     * @param searchTerm the search term to look for
     * @param pageable pagination parameters
     * @return Page of orders matching the search criteria
     */
    @Query("SELECT o FROM Order o WHERE " +
           "LOWER(o.customerName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Order> searchOrders(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Count orders by status
     * Useful for reporting and analytics
     * 
     * @param status the order status to count
     * @return Number of orders with the specified status
     */
    long countByStatus(Order.OrderStatus status);

    /**
     * Count orders by payment status
     * Useful for reporting and analytics
     * 
     * @param paymentStatus the payment status to count
     * @return Number of orders with the specified payment status
     */
    long countByPaymentStatus(Order.PaymentStatus paymentStatus);

    /**
     * Count orders by customer
     * Useful for customer analytics
     * 
     * @param customerId the customer ID to count orders for
     * @return Number of orders for the specified customer
     */
    long countByCustomerId(Long customerId);

    /**
     * Get total revenue for a date range
     * Calculates sum of total amounts for orders in date range
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return Total revenue for the date range
     */
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.paymentStatus = 'COMPLETED'")
    BigDecimal getTotalRevenueByDateRange(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Get total revenue for all time
     * Calculates sum of total amounts for all completed orders
     * 
     * @return Total revenue across all orders
     */
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.paymentStatus = 'COMPLETED'")
    BigDecimal getTotalRevenue();

    /**
     * Get average order value for a date range
     * Calculates average total amount for orders in date range
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return Average order value for the date range
     */
    @Query("SELECT AVG(o.totalAmount) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.paymentStatus = 'COMPLETED'")
    BigDecimal getAverageOrderValueByDateRange(@Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * Get average order value for all time
     * Calculates average total amount for all completed orders
     * 
     * @return Average order value across all orders
     */
    @Query("SELECT AVG(o.totalAmount) FROM Order o WHERE o.paymentStatus = 'COMPLETED'")
    BigDecimal getAverageOrderValue();

    /**
     * Check if an order exists with the given order number
     * Useful for validation before creating new orders
     * 
     * @param orderNumber the order number to check
     * @return true if an order with the order number exists, false otherwise
     */
    boolean existsByOrderNumber(String orderNumber);
}
