package com.bolicos.challenge.infrastructure.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CommunicationPreferenceBatchRequest(
    @NotEmpty(message = "O campo 'preferencias' é obrigatório.")
    List<@Valid CommunicationPreferenceRequest> preferencias
) {
}
