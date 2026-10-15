package com.kmgtechnologies.inventorymanagement.controller;

import com.kmgtechnologies.inventorymanagement.entity.Category;
import com.kmgtechnologies.inventorymanagement.entity.Product;
import com.kmgtechnologies.inventorymanagement.entity.Supplier;
import com.kmgtechnologies.inventorymanagement.repository.CategoryRepository;
import com.kmgtechnologies.inventorymanagement.repository.ProductRepository;
import com.kmgtechnologies.inventorymanagement.repository.SupplierRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void cleanDatabase() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        supplierRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        supplierRepository.deleteAll();
    }

    @Test
    void shouldCreateProduct() throws Exception {
        Category category = new Category();
        category.setName("Accessories");
        category = categoryRepository.saveAndFlush(category);

        Supplier supplier = new Supplier();
        supplier.setName("Accessories Supplier");
        supplier.setEmail("accessories@example.com");
        supplier = supplierRepository.saveAndFlush(supplier);

        String requestBody = """
            {
                "name": "Wireless Mouse",
                "description": "Souris sans fil",
                "price": 49.99,
                "quantity": 20,
                "categoryId": %d,
                "supplierId": %d
            }
            """.formatted(category.getId(), supplier.getId());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Wireless Mouse"))
                .andExpect(jsonPath("$.categoryName").value("Accessories"))
                .andExpect(jsonPath("$.supplierName").value("Accessories Supplier"));

        assertTrue(productRepository.findAll().stream()
                .anyMatch(product -> product.getName().equals("Wireless Mouse")));
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        Product product = createTestProduct();

        String requestBody = """
            {
                "name": "Updated MacBook",
                "description": "Nouvelle description",
                "price": 1799.99,
                "quantity": 15,
                "categoryId": %d,
                "supplierId": %d
            }
            """.formatted(
                product.getCategory().getId(),
                product.getSupplier().getId()
        );

        mockMvc.perform(put("/api/products/{id}", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated MacBook"))
                .andExpect(jsonPath("$.price").value(1799.99))
                .andExpect(jsonPath("$.quantity").value(15));

        Product updatedProduct = productRepository
                .findById(product.getId())
                .orElseThrow();

        assertEquals("Updated MacBook", updatedProduct.getName());
        assertEquals(15, updatedProduct.getQuantity());
        assertEquals(0, new BigDecimal("1799.99")
                .compareTo(updatedProduct.getPrice()));
    }

    @Test
    void shouldReturnProductWithCategoryAndSupplier() throws Exception {
        Category category = new Category();
        category.setName("Electronics");
        category = categoryRepository.saveAndFlush(category);

        Supplier supplier = new Supplier();
        supplier.setName("Tech Distribution");
        supplier.setEmail("integration@tech.com");
        supplier = supplierRepository.saveAndFlush(supplier);

        Product product = new Product();
        product.setName("MacBook Pro");
        product.setDescription("Laptop 14 pouces");
        product.setPrice(new BigDecimal("1999.99"));
        product.setQuantity(10);
        product.setCategory(category);
        product.setSupplier(supplier);
        product = productRepository.saveAndFlush(product);

        mockMvc.perform(get("/api/products/{id}", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("MacBook Pro"))
                .andExpect(jsonPath("$.categoryName").value("Electronics"))
                .andExpect(jsonPath("$.supplierName").value("Tech Distribution"))
                .andExpect(jsonPath("$.categoryId").value(category.getId()))
                .andExpect(jsonPath("$.supplierId").value(supplier.getId()));
    }

    @Test
    void shouldReturnProductsWithCategoryAndSupplier() throws Exception {
        Category category = new Category();
        category.setName("Computers");
        category = categoryRepository.saveAndFlush(category);

        Supplier supplier = new Supplier();
        supplier.setName("Computer Distribution");
        supplier.setEmail("computers@distribution.com");
        supplier = supplierRepository.saveAndFlush(supplier);

        Product product = new Product();
        product.setName("Dell XPS");
        product.setDescription("Laptop 13 pouces");
        product.setPrice(new BigDecimal("1499.99"));
        product.setQuantity(5);
        product.setCategory(category);
        product.setSupplier(supplier);
        productRepository.saveAndFlush(product);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'Dell XPS')].categoryName")
                        .value(org.hamcrest.Matchers.hasItem("Computers")))
                .andExpect(jsonPath("$[?(@.name == 'Dell XPS')].supplierName")
                        .value(org.hamcrest.Matchers.hasItem("Computer Distribution")));
    }

    @Test
    void shouldFindProductsByCategory() throws Exception {
        Product product = createTestProduct();

        mockMvc.perform(get(
                        "/api/products/category/{categoryId}",
                        product.getCategory().getId()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name")
                        .value("Test MacBook"))
                .andExpect(jsonPath("$[0].categoryName")
                        .value("Test Electronics"));
    }

    @Test
    void shouldFindProductsBySupplier() throws Exception {
        Product product = createTestProduct();

        mockMvc.perform(get(
                        "/api/products/supplier/{supplierId}",
                        product.getSupplier().getId()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name")
                        .value("Test MacBook"))
                .andExpect(jsonPath("$[0].supplierName")
                        .value("Test Supplier"));
    }

    @Test
    void shouldSearchProductsByName() throws Exception {
        createTestProduct();

        mockMvc.perform(get("/api/products/search")
                        .param("name", "MacBook"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name")
                        .value("Test MacBook"))
                .andExpect(jsonPath("$[0].categoryName")
                        .value("Test Electronics"));
    }

    @Test
    void shouldFindProductsByPriceRange() throws Exception {
        createTestProduct();

        mockMvc.perform(get("/api/products/price-range")
                        .param("min", "1000")
                        .param("max", "2500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name")
                        .value("Test MacBook"))
                .andExpect(jsonPath("$[0].supplierName")
                        .value("Test Supplier"));
    }

    @Test
    void shouldDeleteProduct() throws Exception {
        Product product = createTestProduct();
        Long productId = product.getId();

        mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isNoContent());

        assertFalse(productRepository.existsById(productId));
    }

    @Test
    void shouldReturn404WhenProductNotFound() throws Exception {
        mockMvc.perform(get("/api/products/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"));
    }

    @Test
    void shouldReturn400WhenProductRequestIsInvalid() throws Exception {
        String invalidRequestBody = """
            {
                "name": "",
                "description": "Produit invalide",
                "price": -10.00,
                "quantity": -5,
                "categoryId": null,
                "supplierId": null
            }
            """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn404WhenCreatingProductWithUnknownCategory() throws Exception {
        Supplier supplier = new Supplier();
        supplier.setName("Test Supplier");
        supplier.setEmail("unknown-category@example.com");
        supplier = supplierRepository.saveAndFlush(supplier);

        String requestBody = """
            {
                "name": "Test Product",
                "description": "Produit de test",
                "price": 99.99,
                "quantity": 10,
                "categoryId": 999999,
                "supplierId": %d
            }
            """.formatted(supplier.getId());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        assertEquals(0, productRepository.count());
    }

    private Product createTestProduct() {
        Category category = new Category();
        category.setName("Test Electronics");
        category = categoryRepository.saveAndFlush(category);

        Supplier supplier = new Supplier();
        supplier.setName("Test Supplier");
        supplier.setEmail("test-supplier@example.com");
        supplier = supplierRepository.saveAndFlush(supplier);

        Product product = new Product();
        product.setName("Test MacBook");
        product.setDescription("Ordinateur portable");
        product.setPrice(new BigDecimal("1999.99"));
        product.setQuantity(10);
        product.setCategory(category);
        product.setSupplier(supplier);

        return productRepository.saveAndFlush(product);
    }
}