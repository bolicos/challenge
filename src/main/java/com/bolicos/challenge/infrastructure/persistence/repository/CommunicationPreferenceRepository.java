package com.bolicos.challenge.infrastructure.persistence.repository;

import com.bolicos.challenge.infrastructure.persistence.entity.CommunicationPreferenceEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunicationPreferenceRepository extends JpaRepository<CommunicationPreferenceEntity, UUID> {

    @EntityGraph(attributePaths = "emails")
    @Query("select preference from CommunicationPreferenceEntity preference where preference.id = :id")
    Optional<CommunicationPreferenceEntity> findWithEmailsById(UUID id);

    @EntityGraph(attributePaths = "emails")
    @Query("select distinct preference from CommunicationPreferenceEntity preference")
    List<CommunicationPreferenceEntity> findAllWithEmails();
}
