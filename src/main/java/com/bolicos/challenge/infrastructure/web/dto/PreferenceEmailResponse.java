package com.bolicos.challenge.infrastructure.web.dto;

import com.bolicos.challenge.domain.model.EmailType;

import java.time.LocalDateTime;

public record PreferenceEmailResponse(
    Long id,
    String email,
    EmailType tipo,
    Boolean verificado,
    LocalDateTime dataAtualizacao,
    LocalDateTime dataCriacao,
    String criadoPor,
    String alteradoPor
) {
}
