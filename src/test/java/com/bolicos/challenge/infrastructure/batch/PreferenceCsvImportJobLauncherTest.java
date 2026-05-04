package com.bolicos.challenge.infrastructure.batch;

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
        var launcher = new PreferenceCsvImportJobLauncher(jobLauncher, preferenceCsvImportJob);
        var file = new MockMultipartFile(
            "file",
            "preferencias.csv",
            "text/csv",
            "preferenciaCanalComunicacao,email,tipo,verificado\nEMAIL,a@b.com,PESSOAL,false\n".getBytes()
        );
        var execution = new JobExecution(1L);

        when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenReturn(execution);

        assertEquals(execution, launcher.launch(file));
        assertEquals("preferenceCsvImportJob", launcher.jobName());
    }

    @Test
    void deveLancarExcecaoQuandoJobFalharAoIniciar() throws Exception {
        var launcher = new PreferenceCsvImportJobLauncher(jobLauncher, preferenceCsvImportJob);
        var file = new MockMultipartFile("file", "preferencias.csv", "text/csv", "header\n".getBytes());

        when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenThrow(new IllegalStateException("boom"));

        assertThrows(IllegalStateException.class, () -> launcher.launch(file));
    }
}
