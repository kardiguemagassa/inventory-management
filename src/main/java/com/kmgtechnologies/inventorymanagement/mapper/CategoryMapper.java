package com.kmgtechnologies.inventorymanagement.mapper;

import com.kmgtechnologies.inventorymanagement.dto.CategoryResponse;
import com.kmgtechnologies.inventorymanagement.entity.Category;

public final class CategoryMapper {

    private CategoryMapper() {}

    public static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}