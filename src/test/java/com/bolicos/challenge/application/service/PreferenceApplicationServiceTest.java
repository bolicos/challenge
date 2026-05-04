package com.bolicos.challenge.application.service;

import com.bolicos.challenge.application.event.PreferenceChangedEvent;
import com.bolicos.challenge.application.event.PreferenceEventType;
import com.bolicos.challenge.application.model.AuditMetadata;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PreferenceApplicationServiceTest {

    private final FakePreferencePersistencePort persistencePort = new FakePreferencePersistencePort();
    private final FakePreferenceEventPublisher eventPublisher = new FakePreferenceEventPublisher();
    private final PreferenceApplicationService service = new PreferenceApplicationService(
        persistencePort,
        eventPublisher
    );

    @Test
    void devePublicarEventoQuandoCriarPreferencia() {
        var preference = preference(null);

        var created = service.create(preference);

        assertNotNull(created.id());
        assertEquals(PreferenceEventType.PREFERENCE_CREATED, eventPublisher.lastEvent().eventType());
        assertEquals(created.id(), eventPublisher.lastEvent().preference().id());
    }

    @Test
    void devePublicarEventoQuandoAtualizarPreferencia() {
        UUID id = UUID.randomUUID();
        persistencePort.nextView = view(id);

        var updated = service.update(id, preference(id));

        assertEquals(id, updated.id());
        assertEquals(PreferenceEventType.PREFERENCE_UPDATED, eventPublisher.lastEvent().eventType());
    }

    @Test
    void devePublicarEventoQuandoDeletarPreferencia() {
        UUID id = UUID.randomUUID();
        persistencePort.nextView = view(id);

        service.delete(id);

        assertEquals(PreferenceEventType.PREFERENCE_DELETED, eventPublisher.lastEvent().eventType());
        assertEquals(id, eventPublisher.lastEvent().preference().id());
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

        private CommunicationPreferenceView nextView;

        @Override
        public CommunicationPreferenceView save(CommunicationPreference preference) {
            nextView = view(preference.getId() != null ? preference.getId() : UUID.randomUUID());
            return nextView;
        }

        @Override
        public Optional<CommunicationPreferenceView> findById(UUID id) {
            return Optional.ofNullable(nextView);
        }

        @Override
        public List<CommunicationPreferenceView> findAll() {
            return List.of();
        }

        @Override
        public boolean existsById(UUID id) {
            return nextView != null && nextView.id().equals(id);
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
    }
}
