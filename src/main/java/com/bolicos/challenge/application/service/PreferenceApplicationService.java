package com.bolicos.challenge.application.service;

import com.bolicos.challenge.application.model.CommunicationPreferenceView;
import com.bolicos.challenge.application.model.CommunicationPreferenceSummaryView;
import com.bolicos.challenge.application.event.PreferenceChangedEvent;
import com.bolicos.challenge.application.event.PreferenceEventType;
import com.bolicos.challenge.application.port.out.PreferenceEventPublisher;
import com.bolicos.challenge.application.port.in.PreferenceUseCase;
import com.bolicos.challenge.application.port.out.PreferencePersistencePort;
import com.bolicos.challenge.domain.exception.PreferenceNotFoundException;
import com.bolicos.challenge.domain.model.CommunicationPreference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PreferenceApplicationService implements PreferenceUseCase {

    private final PreferencePersistencePort persistencePort;
    private final PreferenceEventPublisher eventPublisher;

    @Override
    @Transactional
    public CommunicationPreferenceView create(CommunicationPreference preference) {
        ensureCustomerId(preference);
        CommunicationPreferenceView created = persistencePort.save(preference);
        publishAfterCommit(PreferenceChangedEvent.of(PreferenceEventType.PREFERENCE_CREATED, created));

        return created;
    }

    @Override
    @Transactional(readOnly = true)
    public CommunicationPreferenceView findById(UUID id) {
        return persistencePort.findById(id)
            .orElseThrow(() -> preferenceNotFound(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunicationPreferenceView> findAll() {
        return persistencePort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunicationPreferenceSummaryView> findSummary() {
        return persistencePort.findSummary();
    }

    @Override
    @Transactional
    public CommunicationPreferenceView update(UUID id, CommunicationPreference preference) {
        if (!persistencePort.existsById(id)) {
            throw preferenceNotFound(id);
        }

        preference.setId(id);
        ensureCustomerId(preference);
        CommunicationPreferenceView updated = persistencePort.save(preference);
        publishAfterCommit(PreferenceChangedEvent.of(PreferenceEventType.PREFERENCE_UPDATED, updated));

        return updated;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        CommunicationPreferenceView existing = persistencePort.findById(id)
            .orElseThrow(() -> preferenceNotFound(id));

        persistencePort.deleteById(id);
        publishAfterCommit(PreferenceChangedEvent.of(PreferenceEventType.PREFERENCE_DELETED, existing));
    }

    private void ensureCustomerId(CommunicationPreference preference) {
        if (preference.getCustomerId() == null) {
            preference.setCustomerId(UUID.randomUUID());
        }
    }

    private PreferenceNotFoundException preferenceNotFound(UUID id) {
        return new PreferenceNotFoundException("Preferência " + id + " não encontrada");
    }

    private void publishAfterCommit(PreferenceChangedEvent event) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            eventPublisher.publish(event);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publish(event);
            }
        });
    }
}
