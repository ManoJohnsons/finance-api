package io.github.manojohnsons.financeapi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.manojohnsons.financeapi.application.dto.BudgetCategoryDTO;
import io.github.manojohnsons.financeapi.application.dto.DashboardResponseDTO;
import io.github.manojohnsons.financeapi.domain.enums.TransactionType;
import io.github.manojohnsons.financeapi.domain.model.Transaction;
import io.github.manojohnsons.financeapi.domain.repository.CategoryRepository;
import io.github.manojohnsons.financeapi.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public DashboardResponseDTO generateMonthlySummary(Long userId, int year, int month) {
        var transactions = transactionRepository.findByUserIdAndYearAndMonth(userId, year, month);

        var totalIncome = calculateTotalByType(transactions, TransactionType.INCOME);
        var totalExpense = calculateTotalByType(transactions, TransactionType.EXPENSE);
        var finalBalance = totalIncome.subtract(totalExpense);

        var budgets = calculateBudgets(userId, transactions);

        return new DashboardResponseDTO(totalIncome, totalExpense, finalBalance, budgets);
    }

    private BigDecimal calculateTotalByType(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<BudgetCategoryDTO> calculateBudgets(Long userId, List<Transaction> transactions) {
        Map<Long, BigDecimal> totalSpentPerCategory = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE && t.getCategory() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getId(),
                        Collectors.mapping(Transaction::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

        var userCategories = categoryRepository.findByUserId(userId);

        return userCategories.stream()
                .filter(c -> c.getMonthlyGoal() != null && c.getMonthlyGoal().compareTo(BigDecimal.ZERO) > 0)
                .map(category -> {
                    var totalSpent = totalSpentPerCategory.getOrDefault(category.getId(), BigDecimal.ZERO);
                    var percentageSpent = totalSpent.multiply(new BigDecimal("100"))
                            .divide(category.getMonthlyGoal(), 2, RoundingMode.HALF_UP);

                    return new BudgetCategoryDTO(
                            category.getName(),
                            category.getHexColor(),
                            category.getMonthlyGoal(),
                            totalSpent,
                            percentageSpent);
                })
                .toList();
    }
}
