package com.bolicos.challenge.infrastructure.persistence.mapper;

import com.bolicos.challenge.application.model.AuditMetadata;
import com.bolicos.challenge.application.model.CommunicationPreferenceSummaryView;
import com.bolicos.challenge.application.model.CommunicationPreferenceView;
import com.bolicos.challenge.application.model.PreferenceEmailView;
import com.bolicos.challenge.domain.model.CommunicationChannel;
import com.bolicos.challenge.domain.model.CommunicationPreference;
import com.bolicos.challenge.domain.model.PreferenceEmail;
import com.bolicos.challenge.infrastructure.persistence.entity.AuditableEntity;
import com.bolicos.challenge.infrastructure.persistence.entity.CommunicationPreferenceEntity;
import com.bolicos.challenge.infrastructure.persistence.entity.PreferenceEmailEntity;
import com.bolicos.challenge.infrastructure.persistence.projection.CommunicationPreferenceSummaryProjection;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Component
public class CommunicationPreferenceEntityMapper {

    public CommunicationPreferenceEntity toNewEntity(CommunicationPreference domain) {
        var entity = new CommunicationPreferenceEntity();
        applyDomain(entity, domain);

        return entity;
    }

    public void applyDomain(CommunicationPreferenceEntity entity, CommunicationPreference domain) {
        entity.setCustomerId(domain.getCustomerId());
        entity.setCommunicationChannel(domain.getCommunicationChannel());
        replaceEmails(entity, domain.getEmails());
    }

    public CommunicationPreferenceView toView(CommunicationPreferenceEntity entity) {
        return new CommunicationPreferenceView(
            entity.getId(),
            entity.getCustomerId(),
            entity.getCommunicationChannel(),
            toEmailViews(entity.getEmails()),
            auditOf(entity)
        );
    }

    public CommunicationPreferenceSummaryView toSummaryView(CommunicationPreferenceSummaryProjection projection) {
        return new CommunicationPreferenceSummaryView(
            projection.getId(),
            projection.getCustomerId(),
            CommunicationChannel.valueOf(projection.getCommunicationChannel()),
            projection.getEmailCount(),
            projection.getDataCriacao(),
            projection.getDataAtualizacao()
        );
    }

    private void replaceEmails(CommunicationPreferenceEntity preference, List<PreferenceEmail> emails) {
        Set<PreferenceEmailEntity> retained = Collections.newSetFromMap(new IdentityHashMap<>());

        List<PreferenceEmail> incomingEmails = emails == null ? List.of() : emails;
        for (PreferenceEmail email : incomingEmails) {
            PreferenceEmailEntity emailEntity = findMatchingEmail(
                    preference.getEmails(),
                    email,
                    incomingEmails.size() == 1
                )
                .orElseGet(() -> {
                    var newEmail = new PreferenceEmailEntity();
                    newEmail.setPreference(preference);
                    preference.getEmails().add(newEmail);
                    return newEmail;
                });

            emailEntity.setEmail(email.getEmail());
            emailEntity.setType(email.getType());
            emailEntity.setVerified(email.getVerified());
            retained.add(emailEntity);
        }

        preference.getEmails().removeIf(existing -> !retained.contains(existing));
    }

    private Optional<PreferenceEmailEntity> findMatchingEmail(
        List<PreferenceEmailEntity> existingEmails,
        PreferenceEmail email,
        boolean allowSingleEmailFallback
    ) {
        if (allowSingleEmailFallback && existingEmails.size() == 1 && email.getId() == null) {
            return Optional.of(existingEmails.getFirst());
        }

        if (email.getId() != null) {
            Optional<PreferenceEmailEntity> byId = existingEmails.stream()
                .filter(existing -> email.getId().equals(existing.getId()))
                .findFirst();

            if (byId.isPresent()) {
                return byId;
            }
        }

        String emailKey = normalizeEmail(email.getEmail());
        return existingEmails.stream()
            .filter(existing -> emailKey.equals(normalizeEmail(existing.getEmail())))
            .findFirst();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private List<PreferenceEmailView> toEmailViews(List<PreferenceEmailEntity> emails) {
        if (emails == null || emails.isEmpty()) {
            return List.of();
        }

        return emails.stream()
            .sorted(Comparator.comparing(PreferenceEmailEntity::getId, Comparator.nullsLast(Long::compareTo)))
            .map(this::toEmailView)
            .toList();
    }

    private PreferenceEmailView toEmailView(PreferenceEmailEntity entity) {
        return new PreferenceEmailView(
            entity.getId(),
            entity.getEmail(),
            entity.getType(),
            entity.getVerified(),
            auditOf(entity)
        );
    }

    private AuditMetadata auditOf(AuditableEntity entity) {
        return new AuditMetadata(
            entity.getCreatedAt(),
            entity.getLastModifiedAt(),
            entity.getCreatedBy(),
            entity.getLastModifiedBy()
        );
    }
}
