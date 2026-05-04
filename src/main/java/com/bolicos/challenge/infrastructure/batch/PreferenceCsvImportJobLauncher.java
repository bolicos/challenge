package com.bolicos.challenge.infrastructure.batch;

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
import com.bolicos.challenge.shared.constants.BatchKeys;
import com.bolicos.challenge.shared.constants.MetricsKeys;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class PreferenceCsvImportJobLauncher {

    private final JobLauncher jobLauncher;
    private final Job preferenceCsvImportJob;
    private final MeterRegistry meterRegistry;

    public JobExecution launch(MultipartFile file) {
        Timer.Sample sample = Timer.start(meterRegistry);
        increment(MetricsKeys.BATCH_PREFERENCE_CSV_IMPORT_ATTEMPT);

        try {
            Path tempFile = Files.createTempFile("preference-import-", ".csv");
            file.transferTo(tempFile);

            var parameters = new JobParametersBuilder()
                .addString("filePath", tempFile.toAbsolutePath().toString())
                .addLong("startedAt", System.currentTimeMillis())
                .toJobParameters();

            JobExecution execution = jobLauncher.run(preferenceCsvImportJob, parameters);
            if (BatchStatus.COMPLETED.equals(execution.getStatus())) {
                increment(MetricsKeys.BATCH_PREFERENCE_CSV_IMPORT_SUCCESS);
            } else {
                increment(MetricsKeys.BATCH_PREFERENCE_CSV_IMPORT_FAILURE);
            }

            return execution;
        } catch (Exception ex) {
            increment(MetricsKeys.BATCH_PREFERENCE_CSV_IMPORT_FAILURE);
            throw new IllegalStateException("Erro ao iniciar batch de importação CSV", ex);
        } finally {
            sample.stop(Timer.builder(MetricsKeys.BATCH_PREFERENCE_CSV_IMPORT_DURATION)
                .tag(MetricsKeys.JOB_NAME_TAG, jobName())
                .register(meterRegistry));
        }
    }

    public String jobName() {
        return BatchKeys.PREFERENCE_CSV_IMPORT_JOB;
    }

    private void increment(String metricName) {
        meterRegistry.counter(metricName, MetricsKeys.JOB_NAME_TAG, jobName()).increment();
    }
}
