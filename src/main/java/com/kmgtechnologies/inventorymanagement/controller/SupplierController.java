package com.kmgtechnologies.inventorymanagement.controller;

import com.kmgtechnologies.inventorymanagement.dto.SupplierRequest;
import com.kmgtechnologies.inventorymanagement.dto.SupplierResponse;
import com.kmgtechnologies.inventorymanagement.mapper.SupplierMapper;
import com.kmgtechnologies.inventorymanagement.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public List<SupplierResponse> findAll() {
        return supplierService.findAll().stream().map(SupplierMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public SupplierResponse findById(@PathVariable Long id) {
        return SupplierMapper.toResponse(supplierService.findById(id));
    }

    @PostMapping
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierRequest request) {
        SupplierResponse response = SupplierMapper.toResponse(supplierService.create(request.name(), request.email(), request.phone()));

        return ResponseEntity.created(URI.create("/api/suppliers/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public SupplierResponse update(@PathVariable Long id, @Valid @RequestBody SupplierRequest request) {
        return SupplierMapper.toResponse(supplierService.update(id, request.name(), request.email(), request.phone()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        supplierService.delete(id);
    }
}