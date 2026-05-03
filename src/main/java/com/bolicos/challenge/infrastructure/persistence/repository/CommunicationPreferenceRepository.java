package com.bolicos.challenge.infrastructure.persistence.repository;

import com.bolicos.challenge.infrastructure.persistence.entity.CommunicationPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommunicationPreferenceRepository extends JpaRepository<CommunicationPreferenceEntity, UUID> {
}
