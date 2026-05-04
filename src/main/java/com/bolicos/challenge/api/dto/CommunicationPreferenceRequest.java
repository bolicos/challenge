package com.bolicos.challenge.api.dto;

import com.bolicos.challenge.domain.model.CommunicationChannel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CommunicationPreferenceRequest(
    @NotNull(message = "O campo 'preferenciaCanalComunicacao' é obrigatório.")
    CommunicationChannel preferenciaCanalComunicacao,

    List<@Valid PreferenceEmailRequest> emails
) {
}
