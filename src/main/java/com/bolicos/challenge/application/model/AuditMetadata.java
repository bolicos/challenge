package com.bolicos.challenge.application.model;

import java.time.LocalDateTime;

public record AuditMetadata(
    LocalDateTime dataCriacao,
    LocalDateTime dataAtualizacao,
    String criadoPor,
    String alteradoPor
) {
}
