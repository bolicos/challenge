package com.bolicos.challenge.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PreferenceEmailTest {

    @Test
    void equalsDeveCompararPorIdQuandoNaoNulo() {
        var a = new PreferenceEmail();
        a.setId(10L);

        var b = new PreferenceEmail();
        b.setId(10L);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equalsDeveSerTrueParaMesmaInstancia() {
        var email = new PreferenceEmail();

        assertEquals(email, email);
    }

    @Test
    void equalsDeveSerFalseQuandoCompararComNuloOuOutraClasse() {
        var email = new PreferenceEmail();
        email.setId(10L);

        assertNotEquals(null, email);
        assertNotEquals("email", email);
    }

    @Test
    void equalsDeveSerFalseQuandoIdsForemDiferentes() {
        var a = new PreferenceEmail();
        a.setId(10L);

        var b = new PreferenceEmail();
        b.setId(20L);

        assertNotEquals(a, b);
    }

    @Test
    void equalsDeveSerFalseQuandoIdNulo() {
        var a = new PreferenceEmail();
        var b = new PreferenceEmail();

        assertNotEquals(a, b);
    }
}
