package com.bolicos.challenge.application.event;

import com.bolicos.challenge.application.model.CommunicationPreferenceView;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PreferenceChangedEvent(
    UUID eventId,
    PreferenceEventType eventType,
    OffsetDateTime occurredAt,
    CommunicationPreferenceView preference
) {
    public static PreferenceChangedEvent of(PreferenceEventType eventType, CommunicationPreferenceView preference) {
        return new PreferenceChangedEvent(UUID.randomUUID(), eventType, OffsetDateTime.now(), preference);
    }
}
