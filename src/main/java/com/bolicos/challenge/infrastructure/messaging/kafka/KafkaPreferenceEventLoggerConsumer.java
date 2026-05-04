package com.bolicos.challenge.infrastructure.messaging.kafka;

import com.bolicos.challenge.config.observability.HttpRequestMdcFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class KafkaPreferenceEventLoggerConsumer {

    private static final String EVENT_ID_MDC_KEY = "eventId";
    private static final String EVENT_TYPE_MDC_KEY = "eventType";
    private static final String TOPIC_MDC_KEY = "topic";
    private static final String PARTITION_MDC_KEY = "partition";
    private static final String OFFSET_MDC_KEY = "offset";

    @KafkaListener(
        topics = "${challenge.kafka.topics.preference-events}",
        groupId = "${challenge.kafka.consumers.preference-event-logger.group-id:challenge-preference-event-logger}",
        autoStartup = "${challenge.kafka.consumers.preference-event-logger.enabled:false}"
    )
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String eventId = headerValue(record, KafkaPreferenceEventPublisher.EVENT_ID_HEADER);
        String eventType = headerValue(record, KafkaPreferenceEventPublisher.EVENT_TYPE_HEADER);
        String correlationId = headerValue(record, HttpRequestMdcFilter.CORRELATION_ID_HEADER);

        try {
            fillMdc(record, eventId, eventType, correlationId);

            log.info(
                "Consumed preference event: topic={}, partition={}, offset={}, key={}, eventId={}, eventType={}, correlationId={}, payload={}",
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                eventId,
                eventType,
                correlationId,
                record.value()
            );

            acknowledgment.acknowledge();
        } catch (Exception ex) {
            log.error(
                "Failed to consume preference event: topic={}, partition={}, offset={}, key={}, exceptionClass={}",
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                ex.getClass().getName(),
                ex
            );
            throw ex;
        } finally {
            MDC.clear();
        }
    }

    private void fillMdc(
        ConsumerRecord<String, String> record,
        String eventId,
        String eventType,
        String correlationId
    ) {
        putIfPresent(HttpRequestMdcFilter.CORRELATION_ID_MDC_KEY, correlationId);
        putIfPresent(EVENT_ID_MDC_KEY, eventId);
        putIfPresent(EVENT_TYPE_MDC_KEY, eventType);
        MDC.put(HttpRequestMdcFilter.SOURCE_MDC_KEY, "kafka");
        MDC.put(TOPIC_MDC_KEY, record.topic());
        MDC.put(PARTITION_MDC_KEY, String.valueOf(record.partition()));
        MDC.put(OFFSET_MDC_KEY, String.valueOf(record.offset()));
    }

    private void putIfPresent(String key, String value) {
        if (value != null && !value.isBlank()) {
            MDC.put(key, value);
        }
    }

    private String headerValue(ConsumerRecord<String, String> record, String headerName) {
        Header header = record.headers().lastHeader(headerName);
        if (header == null || header.value() == null) {
            return null;
        }

        return new String(header.value(), StandardCharsets.UTF_8);
    }
}
