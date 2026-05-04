package com.bolicos.challenge.application.port.out;

import com.bolicos.challenge.application.model.CommunicationPreferenceView;
import com.bolicos.challenge.domain.model.CommunicationPreference;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PreferencePersistencePort {

    CommunicationPreferenceView save(CommunicationPreference preference);

    Optional<CommunicationPreferenceView> findById(UUID id);

    List<CommunicationPreferenceView> findAll();

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
