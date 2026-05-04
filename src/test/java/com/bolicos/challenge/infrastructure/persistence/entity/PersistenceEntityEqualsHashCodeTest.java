package com.bolicos.challenge.infrastructure.persistence.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PersistenceEntityEqualsHashCodeTest {

    @Test
    void preferenceEmailEntityEqualsDeveCompararPorIdQuandoNaoNulo() {
        var a = new PreferenceEmailEntity();
        a.setId(10L);

        var b = new PreferenceEmailEntity();
        b.setId(10L);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void preferenceEmailEntityEqualsDeveSerTrueParaMesmaInstancia() {
        var email = new PreferenceEmailEntity();

        assertEquals(email, email);
    }

    @Test
    void preferenceEmailEntityEqualsDeveSerFalseParaNuloClasseDiferenteIdNuloOuIdDiferente() {
        var email = new PreferenceEmailEntity();
        email.setId(10L);

        var other = new PreferenceEmailEntity();
        other.setId(20L);

        assertNotEquals(null, email);
        assertNotEquals(new CommunicationPreferenceEntity(), email);
        assertNotEquals(email, new PreferenceEmailEntity());
        assertNotEquals(email, other);
    }

    @Test
    void communicationPreferenceEntityEqualsDeveCompararPorIdQuandoNaoNulo() {
        var id = UUID.randomUUID();

        var a = new CommunicationPreferenceEntity();
        a.setId(id);

        var b = new CommunicationPreferenceEntity();
        b.setId(id);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void communicationPreferenceEntityEqualsDeveSerTrueParaMesmaInstancia() {
        var preference = new CommunicationPreferenceEntity();

        assertEquals(preference, preference);
    }

    @Test
    void communicationPreferenceEntityEqualsDeveSerFalseParaNuloClasseDiferenteIdNuloOuIdDiferente() {
        var preference = new CommunicationPreferenceEntity();
        preference.setId(UUID.randomUUID());

        var other = new CommunicationPreferenceEntity();
        other.setId(UUID.randomUUID());

        assertNotEquals(null, preference);
        assertNotEquals(new PreferenceEmailEntity(), preference);
        assertNotEquals(preference, new CommunicationPreferenceEntity());
        assertNotEquals(preference, other);
    }
}
