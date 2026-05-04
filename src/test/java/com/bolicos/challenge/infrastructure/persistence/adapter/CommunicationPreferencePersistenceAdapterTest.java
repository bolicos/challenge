package com.bolicos.challenge.infrastructure.persistence.adapter;

import com.bolicos.challenge.application.model.AuditMetadata;
import com.bolicos.challenge.application.model.CommunicationPreferenceSummaryView;
import com.bolicos.challenge.application.model.CommunicationPreferenceView;
import com.bolicos.challenge.domain.model.CommunicationChannel;
import com.bolicos.challenge.domain.model.CommunicationPreference;
import com.bolicos.challenge.infrastructure.persistence.entity.CommunicationPreferenceEntity;
import com.bolicos.challenge.infrastructure.persistence.mapper.CommunicationPreferenceEntityMapper;
import com.bolicos.challenge.infrastructure.persistence.repository.CommunicationPreferenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunicationPreferencePersistenceAdapterTest {

    @Mock
    private CommunicationPreferenceRepository repository;

    @Mock
    private CommunicationPreferenceEntityMapper mapper;

    @InjectMocks
    private CommunicationPreferencePersistenceAdapter adapter;

    @Test
    void deveSalvarPreferenciaNova() {
        var preference = preference(null);
        var entity = new CommunicationPreferenceEntity();
        var view = view(UUID.randomUUID());

        when(mapper.toNewEntity(preference)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toView(entity)).thenReturn(view);

        var saved = adapter.save(preference);

        assertEquals(view, saved);
        verify(mapper).applyDomain(entity, preference);
    }

    @Test
    void deveSalvarPreferenciaExistente() {
        UUID id = UUID.randomUUID();
        var preference = preference(id);
        var entity = new CommunicationPreferenceEntity();
        var view = view(id);

        when(repository.findWithEmailsById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toView(entity)).thenReturn(view);

        var saved = adapter.save(preference);

        assertEquals(id, saved.id());
        verify(mapper).applyDomain(entity, preference);
    }

    @Test
    void deveBuscarPorId() {
        UUID id = UUID.randomUUID();
        var entity = new CommunicationPreferenceEntity();
        var view = view(id);

        when(repository.findWithEmailsById(id)).thenReturn(Optional.of(entity));
        when(mapper.toView(entity)).thenReturn(view);

        assertTrue(adapter.findById(id).isPresent());
    }

    @Test
    void deveListarTodas() {
        var entity = new CommunicationPreferenceEntity();
        var view = view(UUID.randomUUID());

        when(repository.findAllWithEmails()).thenReturn(List.of(entity));
        when(mapper.toView(entity)).thenReturn(view);

        assertEquals(1, adapter.findAll().size());
    }

    @Test
    void deveSalvarPreferenciasEmLote() {
        var preference = preference(null);
        var entity = new CommunicationPreferenceEntity();
        var view = view(UUID.randomUUID());

        when(mapper.toNewEntity(preference)).thenReturn(entity);
        when(repository.saveAll(List.of(entity))).thenReturn(List.of(entity));
        when(mapper.toView(entity)).thenReturn(view);

        assertEquals(1, adapter.saveAll(List.of(preference)).size());
    }

    @Test
    void deveListarResumo() {
        var summary = new CommunicationPreferenceSummaryView(
            UUID.randomUUID(),
            UUID.randomUUID(),
            CommunicationChannel.EMAIL,
            1L,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(repository.findAllSummary()).thenReturn(List.of());

        assertEquals(0, adapter.findSummary().size());
    }

    @Test
    void deveDelegarExistsEDelete() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        assertTrue(adapter.existsById(id));

        adapter.deleteById(id);
        verify(repository).deleteById(id);
    }

    private CommunicationPreference preference(UUID id) {
        var preference = new CommunicationPreference();
        preference.setId(id);
        preference.setCustomerId(UUID.randomUUID());
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
}
