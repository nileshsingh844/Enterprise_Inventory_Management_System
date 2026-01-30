package com.enterprise.inventory.service.controller;

import com.enterprise.inventory.service.dto.ProductDto; // Import Product DTO
import com.enterprise.inventory.service.service.ProductService; // Import ProductService
import com.fasterxml.jackson.databind.ObjectMapper; // Import Jackson ObjectMapper for JSON serialization
import org.junit.jupiter.api.BeforeEach; // Import JUnit 5 BeforeEach annotation
import org.junit.jupiter.api.DisplayName; // Import JUnit 5 DisplayName annotation
import org.junit.jupiter.api.Test; // Import JUnit 5 Test annotation
import org.springframework.beans.factory.annotation.Autowired; // Import Spring Autowired annotation
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest; // Import Spring Boot WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean; // Import Spring Boot MockBean annotation
import org.springframework.http.MediaType; // Import MediaType for HTTP content types
import org.springframework.test.web.servlet.MockMvc; // Import MockMvc for testing controllers

import java.math.BigDecimal; // Import BigDecimal for monetary calculations
import java.util.Arrays; // Import Arrays utility
import java.util.List; // Import List interface

import static org.mockito.ArgumentMatchers.any; // Import Mockito any matcher
import static org.mockito.ArgumentMatchers.anyLong; // Import Mockito anyLong matcher
import static org.mockito.ArgumentMatchers.anyString; // Import Mockito anyString matcher
import static org.mockito.Mockito.when; // Import Mockito when method
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*; // Import MockMvc request builders
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*; // Import MockMvc result matchers

/**
 * Unit tests for ProductController class
 * This test class validates the REST API endpoints of the ProductController
 * Uses MockMvc for testing Spring MVC controllers and Mockito for mocking services
 * 
 * @WebMvcTest: Loads only the web layer for testing controllers
 * @MockBean: Creates mock beans for dependencies
 * @DisplayName: Provides descriptive test names
 */
@WebMvcTest(ProductController.class)
@DisplayName("Product Controller Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc; // MockMvc instance for testing controllers

    @MockBean
    private ProductService productService; // Mocked ProductService dependency

    @Autowired
    private ObjectMapper objectMapper; // Jackson ObjectMapper for JSON serialization

    private ProductDto testProductDto; // Test product DTO instance

    /**
     * Setup method executed before each test
     * Initializes test data and common mock behaviors
     */
    @BeforeEach
    void setUp() {
        // Create test product DTO
        testProductDto = ProductDto.builder()
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
                .status("ACTIVE")
                .lowStock(false)
                .needsReorder(false)
                .build();
    }

    /**
     * Test successful product creation
     * Validates that a product is created via POST request
     */
    @Test
    @DisplayName("Should create product via POST request")
    void shouldCreateProductViaPostRequest() throws Exception {
        // Arrange: Set up mock behavior
        when(productService.createProduct(any(ProductDto.class))).thenReturn(testProductDto);

        // Act & Assert: Perform POST request and verify response
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.sku").value("TEST-001"))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.unitPrice").value(99.99))
                .andExpect(jsonPath("$.quantityInStock").value(100))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * Test product creation with invalid data
     * Validates that validation errors are returned for invalid data
     */
    @Test
    @DisplayName("Should return validation errors for invalid product data")
    void shouldReturnValidationErrorsForInvalidProductData() throws Exception {
        // Arrange: Create invalid product DTO (missing required fields)
        ProductDto invalidProductDto = ProductDto.builder()
                .sku("") // Empty SKU
                .name("") // Empty name
                .unitPrice(null) // Null price
                .quantityInStock(-5) // Negative quantity
                .build();

        // Act & Assert: Perform POST request and verify validation errors
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProductDto)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test successful product update
     * Validates that a product is updated via PUT request
     */
    @Test
    @DisplayName("Should update product via PUT request")
    void shouldUpdateProductViaPutRequest() throws Exception {
        // Arrange: Set up mock behavior
        when(productService.updateProduct(anyLong(), any(ProductDto.class))).thenReturn(testProductDto);

        // Act & Assert: Perform PUT request and verify response
        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.sku").value("TEST-001"))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    /**
     * Test getting product by ID
     * Validates that a product is returned via GET request
     */
    @Test
    @DisplayName("Should return product via GET request by ID")
    void shouldReturnProductViaGetRequestById() throws Exception {
        // Arrange: Set up mock behavior
        when(productService.getProductById(1L)).thenReturn(testProductDto);

        // Act & Assert: Perform GET request and verify response
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.sku").value("TEST-001"))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.unitPrice").value(99.99));
    }

    /**
     * Test getting product by SKU
     * Validates that a product is returned via GET request by SKU
     */
    @Test
    @DisplayName("Should return product via GET request by SKU")
    void shouldReturnProductViaGetRequestBySku() throws Exception {
        // Arrange: Set up mock behavior
        when(productService.getProductBySku("TEST-001")).thenReturn(testProductDto);

        // Act & Assert: Perform GET request and verify response
        mockMvc.perform(get("/api/products/sku/TEST-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("TEST-001"))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    /**
     * Test getting all products
     * Validates that all products are returned via GET request
     */
    @Test
    @DisplayName("Should return all products via GET request")
    void shouldReturnAllProductsViaGetRequest() throws Exception {
        // Arrange: Set up mock behavior
        List<ProductDto> products = Arrays.asList(testProductDto);
        when(productService.getAllProducts()).thenReturn(products);

        // Act & Assert: Perform GET request and verify response
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].sku").value("TEST-001"))
                .andExpect(jsonPath("$[0].name").value("Test Product"));
    }

    /**
     * Test getting products by category
     * Validates that products are filtered by category via GET request
     */
    @Test
    @DisplayName("Should return products filtered by category via GET request")
    void shouldReturnProductsFilteredByCategoryViaGetRequest() throws Exception {
        // Arrange: Set up mock behavior
        List<ProductDto> products = Arrays.asList(testProductDto);
        when(productService.getProductsByCategory("Test Category")).thenReturn(products);

        // Act & Assert: Perform GET request and verify response
        mockMvc.perform(get("/api/products/category/Test Category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].category").value("Test Category"));
    }

    /**
     * Test searching products
     * Validates that products are searched via GET request
     */
    @Test
    @DisplayName("Should return products matching search term via GET request")
    void shouldReturnProductsMatchingSearchTermViaGetRequest() throws Exception {
        // Arrange: Set up mock behavior
        List<ProductDto> products = Arrays.asList(testProductDto);
        when(productService.searchProducts("Test")).thenReturn(products);

        // Act & Assert: Perform GET request and verify response
        mockMvc.perform(get("/api/products/search?term=Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Product"));
    }

    /**
     * Test getting products needing reorder
     * Validates that products needing reorder are returned via GET request
     */
    @Test
    @DisplayName("Should return products needing reorder via GET request")
    void shouldReturnProductsNeedingReorderViaGetRequest() throws Exception {
        // Arrange: Create product that needs reorder
        ProductDto reorderProduct = ProductDto.builder()
                .productId(2L)
                .sku("LOW-001")
                .name("Low Stock Product")
                .quantityInStock(5)
                .reorderLevel(10)
                .needsReorder(true)
                .build();

        List<ProductDto> products = Arrays.asList(reorderProduct);
        when(productService.getProductsNeedingReorder()).thenReturn(products);

        // Act & Assert: Perform GET request and verify response
        mockMvc.perform(get("/api/products/reorder-needed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].needsReorder").value(true));
    }

    /**
     * Test getting products with low stock
     * Validates that products with low stock are returned via GET request
     */
    @Test
    @DisplayName("Should return products with low stock via GET request")
    void shouldReturnProductsWithLowStockViaGetRequest() throws Exception {
        // Arrange: Create product with low stock
        ProductDto lowStockProduct = ProductDto.builder()
                .productId(2L)
                .sku("LOW-001")
                .name("Low Stock Product")
                .quantityInStock(10)
                .reorderLevel(10)
                .lowStock(true)
                .build();

        List<ProductDto> products = Arrays.asList(lowStockProduct);
        when(productService.getProductsWithLowStock()).thenReturn(products);

        // Act & Assert: Perform GET request and verify response
        mockMvc.perform(get("/api/products/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].lowStock").value(true));
    }

    /**
     * Test updating stock quantity
     * Validates that stock quantity is updated via PATCH request
     */
    @Test
    @DisplayName("Should update stock quantity via PATCH request")
    void shouldUpdateStockQuantityViaPatchRequest() throws Exception {
        // Arrange: Create updated product DTO
        ProductDto updatedProduct = ProductDto.builder()
                .productId(1L)
                .sku("TEST-001")
                .name("Test Product")
                .quantityInStock(95) // Reduced by 5
                .build();

        when(productService.updateStockQuantity(1L, -5)).thenReturn(updatedProduct);

        // Act & Assert: Perform PATCH request and verify response
        mockMvc.perform(patch("/api/products/1/stock?change=-5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantityInStock").value(95));
    }

    /**
     * Test deleting a product
     * Validates that a product is deleted via DELETE request
     */
    @Test
    @DisplayName("Should delete product via DELETE request")
    void shouldDeleteProductViaDeleteRequest() throws Exception {
        // Arrange: Set up mock behavior (void method, no return value)
        doNothing().when(productService).deleteProduct(1L);

        // Act & Assert: Perform DELETE request and verify response
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        // Verify: Check that service method was called
        verify(productService).deleteProduct(1L);
    }

    /**
     * Test getting total inventory value
     * Validates that total inventory value is returned via GET request
     */
    @Test
    @DisplayName("Should return total inventory value via GET request")
    void shouldReturnTotalInventoryValueViaGetRequest() throws Exception {
        // Arrange: Set up mock behavior
        when(productService.getTotalInventoryValue()).thenReturn(new BigDecimal("9999.00"));

        // Act & Assert: Perform GET request and verify response
        mockMvc.perform(get("/api/products/inventory-value"))
                .andExpect(status().isOk())
                .andExpect(content().string("9999.00"));
    }

    /**
     * Test health check endpoint
     * Validates that health check returns status
     */
    @Test
    @DisplayName("Should return health status via GET request")
    void shouldReturnHealthStatusViaGetRequest() throws Exception {
        // Act & Assert: Perform GET request and verify response
        mockMvc.perform(get("/api/products/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Inventory Service is healthy"));
    }

    /**
     * Test getting product with invalid ID
     * Validates that validation error is returned for invalid ID
     */
    @Test
    @DisplayName("Should return validation error for invalid product ID")
    void shouldReturnValidationErrorForInvalidProductId() throws Exception {
        // Act & Assert: Perform GET request with invalid ID and verify error
        mockMvc.perform(get("/api/products/0")) // ID must be positive
                .andExpect(status().isBadRequest());
    }

    /**
     * Test getting product with missing search term
     * Validates that validation error is returned for missing search term
     */
    @Test
    @DisplayName("Should return validation error for missing search term")
    void shouldReturnValidationErrorForMissingSearchTerm() throws Exception {
        // Act & Assert: Perform GET request without search term and verify error
        mockMvc.perform(get("/api/products/search")) // Missing 'term' parameter
                .andExpect(status().isBadRequest());
    }

    /**
     * Test getting product with empty search term
     * Validates that validation error is returned for empty search term
     */
    @Test
    @DisplayName("Should return validation error for empty search term")
    void shouldReturnValidationErrorForEmptySearchTerm() throws Exception {
        // Act & Assert: Perform GET request with empty search term and verify error
        mockMvc.perform(get("/api/products/search?term=")) // Empty search term
                .andExpect(status().isBadRequest());
    }
}
