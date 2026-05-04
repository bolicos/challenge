package com.bolicos.challenge.application.model;

import java.util.List;

public record PreferenceBatchImportResult(
    int totalRecebido,
    int totalProcessado,
    int totalComErro,
    List<CommunicationPreferenceView> preferencias
) {
}
