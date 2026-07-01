package edu.rutmiit.demo.auditservice.storage;

import edu.rutmiit.demo.auditservice.model.AuditEntry;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;


@Component
public class AuditStorage {

    private final ArrayDeque<AuditEntry> entries = new ArrayDeque<>();
    private final Set<String> processedEventIds = new HashSet<>();
    private final AtomicLong sequence = new AtomicLong();

    public synchronized boolean isDuplicate(String eventId) {
        return processedEventIds.contains(eventId);
    }

    public synchronized AuditEntry save(AuditEntry entry) {
        AuditEntry numbered = new AuditEntry(
                sequence.incrementAndGet(),
                entry.eventId(),
                entry.eventType(),
                entry.source(),
                entry.occurredAt(),
                entry.processedAt(),
                entry.description()
        );
        processedEventIds.add(numbered.eventId());
        entries.addFirst(numbered);
        return numbered;
    }

    public synchronized List<AuditEntry> findLatest(int limit) {
        return entries.stream()
                .limit(limit)
                .toList();
    }

    public synchronized int count() {
        return entries.size();
    }
}
