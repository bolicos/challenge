package com.bolicos.challenge.infrastructure.batch;

import com.bolicos.challenge.config.BatchConfiguration;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
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

    public static final String BATCH_IMPORT_ATTEMPT_METRIC = "challenge.batch.preference.csv.import.attempt";
    public static final String BATCH_IMPORT_SUCCESS_METRIC = "challenge.batch.preference.csv.import.success";
    public static final String BATCH_IMPORT_FAILURE_METRIC = "challenge.batch.preference.csv.import.failure";
    public static final String BATCH_IMPORT_DURATION_METRIC = "challenge.batch.preference.csv.import.duration";

    private final JobLauncher jobLauncher;
    private final Job preferenceCsvImportJob;
    private final MeterRegistry meterRegistry;

    public JobExecution launch(MultipartFile file) {
        Timer.Sample sample = Timer.start(meterRegistry);
        increment(BATCH_IMPORT_ATTEMPT_METRIC);

        try {
            Path tempFile = Files.createTempFile("preference-import-", ".csv");
            file.transferTo(tempFile);

            var parameters = new JobParametersBuilder()
                .addString("filePath", tempFile.toAbsolutePath().toString())
                .addLong("startedAt", System.currentTimeMillis())
                .toJobParameters();

            JobExecution execution = jobLauncher.run(preferenceCsvImportJob, parameters);
            if (BatchStatus.COMPLETED.equals(execution.getStatus())) {
                increment(BATCH_IMPORT_SUCCESS_METRIC);
            } else {
                increment(BATCH_IMPORT_FAILURE_METRIC);
            }

            return execution;
        } catch (Exception ex) {
            increment(BATCH_IMPORT_FAILURE_METRIC);
            throw new IllegalStateException("Erro ao iniciar batch de importação CSV", ex);
        } finally {
            sample.stop(Timer.builder(BATCH_IMPORT_DURATION_METRIC)
                .tag("job.name", jobName())
                .register(meterRegistry));
        }
    }

    public String jobName() {
        return BatchConfiguration.PREFERENCE_CSV_IMPORT_JOB;
    }

    private void increment(String metricName) {
        meterRegistry.counter(metricName, "job.name", jobName()).increment();
    }
}
