package io.github.manojohnsons.financeapi.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserRequestDTO(
        @NotBlank(message = "The name is obrigatory.")
        String name,

        @NotBlank(message = "The e-mail is obrigatory.")
        @Email(message = "The provided e-mail is invalid.")
        String email,

        @NotBlank(message = "The password is obrigatory.")
        @Size(min = 8, message = "The password must have at least 8 characters long.")
        String password) {

}
