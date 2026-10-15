package com.kmgtechnologies.inventorymanagement.repository;

import com.kmgtechnologies.inventorymanagement.entity.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SupplierRepositoryTest {

    @Autowired
    private SupplierRepository supplierRepository;

    @Test
    void shouldFindSupplierByEmail() {
        Supplier supplier = new Supplier();
        supplier.setName("Apple");
        supplier.setEmail("contact@apple.com");

        supplierRepository.save(supplier);

        var result = supplierRepository.findByEmail("contact@apple.com");
        assertThat(result).isPresent().get().extracting(Supplier::getName).isEqualTo("Apple");
    }
}