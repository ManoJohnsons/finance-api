package io.github.manojohnsons.financeapi.application.dto;

import io.github.manojohnsons.financeapi.domain.model.User;
import lombok.Builder;

@Builder
public record UserResponseDTO(
        Long id,
        String name,
        String email) {
            
    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail());
    }
}
