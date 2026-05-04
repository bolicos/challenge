package com.bolicos.challenge.infrastructure.persistence.adapter;

import com.bolicos.challenge.application.model.CommunicationPreferenceView;
import com.bolicos.challenge.application.model.CommunicationPreferenceSummaryView;
import com.bolicos.challenge.application.port.out.PreferencePersistencePort;
import com.bolicos.challenge.domain.model.CommunicationPreference;
import com.bolicos.challenge.infrastructure.persistence.entity.CommunicationPreferenceEntity;
import com.bolicos.challenge.infrastructure.persistence.mapper.CommunicationPreferenceEntityMapper;
import com.bolicos.challenge.infrastructure.persistence.repository.CommunicationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CommunicationPreferencePersistenceAdapter implements PreferencePersistencePort {

    private final CommunicationPreferenceRepository repository;
    private final CommunicationPreferenceEntityMapper mapper;

    @Override
    public CommunicationPreferenceView save(CommunicationPreference preference) {
        CommunicationPreferenceEntity entity = resolveEntity(preference);
        mapper.applyDomain(entity, preference);

        return mapper.toView(repository.save(entity));
    }

    @Override
    public List<CommunicationPreferenceView> saveAll(List<CommunicationPreference> preferences) {
        List<CommunicationPreferenceEntity> entities = preferences.stream()
            .map(mapper::toNewEntity)
            .toList();

        return repository.saveAll(entities).stream()
            .map(mapper::toView)
            .toList();
    }

    @Override
    public Optional<CommunicationPreferenceView> findById(UUID id) {
        return repository.findWithEmailsById(id).map(mapper::toView);
    }

    @Override
    public List<CommunicationPreferenceView> findAll() {
        return repository.findAllWithEmails().stream().map(mapper::toView).toList();
    }

    @Override
    public List<CommunicationPreferenceSummaryView> findSummary() {
        return repository.findAllSummary().stream()
            .map(mapper::toSummaryView)
            .toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    private CommunicationPreferenceEntity resolveEntity(CommunicationPreference preference) {
        if (preference.getId() == null) {
            return mapper.toNewEntity(preference);
        }

        return repository.findWithEmailsById(preference.getId())
            .orElseGet(() -> mapper.toNewEntity(preference));
    }
}
