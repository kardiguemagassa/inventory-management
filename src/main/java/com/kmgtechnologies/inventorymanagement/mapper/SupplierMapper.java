package com.kmgtechnologies.inventorymanagement.mapper;

import com.kmgtechnologies.inventorymanagement.dto.SupplierResponse;
import com.kmgtechnologies.inventorymanagement.entity.Supplier;

public final class SupplierMapper {

    private SupplierMapper() {}

    public static SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(supplier.getId(), supplier.getName(), supplier.getEmail(), supplier.getPhone());
    }
}