package com.bolicos.challenge.application.model;

import com.bolicos.challenge.domain.model.CommunicationChannel;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommunicationPreferenceSummaryView(
    UUID id,
    UUID customerId,
    CommunicationChannel communicationChannel,
    long emailCount,
    LocalDateTime dataCriacao,
    LocalDateTime dataAtualizacao
) {
}
