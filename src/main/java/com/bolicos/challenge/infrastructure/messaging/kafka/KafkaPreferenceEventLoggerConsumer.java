package com.bolicos.challenge.infrastructure.messaging.kafka;

import com.bolicos.challenge.config.observability.HttpRequestMdcFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class KafkaPreferenceEventLoggerConsumer {

    @KafkaListener(
        topics = "${challenge.kafka.topics.preference-events}",
        groupId = "${challenge.kafka.consumers.preference-event-logger.group-id:challenge-preference-event-logger}",
        autoStartup = "${challenge.kafka.consumers.preference-event-logger.enabled:false}"
    )
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            log.info(
                "Consumed preference event: topic={}, partition={}, offset={}, key={}, eventId={}, eventType={}, correlationId={}, payload={}",
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                headerValue(record, KafkaPreferenceEventPublisher.EVENT_ID_HEADER),
                headerValue(record, KafkaPreferenceEventPublisher.EVENT_TYPE_HEADER),
                headerValue(record, HttpRequestMdcFilter.CORRELATION_ID_HEADER),
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
