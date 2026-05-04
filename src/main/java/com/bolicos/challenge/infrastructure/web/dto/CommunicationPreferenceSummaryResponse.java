package com.bolicos.challenge.infrastructure.web.dto;

import com.bolicos.challenge.domain.model.CommunicationChannel;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommunicationPreferenceSummaryResponse(
    UUID id,
    UUID customerId,
    CommunicationChannel preferenciaCanalComunicacao,
    long totalEmails,
    LocalDateTime dataCriacao,
    LocalDateTime dataAtualizacao
) {
}
