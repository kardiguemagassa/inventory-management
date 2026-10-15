package com.kmgtechnologies.inventorymanagement.service;

import com.kmgtechnologies.inventorymanagement.entity.Supplier;
import com.kmgtechnologies.inventorymanagement.exception.DuplicateResourceException;
import com.kmgtechnologies.inventorymanagement.exception.ResourceNotFoundException;
import com.kmgtechnologies.inventorymanagement.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    public Supplier findById(Long id) {
        return supplierRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }

    @Transactional
    public Supplier create(String name, String email, String phone) {
        checkEmailAvailability(email, null);

        Supplier supplier = new Supplier();
        supplier.setName(name);
        supplier.setEmail(email);
        supplier.setPhone(phone);

        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier update(Long id, String name, String email, String phone) {
        Supplier supplier = findById(id);

        checkEmailAvailability(email, id);

        supplier.setName(name);
        supplier.setEmail(email);
        supplier.setPhone(phone);

        return supplier;
    }

    @Transactional
    public void delete(Long id) {
        Supplier supplier = findById(id);
        supplierRepository.delete(supplier);
    }

    private void checkEmailAvailability(String email, Long currentSupplierId) {
        if (email == null) {
            return;
        }

        supplierRepository.findByEmail(email)
                .filter(existing ->
                        !Objects.equals(existing.getId(), currentSupplierId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Supplier email already exists: " + email);
                });
    }
}