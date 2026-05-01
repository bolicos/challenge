package com.bolicos.challenge.api.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FakeController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveRetornar400QuandoBodyForInvalido() throws Exception {
        mockMvc.perform(post("/fake/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("Payload inválido"))
            .andExpect(jsonPath("$.path").value("/fake/validation"))
            .andExpect(jsonPath("$.details[0]").exists());
    }

    @Test
    void deveRetornar400QuandoJsonForMalformed() throws Exception {
        mockMvc.perform(post("/fake/read")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("JSON malformado ou tipo inválido"))
            .andExpect(jsonPath("$.path").value("/fake/read"));
    }

    @Test
    void deveRetornar400QuandoHouverConstraintViolation() throws Exception {
        mockMvc.perform(get("/fake/constraint")
                .param("name", ""))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Falha de validação"))
            .andExpect(jsonPath("$.path").value("/fake/constraint"));
    }

    @Test
    void deveRetornar404QuandoNaoEncontrarRecurso() throws Exception {
        mockMvc.perform(get("/fake/not-found"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Recurso não encontrado"))
            .andExpect(jsonPath("$.path").value("/fake/not-found"));
    }

    @Test
    void deveRetornar500QuandoErroInesperado() throws Exception {
        mockMvc.perform(get("/fake/error"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.message").value("Erro inesperado"))
            .andExpect(jsonPath("$.path").value("/fake/error"));
    }
}
