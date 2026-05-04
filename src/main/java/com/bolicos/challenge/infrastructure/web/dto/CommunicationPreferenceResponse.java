package com.bolicos.challenge.infrastructure.web.dto;

import com.bolicos.challenge.domain.model.CommunicationChannel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CommunicationPreferenceResponse(
    UUID id,
    CommunicationChannel preferenciaCanalComunicacao,
    LocalDateTime dataAtualizacao,
    LocalDateTime dataCriacao,
    String criadoPor,
    String alteradoPor,
    List<PreferenceEmailResponse> emails
) {
}
