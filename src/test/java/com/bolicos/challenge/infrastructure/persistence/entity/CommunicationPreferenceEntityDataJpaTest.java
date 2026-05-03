package com.bolicos.challenge.infrastructure.persistence.entity;

import com.bolicos.challenge.config.JpaAuditingConfiguration;
import com.bolicos.challenge.domain.model.CommunicationChannel;
import com.bolicos.challenge.domain.model.EmailType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Import(JpaAuditingConfiguration.class)
class CommunicationPreferenceEntityDataJpaTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void devePersistirComAuditoriaPreenchida() {
        var pref = new CommunicationPreferenceEntity();
        pref.setCustomerId(UUID.randomUUID());
        pref.setCommunicationChannel(CommunicationChannel.EMAIL);

        var email = new PreferenceEmailEntity();
        email.setEmail("audit@x.com");
        email.setType(EmailType.PESSOAL);
        email.setVerified(false);
        email.setPreference(pref);

        pref.getEmails().add(email);

        entityManager.persist(pref);
        entityManager.flush();
        entityManager.clear();

        var saved = entityManager.find(CommunicationPreferenceEntity.class, pref.getId());

        assertNotNull(saved);
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getLastModifiedAt());
        assertEquals("system", saved.getCreatedBy());
        assertEquals("system", saved.getLastModifiedBy());
    }

    @Test
    void deveAplicarUniqueGlobalParaEmail() {
        var p1 = new CommunicationPreferenceEntity();
        p1.setCustomerId(UUID.randomUUID());
        p1.setCommunicationChannel(CommunicationChannel.EMAIL);

        var e1 = new PreferenceEmailEntity();
        e1.setEmail("global@x.com");
        e1.setType(EmailType.PESSOAL);
        e1.setVerified(false);
        e1.setPreference(p1);
        p1.getEmails().add(e1);

        entityManager.persist(p1);
        entityManager.flush();

        var p2 = new CommunicationPreferenceEntity();
        p2.setCustomerId(UUID.randomUUID());
        p2.setCommunicationChannel(CommunicationChannel.WHATSAPP);

        var e2 = new PreferenceEmailEntity();
        e2.setEmail("GLOBAL@x.com"); // duplicado global, mesmo variando caixa
        e2.setType(EmailType.COMERCIAL);
        e2.setVerified(true);
        e2.setPreference(p2);
        p2.getEmails().add(e2);

        assertThrows(PersistenceException.class, () -> {
            entityManager.persist(p2);
            entityManager.flush();
        });
    }

    @Test
    void deveRemoverEmailComOrphanRemoval() {
        var pref = new CommunicationPreferenceEntity();
        pref.setCustomerId(UUID.randomUUID());
        pref.setCommunicationChannel(CommunicationChannel.EMAIL);

        var email = new PreferenceEmailEntity();
        email.setEmail("orphan@x.com");
        email.setType(EmailType.PESSOAL);
        email.setVerified(false);
        email.setPreference(pref);

        pref.getEmails().add(email);

        entityManager.persist(pref);
        entityManager.flush();

        pref.getEmails().clear();
        entityManager.flush();
        entityManager.clear();

        var count = entityManager.createQuery(
                "select count(e) from PreferenceEmailEntity e where e.email = :email", Long.class)
            .setParameter("email", "orphan@x.com")
            .getSingleResult();

        assertEquals(0L, count);
    }
}
