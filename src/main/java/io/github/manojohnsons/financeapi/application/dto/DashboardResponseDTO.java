package io.github.manojohnsons.financeapi.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponseDTO(
    BigDecimal totalIncome,
    BigDecimal totalExpense,
    BigDecimal finalBalance,
    List<BudgetCategoryDTO> budgets
) {

}
