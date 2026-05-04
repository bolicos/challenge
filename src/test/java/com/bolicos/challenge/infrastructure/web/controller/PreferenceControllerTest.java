package com.bolicos.challenge.infrastructure.web.controller;

import com.bolicos.challenge.application.model.AuditMetadata;
import com.bolicos.challenge.application.model.CommunicationPreferenceView;
import com.bolicos.challenge.application.model.PreferenceEmailView;
import com.bolicos.challenge.application.port.in.PreferenceUseCase;
import com.bolicos.challenge.domain.exception.PreferenceNotFoundException;
import com.bolicos.challenge.domain.model.CommunicationChannel;
import com.bolicos.challenge.domain.model.EmailType;
import com.bolicos.challenge.infrastructure.web.exception.GlobalExceptionHandler;
import com.bolicos.challenge.infrastructure.web.mapper.PreferenceWebMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PreferenceController.class)
@Import({PreferenceWebMapper.class, GlobalExceptionHandler.class})
class PreferenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PreferenceUseCase preferenceUseCase;

    @Test
    void deveCriarPreferencia() throws Exception {
        UUID id = UUID.randomUUID();
        when(preferenceUseCase.create(any())).thenReturn(view(id));

        mockMvc.perform(post("/api/preferencias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload()))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "http://localhost/api/preferencias/" + id))
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.preferenciaCanalComunicacao").value("EMAIL"))
            .andExpect(jsonPath("$.emails[0].email").value("cliente@example.com"))
            .andExpect(jsonPath("$.emails[0].tipo").value("PESSOAL"));

        var captor = ArgumentCaptor.forClass(com.bolicos.challenge.domain.model.CommunicationPreference.class);
        verify(preferenceUseCase).create(captor.capture());
        assertEquals(CommunicationChannel.EMAIL, captor.getValue().getCommunicationChannel());
        assertEquals(1, captor.getValue().getEmails().size());
    }

    @Test
    void deveBuscarPreferenciaPorId() throws Exception {
        UUID id = UUID.randomUUID();
        when(preferenceUseCase.findById(id)).thenReturn(view(id));

        mockMvc.perform(get("/api/preferencias/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.emails[0].verificado").value(false));
    }

    @Test
    void deveListarPreferencias() throws Exception {
        UUID id = UUID.randomUUID();
        when(preferenceUseCase.findAll()).thenReturn(List.of(view(id)));

        mockMvc.perform(get("/api/preferencias"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(id.toString()))
            .andExpect(jsonPath("$[0].preferenciaCanalComunicacao").value("EMAIL"));
    }

    @Test
    void deveAtualizarPreferencia() throws Exception {
        UUID id = UUID.randomUUID();
        when(preferenceUseCase.update(any(), any())).thenReturn(view(id));

        mockMvc.perform(put("/api/preferencias/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()));

        verify(preferenceUseCase).update(any(), any());
    }

    @Test
    void deveDeletarPreferencia() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/preferencias/{id}", id))
            .andExpect(status().isOk());

        verify(preferenceUseCase).delete(id);
    }

    @Test
    void deveRetornar400QuandoPayloadForInvalido() throws Exception {
        mockMvc.perform(post("/api/preferencias")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "emails": [
                        {
                          "email": "email-invalido",
                          "tipo": "PESSOAL",
                          "verificado": false
                        }
                      ]
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.details[0]").exists());
    }

    @Test
    void deveRetornar404QuandoPreferenciaNaoForEncontrada() throws Exception {
        UUID id = UUID.randomUUID();
        when(preferenceUseCase.findById(id))
            .thenThrow(new PreferenceNotFoundException("Preferência " + id + " não encontrada"));

        mockMvc.perform(get("/api/preferencias/{id}", id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Recurso não encontrado"));
    }

    private CommunicationPreferenceView view(UUID id) {
        var audit = new AuditMetadata(
            LocalDateTime.of(2026, 5, 4, 10, 0),
            LocalDateTime.of(2026, 5, 4, 11, 0),
            "system",
            "system"
        );

        return new CommunicationPreferenceView(
            id,
            UUID.randomUUID(),
            CommunicationChannel.EMAIL,
            List.of(new PreferenceEmailView(
                1L,
                "cliente@example.com",
                EmailType.PESSOAL,
                false,
                audit
            )),
            audit
        );
    }

    private String validPayload() {
        return """
            {
              "preferenciaCanalComunicacao": "EMAIL",
              "emails": [
                {
                  "email": "cliente@example.com",
                  "tipo": "PESSOAL",
                  "verificado": false
                }
              ]
            }
            """;
    }
}
