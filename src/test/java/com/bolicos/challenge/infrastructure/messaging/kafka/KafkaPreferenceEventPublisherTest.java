package com.bolicos.challenge.infrastructure.messaging.kafka;

import com.bolicos.challenge.application.event.PreferenceChangedEvent;
import com.bolicos.challenge.application.event.PreferenceEventType;
import com.bolicos.challenge.application.model.AuditMetadata;
import com.bolicos.challenge.application.model.CommunicationPreferenceView;
import com.bolicos.challenge.config.observability.HttpRequestMdcFilter;
import com.bolicos.challenge.domain.model.CommunicationChannel;
import com.bolicos.challenge.infrastructure.messaging.dto.PreferenceChangedEventPayload;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaPreferenceEventPublisherTest {

    @Mock
    private KafkaTemplate<String, PreferenceChangedEventPayload> kafkaTemplate;

    @Test
    void devePublicarEventoComHeaders() {
        var publisher = publisher();
        var event = event();
        MDC.put(HttpRequestMdcFilter.CORRELATION_ID_MDC_KEY, "corr-123");
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.failedFuture(new RuntimeException("fail")));

        try {
            publisher.publish(event);
        } finally {
            MDC.clear();
        }

        var captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());

        ProducerRecord<?, ?> record = captor.getValue();
        assertEquals("communication-preference-events", record.topic());
        assertEquals(event.preference().id().toString(), record.key());
        assertNotNull(record.headers().lastHeader(KafkaPreferenceEventPublisher.EVENT_ID_HEADER));
        assertNotNull(record.headers().lastHeader(KafkaPreferenceEventPublisher.EVENT_TYPE_HEADER));
        assertNotNull(record.headers().lastHeader(HttpRequestMdcFilter.CORRELATION_ID_HEADER));
    }

    @Test
    void deveTratarFalhaSincronaAoEnviar() {
        var publisher = publisher();
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenThrow(new IllegalStateException("boom"));

        publisher.publish(event());

        verify(kafkaTemplate).send(any(ProducerRecord.class));
    }

    private KafkaPreferenceEventPublisher publisher() {
        var publisher = new KafkaPreferenceEventPublisher(kafkaTemplate, new SimpleMeterRegistry());
        ReflectionTestUtils.setField(publisher, "preferenceEventsTopic", "communication-preference-events");

        return publisher;
    }

    private PreferenceChangedEvent event() {
        return PreferenceChangedEvent.of(PreferenceEventType.PREFERENCE_CREATED, new CommunicationPreferenceView(
            UUID.randomUUID(),
            UUID.randomUUID(),
            CommunicationChannel.EMAIL,
            List.of(),
            new AuditMetadata(LocalDateTime.now(), LocalDateTime.now(), "system", "system")
        ));
    }
}
