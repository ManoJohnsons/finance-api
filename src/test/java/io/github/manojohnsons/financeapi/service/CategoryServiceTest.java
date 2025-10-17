package io.github.manojohnsons.financeapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.github.manojohnsons.financeapi.application.dto.CategoryRequestDTO;
import io.github.manojohnsons.financeapi.application.dto.CategoryResponseDTO;
import io.github.manojohnsons.financeapi.domain.model.Category;
import io.github.manojohnsons.financeapi.domain.model.Transaction;
import io.github.manojohnsons.financeapi.domain.model.User;
import io.github.manojohnsons.financeapi.domain.repository.CategoryRepository;
import io.github.manojohnsons.financeapi.domain.repository.TransactionRepository;
import io.github.manojohnsons.financeapi.domain.repository.UserRepository;
import io.github.manojohnsons.financeapi.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("Should create a new category successfully")
    void shouldCreateCategorySuccessfully() {
        // Arrange (Organizar)
        var requestDTO = new CategoryRequestDTO("Alimentação", "#FF5733", "utensils", null);
        var userId = 10L;
        var user = new User("Fulana Silva", "fulana32@email.com", "senha123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category categoryToSave = invocation.getArgument(0);
            ReflectionTestUtils.setField(categoryToSave, "id", 1L);
            return categoryToSave;
        });

        // Act (Agir)
        CategoryResponseDTO response = categoryService.create(requestDTO, userId);

        // Assert (Verificar)
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Alimentação");
        assertThat(response.hexColor()).isEqualTo("#FF5733");
        assertThat(response.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should return a list of categories to a existing user")
    void shouldReturnListOfCategoriesForExistingUser() {
        // Arrange (Organizar)
        var userId = 10L;
        var user = new User();
        var category1 = new Category("Alimentação", "#FF5733", "utensils", user);
        var category2 = new Category("Transporte", "#337BFF", "car", user);

        when(categoryRepository.findByUserId(userId))
                .thenReturn(List.of(category1, category2));

        // Act (Agir)
        List<CategoryResponseDTO> response = categoryService.findAllByUserId(userId);

        // Assert (Verificar)
        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);

        // Asserting if the data of the first category matches (Verificando se os dados
        // da primeira categoria correspondem).
        assertThat(response.get(0).name()).isEqualTo("Alimentação");
        assertThat(response.get(0).hexColor()).isEqualTo("#FF5733");

        // Asserting if the data of the second category matches (Verificando se os dados
        // da segunda categoria correspondem).
        assertThat(response.get(1).name()).isEqualTo("Transporte");
        assertThat(response.get(1).hexColor()).isEqualTo("#337BFF");
    }

    @Test
    @DisplayName("Should update a category successfully")
    void shouldUpdateCategorySuccessfully() {
        // Arrange (Organizar)
        var userId = 10L;
        var categoryId = 1L;
        var user = new User();
        // DTO with new data to update the category.
        // Um DTO com novos dados para atualizar a categoria.
        var requestDTO = new CategoryRequestDTO("Alimentação (Editado)", "#FFFFFF", "apple", new BigDecimal("1000.00"));
        // Original category, as it was BEFORE the update.
        // Categoria original, como estava ANTES da atualização.
        var originalCategory = new Category("Alimentação", "#FF5733", "utensils", user);
        ReflectionTestUtils.setField(originalCategory, "id", categoryId);

        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(originalCategory));

        // Act (Agir)
        CategoryResponseDTO response = categoryService.update(categoryId, requestDTO, userId);

        // Assert (Verificar)
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(categoryId);
        assertThat(response.name()).isEqualTo("Alimentação (Editado)");
        assertThat(response.hexColor()).isEqualTo("#FFFFFF");
        assertThat(response.monthlyGoal()).isEqualTo(new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("Shoud delete a category successfully and disassociate their transactions")
    void shouldDeleteCategorySuccessfully() {
        // Arrange (Organizar)
        var userId = 10L;
        var categoryId = 1L;
        var user = new User();
        var categoryToDelete = new Category("Lazer", null, null, user);
        var associatedTransaction = new Transaction("Cinema", new BigDecimal("50.00"), LocalDate.now(), null, user, categoryToDelete);
        List<Transaction> transactionList = List.of(associatedTransaction);

        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(categoryToDelete));

        when(transactionRepository.findByCategoryId(categoryId)).thenReturn(transactionList);

        // Act (Agir)
        categoryService.delete(categoryId, userId);

        // Assert (Verificar)
        verify(transactionRepository, times(1)).findByCategoryId(categoryId);
        verify(transactionRepository, times(1)).saveAll(transactionList);
        verify(categoryRepository, times(1)).delete(categoryToDelete);
        assertThat(associatedTransaction.getCategory()).isNull();
    }

    @Test
    @DisplayName("Should throw a ResourceNotFoundException when have a attempt to create a category for a non existing user")
    void shouldThrowResourceNotFoundExceptionWhenCreatingCategoryForNonExistingUser() {
        // Arrange (Organizar)
        var requestDTO = new CategoryRequestDTO("Viagem", "#1E90FF", "plane", null);
        var nonExistingUserId = 999L;

        when(userRepository.findById(nonExistingUserId)).thenReturn(Optional.empty());

        // Act & Assert (Agir e Verificar)
        var exception = assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.create(requestDTO, nonExistingUserId);
        });

        assertThat(exception.getMessage()).isEqualTo("Resource User not found.");
    }

    @Test
    @DisplayName("Should return a empty list when a user don't have categories")
    void shouldReturnEmptyListWhenUserHasNoCategories() {
        // Arrange (Organizar)
        var userId = 20L;

        when(categoryRepository.findByUserId(userId)).thenReturn(List.of());

        // Act (Agir)
        List<CategoryResponseDTO> response = categoryService.findAllByUserId(userId);

        // Assert (Verificar)
        assertThat(response).isNotNull();
        assertThat(response).isEmpty();
    }

    @Test
    @DisplayName("Should throw a ResourceNotFoundException when have a attempt to update a non existing category")
    // This also test the case when a user tries to modify the category that is owned to another user.
    void shouldThrowResourceNotFoundExceptionWhenUpdatingNonExistingCategory() {
        // Arrange (Organizar)
        var nonExistingCategoryId = 999L;
        var userId = 10L;
        var requestDTO = new CategoryRequestDTO("Nome Novo", null, null, null);

        when(categoryRepository.findByIdAndUserId(nonExistingCategoryId, userId)).thenReturn(Optional.empty());

        // Act & Assert (Agir e Verificar)
        var exception = assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.update(nonExistingCategoryId, requestDTO, userId);
        });

        assertThat(exception.getMessage()).isEqualTo("Resource Category not found.");
    }

    @Test
    @DisplayName("Should throw a ResourceNotFoundException when have a attempt to delete a non existing category")
    // This also test the case when a user tries to delete the category that is owned to another user.
    void shouldThrowResourceNotFoundExceptionWhenDeletingNonExistingCategory() {
        // Arrange (Organizar)
        var nonExistingCategoryId = 999L;
        var userId = 10L;

        when(categoryRepository.findByIdAndUserId(nonExistingCategoryId, userId)).thenReturn(Optional.empty());

        // Act & Assert (Agir e Verificar)
        var exception = assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.delete(nonExistingCategoryId, userId);
        });

        assertThat(exception.getMessage()).isEqualTo("Resource Category not found.");
    }
}
