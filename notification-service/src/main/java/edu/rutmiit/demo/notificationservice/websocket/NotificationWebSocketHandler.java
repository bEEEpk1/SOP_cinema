package edu.rutmiit.demo.notificationservice.websocket;

import edu.rutmiit.demo.notificationservice.model.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final JsonMapper jsonMapper;

    public NotificationWebSocketHandler(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        pruneClosedSessions();
        log.info("WebSocket client connected: sessionId={}, activeConnections={}", session.getId(), activeConnections());
        broadcastConnectionStatus();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        pruneClosedSessions();
        log.info("WebSocket client disconnected: sessionId={}, status={}, activeConnections={}",
                session.getId(), status, activeConnections());
        broadcastConnectionStatus();
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        sessions.remove(session);
        closeQuietly(session);
        pruneClosedSessions();
        log.warn("WebSocket transport error: sessionId={}, reason={}, activeConnections={}",
                session.getId(), exception.getMessage(), activeConnections());
        broadcastConnectionStatus();
    }

    public void broadcast(NotificationMessage notification) {
        pruneClosedSessions();
        for (WebSocketSession session : sessions) {
            sendSafely(session, notification);
        }
    }

    public int activeConnections() {
        pruneClosedSessions();
        return sessions.size();
    }

    private void broadcastConnectionStatus() {
        pruneClosedSessions();
        NotificationMessage status = NotificationMessage.connected(activeConnections());
        for (WebSocketSession session : sessions) {
            sendSafely(session, status);
        }
    }

    private void sendSafely(WebSocketSession session, NotificationMessage message) {
        try {
            if (session == null || !session.isOpen()) {
                sessions.remove(session);
                return;
            }
            String payload = jsonMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(payload));
        } catch (Exception e) {
            sessions.remove(session);
            closeQuietly(session);
            log.warn("failed to send WebSocket notification to sessionId={}: {}", session.getId(), e.getMessage());
        }
    }

    private void pruneClosedSessions() {
        sessions.removeIf(session -> session == null || !session.isOpen());
    }

    private void closeQuietly(WebSocketSession session) {
        if (session == null) return;
        try {
            if (session.isOpen()) session.close();
        } catch (IOException ignored) {
        }
    }
}
