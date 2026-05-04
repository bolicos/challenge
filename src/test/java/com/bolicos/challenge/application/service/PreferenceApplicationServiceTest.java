package com.bolicos.challenge.application.service;

import com.bolicos.challenge.application.event.PreferenceChangedEvent;
import com.bolicos.challenge.application.event.PreferenceEventType;
import com.bolicos.challenge.application.model.AuditMetadata;
import com.bolicos.challenge.application.model.CommunicationPreferenceSummaryView;
import com.bolicos.challenge.application.model.CommunicationPreferenceView;
import com.bolicos.challenge.application.port.out.PreferenceEventPublisher;
import com.bolicos.challenge.application.port.out.PreferencePersistencePort;
import com.bolicos.challenge.domain.exception.PreferenceNotFoundException;
import com.bolicos.challenge.domain.model.CommunicationChannel;
import com.bolicos.challenge.domain.model.CommunicationPreference;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void deveGerarCustomerIdQuandoCriarPreferenciaSemCliente() {
        var preference = preference(null);

        service.create(preference);

        assertNotNull(persistencePort.lastSavedPreference.getCustomerId());
    }

    @Test
    void devePreservarCustomerIdQuandoCriarPreferenciaComCliente() {
        UUID customerId = UUID.randomUUID();
        var preference = preference(null);
        preference.setCustomerId(customerId);

        service.create(preference);

        assertEquals(customerId, persistencePort.lastSavedPreference.getCustomerId());
    }

    @Test
    void deveBuscarPreferenciaPorId() {
        UUID id = UUID.randomUUID();
        persistencePort.nextView = view(id);

        var found = service.findById(id);

        assertEquals(id, found.id());
    }

    @Test
    void deveLancarExcecaoQuandoBuscarPreferenciaInexistente() {
        UUID id = UUID.randomUUID();

        var exception = assertThrows(PreferenceNotFoundException.class, () -> service.findById(id));

        assertEquals("Preferência " + id + " não encontrada", exception.getMessage());
    }

    @Test
    void deveListarPreferencias() {
        var view = view(UUID.randomUUID());
        persistencePort.views = List.of(view);

        var result = service.findAll();

        assertEquals(List.of(view), result);
    }

    @Test
    void deveListarResumoDasPreferencias() {
        var summary = new CommunicationPreferenceSummaryView(
            UUID.randomUUID(),
            UUID.randomUUID(),
            CommunicationChannel.EMAIL,
            2L,
            LocalDateTime.of(2026, 5, 4, 10, 0),
            LocalDateTime.of(2026, 5, 4, 11, 0)
        );
        persistencePort.summaryViews = List.of(summary);

        var result = service.findSummary();

        assertEquals(List.of(summary), result);
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
    void deveDefinirIdInformadoQuandoAtualizarPreferencia() {
        UUID id = UUID.randomUUID();
        persistencePort.nextView = view(id);
        var preference = preference(null);

        service.update(id, preference);

        assertEquals(id, persistencePort.lastSavedPreference.getId());
        assertEquals(id, preference.getId());
    }

    @Test
    void deveLancarExcecaoQuandoAtualizarPreferenciaInexistente() {
        UUID id = UUID.randomUUID();

        var exception = assertThrows(PreferenceNotFoundException.class, () -> service.update(id, preference(null)));

        assertEquals("Preferência " + id + " não encontrada", exception.getMessage());
        assertEquals(0, eventPublisher.count());
    }

    @Test
    void devePublicarEventoQuandoDeletarPreferencia() {
        UUID id = UUID.randomUUID();
        persistencePort.nextView = view(id);

        service.delete(id);

        assertEquals(id, persistencePort.deletedId);
        assertEquals(PreferenceEventType.PREFERENCE_DELETED, eventPublisher.lastEvent().eventType());
        assertEquals(id, eventPublisher.lastEvent().preference().id());
    }

    @Test
    void deveLancarExcecaoQuandoDeletarPreferenciaInexistente() {
        UUID id = UUID.randomUUID();

        var exception = assertThrows(PreferenceNotFoundException.class, () -> service.delete(id));

        assertEquals("Preferência " + id + " não encontrada", exception.getMessage());
        assertEquals(0, eventPublisher.count());
    }

    @Test
    void devePublicarEventoSomenteDepoisDoCommit() {
        TransactionSynchronizationManager.initSynchronization();

        try {
            service.create(preference(null));

            assertEquals(0, eventPublisher.count());

            TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);

            assertEquals(1, eventPublisher.count());
            assertEquals(PreferenceEventType.PREFERENCE_CREATED, eventPublisher.lastEvent().eventType());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void naoDevePublicarEventoQuandoTransacaoNaoComitar() {
        TransactionSynchronizationManager.initSynchronization();

        try {
            service.create(preference(null));

            assertEquals(0, eventPublisher.count());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

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

        private CommunicationPreferenceView nextView;
        private List<CommunicationPreferenceView> views = List.of();
        private List<CommunicationPreferenceSummaryView> summaryViews = List.of();
        private CommunicationPreference lastSavedPreference;
        private UUID deletedId;

        @Override
        public CommunicationPreferenceView save(CommunicationPreference preference) {
            lastSavedPreference = preference;
            nextView = view(preference.getId() != null ? preference.getId() : UUID.randomUUID());
            return nextView;
        }

        @Override
        public List<CommunicationPreferenceView> saveAll(List<CommunicationPreference> preferences) {
            return preferences.stream()
                .map(this::save)
                .toList();
        }

        @Override
        public Optional<CommunicationPreferenceView> findById(UUID id) {
            if (nextView == null || !nextView.id().equals(id)) {
                return Optional.empty();
            }

            return Optional.of(nextView);
        }

        @Override
        public List<CommunicationPreferenceView> findAll() {
            return views;
        }

        @Override
        public List<CommunicationPreferenceSummaryView> findSummary() {
            return summaryViews;
        }

        @Override
        public boolean existsById(UUID id) {
            return nextView != null && nextView.id().equals(id);
        }

        @Override
        public void deleteById(UUID id) {
            deletedId = id;
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
