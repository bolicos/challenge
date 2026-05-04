package com.bolicos.challenge.infrastructure.batch;

import com.bolicos.challenge.config.BatchConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class PreferenceCsvImportJobLauncher {

    private final JobLauncher jobLauncher;
    private final Job preferenceCsvImportJob;

    public JobExecution launch(MultipartFile file) {
        try {
            Path tempFile = Files.createTempFile("preference-import-", ".csv");
            file.transferTo(tempFile);

            var parameters = new JobParametersBuilder()
                .addString("filePath", tempFile.toAbsolutePath().toString())
                .addLong("startedAt", System.currentTimeMillis())
                .toJobParameters();

            return jobLauncher.run(preferenceCsvImportJob, parameters);
        } catch (Exception ex) {
            throw new IllegalStateException("Erro ao iniciar batch de importação CSV", ex);
        }
    }

    public String jobName() {
        return BatchConfiguration.PREFERENCE_CSV_IMPORT_JOB;
    }
}
