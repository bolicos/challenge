package com.bolicos.challenge.infrastructure.web.dto;

import java.util.List;

public record CommunicationPreferenceBatchResponse(
    int totalRecebido,
    int totalProcessado,
    int totalComErro,
    List<CommunicationPreferenceResponse> preferencias
) {
}
