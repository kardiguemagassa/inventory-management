package com.kmgtechnologies.inventorymanagement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(

        @NotBlank(message = "Product name is required")
        @Size(max = 255)
        String name,

        @Size(max = 1000)
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", message = "Price must be non-negative")
        BigDecimal price,

        @NotNull(message = "Quantity is required")
        @PositiveOrZero(message = "Quantity must be non-negative")
        Integer quantity,

        @NotNull(message = "Category is required")
        @Positive(message = "Category ID must be positive")
        Long categoryId,

        @NotNull(message = "Supplier is required")
        @Positive(message = "Supplier ID must be positive")
        Long supplierId

) {}