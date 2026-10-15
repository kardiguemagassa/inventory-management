package com.kmgtechnologies.inventorymanagement.controller;

import com.kmgtechnologies.inventorymanagement.entity.Supplier;
import com.kmgtechnologies.inventorymanagement.exception.DuplicateResourceException;
import com.kmgtechnologies.inventorymanagement.exception.ResourceNotFoundException;
import com.kmgtechnologies.inventorymanagement.service.SupplierService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class SupplierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SupplierService supplierService;

    private Supplier supplier(Long id, String name, String email, String phone) {
        Supplier supplier = new Supplier();
        supplier.setId(id);
        supplier.setName(name);
        supplier.setEmail(email);
        supplier.setPhone(phone);
        return supplier;
    }

    @Test
    void shouldFindAllSuppliers() throws Exception {
        when(supplierService.findAll()).thenReturn(List.of(supplier(1L, "Tech Distribution", "contact@tech.com", "0123456789")));

        mockMvc.perform(get("/api/suppliers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Tech Distribution"))
                .andExpect(jsonPath("$[0].email").value("contact@tech.com"));
    }

    @Test
    void shouldFindSupplierById() throws Exception {
        when(supplierService.findById(1L)).thenReturn(supplier(1L, "Tech Distribution", "contact@tech.com", "0123456789"));

        mockMvc.perform(get("/api/suppliers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tech Distribution"));
    }

    @Test
    void shouldReturn404WhenSupplierNotFound() throws Exception {
        when(supplierService.findById(99L)).thenThrow(new ResourceNotFoundException("Supplier not found with id: 99"));

        mockMvc.perform(get("/api/suppliers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldCreateSupplier() throws Exception {
        when(supplierService.create("Tech Distribution", "contact@tech.com", "0123456789"))
                .thenReturn(supplier(2L, "Tech Distribution", "contact@tech.com", "0123456789"));

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Tech Distribution",
                                  "email": "contact@tech.com",
                                  "phone": "0123456789"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/suppliers/2"))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Tech Distribution"));
    }

    @Test
    void shouldRejectBlankSupplierName() throws Exception {
        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": " ",
                                  "email": "contact@tech.com"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(supplierService);
    }

    @Test
    void shouldRejectInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Tech Distribution",
                                  "email": "invalid-email"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(supplierService);
    }

    @Test
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        when(supplierService.create("Tech Distribution", "contact@tech.com", "0123456789")).thenThrow(new DuplicateResourceException("Supplier email already exists"));

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Tech Distribution",
                                  "email": "contact@tech.com",
                                  "phone": "0123456789"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void shouldUpdateSupplier() throws Exception {
        when(supplierService.update(1L, "Updated Supplier", "updated@tech.com", "0987654321")).thenReturn(supplier(1L, "Updated Supplier", "updated@tech.com", "0987654321"));

        mockMvc.perform(put("/api/suppliers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Supplier",
                                  "email": "updated@tech.com",
                                  "phone": "0987654321"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Supplier"))
                .andExpect(jsonPath("$.email").value("updated@tech.com"));
    }

    @Test
    void shouldDeleteSupplier() throws Exception {
        mockMvc.perform(delete("/api/suppliers/1"))
                .andExpect(status().isNoContent());

        verify(supplierService).delete(1L);
    }
}