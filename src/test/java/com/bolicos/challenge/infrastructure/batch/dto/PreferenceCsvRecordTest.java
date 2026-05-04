package com.bolicos.challenge.infrastructure.batch.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PreferenceCsvRecordTest {

    @Test
    void devePreencherCamposDoRegistroCsv() {
        var record = new PreferenceCsvRecord();
        record.setCustomerId("11111111-1111-1111-1111-111111111111");
        record.setPreferenciaCanalComunicacao("EMAIL");
        record.setEmail("cliente@example.com");
        record.setTipo("PESSOAL");
        record.setVerificado(false);

        assertEquals("11111111-1111-1111-1111-111111111111", record.getCustomerId());
        assertEquals("EMAIL", record.getPreferenciaCanalComunicacao());
        assertEquals("cliente@example.com", record.getEmail());
        assertEquals("PESSOAL", record.getTipo());
        assertEquals(false, record.getVerificado());
    }
}
