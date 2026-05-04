package com.bolicos.challenge.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmailNotFoundExceptionTest {

    @Test
    void devePreservarMensagemDaExcecao() {
        var exception = new EmailNotFoundException("E-mail não encontrado");

        assertEquals("E-mail não encontrado", exception.getMessage());
    }
}
