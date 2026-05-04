package com.bolicos.challenge.infrastructure.messaging.kafka;

import com.bolicos.challenge.application.event.PreferenceChangedEvent;
import com.bolicos.challenge.application.event.PreferenceEventType;
import com.bolicos.challenge.application.model.AuditMetadata;
import com.bolicos.challenge.application.model.CommunicationPreferenceView;
import com.bolicos.challenge.domain.model.CommunicationChannel;
import com.bolicos.challenge.infrastructure.messaging.dto.PreferenceChangedEventPayload;
import com.bolicos.challenge.shared.constants.KafkaKeys;
import com.bolicos.challenge.shared.constants.MdcKeys;
import com.bolicos.challenge.shared.constants.MetricsKeys;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
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
import static org.mockito.Mockito.doAnswer;
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
        MDC.put(MdcKeys.CORRELATION_ID, "corr-123");
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
        assertEquals(event.preference().customerId().toString(), record.key());
        assertNotNull(record.headers().lastHeader(KafkaKeys.EVENT_ID_HEADER));
        assertNotNull(record.headers().lastHeader(KafkaKeys.EVENT_TYPE_HEADER));
        assertNotNull(record.headers().lastHeader(KafkaKeys.CUSTOMER_ID_HEADER));
        assertNotNull(record.headers().lastHeader(KafkaKeys.CORRELATION_ID_HEADER));
    }

    @Test
    void deveTratarFalhaSincronaAoEnviar() {
        var publisher = publisher();
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenThrow(new IllegalStateException("boom"));

        publisher.publish(event());

        verify(kafkaTemplate).send(any(ProducerRecord.class));
    }

    @Test
    void deveRegistrarMetricaDeSucessoQuandoPublicarEvento() {
        var meterRegistry = new SimpleMeterRegistry();
        var publisher = publisher(meterRegistry);
        var event = event();

        doAnswer(invocation -> {
            ProducerRecord<String, PreferenceChangedEventPayload> record = invocation.getArgument(0);
            var metadata = new RecordMetadata(
                new TopicPartition(record.topic(), 0),
                0L,
                1,
                System.currentTimeMillis(),
                0,
                0
            );
            return CompletableFuture.completedFuture(new org.springframework.kafka.support.SendResult<>(record, metadata));
        }).when(kafkaTemplate).send(any(ProducerRecord.class));

        publisher.publish(event);

        assertEquals(
            1.0,
            meterRegistry.counter(
                MetricsKeys.KAFKA_PREFERENCE_EVENTS_PUBLISH_SUCCESS,
                MetricsKeys.EVENT_TYPE_TAG,
                event.eventType().name()
            ).count()
        );
    }

    private KafkaPreferenceEventPublisher publisher() {
        return publisher(new SimpleMeterRegistry());
    }

    private KafkaPreferenceEventPublisher publisher(SimpleMeterRegistry meterRegistry) {
        var publisher = new KafkaPreferenceEventPublisher(kafkaTemplate, meterRegistry);
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
