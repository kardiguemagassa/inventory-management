package com.kmgtechnologies.inventorymanagement.controller;

import com.kmgtechnologies.inventorymanagement.entity.Category;
import com.kmgtechnologies.inventorymanagement.repository.CategoryRepository;
import com.kmgtechnologies.inventorymanagement.repository.ProductRepository;
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
class CategoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void shouldGetAllCategories() throws Exception {
        // Arrange : créer deux catégories
        Category electronics = new Category();
        electronics.setName("Electronics");

        Category books = new Category();
        books.setName("Books");

        categoryRepository.saveAllAndFlush(List.of(electronics, books));

        // Act & Assert : récupérer toutes les catégories
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].name",
                        hasItems("Electronics", "Books")));
    }

    @Test
    void shouldReturn409WhenCategoryNameAlreadyExists() throws Exception {
        Category category = new Category();
        category.setName("Electronics");
        categoryRepository.saveAndFlush(category);

        String requestBody = """
                {
                    "name": "Electronics"
                }
                """;

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void shouldCreateCategorySuccessfully() throws Exception {
        String requestBody = """
            {
                "name": "Books"
            }
            """;

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Books"));

        assertEquals(1, categoryRepository.count());
        assertTrue(categoryRepository.existsByNameIgnoreCase("Books"));
    }

    @Test
    void shouldGetCategoryById() throws Exception {
        // Arrange : créer une catégorie dans H2
        Category category = new Category();
        category.setName("Electronics");
        category = categoryRepository.saveAndFlush(category);

        // Act & Assert : appeler l'API et vérifier la réponse
        mockMvc.perform(get("/api/categories/{id}", category.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId()))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void shouldUpdateCategorySuccessfully() throws Exception {
        // Arrange : créer une catégorie
        Category category = new Category();
        category.setName("Electronics");
        category = categoryRepository.saveAndFlush(category);

        Long categoryId = category.getId();

        String requestBody = """
            {
                "name": "Computer Hardware"
            }
            """;

        // Act & Assert : modifier la catégorie via l'API
        mockMvc.perform(put("/api/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.name").value("Computer Hardware"));

        // Assert : vérifier la persistance dans H2
        Category updatedCategory = categoryRepository.findById(categoryId)
                .orElseThrow();

        assertEquals("Computer Hardware", updatedCategory.getName());
        assertEquals(1, categoryRepository.count());
    }

    @Test
    void shouldDeleteCategorySuccessfully() throws Exception {
        // Arrange : créer une catégorie
        Category category = new Category();
        category.setName("Electronics");
        category = categoryRepository.saveAndFlush(category);

        Long categoryId = category.getId();

        // Vérifier que la catégorie existe avant suppression
        assertTrue(categoryRepository.existsById(categoryId));

        // Act & Assert : supprimer la catégorie via l'API
        mockMvc.perform(delete("/api/categories/{id}", categoryId))
                .andExpect(status().isNoContent());

        // Vérifier que la catégorie a disparu de H2
        assertFalse(categoryRepository.existsById(categoryId));
        assertEquals(0, categoryRepository.count());
    }

    @Test
    void shouldReturn404WhenCategoryNotFound() throws Exception {
        // Arrange : utiliser un identifiant inexistant
        Long unknownCategoryId = 999999L;

        // Act & Assert
        mockMvc.perform(get("/api/categories/{id}", unknownCategoryId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"));
    }

    @Test
    void shouldReturn400WhenCategoryNameIsBlank() throws Exception {
        // Arrange : préparer une requête invalide
        String requestBody = """
            {
                "name": ""
            }
            """;

        // Act & Assert : envoyer la requête
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        // Assert : aucune catégorie ne doit être créée
        assertEquals(0, categoryRepository.count());
    }
}