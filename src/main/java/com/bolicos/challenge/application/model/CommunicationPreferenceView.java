package com.bolicos.challenge.application.model;

import com.bolicos.challenge.domain.model.CommunicationChannel;

import java.util.List;
import java.util.UUID;

public record CommunicationPreferenceView(
    UUID id,
    UUID customerId,
    CommunicationChannel communicationChannel,
    List<PreferenceEmailView> emails,
    AuditMetadata audit
) {
}
