package io.github.manojohnsons.financeapi.application.dto;

import java.math.BigDecimal;

public record BudgetCategoryDTO(
    String categoryName,
    String categoryColor,
    BigDecimal monthlyGoal,
    BigDecimal totalSpent,
    BigDecimal percentageSpent
) {

}
