package com.kmgtechnologies.inventorymanagement.repository;

import com.kmgtechnologies.inventorymanagement.entity.Product;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"category", "supplier"})
    List<Product> findByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = {"category", "supplier"})
    List<Product> findBySupplierId(Long supplierId);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"category", "supplier"})
    List<Product> findAll();

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"category", "supplier"})
    Optional<Product> findById(@NonNull Long id);

    @EntityGraph(attributePaths = {"category", "supplier"})
    List<Product> findByNameContainingIgnoreCase(String name);

    @EntityGraph(attributePaths = {"category", "supplier"})
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
}