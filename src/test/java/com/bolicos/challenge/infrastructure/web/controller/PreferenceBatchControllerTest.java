package com.bolicos.challenge.infrastructure.web.controller;

import com.bolicos.challenge.application.model.AuditMetadata;
import com.bolicos.challenge.application.model.CommunicationPreferenceView;
import com.bolicos.challenge.application.model.PreferenceBatchImportResult;
import com.bolicos.challenge.application.model.PreferenceEmailView;
import com.bolicos.challenge.application.port.in.PreferenceBatchUseCase;
import com.bolicos.challenge.domain.model.CommunicationChannel;
import com.bolicos.challenge.domain.model.EmailType;
import com.bolicos.challenge.infrastructure.batch.PreferenceCsvImportJobLauncher;
import com.bolicos.challenge.infrastructure.web.exception.GlobalExceptionHandler;
import com.bolicos.challenge.infrastructure.web.mapper.PreferenceWebMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PreferenceBatchController.class)
@Import({PreferenceWebMapper.class, GlobalExceptionHandler.class})
class PreferenceBatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PreferenceBatchUseCase preferenceBatchUseCase;

    @MockitoBean
    private PreferenceCsvImportJobLauncher csvImportJobLauncher;

    @Test
    void deveImportarPreferenciasEmLote() throws Exception {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        when(preferenceBatchUseCase.importBatch(any())).thenReturn(new PreferenceBatchImportResult(
            1,
            1,
            0,
            List.of(view(id, customerId))
        ));

        mockMvc.perform(post("/api/preferencias/importacao")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "preferencias": [
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
                      ]
                    }
                    """.formatted(customerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalRecebido").value(1))
            .andExpect(jsonPath("$.totalProcessado").value(1))
            .andExpect(jsonPath("$.preferencias[0].id").value(id.toString()))
            .andExpect(jsonPath("$.preferencias[0].customerId").value(customerId.toString()));

        var captor = ArgumentCaptor.forClass(List.class);
        verify(preferenceBatchUseCase).importBatch(captor.capture());
        var preferences = captor.getValue();
        var preference = (com.bolicos.challenge.domain.model.CommunicationPreference) preferences.getFirst();
        assertEquals(customerId, preference.getCustomerId());
    }

    @Test
    void deveImportarPreferenciasPorCsv() throws Exception {
        var file = new MockMultipartFile(
            "file",
            "preferencias.csv",
            "text/csv",
            """
                customerId,preferenciaCanalComunicacao,email,tipo,verificado
                11111111-1111-1111-1111-111111111111,EMAIL,cliente.csv@example.com,PESSOAL,false
                """.getBytes()
        );
        var execution = new JobExecution(10L);
        execution.setStatus(BatchStatus.COMPLETED);
        execution.setExitStatus(ExitStatus.COMPLETED);

        when(csvImportJobLauncher.launch(any())).thenReturn(execution);
        when(csvImportJobLauncher.jobName()).thenReturn("preferenceCsvImportJob");

        mockMvc.perform(multipart("/api/preferencias/importacao/csv").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.jobExecutionId").value(10))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.exitStatus").value("COMPLETED"));
    }

    private CommunicationPreferenceView view(UUID id) {
        return view(id, UUID.randomUUID());
    }

    private CommunicationPreferenceView view(UUID id, UUID customerId) {
        var audit = new AuditMetadata(
            LocalDateTime.of(2026, 5, 4, 10, 0),
            LocalDateTime.of(2026, 5, 4, 11, 0),
            "system",
            "system"
        );

        return new CommunicationPreferenceView(
            id,
            customerId,
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
}
