package com.kmgtechnologies.inventorymanagement.controller;

import com.kmgtechnologies.inventorymanagement.dto.CategoryRequest;
import com.kmgtechnologies.inventorymanagement.dto.CategoryResponse;
import com.kmgtechnologies.inventorymanagement.mapper.CategoryMapper;
import com.kmgtechnologies.inventorymanagement.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryResponse> findAll() {
        return categoryService.findAll().stream().map(CategoryMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public CategoryResponse findById(@PathVariable Long id) {
        return CategoryMapper.toResponse(categoryService.findById(id));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = CategoryMapper.toResponse(categoryService.create(request.name()));

        return ResponseEntity.created(URI.create("/api/categories/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return CategoryMapper.toResponse(categoryService.update(id, request.name()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}