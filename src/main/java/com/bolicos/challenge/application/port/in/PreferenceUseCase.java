package com.bolicos.challenge.application.port.in;

import com.bolicos.challenge.application.model.CommunicationPreferenceView;
import com.bolicos.challenge.application.model.CommunicationPreferenceSummaryView;
import com.bolicos.challenge.application.model.PreferenceBatchImportResult;
import com.bolicos.challenge.domain.model.CommunicationPreference;

import java.util.List;
import java.util.UUID;

public interface PreferenceUseCase {

    CommunicationPreferenceView create(CommunicationPreference preference);

    CommunicationPreferenceView findById(UUID id);

    List<CommunicationPreferenceView> findAll();

    List<CommunicationPreferenceSummaryView> findSummary();

    PreferenceBatchImportResult importBatch(List<CommunicationPreference> preferences);

    CommunicationPreferenceView update(UUID id, CommunicationPreference preference);

    void delete(UUID id);
}
