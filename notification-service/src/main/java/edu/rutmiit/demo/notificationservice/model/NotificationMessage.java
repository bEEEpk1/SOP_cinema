package edu.rutmiit.demo.notificationservice.model;

import java.time.Instant;


public record NotificationMessage(
        String type,
        String eventId,
        String eventType,
        String title,
        String message,
        String level,
        String icon,
        String source,
        Instant eventTimestamp,
        Instant receivedAt,
        Integer activeConnections
) {
    public static NotificationMessage connected(int activeConnections) {
        return new NotificationMessage(
                "CONNECTED",
                null,
                null,
                "Notification Service connected",
                "Realtime WebSocket-канал подключён.",
                "info",
                "plug",
                "notification-service",
                null,
                Instant.now(),
                activeConnections
        );
    }
}
