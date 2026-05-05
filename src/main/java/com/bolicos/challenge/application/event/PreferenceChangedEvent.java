package com.bolicos.challenge.application.event;

import com.bolicos.challenge.application.model.CommunicationPreferenceView;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public record PreferenceChangedEvent(
    UUID eventId,
    PreferenceEventType eventType,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    OffsetDateTime occurredAt,
    CommunicationPreferenceView preference
) {
    public static PreferenceChangedEvent of(PreferenceEventType eventType, CommunicationPreferenceView preference) {
        return new PreferenceChangedEvent(UUID.randomUUID(), eventType, OffsetDateTime.now(ZoneOffset.UTC), preference);
    }
}
