package edu.rutmiit.demo.auditservice.model;

import java.util.List;

public record AuditResponse(
        int totalEntries,
        int showing,
        List<AuditEntry> entries
) {
}
