package io.github.manojohnsons.financeapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.github.manojohnsons.financeapi.application.dto.TransactionRequestDTO;
import io.github.manojohnsons.financeapi.application.dto.TransactionResponseDTO;
import io.github.manojohnsons.financeapi.domain.enums.TransactionType;
import io.github.manojohnsons.financeapi.domain.model.Category;
import io.github.manojohnsons.financeapi.domain.model.Transaction;
import io.github.manojohnsons.financeapi.domain.model.User;
import io.github.manojohnsons.financeapi.domain.repository.CategoryRepository;
import io.github.manojohnsons.financeapi.domain.repository.TransactionRepository;
import io.github.manojohnsons.financeapi.domain.repository.UserRepository;
import io.github.manojohnsons.financeapi.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    @DisplayName("Should create a new transaction successfully")
    void shouldCreateTransactionSuccessfully() {
        // Arrange (Organizar)
        var requestDTO = new TransactionRequestDTO("Almoço", new BigDecimal("50.00"), LocalDate.now(), TransactionType.EXPENSE, 1L);
        var userId = 10L;
        var user = new User("Fulana Silva", "fulana32@email.com", "senha123");
        var category = new Category("Alimentação", "#FF5733", "utensils", user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndUserId(requestDTO.categoryId(), userId)).thenReturn(Optional.of(category));

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transactionToSave = invocation.getArgument(0);
            ReflectionTestUtils.setField(transactionToSave, "id", 1L);
            return transactionToSave;
        });

        // Act (Agir)
        TransactionResponseDTO response = transactionService.create(requestDTO, userId);

        // Assert (Verificar)
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.description()).isEqualTo("Almoço");
        assertThat(response.category().name()).isEqualTo("Alimentação");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when trying to create a transaction for a non-existent user")
    void shouldThrowResourceNotFoundExceptionWhenCreatingTransactionForNonExistingUser() {
        // Arrange (Organizar)
        var requestDTO = new TransactionRequestDTO("Salário", new BigDecimal("5000.00"), LocalDate.now(), TransactionType.INCOME, null);
        var nonExistingUser = 999L;

        when(userRepository.findById(nonExistingUser)).thenReturn(Optional.empty());

        // Act & Assert (Agir e Verificar)
        var exception = assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.create(requestDTO, nonExistingUser);
        });

        assertThat(exception.getMessage()).isEqualTo("Resource User not found.");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when trying to create a transactio with a invalid category")
    void shouldThrowResourceNotFoundExceptionWhenCreatingTransactionWithInvalidCategory() {
        // Arrange (Organizar)
        var invalidCategoryId = 999L;
        var requestDTO = new TransactionRequestDTO("Cinema", new BigDecimal("50.00"), LocalDate.now(), TransactionType.EXPENSE, invalidCategoryId);
        var userId = 10L;
        var user = new User("Fulana Silva", "fulana32@email.com", "senha123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(categoryRepository.findByIdAndUserId(invalidCategoryId, userId)).thenReturn(Optional.empty());

        // Act & Assert (Agir e Verificar)
        var exception = assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.create(requestDTO, userId);
        });

        assertThat(exception.getMessage()).isEqualTo("Resource Category not found.");
    }
}
