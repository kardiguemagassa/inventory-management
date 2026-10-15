package com.kmgtechnologies.inventorymanagement.controller;

import com.kmgtechnologies.inventorymanagement.entity.Supplier;
import com.kmgtechnologies.inventorymanagement.repository.ProductRepository;
import com.kmgtechnologies.inventorymanagement.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SupplierIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        supplierRepository.deleteAll();
    }

    @Test
    void shouldCreateSupplierSuccessfully() throws Exception {
        // Arrange
        String requestBody = """
                {
                    "name": "Tech Distribution",
                    "email": "contact@techdistribution.com",
                    "phone": "0123456789"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Tech Distribution"))
                .andExpect(jsonPath("$.email").value("contact@techdistribution.com"))
                .andExpect(jsonPath("$.phone").value("0123456789"));

        // Vérifier que le fournisseur est enregistré dans H2
        assertEquals(1, supplierRepository.count());
    }

    @Test
    void shouldGetSupplierById() throws Exception {
        // Arrange : créer un fournisseur dans H2
        Supplier supplier = new Supplier();
        supplier.setName("Tech Distribution");
        supplier.setEmail("contact@techdistribution.com");
        supplier.setPhone("0123456789");

        supplier = supplierRepository.saveAndFlush(supplier);

        // Act & Assert : récupérer le fournisseur par son ID
        mockMvc.perform(get("/api/suppliers/{id}", supplier.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(supplier.getId()))
                .andExpect(jsonPath("$.name").value("Tech Distribution"))
                .andExpect(jsonPath("$.email").value("contact@techdistribution.com"))
                .andExpect(jsonPath("$.phone").value("0123456789"));
    }

    @Test
    void shouldGetAllSuppliers() throws Exception {
        // Arrange : créer deux fournisseurs
        Supplier supplier1 = new Supplier();
        supplier1.setName("Tech Distribution");
        supplier1.setEmail("contact@techdistribution.com");
        supplier1.setPhone("0123456789");

        Supplier supplier2 = new Supplier();
        supplier2.setName("Global Supplies");
        supplier2.setEmail("contact@globalsupplies.com");
        supplier2.setPhone("0987654321");

        supplierRepository.saveAllAndFlush(
                List.of(supplier1, supplier2)
        );

        // Act & Assert : récupérer tous les fournisseurs
        mockMvc.perform(get("/api/suppliers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].name",
                        hasItems("Tech Distribution", "Global Supplies")));
    }

    @Test
    void shouldUpdateSupplierSuccessfully() throws Exception {
        // Arrange : créer un fournisseur
        Supplier supplier = new Supplier();
        supplier.setName("Tech Distribution");
        supplier.setEmail("contact@techdistribution.com");
        supplier.setPhone("0123456789");

        supplier = supplierRepository.saveAndFlush(supplier);

        Long supplierId = supplier.getId();

        // Préparer les nouvelles informations
        String requestBody = """
            {
                "name": "Global Tech Distribution",
                "email": "support@globaltech.com",
                "phone": "0987654321"
            }
            """;

        // Act & Assert : modifier le fournisseur
        mockMvc.perform(put("/api/suppliers/{id}", supplierId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(supplierId))
                .andExpect(jsonPath("$.name").value("Global Tech Distribution"))
                .andExpect(jsonPath("$.email").value("support@globaltech.com"))
                .andExpect(jsonPath("$.phone").value("0987654321"));

        // Vérifier les modifications dans la base de données
        Supplier updatedSupplier = supplierRepository.findById(supplierId)
                .orElseThrow();

        assertEquals("Global Tech Distribution", updatedSupplier.getName());
        assertEquals("support@globaltech.com", updatedSupplier.getEmail());
        assertEquals("0987654321", updatedSupplier.getPhone());

        // Vérifier qu'aucun fournisseur supplémentaire n'a été créé
        assertEquals(1, supplierRepository.count());
    }

    @Test
    void shouldDeleteSupplierSuccessfully() throws Exception {
        // Arrange : créer un fournisseur
        Supplier supplier = new Supplier();
        supplier.setName("Tech Distribution");
        supplier.setEmail("contact@techdistribution.com");
        supplier.setPhone("0123456789");

        supplier = supplierRepository.saveAndFlush(supplier);

        Long supplierId = supplier.getId();

        // Vérifier que le fournisseur existe avant la suppression
        assertTrue(supplierRepository.existsById(supplierId));

        // Act & Assert : supprimer le fournisseur
        mockMvc.perform(delete("/api/suppliers/{id}", supplierId))
                .andExpect(status().isNoContent());

        // Vérifier que le fournisseur n'existe plus dans H2
        assertFalse(supplierRepository.existsById(supplierId));
        assertEquals(0, supplierRepository.count());
    }

    @Test
    void shouldReturn404WhenSupplierNotFound() throws Exception {
        // Arrange : utiliser un ID inexistant
        Long supplierId = 999999L;

        // Act & Assert : demander ce fournisseur
        mockMvc.perform(get("/api/suppliers/{id}", supplierId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"));

        // Vérifier qu'aucun fournisseur n'a été créé
        assertEquals(0, supplierRepository.count());
    }

    @Test
    void shouldReturn400WhenSupplierNameIsBlank() throws Exception {
        // Arrange : préparer un fournisseur avec un nom vide
        String requestBody = """
            {
                "name": "",
                "email": "contact@example.com",
                "phone": "0123456789"
            }
            """;

        // Act & Assert : envoyer les données invalides
        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"));

        // Vérifier qu'aucun fournisseur invalide n'a été enregistré
        assertEquals(0, supplierRepository.count());
    }

    @Test
    void shouldReturn409WhenSupplierEmailAlreadyExists() throws Exception {
        // Arrange : enregistrer un premier fournisseur
        Supplier existingSupplier = new Supplier();
        existingSupplier.setName("Tech Distribution");
        existingSupplier.setEmail("contact@techdistribution.com");
        existingSupplier.setPhone("0123456789");

        supplierRepository.saveAndFlush(existingSupplier);

        // Préparer un second fournisseur avec le même e-mail
        String requestBody = """
            {
                "name": "Global Supplies",
                "email": "contact@techdistribution.com",
                "phone": "0987654321"
            }
            """;

        // Act & Assert : tenter de créer le second fournisseur
        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Conflict"));

        // Vérifier que le second fournisseur n'a pas été créé
        assertEquals(1, supplierRepository.count());

        // Vérifier que le fournisseur initial existe toujours
        Supplier savedSupplier = supplierRepository
                .findById(existingSupplier.getId())
                .orElseThrow();

        assertEquals("Tech Distribution", savedSupplier.getName());
        assertEquals("contact@techdistribution.com", savedSupplier.getEmail());
    }
}