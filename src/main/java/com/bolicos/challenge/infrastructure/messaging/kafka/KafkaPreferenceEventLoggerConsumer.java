package com.bolicos.challenge.infrastructure.messaging.kafka;

import com.bolicos.challenge.shared.constants.KafkaKeys;
import com.bolicos.challenge.shared.constants.MdcKeys;
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

    @KafkaListener(
        topics = KafkaKeys.PREFERENCE_EVENTS_TOPIC_PROPERTY,
        groupId = KafkaKeys.PREFERENCE_EVENT_LOGGER_GROUP_ID_PROPERTY,
        autoStartup = KafkaKeys.PREFERENCE_EVENT_LOGGER_ENABLED_PROPERTY
    )
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String eventId = headerValue(record, KafkaKeys.EVENT_ID_HEADER);
        String eventType = headerValue(record, KafkaKeys.EVENT_TYPE_HEADER);
        String correlationId = headerValue(record, KafkaKeys.CORRELATION_ID_HEADER);

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
        putIfPresent(MdcKeys.CORRELATION_ID, correlationId);
        putIfPresent(MdcKeys.EVENT_ID, eventId);
        putIfPresent(MdcKeys.EVENT_TYPE, eventType);
        MDC.put(MdcKeys.SOURCE, MdcKeys.KAFKA_SOURCE);
        MDC.put(MdcKeys.TOPIC, record.topic());
        MDC.put(MdcKeys.PARTITION, String.valueOf(record.partition()));
        MDC.put(MdcKeys.OFFSET, String.valueOf(record.offset()));
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
