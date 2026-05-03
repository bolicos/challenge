package com.bolicos.challenge.domain.model;

import com.bolicos.challenge.domain.exception.DuplicateEmailException;
import com.bolicos.challenge.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CommunicationPreferenceTest {

    @Test
    void deveAdicionarEmail() {
        var pref = new CommunicationPreference();
        pref.setId(UUID.randomUUID());
        pref.setCustomerId(UUID.randomUUID());
        pref.setCommunicationChannel(CommunicationChannel.EMAIL);

        var email = new PreferenceEmail();
        email.setId(1L);
        email.setEmail("a@b.com");
        email.setType(EmailType.PESSOAL);
        email.setVerified(false);

        pref.addEmail(email);

        assertEquals(1, pref.getEmails().size());
    }

    @Test
    void naoDevePermitirAlteracaoDiretaDaListaDeEmails() {
        var pref = new CommunicationPreference();

        var email = new PreferenceEmail();
        email.setEmail("a@b.com");
        email.setType(EmailType.PESSOAL);
        email.setVerified(false);

        assertThrows(UnsupportedOperationException.class, () -> pref.getEmails().add(email));
    }

    @Test
    void deveLancarExcecaoQuandoEmailDuplicadoNoAdd() {
        var pref = new CommunicationPreference();

        var e1 = new PreferenceEmail();
        e1.setEmail("a@b.com");
        e1.setType(EmailType.PESSOAL);
        e1.setVerified(false);

        var e2 = new PreferenceEmail();
        e2.setEmail("A@B.COM");
        e2.setType(EmailType.COMERCIAL);
        e2.setVerified(false);

        pref.addEmail(e1);

        assertThrows(DuplicateEmailException.class, () -> pref.addEmail(e2));
    }

    @Test
    void deveLancarExcecaoQuandoEmailForNuloNoAdd() {
        var pref = new CommunicationPreference();

        assertThrows(DomainException.class, () -> pref.addEmail(null));
    }

    @Test
    void deveLancarExcecaoQuandoEnderecoForNuloNoAdd() {
        var pref = new CommunicationPreference();

        var email = new PreferenceEmail();
        email.setEmail(null);
        email.setType(EmailType.PESSOAL);
        email.setVerified(false);

        assertThrows(DomainException.class, () -> pref.addEmail(email));
    }

    @Test
    void deveSubstituirEmailsNoReplace() {
        var pref = new CommunicationPreference();

        var antigo = new PreferenceEmail();
        antigo.setEmail("old@x.com");
        antigo.setType(EmailType.PESSOAL);
        antigo.setVerified(false);
        pref.addEmail(antigo);

        var novo1 = new PreferenceEmail();
        novo1.setEmail("new1@x.com");
        novo1.setType(EmailType.PESSOAL);
        novo1.setVerified(false);

        var novo2 = new PreferenceEmail();
        novo2.setEmail("new2@x.com");
        novo2.setType(EmailType.COMERCIAL);
        novo2.setVerified(true);

        pref.replaceEmails(List.of(novo1, novo2));

        assertEquals(2, pref.getEmails().size());
        assertTrue(pref.getEmails().stream().anyMatch(e -> "new1@x.com".equalsIgnoreCase(e.getEmail())));
    }

    @Test
    void deveLancarExcecaoQuandoDuplicadoNoReplace() {
        var pref = new CommunicationPreference();

        var e1 = new PreferenceEmail();
        e1.setEmail("dup@x.com");
        e1.setType(EmailType.PESSOAL);
        e1.setVerified(false);

        var e2 = new PreferenceEmail();
        e2.setEmail("DUP@x.com");
        e2.setType(EmailType.COMERCIAL);
        e2.setVerified(true);

        assertThrows(DuplicateEmailException.class, () -> pref.replaceEmails(List.of(e1, e2)));
    }

    @Test
    void deveLancarExcecaoQuandoEmailForNuloNoReplace() {
        var pref = new CommunicationPreference();

        assertThrows(DomainException.class, () -> pref.replaceEmails(Collections.singletonList(null)));
    }

    @Test
    void equalsDeveCompararPorIdQuandoNaoNulo() {
        var id = UUID.randomUUID();

        var a = new CommunicationPreference();
        a.setId(id);

        var b = new CommunicationPreference();
        b.setId(id);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equalsDeveSerFalseQuandoIdNulo() {
        var a = new CommunicationPreference();
        var b = new CommunicationPreference();

        assertNotEquals(a, b);
    }
}
