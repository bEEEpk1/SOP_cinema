package edu.rutmiit.demo.notificationservice.model;

import java.util.List;

public record NotificationHistoryResponse(
        int totalEntries,
        int showing,
        int activeConnections,
        List<NotificationMessage> entries
) {
}
