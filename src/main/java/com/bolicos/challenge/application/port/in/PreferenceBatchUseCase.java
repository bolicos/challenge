package com.bolicos.challenge.application.port.in;

import com.bolicos.challenge.application.model.PreferenceBatchImportResult;
import com.bolicos.challenge.domain.model.CommunicationPreference;

import java.util.List;

public interface PreferenceBatchUseCase {

    PreferenceBatchImportResult importBatch(List<CommunicationPreference> preferences);
}
