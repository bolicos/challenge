package com.bolicos.challenge.application.service;

import com.bolicos.challenge.application.model.CommunicationPreferenceView;
import com.bolicos.challenge.application.port.in.PreferenceUseCase;
import com.bolicos.challenge.application.port.out.PreferencePersistencePort;
import com.bolicos.challenge.domain.exception.PreferenceNotFoundException;
import com.bolicos.challenge.domain.model.CommunicationPreference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PreferenceApplicationService implements PreferenceUseCase {

    private final PreferencePersistencePort persistencePort;

    @Override
    @Transactional
    public CommunicationPreferenceView create(CommunicationPreference preference) {
        ensureCustomerId(preference);
        return persistencePort.save(preference);
    }

    @Override
    @Transactional(readOnly = true)
    public CommunicationPreferenceView findById(UUID id) {
        return persistencePort.findById(id)
            .orElseThrow(() -> new PreferenceNotFoundException("preference not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunicationPreferenceView> findAll() {
        return persistencePort.findAll();
    }

    @Override
    @Transactional
    public CommunicationPreferenceView update(UUID id, CommunicationPreference preference) {
        if (!persistencePort.existsById(id)) {
            throw new PreferenceNotFoundException("preference not found");
        }

        preference.setId(id);
        ensureCustomerId(preference);
        return persistencePort.save(preference);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!persistencePort.existsById(id)) {
            throw new PreferenceNotFoundException("preference not found");
        }

        persistencePort.deleteById(id);
    }

    private void ensureCustomerId(CommunicationPreference preference) {
        if (preference.getCustomerId() == null) {
            preference.setCustomerId(UUID.randomUUID());
        }
    }
}
