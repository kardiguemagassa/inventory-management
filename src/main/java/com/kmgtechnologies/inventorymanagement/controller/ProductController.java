package com.kmgtechnologies.inventorymanagement.controller;

import com.kmgtechnologies.inventorymanagement.dto.ProductRequest;
import com.kmgtechnologies.inventorymanagement.dto.ProductResponse;
import com.kmgtechnologies.inventorymanagement.mapper.ProductMapper;
import com.kmgtechnologies.inventorymanagement.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> findAll() {
        return productService.findAll().stream().map(ProductMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ProductResponse findById(@PathVariable Long id) {
        return ProductMapper.toResponse(productService.findById(id));
    }

    @GetMapping("/category/{categoryId}")
    public List<ProductResponse> findByCategory(@PathVariable Long categoryId) {
        return productService.findByCategoryId(categoryId).stream().map(ProductMapper::toResponse).toList();
    }

    @GetMapping("/supplier/{supplierId}")
    public List<ProductResponse> findBySupplier(@PathVariable Long supplierId) {
        return productService.findBySupplierId(supplierId).stream().map(ProductMapper::toResponse).toList();
    }

    @GetMapping("/search")
    public List<ProductResponse> searchByName(@RequestParam String name) {
        return productService.searchByName(name).stream().map(ProductMapper::toResponse).toList();
    }

    @GetMapping("/price-range")
    public List<ProductResponse> findByPriceRange(@RequestParam BigDecimal min, @RequestParam BigDecimal max) {
        return productService.findByPriceRange(min, max).stream().map(ProductMapper::toResponse).toList();
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = ProductMapper.toResponse(productService.create(request.name(), request.description(), request.price(), request.quantity(), request.categoryId(), request.supplierId()));

        return ResponseEntity.created(URI.create("/api/products/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ProductMapper.toResponse(productService.update(id, request.name(), request.description(), request.price(), request.quantity(), request.categoryId(), request.supplierId()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}