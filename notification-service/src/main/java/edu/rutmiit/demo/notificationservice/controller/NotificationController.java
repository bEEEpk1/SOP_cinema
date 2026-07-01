package edu.rutmiit.demo.notificationservice.controller;

import edu.rutmiit.demo.notificationservice.model.NotificationHistoryResponse;
import edu.rutmiit.demo.notificationservice.model.NotificationMessage;
import edu.rutmiit.demo.notificationservice.storage.NotificationStorage;
import edu.rutmiit.demo.notificationservice.websocket.NotificationWebSocketHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class NotificationController {

    private final NotificationStorage storage;
    private final NotificationWebSocketHandler webSocketHandler;

    public NotificationController(NotificationStorage storage, NotificationWebSocketHandler webSocketHandler) {
        this.storage = storage;
        this.webSocketHandler = webSocketHandler;
    }

    @GetMapping("/api/notifications")
    public NotificationHistoryResponse latest(@RequestParam(defaultValue = "50") int limit) {
        List<NotificationMessage> entries = storage.findLatest(limit);
        return new NotificationHistoryResponse(
                storage.count(),
                entries.size(),
                webSocketHandler.activeConnections(),
                entries
        );
    }

    @GetMapping("/api/notifications/count")
    public Map<String, Integer> count() {
        return Map.of("count", storage.count());
    }

    @GetMapping("/api/notifications/connections")
    public Map<String, Integer> activeConnections() {
        return Map.of("activeConnections", webSocketHandler.activeConnections());
    }
}
