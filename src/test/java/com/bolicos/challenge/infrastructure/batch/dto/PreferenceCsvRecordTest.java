package com.bolicos.challenge.infrastructure.batch.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PreferenceCsvRecordTest {

    @Test
    void devePreencherCamposDoRegistroCsv() {
        var record = new PreferenceCsvRecord();
        record.setPreferenciaCanalComunicacao("EMAIL");
        record.setEmail("cliente@example.com");
        record.setTipo("PESSOAL");
        record.setVerificado(false);

        assertEquals("EMAIL", record.getPreferenciaCanalComunicacao());
        assertEquals("cliente@example.com", record.getEmail());
        assertEquals("PESSOAL", record.getTipo());
        assertEquals(false, record.getVerificado());
    }
}
