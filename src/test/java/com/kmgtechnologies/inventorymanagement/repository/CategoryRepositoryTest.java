package com.kmgtechnologies.inventorymanagement.repository;

import com.kmgtechnologies.inventorymanagement.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@ActiveProfiles("test")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldFindCategoryByNameIgnoringCase() {
        Category category = new Category();
        category.setName("Electronics");

        categoryRepository.save(category);

        var result = categoryRepository.findByNameIgnoreCase("electronics");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Electronics");
    }

    @Test
    void shouldCheckIfCategoryExistsIgnoringCase() {
        Category category = new Category();
        category.setName("Electronics");

        categoryRepository.save(category);

        assertThat(categoryRepository.existsByNameIgnoreCase("ELECTRONICS")).isTrue();

        assertThat(categoryRepository.existsByNameIgnoreCase("electronics")).isTrue();

        assertThat(categoryRepository.existsByNameIgnoreCase("Books")).isFalse();
    }
}