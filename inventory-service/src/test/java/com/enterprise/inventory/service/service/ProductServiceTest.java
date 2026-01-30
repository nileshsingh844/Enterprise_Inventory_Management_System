package com.enterprise.inventory.service.service;

import com.enterprise.inventory.service.dto.ProductDto; // Import Product DTO
import com.enterprise.inventory.service.exception.DuplicateResourceException; // Import custom exception
import com.enterprise.inventory.service.exception.ResourceNotFoundException; // Import custom exception
import com.enterprise.inventory.service.model.Product; // Import Product entity
import com.enterprise.inventory.service.repository.ProductRepository; // Import Product repository
import org.junit.jupiter.api.BeforeEach; // Import JUnit 5 BeforeEach annotation
import org.junit.jupiter.api.DisplayName; // Import JUnit 5 DisplayName annotation
import org.junit.jupiter.api.Test; // Import JUnit 5 Test annotation
import org.junit.jupiter.api.extension.ExtendWith; // Import JUnit 5 ExtendWith annotation
import org.mockito.InjectMocks; // Import Mockito InjectMocks annotation
import org.mockito.Mock; // Import Mockito Mock annotation
import org.mockito.junit.jupiter.MockitoExtension; // Import Mockito JUnit 5 extension

import java.math.BigDecimal; // Import BigDecimal for monetary calculations
import java.util.Arrays; // Import Arrays utility
import java.util.List; // Import List interface
import java.util.Optional; // Import Optional for nullable results

import static org.junit.jupiter.api.Assertions.*; // Import JUnit 5 assertions
import static org.mockito.ArgumentMatchers.*; // Import Mockito argument matchers
import static org.mockito.Mockito.*; // Import Mockito methods

/**
 * Unit tests for ProductService class
 * This test class validates the business logic of the ProductService
 * Uses Mockito for mocking dependencies and JUnit 5 for testing framework
 * 
 * @ExtendWith: Enables Mockito extension for JUnit 5
 * @DisplayName: Provides descriptive test names
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository; // Mocked ProductRepository dependency

    @InjectMocks
    private ProductService productService; // ProductService instance with mocked dependencies

    private Product testProduct; // Test product instance
    private ProductDto testProductDto; // Test product DTO instance

    /**
     * Setup method executed before each test
     * Initializes test data and common mock behaviors
     */
    @BeforeEach
    void setUp() {
        // Create test product entity
        testProduct = Product.builder()
                .productId(1L)
                .sku("TEST-001")
                .name("Test Product")
                .description("Test Description")
                .category("Test Category")
                .unitPrice(new BigDecimal("99.99"))
                .quantityInStock(100)
                .reorderLevel(10)
                .maxStockLevel(200)
                .location("Warehouse A")
                .supplier("Test Supplier")
                .status(Product.ProductStatus.ACTIVE)
                .build();

        // Create test product DTO
        testProductDto = ProductDto.builder()
                .sku("TEST-001")
                .name("Test Product")
                .description("Test Description")
                .category("Test Category")
                .unitPrice(new BigDecimal("99.99"))
                .quantityInStock(100)
                .reorderLevel(10)
                .maxStockLevel(200)
                .location("Warehouse A")
                .supplier("Test Supplier")
                .status("ACTIVE")
                .build();
    }

    /**
     * Test successful product creation
     * Validates that a product is created when valid data is provided
     */
    @Test
    @DisplayName("Should create product when valid data is provided")
    void shouldCreateProductWhenValidDataIsProvided() {
        // Arrange: Set up mock behavior
        when(productRepository.existsBySku("TEST-001")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act: Call the method under test
        ProductDto result = productService.createProduct(testProductDto);

        // Assert: Verify the result
        assertNotNull(result);
        assertEquals("TEST-001", result.getSku());
        assertEquals("Test Product", result.getName());
        assertEquals(new BigDecimal("99.99"), result.getUnitPrice());
        assertEquals(100, result.getQuantityInStock());

        // Verify: Check that repository methods were called
        verify(productRepository).existsBySku("TEST-001");
        verify(productRepository).save(any(Product.class));
    }

    /**
     * Test product creation with duplicate SKU
     * Validates that DuplicateResourceException is thrown when SKU already exists
     */
    @Test
    @DisplayName("Should throw DuplicateResourceException when SKU already exists")
    void shouldThrowDuplicateResourceExceptionWhenSkuAlreadyExists() {
        // Arrange: Set up mock to return true for SKU existence
        when(productRepository.existsBySku("TEST-001")).thenReturn(true);

        // Act & Assert: Verify exception is thrown
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> productService.createProduct(testProductDto)
        );

        // Assert: Verify exception message
        assertEquals("Product with SKU TEST-001 already exists", exception.getMessage());

        // Verify: Check that save was not called
        verify(productRepository, never()).save(any(Product.class));
    }

    /**
     * Test successful product update
     * Validates that a product is updated when valid data is provided
     */
    @Test
    @DisplayName("Should update product when valid data is provided")
    void shouldUpdateProductWhenValidDataIsProvided() {
        // Arrange: Set up mock behaviors
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.existsBySku("TEST-002")).thenReturn(false);
        
        Product updatedProduct = Product.builder()
                .productId(1L)
                .sku("TEST-002")
                .name("Updated Product")
                .description("Updated Description")
                .category("Updated Category")
                .unitPrice(new BigDecimal("149.99"))
                .quantityInStock(150)
                .reorderLevel(15)
                .maxStockLevel(250)
                .location("Warehouse B")
                .supplier("Updated Supplier")
                .status(Product.ProductStatus.ACTIVE)
                .build();
        
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        ProductDto updateDto = ProductDto.builder()
                .sku("TEST-002")
                .name("Updated Product")
                .description("Updated Description")
                .category("Updated Category")
                .unitPrice(new BigDecimal("149.99"))
                .quantityInStock(150)
                .reorderLevel(15)
                .maxStockLevel(250)
                .location("Warehouse B")
                .supplier("Updated Supplier")
                .status("ACTIVE")
                .build();

        // Act: Call the method under test
        ProductDto result = productService.updateProduct(1L, updateDto);

        // Assert: Verify the result
        assertNotNull(result);
        assertEquals("TEST-002", result.getSku());
        assertEquals("Updated Product", result.getName());
        assertEquals(new BigDecimal("149.99"), result.getUnitPrice());
        assertEquals(150, result.getQuantityInStock());

        // Verify: Check that repository methods were called
        verify(productRepository).findById(1L);
        verify(productRepository).existsBySku("TEST-002");
        verify(productRepository).save(any(Product.class));
    }

    /**
     * Test product update with non-existent product
     * Validates that ResourceNotFoundException is thrown when product doesn't exist
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent product")
    void shouldThrowResourceNotFoundExceptionWhenUpdatingNonExistentProduct() {
        // Arrange: Set up mock to return empty optional
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert: Verify exception is thrown
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.updateProduct(999L, testProductDto)
        );

        // Assert: Verify exception message
        assertEquals("Product not found with id: 999", exception.getMessage());

        // Verify: Check that save was not called
        verify(productRepository, never()).save(any(Product.class));
    }

    /**
     * Test getting product by ID
     * Validates that a product is returned when valid ID is provided
     */
    @Test
    @DisplayName("Should return product when valid ID is provided")
    void shouldReturnProductWhenValidIdIsProvided() {
        // Arrange: Set up mock behavior
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act: Call the method under test
        ProductDto result = productService.getProductById(1L);

        // Assert: Verify the result
        assertNotNull(result);
        assertEquals("TEST-001", result.getSku());
        assertEquals("Test Product", result.getName());

        // Verify: Check that repository method was called
        verify(productRepository).findById(1L);
    }

    /**
     * Test getting product by non-existent ID
     * Validates that ResourceNotFoundException is thrown when product doesn't exist
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when getting non-existent product")
    void shouldThrowResourceNotFoundExceptionWhenGettingNonExistentProduct() {
        // Arrange: Set up mock to return empty optional
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert: Verify exception is thrown
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProductById(999L)
        );

        // Assert: Verify exception message
        assertEquals("Product not found with id: 999", exception.getMessage());
    }

    /**
     * Test getting product by SKU
     * Validates that a product is returned when valid SKU is provided
     */
    @Test
    @DisplayName("Should return product when valid SKU is provided")
    void shouldReturnProductWhenValidSkuIsProvided() {
        // Arrange: Set up mock behavior
        when(productRepository.findBySku("TEST-001")).thenReturn(Optional.of(testProduct));

        // Act: Call the method under test
        ProductDto result = productService.getProductBySku("TEST-001");

        // Assert: Verify the result
        assertNotNull(result);
        assertEquals("TEST-001", result.getSku());
        assertEquals("Test Product", result.getName());

        // Verify: Check that repository method was called
        verify(productRepository).findBySku("TEST-001");
    }

    /**
     * Test getting all products
     * Validates that all products are returned
     */
    @Test
    @DisplayName("Should return all products when requested")
    void shouldReturnAllProductsWhenRequested() {
        // Arrange: Set up mock behavior
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findAll()).thenReturn(products);

        // Act: Call the method under test
        List<ProductDto> result = productService.getAllProducts();

        // Assert: Verify the result
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TEST-001", result.get(0).getSku());

        // Verify: Check that repository method was called
        verify(productRepository).findAll();
    }

    /**
     * Test getting products by category
     * Validates that products are filtered by category
     */
    @Test
    @DisplayName("Should return products filtered by category")
    void shouldReturnProductsFilteredByCategory() {
        // Arrange: Set up mock behavior
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByCategory("Test Category")).thenReturn(products);

        // Act: Call the method under test
        List<ProductDto> result = productService.getProductsByCategory("Test Category");

        // Assert: Verify the result
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Category", result.get(0).getCategory());

        // Verify: Check that repository method was called
        verify(productRepository).findByCategory("Test Category");
    }

    /**
     * Test searching products
     * Validates that products are searched by name or SKU
     */
    @Test
    @DisplayName("Should return products matching search term")
    void shouldReturnProductsMatchingSearchTerm() {
        // Arrange: Set up mock behavior
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.searchProductsByNameOrSku("Test")).thenReturn(products);

        // Act: Call the method under test
        List<ProductDto> result = productService.searchProducts("Test");

        // Assert: Verify the result
        assertNotNull(result);
        assertEquals(1, result.size());

        // Verify: Check that repository method was called
        verify(productRepository).searchProductsByNameOrSku("Test");
    }

    /**
     * Test getting products needing reorder
     * Validates that products with low stock are returned
     */
    @Test
    @DisplayName("Should return products needing reorder")
    void shouldReturnProductsNeedingReorder() {
        // Arrange: Create product with low stock
        Product lowStockProduct = Product.builder()
                .productId(2L)
                .sku("LOW-001")
                .name("Low Stock Product")
                .quantityInStock(5)
                .reorderLevel(10)
                .status(Product.ProductStatus.ACTIVE)
                .build();

        List<Product> products = Arrays.asList(lowStockProduct);
        when(productRepository.findProductsNeedingReorder()).thenReturn(products);

        // Act: Call the method under test
        List<ProductDto> result = productService.getProductsNeedingReorder();

        // Assert: Verify the result
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getNeedsReorder());

        // Verify: Check that repository method was called
        verify(productRepository).findProductsNeedingReorder();
    }

    /**
     * Test updating stock quantity
     * Validates that stock quantity is updated correctly
     */
    @Test
    @DisplayName("Should update stock quantity successfully")
    void shouldUpdateStockQuantitySuccessfully() {
        // Arrange: Set up mock behaviors
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        
        Product updatedProduct = Product.builder()
                .productId(1L)
                .sku("TEST-001")
                .name("Test Product")
                .quantityInStock(95) // Reduced by 5
                .reorderLevel(10)
                .status(Product.ProductStatus.ACTIVE)
                .build();
        
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // Act: Call the method under test
        ProductDto result = productService.updateStockQuantity(1L, -5);

        // Assert: Verify the result
        assertNotNull(result);
        assertEquals(95, result.getQuantityInStock());

        // Verify: Check that repository methods were called
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    /**
     * Test updating stock quantity with negative result
     * Validates that IllegalArgumentException is thrown when result would be negative
     */
    @Test
    @DisplayName("Should throw IllegalArgumentException when stock update results in negative quantity")
    void shouldThrowIllegalArgumentExceptionWhenStockUpdateResultsInNegativeQuantity() {
        // Arrange: Set up mock behavior
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act & Assert: Verify exception is thrown
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.updateStockQuantity(1L, -150) // More than current stock
        );

        // Assert: Verify exception message
        assertEquals("Stock quantity cannot be negative", exception.getMessage());

        // Verify: Check that save was not called
        verify(productRepository, never()).save(any(Product.class));
    }

    /**
     * Test deleting a product
     * Validates that a product is deleted successfully
     */
    @Test
    @DisplayName("Should delete product successfully")
    void shouldDeleteProductSuccessfully() {
        // Arrange: Set up mock behavior
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        doNothing().when(productRepository).delete(testProduct);

        // Act: Call the method under test
        assertDoesNotThrow(() -> productService.deleteProduct(1L));

        // Verify: Check that repository methods were called
        verify(productRepository).findById(1L);
        verify(productRepository).delete(testProduct);
    }

    /**
     * Test deleting non-existent product
     * Validates that ResourceNotFoundException is thrown when product doesn't exist
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent product")
    void shouldThrowResourceNotFoundExceptionWhenDeletingNonExistentProduct() {
        // Arrange: Set up mock to return empty optional
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert: Verify exception is thrown
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.deleteProduct(999L)
        );

        // Assert: Verify exception message
        assertEquals("Product not found with id: 999", exception.getMessage());

        // Verify: Check that delete was not called
        verify(productRepository, never()).delete(any(Product.class));
    }

    /**
     * Test getting total inventory value
     * Validates that total inventory value is calculated correctly
     */
    @Test
    @DisplayName("Should return total inventory value")
    void shouldReturnTotalInventoryValue() {
        // Arrange: Set up mock behavior
        BigDecimal expectedValue = new BigDecimal("9999.00");
        when(productRepository.getTotalInventoryValue()).thenReturn(expectedValue);

        // Act: Call the method under test
        BigDecimal result = productService.getTotalInventoryValue();

        // Assert: Verify the result
        assertNotNull(result);
        assertEquals(expectedValue, result);

        // Verify: Check that repository method was called
        verify(productRepository).getTotalInventoryValue();
    }

    /**
     * Test getting total inventory value when null is returned
     * Validates that zero is returned when repository returns null
     */
    @Test
    @DisplayName("Should return zero when total inventory value is null")
    void shouldReturnZeroWhenTotalInventoryValueIsNull() {
        // Arrange: Set up mock to return null
        when(productRepository.getTotalInventoryValue()).thenReturn(null);

        // Act: Call the method under test
        BigDecimal result = productService.getTotalInventoryValue();

        // Assert: Verify the result
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result);

        // Verify: Check that repository method was called
        verify(productRepository).getTotalInventoryValue();
    }
}
