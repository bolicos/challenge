package com.bolicos.challenge.infrastructure.persistence.mapper;

import com.bolicos.challenge.application.model.AuditMetadata;
import com.bolicos.challenge.application.model.CommunicationPreferenceView;
import com.bolicos.challenge.application.model.PreferenceEmailView;
import com.bolicos.challenge.domain.model.CommunicationPreference;
import com.bolicos.challenge.domain.model.PreferenceEmail;
import com.bolicos.challenge.infrastructure.persistence.entity.AuditableEntity;
import com.bolicos.challenge.infrastructure.persistence.entity.CommunicationPreferenceEntity;
import com.bolicos.challenge.infrastructure.persistence.entity.PreferenceEmailEntity;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

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

    private void replaceEmails(CommunicationPreferenceEntity preference, List<PreferenceEmail> emails) {
        preference.getEmails().clear();

        if (emails == null || emails.isEmpty()) {
            return;
        }

        for (PreferenceEmail email : emails) {
            var emailEntity = new PreferenceEmailEntity();
            emailEntity.setEmail(email.getEmail());
            emailEntity.setType(email.getType());
            emailEntity.setVerified(email.getVerified());
            emailEntity.setPreference(preference);

            preference.getEmails().add(emailEntity);
        }
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
