package com.bolicos.challenge.infrastructure.persistence.mapper;

import com.bolicos.challenge.domain.model.CommunicationChannel;
import com.bolicos.challenge.domain.model.CommunicationPreference;
import com.bolicos.challenge.domain.model.EmailType;
import com.bolicos.challenge.domain.model.PreferenceEmail;
import com.bolicos.challenge.infrastructure.persistence.entity.CommunicationPreferenceEntity;
import com.bolicos.challenge.infrastructure.persistence.entity.PreferenceEmailEntity;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CommunicationPreferenceEntityMapperTest {

    private final CommunicationPreferenceEntityMapper mapper = new CommunicationPreferenceEntityMapper();

    @Test
    void deveCriarEntidadeNovaAPartirDoDominio() {
        var domain = preference(List.of(email(null, "cliente@example.com", false)));

        var entity = mapper.toNewEntity(domain);

        assertEquals(domain.getCustomerId(), entity.getCustomerId());
        assertEquals(CommunicationChannel.EMAIL, entity.getCommunicationChannel());
        assertEquals(1, entity.getEmails().size());
        assertSame(entity, entity.getEmails().getFirst().getPreference());
    }

    @Test
    void deveReconciliarEmailsExistentesPorIdEEmail() {
        var entity = new CommunicationPreferenceEntity();
        entity.setCustomerId(UUID.randomUUID());
        entity.setCommunicationChannel(CommunicationChannel.EMAIL);

        var byId = emailEntity(10L, "old@example.com", false, entity);
        var byEmail = emailEntity(20L, "same@example.com", false, entity);
        var removed = emailEntity(30L, "remove@example.com", false, entity);
        entity.getEmails().addAll(List.of(byId, byEmail, removed));

        var domain = preference(List.of(
            email(10L, "updated@example.com", true),
            email(null, "SAME@example.com", true),
            email(null, "new@example.com", false)
        ));

        mapper.applyDomain(entity, domain);

        assertEquals(3, entity.getEmails().size());
        assertSame(byId, entity.getEmails().stream().filter(email -> Long.valueOf(10L).equals(email.getId())).findFirst().orElseThrow());
        assertSame(byEmail, entity.getEmails().stream().filter(email -> Long.valueOf(20L).equals(email.getId())).findFirst().orElseThrow());
        assertEquals("updated@example.com", byId.getEmail());
        assertEquals(true, byEmail.getVerified());
    }

    @Test
    void deveMapearEntidadeParaViewComAuditoria() {
        var entity = new CommunicationPreferenceEntity();
        entity.setId(UUID.randomUUID());
        entity.setCustomerId(UUID.randomUUID());
        entity.setCommunicationChannel(CommunicationChannel.WHATSAPP);
        setAudit(entity);

        var email = emailEntity(1L, "cliente@example.com", true, entity);
        setAudit(email);
        entity.getEmails().add(email);

        var view = mapper.toView(entity);

        assertEquals(entity.getId(), view.id());
        assertEquals(CommunicationChannel.WHATSAPP, view.communicationChannel());
        assertEquals("cliente@example.com", view.emails().getFirst().email());
        assertEquals("system", view.audit().criadoPor());
        assertEquals("system", view.emails().getFirst().audit().alteradoPor());
    }

    private CommunicationPreference preference(List<PreferenceEmail> emails) {
        var preference = new CommunicationPreference();
        preference.setCustomerId(UUID.randomUUID());
        preference.setCommunicationChannel(CommunicationChannel.EMAIL);
        preference.replaceEmails(emails);

        return preference;
    }

    private PreferenceEmail email(Long id, String address, boolean verified) {
        var email = new PreferenceEmail();
        email.setId(id);
        email.setEmail(address);
        email.setType(EmailType.PESSOAL);
        email.setVerified(verified);

        return email;
    }

    private PreferenceEmailEntity emailEntity(
        Long id,
        String address,
        boolean verified,
        CommunicationPreferenceEntity preference
    ) {
        var email = new PreferenceEmailEntity();
        email.setId(id);
        email.setEmail(address);
        email.setType(EmailType.PESSOAL);
        email.setVerified(verified);
        email.setPreference(preference);

        return email;
    }

    private void setAudit(Object entity) {
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 5, 4, 10, 0));
        ReflectionTestUtils.setField(entity, "lastModifiedAt", LocalDateTime.of(2026, 5, 4, 11, 0));
        ReflectionTestUtils.setField(entity, "createdBy", "system");
        ReflectionTestUtils.setField(entity, "lastModifiedBy", "system");
    }
}
