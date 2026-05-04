package com.bolicos.challenge.infrastructure.messaging.kafka;

import com.bolicos.challenge.application.event.PreferenceChangedEvent;
import com.bolicos.challenge.application.port.out.PreferenceEventPublisher;
import com.bolicos.challenge.infrastructure.messaging.dto.PreferenceChangedEventPayload;
import com.bolicos.challenge.shared.constants.KafkaKeys;
import com.bolicos.challenge.shared.constants.MdcKeys;
import com.bolicos.challenge.shared.constants.MetricsKeys;
import io.micrometer.core.instrument.MeterRegistry;
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

    private final KafkaTemplate<String, PreferenceChangedEventPayload> kafkaTemplate;
    private final MeterRegistry meterRegistry;

    @Value(KafkaKeys.PREFERENCE_EVENTS_TOPIC_PROPERTY_WITH_DEFAULT)
    private String preferenceEventsTopic;

    @Override
    public void publish(PreferenceChangedEvent event) {
        String preferenceId = event.preference().id().toString();
        String customerId = event.preference().customerId().toString();
        String correlationId = MDC.get(MdcKeys.CORRELATION_ID);
        incrementMetric(MetricsKeys.KAFKA_PREFERENCE_EVENTS_PUBLISH_ATTEMPT, event.eventType().name());
        var payload = new PreferenceChangedEventPayload(
            event.eventId(),
            event.eventType().name(),
            event.occurredAt(),
            event.preference()
        );

        ProducerRecord<String, PreferenceChangedEventPayload> record = new ProducerRecord<>(
            preferenceEventsTopic,
            customerId,
            payload
        );

        addHeader(record, KafkaKeys.EVENT_ID_HEADER, event.eventId().toString());
        addHeader(record, KafkaKeys.EVENT_TYPE_HEADER, event.eventType().name());
        addHeader(record, KafkaKeys.CUSTOMER_ID_HEADER, customerId);
        addHeader(record, KafkaKeys.CORRELATION_ID_HEADER, correlationId);

        log.info(
            "Publishing preference event: eventId={}, eventType={}, preferenceId={}, customerId={}, topic={}, correlationId={}",
            event.eventId(),
            event.eventType(),
            preferenceId,
            customerId,
            preferenceEventsTopic,
            correlationId
        );

        try {
            kafkaTemplate.send(record).whenComplete((result, exception) -> {
                if (exception != null) {
                    incrementMetric(MetricsKeys.KAFKA_PREFERENCE_EVENTS_PUBLISH_FAILURE, event.eventType().name());
                    log.error(
                        "Failed to publish preference event: eventId={}, eventType={}, preferenceId={}, customerId={}, topic={}, correlationId={}",
                        event.eventId(),
                        event.eventType(),
                        preferenceId,
                        customerId,
                        preferenceEventsTopic,
                        correlationId,
                        exception
                    );
                    return;
                }

                incrementMetric(MetricsKeys.KAFKA_PREFERENCE_EVENTS_PUBLISH_SUCCESS, event.eventType().name());
                log.info(
                    "Published preference event: eventId={}, eventType={}, preferenceId={}, customerId={}, topic={}, partition={}, offset={}, correlationId={}",
                    event.eventId(),
                    event.eventType(),
                    preferenceId,
                    customerId,
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    correlationId
                );
            });
        } catch (Exception ex) {
            incrementMetric(MetricsKeys.KAFKA_PREFERENCE_EVENTS_PUBLISH_FAILURE, event.eventType().name());
            log.error(
                "Failed to schedule preference event publish: eventId={}, eventType={}, preferenceId={}, customerId={}, topic={}, correlationId={}, exceptionClass={}",
                event.eventId(),
                event.eventType(),
                preferenceId,
                customerId,
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

    private void incrementMetric(String metricName, String eventType) {
        meterRegistry.counter(metricName, MetricsKeys.EVENT_TYPE_TAG, eventType).increment();
    }
}
