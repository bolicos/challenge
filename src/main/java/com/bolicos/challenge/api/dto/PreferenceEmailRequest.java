package com.bolicos.challenge.api.dto;

import com.bolicos.challenge.domain.model.EmailType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PreferenceEmailRequest(
    Long id,

    @NotBlank(message = "O campo 'email' é obrigatório.")
    @Email(message = "O campo 'email' não é um e-mail válido.")
    String email,

    @NotNull(message = "O campo 'tipo' é obrigatório.")
    EmailType tipo,

    @NotNull(message = "O campo 'verificado' é obrigatório.")
    Boolean verificado
) {
}
