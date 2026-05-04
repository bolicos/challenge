package com.bolicos.challenge.infrastructure.web.dto;

import com.bolicos.challenge.domain.model.CommunicationChannel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CommunicationPreferenceRequest(
    UUID customerId,

    @NotNull(message = "O campo 'preferenciaCanalComunicacao' é obrigatório.")
    CommunicationChannel preferenciaCanalComunicacao,

    List<@Valid PreferenceEmailRequest> emails
) {
}
