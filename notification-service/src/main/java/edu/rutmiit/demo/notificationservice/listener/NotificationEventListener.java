package edu.rutmiit.demo.notificationservice.listener;

import edu.rutmiit.demo.cinemaeventscontract.EventMetadata;
import edu.rutmiit.demo.cinemaeventscontract.RoutingKeys;
import edu.rutmiit.demo.cinemaeventscontract.TicketEvent;
import edu.rutmiit.demo.cinemaeventscontract.WaitlistEvent;
import edu.rutmiit.demo.notificationservice.config.RabbitMqNotificationConfig;
import edu.rutmiit.demo.notificationservice.model.NotificationMessage;
import edu.rutmiit.demo.notificationservice.service.EmailNotificationService;
import edu.rutmiit.demo.notificationservice.service.NotificationMessageFactory;
import edu.rutmiit.demo.notificationservice.storage.NotificationStorage;
import edu.rutmiit.demo.notificationservice.websocket.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;


@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final JsonMapper jsonMapper;
    private final NotificationMessageFactory notificationFactory;
    private final NotificationStorage notificationStorage;
    private final NotificationWebSocketHandler webSocketHandler;
    private final EmailNotificationService emailNotificationService;

    @RabbitListener(queues = RabbitMqNotificationConfig.NOTIFICATIONS_QUEUE)
    public void handleEvent(Message message) {
        try {
            JsonNode root = jsonMapper.readTree(message.getBody());
            EventMetadata metadata = jsonMapper.treeToValue(root.get("metadata"), EventMetadata.class);

            if (notificationStorage.isDuplicate(metadata.eventId())) {
                log.warn("duplicate notification event skipped: eventId={} eventType={}", metadata.eventId(), metadata.eventType());
                return;
            }

            JsonNode payloadNode = root.get("payload");
            sendEmailIfRequired(metadata.eventType(), payloadNode);

            NotificationMessage notification = notificationFactory.build(metadata, payloadNode);
            notificationStorage.save(notification);
            webSocketHandler.broadcast(notification);

            log.info("notification delivered: eventType={} eventId={} activeConnections={}",
                    metadata.eventType(), metadata.eventId(), webSocketHandler.activeConnections());
        } catch (Exception e) {
            log.error("failed to process notification event: {}", e.getMessage(), e);
            throw new RuntimeException("Could not process notification event", e);
        }
    }

    private void sendEmailIfRequired(String eventType, JsonNode payloadNode) throws Exception {
        switch (eventType) {
            case RoutingKeys.TICKET_CREATED -> {
                TicketEvent.Created event = jsonMapper.treeToValue(payloadNode, TicketEvent.Created.class);
                emailNotificationService.sendTicketCreated(event);
            }
            case RoutingKeys.WAITLIST_USER_NOTIFIED -> {
                WaitlistEvent.UserNotified event = jsonMapper.treeToValue(payloadNode, WaitlistEvent.UserNotified.class);
                emailNotificationService.sendWaitlistUserNotified(event);
            }
            default -> {
            }
        }
    }
}
