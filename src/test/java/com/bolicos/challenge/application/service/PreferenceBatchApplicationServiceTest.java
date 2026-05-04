package com.bolicos.challenge.application.service;

import com.bolicos.challenge.application.event.PreferenceChangedEvent;
import com.bolicos.challenge.application.event.PreferenceEventType;
import com.bolicos.challenge.application.model.AuditMetadata;
import com.bolicos.challenge.application.model.CommunicationPreferenceSummaryView;
import com.bolicos.challenge.application.model.CommunicationPreferenceView;
import com.bolicos.challenge.application.port.out.PreferenceEventPublisher;
import com.bolicos.challenge.application.port.out.PreferencePersistencePort;
import com.bolicos.challenge.domain.model.CommunicationChannel;
import com.bolicos.challenge.domain.model.CommunicationPreference;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PreferenceBatchApplicationServiceTest {

    private final FakePreferencePersistencePort persistencePort = new FakePreferencePersistencePort();
    private final FakePreferenceEventPublisher eventPublisher = new FakePreferenceEventPublisher();
    private final PreferenceBatchApplicationService service = new PreferenceBatchApplicationService(
        persistencePort,
        eventPublisher
    );

    @Test
    void deveImportarPreferenciasEmLote() {
        var result = service.importBatch(List.of(preference(null), preference(null)));

        assertEquals(2, result.totalRecebido());
        assertEquals(2, result.totalProcessado());
        assertEquals(0, result.totalComErro());
        assertEquals(2, eventPublisher.count());
        assertEquals(PreferenceEventType.PREFERENCE_CREATED, eventPublisher.lastEvent().eventType());
    }

    @Test
    void deveTratarListaNulaComoImportacaoVazia() {
        var result = service.importBatch(null);

        assertEquals(0, result.totalRecebido());
        assertEquals(0, result.totalProcessado());
        assertEquals(0, result.totalComErro());
        assertEquals(0, eventPublisher.count());
    }

    private CommunicationPreference preference(UUID id) {
        var preference = new CommunicationPreference();
        preference.setId(id);
        preference.setCommunicationChannel(CommunicationChannel.EMAIL);

        return preference;
    }

    private CommunicationPreferenceView view(UUID id) {
        return new CommunicationPreferenceView(
            id,
            UUID.randomUUID(),
            CommunicationChannel.EMAIL,
            List.of(),
            new AuditMetadata(LocalDateTime.now(), LocalDateTime.now(), "system", "system")
        );
    }

    private class FakePreferencePersistencePort implements PreferencePersistencePort {

        @Override
        public CommunicationPreferenceView save(CommunicationPreference preference) {
            return view(preference.getId() != null ? preference.getId() : UUID.randomUUID());
        }

        @Override
        public List<CommunicationPreferenceView> saveAll(List<CommunicationPreference> preferences) {
            return preferences.stream()
                .map(this::save)
                .toList();
        }

        @Override
        public Optional<CommunicationPreferenceView> findById(UUID id) {
            return Optional.empty();
        }

        @Override
        public List<CommunicationPreferenceView> findAll() {
            return List.of();
        }

        @Override
        public List<CommunicationPreferenceSummaryView> findSummary() {
            return List.of();
        }

        @Override
        public boolean existsById(UUID id) {
            return false;
        }

        @Override
        public void deleteById(UUID id) {
        }
    }

    private static class FakePreferenceEventPublisher implements PreferenceEventPublisher {

        private final List<PreferenceChangedEvent> events = new ArrayList<>();

        @Override
        public void publish(PreferenceChangedEvent event) {
            events.add(event);
        }

        private PreferenceChangedEvent lastEvent() {
            return events.getLast();
        }

        private int count() {
            return events.size();
        }
    }
}
