package io.github.manojohnsons.financeapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.github.manojohnsons.financeapi.application.dto.DashboardResponseDTO;
import io.github.manojohnsons.financeapi.domain.enums.TransactionType;
import io.github.manojohnsons.financeapi.domain.model.Category;
import io.github.manojohnsons.financeapi.domain.model.Transaction;
import io.github.manojohnsons.financeapi.domain.model.User;
import io.github.manojohnsons.financeapi.domain.repository.CategoryRepository;
import io.github.manojohnsons.financeapi.domain.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("Should generate monthly dashboard summary successfully")
    void shouldGenerateMonthlySummarySuccessfully() {
        // Arrange (Organizar)
        var userId = 1L;
        var year = 2025;
        var month = 10;
        var user = new User();

        // Created two categories, one with a monthly goal and the other without
        // Criada duas categorias, uma com meta e a outra sem
        var categoryFood = new Category("Alimentação", "#FF0000", "utensils", user);
        categoryFood.setMonthlyGoal(new BigDecimal("800.00"));
        ReflectionTestUtils.setField(categoryFood, "id", 1L);
        var categoryLeisure = new Category("Lazer", "#00FF00", "gamepad", user);
        ReflectionTestUtils.setField(categoryLeisure, "id", 2L);

        var transactions = List.of(
                new Transaction("Salário", new BigDecimal("5000.00"), LocalDate.of(2025, 10, 5), TransactionType.INCOME, user, null),
                new Transaction("Supermercado", new BigDecimal("600.00"), LocalDate.of(2025, 10, 10), TransactionType.EXPENSE, user, categoryFood),
                new Transaction("Cinema", new BigDecimal("100.00"), LocalDate.of(2025, 10, 15), TransactionType.EXPENSE, user, categoryLeisure));

        when(transactionRepository.findByUserIdAndYearAndMonth(userId, year, month)).thenReturn(transactions);
        when(categoryRepository.findByUserId(userId)).thenReturn(List.of(categoryFood, categoryLeisure));

        // Act (Agir)
        DashboardResponseDTO response = dashboardService.generateMonthlySummary(userId, year, month);

        // Assert (Verificar)
        assertThat(response).isNotNull();
        assertThat(response.totalIncome()).isEqualByComparingTo("5000.00");
        assertThat(response.totalExpense()).isEqualByComparingTo("700.00");
        assertThat(response.finalBalance()).isEqualByComparingTo("4300.00");
        assertThat(response.budgets()).hasSize(1);
        assertThat(response.budgets().get(0).categoryName()).isEqualTo("Alimentação");
        assertThat(response.budgets().get(0).totalSpent()).isEqualByComparingTo("600.00");
        assertThat(response.budgets().get(0).percentageSpent()).isEqualByComparingTo("75.00");
    }

    @Test
    @DisplayName("Should return a zeroed summary when there are no transactions in the period")
    void shouldReturnZeroedSummaryNoTransactionsInPeriod() {
        // Arrange (Organizar)
        var userId = 1L;
        var year = 2025;
        var month = 11;

        when(transactionRepository.findByUserIdAndYearAndMonth(userId, year, month)).thenReturn(List.of());

        when(categoryRepository.findByUserId(userId)).thenReturn(List.of());

        // Act (Agir)
        DashboardResponseDTO response = dashboardService.generateMonthlySummary(userId, year, month);

        // Assert (Verificar)
        assertThat(response).isNotNull();
        assertThat(response.totalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.totalExpense()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.finalBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.budgets()).isEmpty();
    }
}
