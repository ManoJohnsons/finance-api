package io.github.manojohnsons.financeapi.application.dto;

import java.math.BigDecimal;

import io.github.manojohnsons.financeapi.domain.model.Category;

public record CategoryResponseDTO(
        Long id,
        String name,
        String hexColor,
        String icon,
        BigDecimal monthlyGoal,
        boolean isActive) {

    public static CategoryResponseDTO fromEntity(Category category) {
        return new CategoryResponseDTO(
                category.getId(),
                category.getName(),
                category.getHexColor(), category.getIcon(),
                category.getMonthlyGoal(),
                category.isActive());
    }
}
