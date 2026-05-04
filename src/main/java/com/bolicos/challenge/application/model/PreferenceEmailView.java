package com.bolicos.challenge.application.model;

import com.bolicos.challenge.domain.model.EmailType;

public record PreferenceEmailView(
    Long id,
    String email,
    EmailType type,
    Boolean verified,
    AuditMetadata audit
) {
}
