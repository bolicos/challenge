package com.bolicos.challenge.application.service;

import com.bolicos.challenge.application.event.PreferenceChangedEvent;
import com.bolicos.challenge.application.event.PreferenceEventType;
import com.bolicos.challenge.application.model.CommunicationPreferenceView;
import com.bolicos.challenge.application.model.PreferenceBatchImportResult;
import com.bolicos.challenge.application.port.in.PreferenceBatchUseCase;
import com.bolicos.challenge.application.port.out.PreferenceEventPublisher;
import com.bolicos.challenge.application.port.out.PreferencePersistencePort;
import com.bolicos.challenge.domain.model.CommunicationPreference;
import com.bolicos.challenge.shared.util.PreferenceCustomerIdUtils;
import com.bolicos.challenge.shared.util.TransactionEventPublisherUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PreferenceBatchApplicationService implements PreferenceBatchUseCase {

    private final PreferencePersistencePort persistencePort;
    private final PreferenceEventPublisher eventPublisher;

    @Override
    @Transactional
    public PreferenceBatchImportResult importBatch(List<CommunicationPreference> preferences) {
        List<CommunicationPreference> safePreferences = preferences == null ? List.of() : preferences;
        safePreferences.forEach(PreferenceCustomerIdUtils::ensureCustomerId);

        List<CommunicationPreferenceView> imported = persistencePort.saveAll(safePreferences);
        imported.stream()
            .map(preference -> PreferenceChangedEvent.of(PreferenceEventType.PREFERENCE_CREATED, preference))
            .forEach(this::publishAfterCommit);

        return new PreferenceBatchImportResult(
            safePreferences.size(),
            imported.size(),
            safePreferences.size() - imported.size(),
            imported
        );
    }

    private void publishAfterCommit(PreferenceChangedEvent event) {
        TransactionEventPublisherUtils.publishAfterCommit(eventPublisher, event);
    }
}
