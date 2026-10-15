package com.kmgtechnologies.inventorymanagement.mapper;

import com.kmgtechnologies.inventorymanagement.dto.ProductResponse;
import com.kmgtechnologies.inventorymanagement.entity.Product;

public final class ProductMapper {

    private ProductMapper() {}

    public static ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getSupplier().getId(),
                product.getSupplier().getName()
        );
    }
}