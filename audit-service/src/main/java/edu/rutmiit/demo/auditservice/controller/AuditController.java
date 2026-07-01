package edu.rutmiit.demo.auditservice.controller;

import edu.rutmiit.demo.auditservice.model.AuditEntry;
import edu.rutmiit.demo.auditservice.model.AuditResponse;
import edu.rutmiit.demo.auditservice.storage.AuditStorage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class AuditController {

    private final AuditStorage storage;

    public AuditController(AuditStorage storage) {
        this.storage = storage;
    }

    @GetMapping("/api/audit")
    public AuditResponse latest(@RequestParam(defaultValue = "50") int limit) {
        List<AuditEntry> entries = storage.findLatest(limit);
        return new AuditResponse(storage.count(), entries.size(), entries);
    }

    @GetMapping("/api/audit/count")
    public Map<String, Integer> count() {
        return Map.of("count", storage.count());
    }


    @GetMapping("/events/audit")
    public List<AuditEntry> legacyLatest(@RequestParam(defaultValue = "50") int limit) {
        return storage.findLatest(limit);
    }

    @GetMapping("/events/audit/count")
    public Map<String, Integer> legacyCount() {
        return count();
    }
}
