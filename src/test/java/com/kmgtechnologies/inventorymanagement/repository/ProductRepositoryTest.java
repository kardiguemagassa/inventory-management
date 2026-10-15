package com.kmgtechnologies.inventorymanagement.repository;

import com.kmgtechnologies.inventorymanagement.entity.Category;
import com.kmgtechnologies.inventorymanagement.entity.Product;
import com.kmgtechnologies.inventorymanagement.entity.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Test
    void shouldFindProductByNameIgnoringCase() {

        Category category = new Category();
        category.setName("Electronics");
        category = categoryRepository.save(category);

        Supplier supplier = new Supplier();
        supplier.setName("Test Supplier");
        supplier = supplierRepository.save(supplier);

        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new BigDecimal("99.99"));
        product.setQuantity(10);
        product.setCategory(category);
        product.setSupplier(supplier);

        productRepository.save(product);

        List<Product> results = productRepository.findByNameContainingIgnoreCase("test product");

        assertEquals(1, results.size());
        assertEquals("Test Product", results.getFirst().getName());
    }

    @Test
    void shouldFindProductsByCategoryId() {
        Category electronics = new Category();
        electronics.setName("Electronics");
        electronics = categoryRepository.save(electronics);

        Category books = new Category();
        books.setName("Books");
        books = categoryRepository.save(books);

        Supplier supplier = new Supplier();
        supplier.setName("Amazon");
        supplier = supplierRepository.save(supplier);

        Product laptop = createProduct("Laptop", "999.99", electronics, supplier);
        Product mouse = createProduct("Mouse", "29.99", electronics, supplier);
        Product novel = createProduct("Novel", "19.99", books, supplier);

        productRepository.saveAll(List.of(laptop, mouse, novel));

        Long categoryId = electronics.getId();
        List<Product> results = productRepository.findByCategoryId(electronics.getId());

        assertEquals(2, results.size());
        assertTrue(results.stream()
                .allMatch(product ->
                        product.getCategory().getId().equals(categoryId)));
    }

    @Test
    void shouldFindProductsWithinPriceRange() {
        Category category = new Category();
        category.setName("Electronics");
        category = categoryRepository.save(category);

        Supplier supplier = new Supplier();
        supplier.setName("Test Supplier");
        supplier = supplierRepository.save(supplier);

        Product mouse = createProduct("Mouse", "25.00", category, supplier);
        Product keyboard = createProduct("Keyboard", "75.00", category, supplier);
        Product laptop = createProduct("Laptop", "1200.00", category, supplier);

        productRepository.saveAll(List.of(mouse, keyboard, laptop));

        List<Product> results = productRepository.findByPriceBetween(new BigDecimal("20.00"), new BigDecimal("100.00"));

        assertThat(results).extracting(Product::getName).containsExactlyInAnyOrder("Mouse", "Keyboard");
    }

    @Test
    void shouldFindProductsBySupplierId() {
        Category category = new Category();
        category.setName("Electronics");
        category = categoryRepository.save(category);

        Supplier supplier1 = new Supplier();
        supplier1.setName("Apple");
        supplier1 = supplierRepository.save(supplier1);

        Supplier supplier2 = new Supplier();
        supplier2.setName("Samsung");
        supplier2 = supplierRepository.save(supplier2);

        Product iphone = createProduct("iPhone", "999.00", category, supplier1);
        Product macbook = createProduct("MacBook", "1999.00", category, supplier1);
        Product galaxy = createProduct("Galaxy", "899.00", category, supplier2);

        productRepository.saveAll(List.of(iphone, macbook, galaxy));

        List<Product> results = productRepository.findBySupplierId(supplier1.getId());

        assertThat(results).extracting(Product::getName).containsExactlyInAnyOrder("iPhone", "MacBook");
    }

    private Product createProduct(String name, String price, Category category, Supplier supplier) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        product.setQuantity(10);
        product.setCategory(category);
        product.setSupplier(supplier);
        return product;
    }
}