package io.github.manojohnsons.financeapi.application.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequestDTO(
    @NotBlank(message = "The category name is obrigatory.")
    String name,

    String hexColor,
    String icon,
    BigDecimal monthlyGoal
) {

}
