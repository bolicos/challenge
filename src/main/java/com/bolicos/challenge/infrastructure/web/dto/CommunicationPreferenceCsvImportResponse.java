package com.bolicos.challenge.infrastructure.web.dto;

public record CommunicationPreferenceCsvImportResponse(
    Long jobExecutionId,
    String jobName,
    String status,
    String exitStatus
) {
}
