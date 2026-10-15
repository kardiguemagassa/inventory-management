package com.kmgtechnologies.inventorymanagement.service;

import com.kmgtechnologies.inventorymanagement.entity.Category;
import com.kmgtechnologies.inventorymanagement.entity.Product;
import com.kmgtechnologies.inventorymanagement.entity.Supplier;
import com.kmgtechnologies.inventorymanagement.exception.ResourceNotFoundException;
import com.kmgtechnologies.inventorymanagement.repository.CategoryRepository;
import com.kmgtechnologies.inventorymanagement.repository.ProductRepository;
import com.kmgtechnologies.inventorymanagement.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Category category;
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        supplier = new Supplier();
        supplier.setId(2L);
        supplier.setName("Apple");

        product = new Product();
        product.setId(3L);
        product.setName("iPhone");
        product.setDescription("Smartphone");
        product.setPrice(new BigDecimal("999.99"));
        product.setQuantity(10);
        product.setCategory(category);
        product.setSupplier(supplier);
    }

    @Test
    void shouldFindAllProducts() {when(productRepository.findAll()).thenReturn(List.of(product));

        assertThat(productService.findAll()).containsExactly(product);
    }

    @Test
    void shouldFindProductById() {
        when(productRepository.findById(3L)).thenReturn(Optional.of(product));

        assertThat(productService.findById(3L).getName()).isEqualTo("iPhone");
    }

    @Test
    void shouldRejectUnknownProduct() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldFindProductsByCategory() {
        when(productRepository.findByCategoryId(1L)).thenReturn(List.of(product));

        assertThat(productService.findByCategoryId(1L)).containsExactly(product);
    }

    @Test
    void shouldFindProductsBySupplier() {
        when(productRepository.findBySupplierId(2L)).thenReturn(List.of(product));

        assertThat(productService.findBySupplierId(2L)).containsExactly(product);
    }

    @Test
    void shouldSearchProductsByName() {
        when(productRepository.findByNameContainingIgnoreCase("phone")).thenReturn(List.of(product));

        assertThat(productService.searchByName("phone")).containsExactly(product);
    }

    @Test
    void shouldFindProductsByPriceRange() {
        BigDecimal min = new BigDecimal("500");
        BigDecimal max = new BigDecimal("1500");

        when(productRepository.findByPriceBetween(min, max)).thenReturn(List.of(product));

        assertThat(productService.findByPriceRange(min, max)).containsExactly(product);
    }

    @Test
    void shouldRejectInvalidPriceRange() {
        assertThatThrownBy(() -> productService.findByPriceRange(new BigDecimal("1500"), new BigDecimal("500"))).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(productRepository);
    }

    @Test
    void shouldCreateProduct() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(supplierRepository.findById(2L)).thenReturn(Optional.of(supplier));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.create("MacBook", "Laptop", new BigDecimal("1499.99"), 5, 1L, 2L);

        assertThat(result.getName()).isEqualTo("MacBook");
        assertThat(result.getCategory()).isSameAs(category);
        assertThat(result.getSupplier()).isSameAs(supplier);

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldRejectUnknownCategoryOnCreate() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create("MacBook", "Laptop", BigDecimal.TEN, 5, 99L, 2L)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldRejectUnknownSupplierOnCreate() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(supplierRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create("MacBook", "Laptop", BigDecimal.TEN, 5, 1L, 99L)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Supplier");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldRejectNegativePrice() {
        assertThatThrownBy(() -> productService.create("MacBook", "Laptop", new BigDecimal("-10"), 5, 1L, 2L)).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(categoryRepository, supplierRepository, productRepository);
    }

    @Test
    void shouldRejectNegativeQuantity() {
        assertThatThrownBy(() -> productService.create("MacBook", "Laptop", BigDecimal.TEN, -1, 1L, 2L)).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(categoryRepository, supplierRepository, productRepository);
    }

    @Test
    void shouldUpdateProduct() {
        when(productRepository.findById(3L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(supplierRepository.findById(2L)).thenReturn(Optional.of(supplier));

        Product result = productService.update(3L, "iPhone Pro", "Updated", new BigDecimal("1199.99"), 8, 1L, 2L);

        assertThat(result.getName()).isEqualTo("iPhone Pro");
        assertThat(result.getQuantity()).isEqualTo(8);
        assertThat(result.getPrice()).isEqualByComparingTo("1199.99");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldDeleteProduct() {
        when(productRepository.findById(3L)).thenReturn(Optional.of(product));

        productService.delete(3L);

        verify(productRepository).delete(product);
    }

    @Test
    void shouldRejectDeletingUnknownProduct() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(99L)).isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).delete(any(Product.class));
    }
}