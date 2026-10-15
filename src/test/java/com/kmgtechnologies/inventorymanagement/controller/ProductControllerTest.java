package com.kmgtechnologies.inventorymanagement.controller;

import com.kmgtechnologies.inventorymanagement.entity.Category;
import com.kmgtechnologies.inventorymanagement.entity.Product;
import com.kmgtechnologies.inventorymanagement.entity.Supplier;
import com.kmgtechnologies.inventorymanagement.exception.ResourceNotFoundException;
import com.kmgtechnologies.inventorymanagement.service.ProductService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    private Product product(Long id) {
        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        Supplier supplier = new Supplier();
        supplier.setId(2L);
        supplier.setName("Tech Distribution");

        Product product = new Product();
        product.setId(id);
        product.setName("MacBook Pro");
        product.setDescription("Laptop 14 pouces");
        product.setPrice(new BigDecimal("1999.99"));
        product.setQuantity(10);
        product.setCategory(category);
        product.setSupplier(supplier);

        return product;
    }

    @Test
    void shouldFindAllProducts() throws Exception {
        when(productService.findAll()).thenReturn(List.of(product(1L)));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("MacBook Pro"))
                .andExpect(jsonPath("$[0].categoryName").value("Electronics"))
                .andExpect(jsonPath("$[0].supplierName").value("Tech Distribution"));
    }

    @Test
    void shouldFindProductById() throws Exception {
        when(productService.findById(1L)).thenReturn(product(1L));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.price").value(1999.99))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.categoryId").value(1))
                .andExpect(jsonPath("$.supplierId").value(2));
    }

    @Test
    void shouldReturn404WhenProductNotFound() throws Exception {
        when(productService.findById(99L)).thenThrow(new ResourceNotFoundException("Product not found with id: 99"));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldCreateProduct() throws Exception {
        when(productService.create("MacBook Pro", "Laptop 14 pouces", new BigDecimal("1999.99"), 10, 1L, 2L)).thenReturn(product(3L));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "MacBook Pro",
                                  "description": "Laptop 14 pouces",
                                  "price": 1999.99,
                                  "quantity": 10,
                                  "categoryId": 1,
                                  "supplierId": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/products/3"))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.categoryName").value("Electronics"));
    }

    @Test
    void shouldRejectNegativePrice() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "MacBook Pro",
                                  "price": -100,
                                  "quantity": 10,
                                  "categoryId": 1,
                                  "supplierId": 2
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    @Test
    void shouldFindProductsByCategory() throws Exception {
        when(productService.findByCategoryId(1L)).thenReturn(List.of(product(1L)));

        mockMvc.perform(get("/api/products/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("MacBook Pro"));

        verify(productService).findByCategoryId(1L);
    }

    @Test
    void shouldFindProductsBySupplier() throws Exception {
        when(productService.findBySupplierId(2L)).thenReturn(List.of(product(1L)));

        mockMvc.perform(get("/api/products/supplier/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].supplierId").value(2));

        verify(productService).findBySupplierId(2L);
    }

    @Test
    void shouldSearchProductsByName() throws Exception {
        when(productService.searchByName("MacBook")).thenReturn(List.of(product(1L)));

        mockMvc.perform(get("/api/products/search")
                        .param("name", "MacBook"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("MacBook Pro"));

        verify(productService).searchByName("MacBook");
    }

    @Test
    void shouldFindProductsByPriceRange() throws Exception {
        when(productService.findByPriceRange(new BigDecimal("1000"), new BigDecimal("2500"))).thenReturn(List.of(product(1L)));

        mockMvc.perform(get("/api/products/price-range")
                        .param("min", "1000")
                        .param("max", "2500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(productService).findByPriceRange(new BigDecimal("1000"), new BigDecimal("2500"));
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        when(productService.update(eq(1L), eq("MacBook Pro"), eq("Laptop 14 pouces"), eq(new BigDecimal("1999.99")), eq(10), eq(1L), eq(2L))).thenReturn(product(1L));

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "MacBook Pro",
                              "description": "Laptop 14 pouces",
                              "price": 1999.99,
                              "quantity": 10,
                              "categoryId": 1,
                              "supplierId": 2
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("MacBook Pro"));
    }

    @Test
    void shouldDeleteProduct() throws Exception {
        mockMvc.perform(delete("/api/products/1")).andExpect(status().isNoContent());

        verify(productService).delete(1L);
    }
}