package com.bolicos.challenge.infrastructure.batch;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.batch.core.BatchStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreferenceCsvImportJobLauncherTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job preferenceCsvImportJob;

    @Test
    void deveIniciarJobDeImportacaoCsv() throws Exception {
        var meterRegistry = new SimpleMeterRegistry();
        var launcher = new PreferenceCsvImportJobLauncher(jobLauncher, preferenceCsvImportJob, meterRegistry);
        var file = new MockMultipartFile(
            "file",
            "preferencias.csv",
            "text/csv",
            """
                customerId,preferenciaCanalComunicacao,email,tipo,verificado
                11111111-1111-1111-1111-111111111111,EMAIL,a@b.com,PESSOAL,false
                """.getBytes()
        );
        var execution = new JobExecution(1L);
        execution.setStatus(BatchStatus.COMPLETED);

        when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenReturn(execution);

        assertEquals(execution, launcher.launch(file));
        assertEquals("preferenceCsvImportJob", launcher.jobName());
        assertEquals(1.0, metricCount(meterRegistry, PreferenceCsvImportJobLauncher.BATCH_IMPORT_ATTEMPT_METRIC));
        assertEquals(1.0, metricCount(meterRegistry, PreferenceCsvImportJobLauncher.BATCH_IMPORT_SUCCESS_METRIC));
    }

    @Test
    void deveLancarExcecaoQuandoJobFalharAoIniciar() throws Exception {
        var meterRegistry = new SimpleMeterRegistry();
        var launcher = new PreferenceCsvImportJobLauncher(jobLauncher, preferenceCsvImportJob, meterRegistry);
        var file = new MockMultipartFile("file", "preferencias.csv", "text/csv", "header\n".getBytes());

        when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenThrow(new IllegalStateException("boom"));

        assertThrows(IllegalStateException.class, () -> launcher.launch(file));
        assertEquals(1.0, metricCount(meterRegistry, PreferenceCsvImportJobLauncher.BATCH_IMPORT_ATTEMPT_METRIC));
        assertEquals(1.0, metricCount(meterRegistry, PreferenceCsvImportJobLauncher.BATCH_IMPORT_FAILURE_METRIC));
    }

    private double metricCount(SimpleMeterRegistry meterRegistry, String metricName) {
        return meterRegistry.counter(metricName, "job.name", "preferenceCsvImportJob").count();
    }
}
