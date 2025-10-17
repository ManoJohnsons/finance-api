package io.github.manojohnsons.financeapi.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransactionUpdateRequestDTO(
        @NotBlank(message = "The description is obrigatory.") String description,
        @NotNull(message = "The amount is obrigatory.") @Positive(message = "The amount must be positive.") BigDecimal amount,
        @NotNull(message = "The date is obrigatory.") LocalDate date,
        Long categoryId) {

}
