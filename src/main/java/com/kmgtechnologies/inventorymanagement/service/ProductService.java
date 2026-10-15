package com.kmgtechnologies.inventorymanagement.service;

import com.kmgtechnologies.inventorymanagement.entity.Category;
import com.kmgtechnologies.inventorymanagement.entity.Product;
import com.kmgtechnologies.inventorymanagement.entity.Supplier;
import com.kmgtechnologies.inventorymanagement.exception.ResourceNotFoundException;
import com.kmgtechnologies.inventorymanagement.repository.CategoryRepository;
import com.kmgtechnologies.inventorymanagement.repository.ProductRepository;
import com.kmgtechnologies.inventorymanagement.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, SupplierRepository supplierRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public List<Product> findByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public List<Product> findBySupplierId(Long supplierId) {
        return productRepository.findBySupplierId(supplierId);
    }

    public List<Product> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        validatePriceRange(minPrice, maxPrice);
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    @Transactional
    public Product create(String name, String description, BigDecimal price, int quantity, Long categoryId, Long supplierId) {
        validateProduct(price, quantity);

        Category category = getCategory(categoryId);
        Supplier supplier = getSupplier(supplierId);

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setCategory(category);
        product.setSupplier(supplier);

        return productRepository.save(product);
    }

    @Transactional
    public Product update(Long id, String name, String description, BigDecimal price, int quantity, Long categoryId, Long supplierId) {
        Product product = findById(id);
        validateProduct(price, quantity);

        Category category = getCategory(categoryId);
        Supplier supplier = getSupplier(supplierId);

        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setCategory(category);
        product.setSupplier(supplier);

        return product;
    }

    @Transactional
    public void delete(Long id) {
        Product product = findById(id);
        productRepository.delete(product);
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    private Supplier getSupplier(Long id) {
        return supplierRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }

    private void validateProduct(BigDecimal price, int quantity) {
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Product price must be greater than or equal to zero");
        }

        if (quantity < 0) {
            throw new IllegalArgumentException("Product quantity cannot be negative");
        }
    }

    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null || maxPrice == null || minPrice.signum() < 0 || maxPrice.compareTo(minPrice) < 0) {
            throw new IllegalArgumentException("Invalid product price range");
        }
    }
}