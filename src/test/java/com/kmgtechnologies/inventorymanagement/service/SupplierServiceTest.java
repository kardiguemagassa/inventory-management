package com.kmgtechnologies.inventorymanagement.service;

import com.kmgtechnologies.inventorymanagement.entity.Supplier;
import com.kmgtechnologies.inventorymanagement.exception.DuplicateResourceException;
import com.kmgtechnologies.inventorymanagement.exception.ResourceNotFoundException;
import com.kmgtechnologies.inventorymanagement.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierService supplierService;

    private Supplier supplier;

    @BeforeEach
    void setUp() {
        supplier = new Supplier();
        supplier.setId(1L);
        supplier.setName("Apple");
        supplier.setEmail("contact@apple.com");
        supplier.setPhone("0102030405");
    }

    @Test
    void shouldFindAllSuppliers() {
        when(supplierRepository.findAll()).thenReturn(List.of(supplier));

        List<Supplier> results = supplierService.findAll();

        assertThat(results).hasSize(1).extracting(Supplier::getName).containsExactly("Apple");
    }

    @Test
    void shouldFindSupplierById() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));

        Supplier result = supplierService.findById(1L);

        assertThat(result.getEmail()).isEqualTo("contact@apple.com");
    }

    @Test
    void shouldThrowWhenSupplierNotFound() {
        when(supplierRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.findById(99L)).isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("99");
    }

    @Test
    void shouldCreateSupplier() {
        when(supplierRepository.findByEmail("contact@samsung.com")).thenReturn(Optional.empty());

        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Supplier result = supplierService.create("Samsung", "contact@samsung.com", "0601020304");

        assertThat(result.getName()).isEqualTo("Samsung");
        assertThat(result.getEmail()).isEqualTo("contact@samsung.com");

        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    void shouldRejectDuplicateEmailOnCreate() {
        when(supplierRepository.findByEmail("contact@apple.com")).thenReturn(Optional.of(supplier));

        assertThatThrownBy(() -> supplierService.create("Another Apple", "contact@apple.com", "0600000000"))
                .isInstanceOf(DuplicateResourceException.class);

        verify(supplierRepository, never()).save(any(Supplier.class));
    }

    @Test
    void shouldCreateSupplierWithoutEmail() {
        when(supplierRepository.save(any(Supplier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Supplier result = supplierService.create("Local Supplier", null, "0600000000");

        assertThat(result.getEmail()).isNull();
        verify(supplierRepository, never()).findByEmail(any());
        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    void shouldUpdateSupplier() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));

        when(supplierRepository.findByEmail("support@apple.com")).thenReturn(Optional.empty());

        Supplier result = supplierService.update(1L, "Apple France", "support@apple.com", "0600000000");

        assertThat(result.getName()).isEqualTo("Apple France");
        assertThat(result.getEmail()).isEqualTo("support@apple.com");

        verify(supplierRepository, never()).save(any(Supplier.class));
    }

    @Test
    void shouldAllowUpdatingSupplierWithSameEmail() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));

        when(supplierRepository.findByEmail("contact@apple.com")).thenReturn(Optional.of(supplier));

        Supplier result = supplierService.update(1L, "Apple Updated", "contact@apple.com", "0102030405");

        assertThat(result.getName()).isEqualTo("Apple Updated");
    }

    @Test
    void shouldRejectDuplicateEmailOnUpdate() {
        Supplier existing = new Supplier();
        existing.setId(2L);
        existing.setName("Samsung");
        existing.setEmail("contact@samsung.com");

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));

        when(supplierRepository.findByEmail("contact@samsung.com")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> supplierService.update(1L, "Apple Updated", "contact@samsung.com", "0600000000"))
                .isInstanceOf(DuplicateResourceException.class);

        assertThat(supplier.getName()).isEqualTo("Apple");
    }

    @Test
    void shouldDeleteSupplier() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));

        supplierService.delete(1L);

        verify(supplierRepository).delete(supplier);
    }

    @Test
    void shouldRejectDeletingUnknownSupplier() {
        when(supplierRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.delete(99L)).isInstanceOf(ResourceNotFoundException.class);

        verify(supplierRepository, never()).delete(any(Supplier.class));
    }
}