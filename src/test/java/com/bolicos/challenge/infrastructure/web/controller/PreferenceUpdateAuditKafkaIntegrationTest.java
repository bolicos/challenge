package com.bolicos.challenge.infrastructure.web.controller;

import com.bolicos.challenge.infrastructure.messaging.dto.PreferenceChangedEventPayload;
import com.bolicos.challenge.infrastructure.persistence.repository.CommunicationPreferenceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PreferenceUpdateAuditKafkaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommunicationPreferenceRepository repository;

    @MockitoBean
    private KafkaTemplate<String, PreferenceChangedEventPayload> kafkaTemplate;

    @Test
    void devePreservarDataCriacaoDoEmailNoBancoENoPayloadKafkaAoAtualizarEmail() throws Exception {
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.failedFuture(new RuntimeException("skip broker")));

        UUID customerId = UUID.randomUUID();
        JsonNode created = performJson(post("/api/preferencias")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "customerId": "%s",
                  "preferenciaCanalComunicacao": "EMAIL",
                  "emails": [
                    {
                      "email": "cliente@example.com",
                      "tipo": "PESSOAL",
                      "verificado": false
                    }
                  ]
                }
                """.formatted(customerId)));

        UUID preferenceId = UUID.fromString(created.get("id").asText());
        long emailId = created.at("/emails/0/id").asLong();
        LocalDateTime emailCreatedAt = LocalDateTime.parse(created.at("/emails/0/dataCriacao").asText());

        Thread.sleep(20);

        JsonNode updated = performJson(put("/api/preferencias/{id}", preferenceId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "customerId": "%s",
                  "preferenciaCanalComunicacao": "EMAIL",
                  "emails": [
                    {
                      "email": "cliente.atualizado@example.com",
                      "tipo": "COMERCIAL",
                      "verificado": true
                    }
                  ]
                }
                """.formatted(customerId)));

        LocalDateTime emailUpdatedCreatedAt = LocalDateTime.parse(updated.at("/emails/0/dataCriacao").asText());
        LocalDateTime emailUpdatedAt = LocalDateTime.parse(updated.at("/emails/0/dataAtualizacao").asText());

        assertEquals(emailId, updated.at("/emails/0/id").asLong());
        assertSameTimestampIgnoringDatabaseRounding(emailCreatedAt, emailUpdatedCreatedAt);
        assertTrue(emailUpdatedAt.isAfter(emailUpdatedCreatedAt));

        var savedPreference = repository.findWithEmailsById(preferenceId).orElseThrow();
        var savedEmail = savedPreference.getEmails().getFirst();
        assertEquals(emailId, savedEmail.getId());
        assertEquals("cliente.atualizado@example.com", savedEmail.getEmail());
        assertSameTimestampIgnoringDatabaseRounding(emailCreatedAt, savedEmail.getCreatedAt());
        assertSameTimestampIgnoringDatabaseRounding(emailUpdatedAt, savedEmail.getLastModifiedAt());

        var captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate, timeout(2000).times(2)).send(captor.capture());

        ProducerRecord<?, ?> updateRecord = captor.getAllValues().getLast();
        var payload = (PreferenceChangedEventPayload) updateRecord.value();
        String kafkaJson = objectMapper.writeValueAsString(payload);
        JsonNode kafkaPayloadJson = objectMapper.readTree(kafkaJson);

        assertEquals(ZoneOffset.UTC, payload.occurredAt().getOffset());
        assertEquals(emailUpdatedCreatedAt, payload.preference().emails().getFirst().audit().dataCriacao());
        assertEquals(emailUpdatedAt, payload.preference().emails().getFirst().audit().dataAtualizacao());
        assertTrue(kafkaJson.contains("\"occurredAt\""));
        assertEquals(
            emailUpdatedCreatedAt,
            LocalDateTime.parse(kafkaPayloadJson.at("/preference/emails/0/audit/dataCriacao").asText())
        );
        assertEquals(
            emailUpdatedAt,
            LocalDateTime.parse(kafkaPayloadJson.at("/preference/emails/0/audit/dataAtualizacao").asText())
        );
    }

    private JsonNode performJson(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request)
        throws Exception {
        String response = mockMvc.perform(request)
            .andExpect(status().is2xxSuccessful())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return objectMapper.readTree(response);
    }

    private void assertSameTimestampIgnoringDatabaseRounding(LocalDateTime expected, LocalDateTime actual) {
        long nanos = Math.abs(ChronoUnit.NANOS.between(expected, actual));
        assertTrue(nanos <= 1_000, () -> "expected " + expected + " to match " + actual + " within 1 microsecond");
    }
}
