package com.bolicos.challenge.infrastructure.messaging.dto;

import com.bolicos.challenge.application.model.CommunicationPreferenceView;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PreferenceChangedEventPayload(
    UUID eventId,
    String eventType,
    OffsetDateTime occurredAt,
    CommunicationPreferenceView preference
) {
}
