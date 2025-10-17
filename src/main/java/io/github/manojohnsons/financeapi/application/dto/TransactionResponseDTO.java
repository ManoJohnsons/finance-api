package io.github.manojohnsons.financeapi.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.github.manojohnsons.financeapi.domain.enums.TransactionType;
import io.github.manojohnsons.financeapi.domain.model.Transaction;

public record TransactionResponseDTO(
        Long id,
        String description,
        BigDecimal amount,
        LocalDate date,
        TransactionType type,
        CategorySummaryDTO category) {

    public static TransactionResponseDTO fromEntity(Transaction transaction) {
        CategorySummaryDTO categoryDTO = null;
        if (transaction.getCategory() != null) {
            var category = transaction.getCategory();
            categoryDTO = new CategorySummaryDTO(
                    category.getId(),
                    category.getName(),
                    category.getHexColor(),
                    category.getIcon());
        }

        return new TransactionResponseDTO(
                transaction.getId(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getType(), 
                categoryDTO);
    }
}
