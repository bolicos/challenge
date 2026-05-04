package com.bolicos.challenge.config;

import com.bolicos.challenge.application.port.in.PreferenceBatchUseCase;
import com.bolicos.challenge.domain.model.CommunicationChannel;
import com.bolicos.challenge.domain.model.CommunicationPreference;
import com.bolicos.challenge.domain.model.EmailType;
import com.bolicos.challenge.domain.model.PreferenceEmail;
import com.bolicos.challenge.infrastructure.batch.dto.PreferenceCsvRecord;
import com.bolicos.challenge.shared.constants.BatchKeys;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Configuration
public class BatchConfiguration {

    @Bean
    public Job preferenceCsvImportJob(
        JobRepository jobRepository,
        Step preferenceCsvImportStep
    ) {
        return new JobBuilder(BatchKeys.PREFERENCE_CSV_IMPORT_JOB, jobRepository)
            .start(preferenceCsvImportStep)
            .build();
    }

    @Bean
    public Step preferenceCsvImportStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        FlatFileItemReader<PreferenceCsvRecord> preferenceCsvReader,
        ItemProcessor<PreferenceCsvRecord, CommunicationPreference> preferenceCsvProcessor,
        ItemWriter<CommunicationPreference> preferenceCsvWriter
    ) {
        return new StepBuilder(BatchKeys.PREFERENCE_CSV_IMPORT_STEP, jobRepository)
            .<PreferenceCsvRecord, CommunicationPreference>chunk(100, transactionManager)
            .reader(preferenceCsvReader)
            .processor(preferenceCsvProcessor)
            .writer(preferenceCsvWriter)
            .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<PreferenceCsvRecord> preferenceCsvReader(
        @Value("#{jobParameters['filePath']}") String filePath
    ) {
        var reader = new FlatFileItemReader<PreferenceCsvRecord>();
        reader.setName("preferenceCsvReader");
        reader.setResource(new FileSystemResource(filePath));
        reader.setLinesToSkip(1);
        reader.setLineMapper(preferenceCsvLineMapper());

        return reader;
    }

    @Bean
    public ItemProcessor<PreferenceCsvRecord, CommunicationPreference> preferenceCsvProcessor() {
        return record -> {
            var preference = new CommunicationPreference();
            if (record.getCustomerId() != null && !record.getCustomerId().isBlank()) {
                preference.setCustomerId(UUID.fromString(record.getCustomerId()));
            }
            preference.setCommunicationChannel(CommunicationChannel.valueOf(record.getPreferenciaCanalComunicacao()));

            var email = new PreferenceEmail();
            email.setEmail(record.getEmail());
            email.setType(EmailType.valueOf(record.getTipo()));
            email.setVerified(record.getVerificado());
            preference.replaceEmails(List.of(email));

            return preference;
        };
    }

    @Bean
    public ItemWriter<CommunicationPreference> preferenceCsvWriter(PreferenceBatchUseCase preferenceBatchUseCase) {
        return chunk -> preferenceBatchUseCase.importBatch(new ArrayList<>(chunk.getItems()));
    }

    private DefaultLineMapper<PreferenceCsvRecord> preferenceCsvLineMapper() {
        var tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("customerId", "preferenciaCanalComunicacao", "email", "tipo", "verificado");
        tokenizer.setStrict(true);

        var fieldSetMapper = new BeanWrapperFieldSetMapper<PreferenceCsvRecord>();
        fieldSetMapper.setTargetType(PreferenceCsvRecord.class);

        var lineMapper = new DefaultLineMapper<PreferenceCsvRecord>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }
}
