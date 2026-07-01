package edu.rutmiit.demo.notificationservice.storage;

import edu.rutmiit.demo.notificationservice.model.NotificationMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Component
public class NotificationStorage {

    private static final int MAX_SIZE = 100;

    private final ArrayDeque<NotificationMessage> messages = new ArrayDeque<>();
    private final Set<String> processedEventIds = new HashSet<>();

    public synchronized boolean isDuplicate(String eventId) {
        return processedEventIds.contains(eventId);
    }

    public synchronized void save(NotificationMessage message) {
        if (message.eventId() != null) {
            processedEventIds.add(message.eventId());
        }
        messages.addFirst(message);
        while (messages.size() > MAX_SIZE) {
            messages.removeLast();
        }
    }

    public synchronized List<NotificationMessage> findLatest(int limit) {
        return messages.stream()
                .limit(limit)
                .toList();
    }

    public synchronized int count() {
        return messages.size();
    }
}
