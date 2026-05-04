package com.bolicos.challenge.application.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record AuditMetadata(
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    LocalDateTime dataCriacao,

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    LocalDateTime dataAtualizacao,

    String criadoPor,
    String alteradoPor
) {
}
