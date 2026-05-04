package com.bolicos.challenge.infrastructure.messaging.kafka;

import com.bolicos.challenge.config.observability.HttpRequestMdcFilter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.kafka.support.Acknowledgment;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class KafkaPreferenceEventLoggerConsumerTest {

    private final KafkaPreferenceEventLoggerConsumer consumer = new KafkaPreferenceEventLoggerConsumer();

    @Test
    void deveConsumirEventoComHeaders() {
        var record = new ConsumerRecord<>("communication-preference-events", 0, 1L, "key", "{\"eventId\":\"1\"}");
        var acknowledgment = mock(Acknowledgment.class);
        record.headers().add(KafkaPreferenceEventPublisher.EVENT_ID_HEADER, "event-1".getBytes(StandardCharsets.UTF_8));
        record.headers().add(KafkaPreferenceEventPublisher.EVENT_TYPE_HEADER, "PREFERENCE_CREATED".getBytes(StandardCharsets.UTF_8));
        record.headers().add(HttpRequestMdcFilter.CORRELATION_ID_HEADER, "corr-1".getBytes(StandardCharsets.UTF_8));

        consumer.consume(record, acknowledgment);

        verify(acknowledgment).acknowledge();
        org.junit.jupiter.api.Assertions.assertNull(MDC.get(HttpRequestMdcFilter.CORRELATION_ID_MDC_KEY));
    }

    @Test
    void deveConsumirEventoSemHeaders() {
        var record = new ConsumerRecord<>("communication-preference-events", 0, 1L, "key", "{}");
        var acknowledgment = mock(Acknowledgment.class);

        consumer.consume(record, acknowledgment);

        verify(acknowledgment).acknowledge();
    }

    @Test
    void deveLogarERelancarErroQuandoFalharAoConsumirEvento() {
        var record = new ConsumerRecord<>("communication-preference-events", 0, 1L, "key", "{}");
        var acknowledgment = mock(Acknowledgment.class);
        doThrow(new IllegalStateException("ack fail")).when(acknowledgment).acknowledge();

        assertThrows(IllegalStateException.class, () -> consumer.consume(record, acknowledgment));

        verify(acknowledgment).acknowledge();
        org.junit.jupiter.api.Assertions.assertNull(MDC.get(HttpRequestMdcFilter.SOURCE_MDC_KEY));
    }
}
