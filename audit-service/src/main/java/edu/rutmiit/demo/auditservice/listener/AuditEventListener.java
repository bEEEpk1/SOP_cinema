package edu.rutmiit.demo.auditservice.listener;

import edu.rutmiit.demo.auditservice.config.RabbitMqAuditConfig;
import edu.rutmiit.demo.auditservice.model.AuditEntry;
import edu.rutmiit.demo.auditservice.storage.AuditStorage;
import edu.rutmiit.demo.cinemaeventscontract.BookingEvent;
import edu.rutmiit.demo.cinemaeventscontract.EventMetadata;
import edu.rutmiit.demo.cinemaeventscontract.LoyaltyEvent;
import edu.rutmiit.demo.cinemaeventscontract.RoutingKeys;
import edu.rutmiit.demo.cinemaeventscontract.SeatEvent;
import edu.rutmiit.demo.cinemaeventscontract.TicketEvent;
import edu.rutmiit.demo.cinemaeventscontract.WaitlistEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private static final DateTimeFormatter SHORT_TIME = DateTimeFormatter.ofPattern("dd.MM HH:mm");

    private final AuditStorage auditStorage;
    private final JsonMapper jsonMapper;

    @RabbitListener(queues = RabbitMqAuditConfig.AUDIT_QUEUE)
    public void handleEvent(Message message) {
        try {
            JsonNode root = jsonMapper.readTree(message.getBody());
            EventMetadata metadata = jsonMapper.treeToValue(root.get("metadata"), EventMetadata.class);

            if (auditStorage.isDuplicate(metadata.eventId())) {
                log.warn("duplicate audit event skipped: eventId={} eventType={}", metadata.eventId(), metadata.eventType());
                return;
            }

            String description = buildDescription(metadata.eventType(), root.get("payload"));
            AuditEntry saved = auditStorage.save(new AuditEntry(
                    0,
                    metadata.eventId(),
                    metadata.eventType(),
                    metadata.source(),
                    metadata.timestamp(),
                    Instant.now(),
                    description
            ));

            log.info("[AUDIT #{}] {} | {}", saved.sequenceNumber(), saved.eventType(), saved.description());
        } catch (Exception e) {
            log.error("failed to audit cinema domain event: {}", e.getMessage(), e);
            throw new RuntimeException("Could not audit cinema domain event", e);
        }
    }

    private String buildDescription(String eventType, JsonNode payloadNode) throws Exception {
        return switch (eventType) {
            case RoutingKeys.BOOKING_CREATED -> {
                BookingEvent.Created event = jsonMapper.treeToValue(payloadNode, BookingEvent.Created.class);
                yield "Бронь создана: %s · %s · %s · %s · %s %s · оплатить до %s"
                        .formatted(event.movieTitle(), formatTime(event.showStartTime()), event.hallName(), place(event.rowNumber(), event.seatNumber()), event.finalPrice(), event.currency(), formatTime(event.reservedUntil()));
            }
            case RoutingKeys.BOOKING_PAID -> {
                BookingEvent.Paid event = jsonMapper.treeToValue(payloadNode, BookingEvent.Paid.class);
                String loyalty = event.loyaltyPointsUsed() != null && event.loyaltyPointsUsed() > 0
                        ? " · списано бонусов: %d".formatted(event.loyaltyPointsUsed())
                        : "";
                yield "Бронь оплачена: %s · %s · %s · %s · оплачено %s %s%s"
                        .formatted(event.movieTitle(), formatTime(event.showStartTime()), event.hallName(), place(event.rowNumber(), event.seatNumber()), event.finalPrice(), event.currency(), loyalty);
            }
            case RoutingKeys.BOOKING_EXPIRED -> {
                BookingEvent.Expired event = jsonMapper.treeToValue(payloadNode, BookingEvent.Expired.class);
                yield "Бронь истекла: %s · %s · %s · %s · оплата не выполнена вовремя"
                        .formatted(event.movieTitle(), formatTime(event.showStartTime()), event.hallName(), place(event.rowNumber(), event.seatNumber()));
            }
            case RoutingKeys.BOOKING_CANCELLED -> {
                BookingEvent.Cancelled event = jsonMapper.treeToValue(payloadNode, BookingEvent.Cancelled.class);
                String action = "PAID".equals(event.previousStatus()) ? "Билет возвращён" : "Бронь отменена";
                yield "%s: %s · %s · %s · %s · место освобождено"
                        .formatted(action, event.movieTitle(), formatTime(event.showStartTime()), event.hallName(), place(event.rowNumber(), event.seatNumber()));
            }
            case RoutingKeys.SEAT_RELEASED -> {
                SeatEvent.Released event = jsonMapper.treeToValue(payloadNode, SeatEvent.Released.class);
                yield "Место освобождено: %s · %s · %s · %s"
                        .formatted(event.movieTitle(), formatTime(event.showStartTime()), event.hallName(), place(event.rowNumber(), event.seatNumber()));
            }
            case RoutingKeys.TICKET_CREATED -> {
                TicketEvent.Created event = jsonMapper.treeToValue(payloadNode, TicketEvent.Created.class);
                yield "Билет создан: %s · %s · %s · %s · %s · %s"
                        .formatted(event.ticketNumber(), event.movieTitle(), formatTime(event.showStartTime()), event.hallName(), place(event.rowNumber(), event.seatNumber()), event.customerEmail());
            }
            case RoutingKeys.WAITLIST_USER_NOTIFIED -> {
                WaitlistEvent.UserNotified event = jsonMapper.treeToValue(payloadNode, WaitlistEvent.UserNotified.class);
                String seatText = event.rowNumber() == null || event.seatNumber() == null
                        ? "появились свободные места"
                        : place(event.rowNumber(), event.seatNumber()) + " снова доступно";
                yield "Waitlist-уведомление: %s · %s · %s · %s · %s"
                        .formatted(event.customerEmail(), event.movieTitle(), formatTime(event.showStartTime()), event.hallName(), seatText);
            }
            case RoutingKeys.TICKET_ENRICHED -> {
                TicketEvent.Enriched event = jsonMapper.treeToValue(payloadNode, TicketEvent.Enriched.class);
                String recommendations = event.recommendedMovies() == null || event.recommendedMovies().isEmpty()
                        ? "похожих фильмов в текущем каталоге пока нет"
                        : event.recommendedMovies().stream()
                                .map(movie -> "%s (%d мин.)".formatted(movie.title(), movie.durationMinutes()))
                                .collect(Collectors.joining(", "));

                yield "Билет дополнен: %s · %s · %d мин. · %s · %s · рекомендации: %s"
                        .formatted(event.movieTitle(), event.movieGenre(), event.movieDurationMinutes(), event.showTimeInfo(), event.seatInfo(), recommendations);
            }
            case RoutingKeys.LOYALTY_POINTS_EARNED -> {
                LoyaltyEvent.PointsEarned event = jsonMapper.treeToValue(payloadNode, LoyaltyEvent.PointsEarned.class);
                yield "Бонусы начислены: %s · %s · %s · %s · +%d · баланс %d"
                        .formatted(event.movieTitle(), formatTime(event.showStartTime()), event.hallName(), place(event.rowNumber(), event.seatNumber()), event.earnedPoints(), event.currentPointsBalance());
            }
            case RoutingKeys.LOYALTY_POINTS_ROLLBACK_APPLIED -> {
                LoyaltyEvent.PointsRollbackApplied event = jsonMapper.treeToValue(payloadNode, LoyaltyEvent.PointsRollbackApplied.class);
                yield "Бонусы пересчитаны после возврата: %s · %s · %s · %s · +%d списанных, −%d начисленных · баланс %d"
                        .formatted(event.movieTitle(), formatTime(event.showStartTime()), event.hallName(), place(event.rowNumber(), event.seatNumber()), event.restoredSpentPoints(), event.deductedEarnedPoints(), event.currentPointsBalance());
            }
            default -> "Событие обработано: " + eventType;
        };
    }

    private String place(Integer rowNumber, Integer seatNumber) {
        if (rowNumber == null || seatNumber == null) return "место не указано";
        return "ряд %d, место %d".formatted(rowNumber, seatNumber);
    }

    private String formatTime(OffsetDateTime value) {
        if (value == null) return "—";
        return SHORT_TIME.format(value);
    }
}
