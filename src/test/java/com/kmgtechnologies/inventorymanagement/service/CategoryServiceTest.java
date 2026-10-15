package com.kmgtechnologies.inventorymanagement.service;

import com.kmgtechnologies.inventorymanagement.entity.Category;
import com.kmgtechnologies.inventorymanagement.exception.DuplicateResourceException;
import com.kmgtechnologies.inventorymanagement.exception.ResourceNotFoundException;
import com.kmgtechnologies.inventorymanagement.repository.CategoryRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Electronics");
    }

    @Test
    void shouldFindCategoryById() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Category result = categoryService.findById(1L);

        assertThat(result.getName()).isEqualTo("Electronics");
        verify(categoryRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenCategoryNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void shouldCreateCategory() {
        when(categoryRepository.existsByNameIgnoreCase("Books")).thenReturn(false);

        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category result = categoryService.create("Books");

        assertThat(result.getName()).isEqualTo("Books");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void shouldRejectDuplicateCategory() {
        when(categoryRepository.existsByNameIgnoreCase("Electronics")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create("Electronics")).isInstanceOf(DuplicateResourceException.class);

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void shouldUpdateCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        when(categoryRepository.findByNameIgnoreCase("Computers")).thenReturn(Optional.empty());

        Category result = categoryService.update(1L, "Computers");

        assertThat(result.getName()).isEqualTo("Computers");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void shouldDeleteCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.delete(1L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void shouldFindAllCategories() {
        Category books = new Category();
        books.setId(2L);
        books.setName("Books");

        when(categoryRepository.findAll()).thenReturn(List.of(category, books));

        List<Category> results = categoryService.findAll();

        assertThat(results).extracting(Category::getName).containsExactly("Electronics", "Books");
    }

    @Test
    void shouldRejectDuplicateCategoryOnUpdate() {
        Category existing = new Category();
        existing.setId(2L);
        existing.setName("Books");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        when(categoryRepository.findByNameIgnoreCase("Books")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> categoryService.update(1L, "Books")).isInstanceOf(DuplicateResourceException.class);

        assertThat(category.getName()).isEqualTo("Electronics");
    }

    @Test
    void shouldAllowUpdatingCategoryWithSameName() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        when(categoryRepository.findByNameIgnoreCase("electronics")).thenReturn(Optional.of(category));

        Category result = categoryService.update(1L, "electronics");

        assertThat(result.getName()).isEqualTo("electronics");
    }

    @Test
    void shouldRejectDeletingUnknownCategory() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(99L)).isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository, never()).delete(any(Category.class));
    }
}