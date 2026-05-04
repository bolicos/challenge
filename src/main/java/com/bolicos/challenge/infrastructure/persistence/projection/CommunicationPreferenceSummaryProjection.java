package com.bolicos.challenge.infrastructure.persistence.projection;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CommunicationPreferenceSummaryProjection {

    UUID getId();

    UUID getCustomerId();

    String getCommunicationChannel();

    Long getEmailCount();

    LocalDateTime getDataCriacao();

    LocalDateTime getDataAtualizacao();
}
