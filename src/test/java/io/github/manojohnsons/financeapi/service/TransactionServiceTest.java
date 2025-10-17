package io.github.manojohnsons.financeapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
        var requestDTO = new TransactionRequestDTO("Almoço", new BigDecimal("50.00"), LocalDate.now(),
                TransactionType.EXPENSE, 1L);
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
    @DisplayName("Should return a list of transactions for the user in a given time period")
    void shouldReturnListOfTransactionsForUserInGivenMonthAndYear() {
        // Arrange (Organizar)
        var userId = 10L;
        var year = 2025;
        var month = 10;
        var user = new User();

        // Two transactions that are WITHIN the filter period (October/2025).
        // Duas transações que estão DENTRO do período do filtro (Outubro/2025).
        var transactionInOctober1 = new Transaction("Supermercado", new BigDecimal("350.00"), LocalDate.of(2025, 10, 5),
                TransactionType.EXPENSE, user, null);
        var transactionInOctober2 = new Transaction("Salário", new BigDecimal("5000.00"), LocalDate.of(2025, 10, 1),
                TransactionType.INCOME, user, null);

        // This transaction is OUTSIDE of the filter period, to ensure that it is not
        // returned.
        // Essa transação está FORA do período do filtro, para garantir que ela não seja
        // retornada.
        @SuppressWarnings("unused")
        var transactionInSeptember = new Transaction("Gasolina", new BigDecimal("150.00"), LocalDate.of(2025, 9, 28),
                TransactionType.EXPENSE, user, null);

        var expectedTransactions = List.of(transactionInOctober1, transactionInOctober2);

        when(transactionRepository.findByUserIdAndYearAndMonth(userId, year, month))
                .thenReturn(expectedTransactions);

        // Act (Agir)
        List<TransactionResponseDTO> response = transactionService.findAllByUserIdAndDate(userId, year, month);

        // Assert (Verificar)
        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);
        assertThat(response.get(0).description()).isEqualTo("Supermercado");
        assertThat(response.get(1).description()).isEqualTo("Salário");
    }

    @Test
    @DisplayName("Should return a specific transaction by its ID and user ID")
    void shouldFindTransactionByIdAndUserId() {
        // Arrange (Organizar)
        var userId = 10L;
        var transactionId = 1L;
        var user = new User();

        var transaction = new Transaction("Supermercado", new BigDecimal("350.00"), LocalDate.now(),
                TransactionType.EXPENSE, user, null);
        ReflectionTestUtils.setField(transaction, "id", transactionId);

        when(transactionRepository.findByIdAndUserId(transactionId, userId)).thenReturn(Optional.of(transaction));

        // Act (Agir)
        TransactionResponseDTO response = transactionService.findById(transactionId, userId);

        // Assert (Verificar)
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(transactionId);
        assertThat(response.description()).isEqualTo("Supermercado");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when trying to create a transaction for a non-existent user")
    void shouldThrowResourceNotFoundExceptionWhenCreatingTransactionForNonExistingUser() {
        // Arrange (Organizar)
        var requestDTO = new TransactionRequestDTO("Salário", new BigDecimal("5000.00"), LocalDate.now(),
                TransactionType.INCOME, null);
        var nonExistingUser = 999L;

        when(userRepository.findById(nonExistingUser)).thenReturn(Optional.empty());

        // Act & Assert (Agir e Verificar)
        var exception = assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.create(requestDTO, nonExistingUser);
        });

        assertThat(exception.getMessage()).isEqualTo("Resource User not found.");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when trying to create a transaction with a invalid category")
    void shouldThrowResourceNotFoundExceptionWhenCreatingTransactionWithInvalidCategory() {
        // Arrange (Organizar)
        var invalidCategoryId = 999L;
        var requestDTO = new TransactionRequestDTO("Cinema", new BigDecimal("50.00"), LocalDate.now(),
                TransactionType.EXPENSE, invalidCategoryId);
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

    @Test
    @DisplayName("Should return a empty list when a user has no transactions in the given time period")
    void shouldReturnEmptyListWhenNoTransactionsFoundForPeriod() {
        // Arrange (Organizar)
        var userId = 10L;
        var year = 2025;
        var month = 11;

        when(transactionRepository.findByUserIdAndYearAndMonth(userId, year, month)).thenReturn(List.of());

        // Act (Agir)
        List<TransactionResponseDTO> response = transactionService.findAllByUserIdAndDate(userId, year, month);

        // Assert (Verificar)
        assertThat(response).isNotNull();
        assertThat(response).isEmpty();
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when searching a non-existing transaction")
    void shouldThrowResourceNotFoundExceptionWhenFindingNonExistingTransaction() {
        // Arrange (Organizar)
        var nonExistingTransactionId = 999L;
        var userId = 10L;

        when(transactionRepository.findByIdAndUserId(nonExistingTransactionId, userId)).thenReturn(Optional.empty());

        // Act & Assert (Agir e Verificar)
        var exception = assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.findById(nonExistingTransactionId, userId);
        });

        assertThat(exception.getMessage()).isEqualTo("Resource Transaction not found.");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when searching an transaction of another user")
    void shouldThrowResourceNotFoundExceptionWhenFindingTransactionOfAnotherUser() {
        // Arrange (Organizar)
        var transactionId = 5L;
        @SuppressWarnings("unused")
		var ownerUserId = 20L;
        var attackerUserId = 10L;
        
        when(transactionRepository.findByIdAndUserId(transactionId, attackerUserId)).thenReturn(Optional.empty());

        // Act & Assert (Agir e Verificar)
        var exception = assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.findById(transactionId, attackerUserId);
        });

        assertThat(exception.getMessage()).isEqualTo("Resource Transaction not found.");
    }
}
