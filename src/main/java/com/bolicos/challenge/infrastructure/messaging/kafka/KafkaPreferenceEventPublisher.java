package com.bolicos.challenge.infrastructure.messaging.kafka;

import com.bolicos.challenge.application.event.PreferenceChangedEvent;
import com.bolicos.challenge.application.port.out.PreferenceEventPublisher;
import com.bolicos.challenge.config.observability.HttpRequestMdcFilter;
import com.bolicos.challenge.infrastructure.messaging.dto.PreferenceChangedEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPreferenceEventPublisher implements PreferenceEventPublisher {

    public static final String EVENT_ID_HEADER = "eventId";
    public static final String EVENT_TYPE_HEADER = "eventType";

    private final KafkaTemplate<String, PreferenceChangedEventPayload> kafkaTemplate;

    @Value("${challenge.kafka.topics.preference-events:communication-preference-events}")
    private String preferenceEventsTopic;

    @Override
    public void publish(PreferenceChangedEvent event) {
        String preferenceId = event.preference().id().toString();
        String correlationId = MDC.get(HttpRequestMdcFilter.CORRELATION_ID_MDC_KEY);
        var payload = new PreferenceChangedEventPayload(
            event.eventId(),
            event.eventType().name(),
            event.occurredAt(),
            event.preference()
        );

        ProducerRecord<String, PreferenceChangedEventPayload> record = new ProducerRecord<>(
            preferenceEventsTopic,
            preferenceId,
            payload
        );

        addHeader(record, EVENT_ID_HEADER, event.eventId().toString());
        addHeader(record, EVENT_TYPE_HEADER, event.eventType().name());
        addHeader(record, HttpRequestMdcFilter.CORRELATION_ID_HEADER, correlationId);

        log.info(
            "Publishing preference event: eventId={}, eventType={}, preferenceId={}, topic={}, correlationId={}",
            event.eventId(),
            event.eventType(),
            preferenceId,
            preferenceEventsTopic,
            correlationId
        );

        try {
            kafkaTemplate.send(record).whenComplete((result, exception) -> {
                if (exception != null) {
                    log.error(
                        "Failed to publish preference event: eventId={}, eventType={}, preferenceId={}, topic={}, correlationId={}",
                        event.eventId(),
                        event.eventType(),
                        preferenceId,
                        preferenceEventsTopic,
                        correlationId,
                        exception
                    );
                    return;
                }

                log.info(
                    "Published preference event: eventId={}, eventType={}, preferenceId={}, topic={}, partition={}, offset={}, correlationId={}",
                    event.eventId(),
                    event.eventType(),
                    preferenceId,
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    correlationId
                );
            });
        } catch (Exception ex) {
            log.error(
                "Failed to schedule preference event publish: eventId={}, eventType={}, preferenceId={}, topic={}, correlationId={}, exceptionClass={}",
                event.eventId(),
                event.eventType(),
                preferenceId,
                preferenceEventsTopic,
                correlationId,
                ex.getClass().getName(),
                ex
            );
        }
    }

    private void addHeader(
        ProducerRecord<String, PreferenceChangedEventPayload> record,
        String name,
        String value
    ) {
        if (value == null || value.isBlank()) {
            return;
        }

        record.headers().add(name, value.getBytes(StandardCharsets.UTF_8));
    }
}
