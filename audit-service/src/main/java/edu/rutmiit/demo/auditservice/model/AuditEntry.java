package edu.rutmiit.demo.auditservice.model;

import java.time.Instant;


public record AuditEntry(
        long sequenceNumber,
        String eventId,
        String eventType,
        String source,
        Instant occurredAt,
        Instant processedAt,
        String description
) {
}
