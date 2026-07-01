package edu.rutmiit.demo.notificationservice.service;

import edu.rutmiit.demo.cinemaeventscontract.BookingEvent;
import edu.rutmiit.demo.cinemaeventscontract.EventMetadata;
import edu.rutmiit.demo.cinemaeventscontract.LoyaltyEvent;
import edu.rutmiit.demo.cinemaeventscontract.RoutingKeys;
import edu.rutmiit.demo.cinemaeventscontract.SeatEvent;
import edu.rutmiit.demo.cinemaeventscontract.TicketEvent;
import edu.rutmiit.demo.cinemaeventscontract.WaitlistEvent;
import edu.rutmiit.demo.notificationservice.model.NotificationMessage;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Component
public class NotificationMessageFactory {

    private static final DateTimeFormatter SHORT_TIME = DateTimeFormatter.ofPattern("dd.MM HH:mm");

    private final JsonMapper jsonMapper;

    public NotificationMessageFactory(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public NotificationMessage build(EventMetadata metadata, JsonNode payloadNode) throws Exception {
        String title = buildTitle(metadata.eventType());
        String level = buildLevel(metadata.eventType());
        String icon = buildIcon(metadata.eventType());
        String message = buildMessage(metadata.eventType(), payloadNode);

        return new NotificationMessage(
                "NOTIFICATION",
                metadata.eventId(),
                metadata.eventType(),
                title,
                message,
                level,
                icon,
                metadata.source(),
                metadata.timestamp(),
                Instant.now(),
                null
        );
    }

    private String buildTitle(String eventType) {
        return switch (eventType) {
            case RoutingKeys.BOOKING_CREATED -> "Бронь создана";
            case RoutingKeys.BOOKING_PAID -> "Бронь оплачена";
            case RoutingKeys.BOOKING_CANCELLED -> "Бронь отменена";
            case RoutingKeys.BOOKING_EXPIRED -> "Бронь истекла";
            case RoutingKeys.TICKET_CREATED -> "Билет создан";
            case RoutingKeys.TICKET_ENRICHED -> "Билет дополнен";
            case RoutingKeys.SEAT_RELEASED -> "Место освобождено";
            case RoutingKeys.WAITLIST_USER_NOTIFIED -> "Пользователь уведомлён";
            case RoutingKeys.LOYALTY_POINTS_EARNED -> "Бонусы начислены";
            case RoutingKeys.LOYALTY_POINTS_ROLLBACK_APPLIED -> "Бонусы пересчитаны";
            default -> "Событие";
        };
    }

    private String buildLevel(String eventType) {
        return switch (eventType) {
            case RoutingKeys.BOOKING_PAID, RoutingKeys.TICKET_CREATED, RoutingKeys.TICKET_ENRICHED, RoutingKeys.LOYALTY_POINTS_EARNED -> "success";
            case RoutingKeys.LOYALTY_POINTS_ROLLBACK_APPLIED, RoutingKeys.BOOKING_CANCELLED, RoutingKeys.BOOKING_EXPIRED -> "warning";
            case RoutingKeys.WAITLIST_USER_NOTIFIED, RoutingKeys.SEAT_RELEASED -> "info";
            default -> "info";
        };
    }

    private String buildIcon(String eventType) {
        return switch (eventType) {
            case RoutingKeys.BOOKING_CREATED -> "calendar-plus";
            case RoutingKeys.BOOKING_PAID -> "credit-card";
            case RoutingKeys.BOOKING_CANCELLED -> "x-circle";
            case RoutingKeys.BOOKING_EXPIRED -> "clock";
            case RoutingKeys.TICKET_CREATED -> "ticket";
            case RoutingKeys.TICKET_ENRICHED -> "sparkles";
            case RoutingKeys.SEAT_RELEASED -> "armchair";
            case RoutingKeys.WAITLIST_USER_NOTIFIED -> "bell";
            case RoutingKeys.LOYALTY_POINTS_EARNED -> "badge-plus";
            case RoutingKeys.LOYALTY_POINTS_ROLLBACK_APPLIED -> "rotate-ccw";
            default -> "message-square";
        };
    }

    private String buildMessage(String eventType, JsonNode payloadNode) throws Exception {
        return switch (eventType) {
            case RoutingKeys.BOOKING_CREATED -> {
                BookingEvent.Created event = jsonMapper.treeToValue(payloadNode, BookingEvent.Created.class);
                yield "%s · %s · %s · %s. Цена: %s %s. Оплатить до %s."
                        .formatted(event.movieTitle(), formatTime(event.showStartTime()), event.hallName(), place(event.rowNumber(), event.seatNumber()), event.finalPrice(), event.currency(), formatTime(event.reservedUntil()));
            }
            case RoutingKeys.BOOKING_PAID -> {
                BookingEvent.Paid event = jsonMapper.treeToValue(payloadNode, BookingEvent.Paid.class);
                String loyalty = event.loyaltyPointsUsed() != null && event.loyaltyPointsUsed() > 0
                        ? " Списано бонусов: %d.".formatted(event.loyaltyPointsUsed())
                        : "";
                yield "%s · %s · %s · %s. Оплачено: %s %s.%s"
                        .formatted(event.movieTitle(), formatTime(event.showStartTime()), event.hallName(), place(event.rowNumber(), event.seatNumber()), event.finalPrice(), event.currency(), loyalty);
            }
            case RoutingKeys.BOOKING_CANCELLED -> {
                BookingEvent.Cancelled event = jsonMapper.treeToValue(payloadNode, BookingEvent.Cancelled.class);
                String title = "PAID".equals(event.previousStatus()) ? "Билет возвращён" : "Бронь отменена";
                yield "%s: %s · %s · %s · %s. Место освобождено."
                        .formatted(title, event.movieTitle(), formatTime(event.showStartTime()), event.hallName(), place(event.rowNumber(), event.seatNumber()));
            }
            case RoutingKeys.BOOKING_EXPIRED -> {
                BookingEvent.Expired event = jsonMapper.treeToValue(payloadNode, BookingEvent.Expired.class);
                yield "%s · %s · %s · %s. Оплата не была выполнена вовремя."
                        .formatted(event.movieTitle(), formatTime(event.showStartTime()), event.hallName(), place(event.rowNumber(), event.seatNumber()));
            }
            case RoutingKeys.TICKET_CREATED -> {
                TicketEvent.Created event = jsonMapper.treeToValue(payloadNode, TicketEvent.Created.class);
                yield "%s · %s · %s · %s. Билет %s создан, QR доступен в письме и в разделе брони."
                        .formatted(event.movieTitle(), formatTime(event.showStartTime()), event.hallName(), place(event.rowNumber(), event.seatNumber()), event.ticketNumber());
            }
            case RoutingKeys.SEAT_RELEASED -> {
                SeatEvent.Released event = jsonMapper.treeToValue(payloadNode, SeatEvent.Released.class);
                yield "%s · %s · %s · %s снова доступно."
                        .formatted(event.movieTitle(), formatTime(event.showStartTime()), event.hallName(), place(event.rowNumber(), event.seatNumber()));
            }
            case RoutingKeys.WAITLIST_USER_NOTIFIED -> {
                WaitlistEvent.UserNotified event = jsonMapper.treeToValue(payloadNode, WaitlistEvent.UserNotified.class);
                String seatText = event.rowNumber() == null || event.seatNumber() == null
                        ? "появились свободные места"
                        : place(event.rowNumber(), event.seatNumber()) + " снова доступно";
                yield "%s получил уведомление: %s · %s · %s · %s."
                        .formatted(event.customerEmail(), event.movieTitle(), formatTime(event.showStartTime()), event.hallName(), seatText);
            }
            case RoutingKeys.TICKET_ENRICHED -> {
                TicketEvent.Enriched event = jsonMapper.treeToValue(payloadNode, TicketEvent.Enriched.class);
                String recommendations = event.recommendedMovies() == null || event.recommendedMovies().isEmpty()
                        ? "похожих фильмов в текущем каталоге пока нет"
                        : event.recommendedMovies().stream()
                                .map(movie -> "%s (%d мин.)".formatted(movie.title(), movie.durationMinutes()))
                                .collect(Collectors.joining(", "));

                yield "%s · %s · %d мин. · %s · %s. Рекомендации по жанру: %s."
                        .formatted(event.movieTitle(), event.movieGenre(), event.movieDurationMinutes(), event.showTimeInfo(), event.seatInfo(), recommendations);
            }
            case RoutingKeys.LOYALTY_POINTS_EARNED -> {
                LoyaltyEvent.PointsEarned event = jsonMapper.treeToValue(payloadNode, LoyaltyEvent.PointsEarned.class);
                yield "%s · %s · %s · %s. Начислено: %d бонусов. Баланс: %d."
                        .formatted(event.movieTitle(), formatTime(event.showStartTime()), event.hallName(), place(event.rowNumber(), event.seatNumber()), event.earnedPoints(), event.currentPointsBalance());
            }
            case RoutingKeys.LOYALTY_POINTS_ROLLBACK_APPLIED -> {
                LoyaltyEvent.PointsRollbackApplied event = jsonMapper.treeToValue(payloadNode, LoyaltyEvent.PointsRollbackApplied.class);
                yield "%s · %s · %s · %s. Возврат бонусов: +%d ранее списанных, −%d начисленных. Баланс: %d."
                        .formatted(event.movieTitle(), formatTime(event.showStartTime()), event.hallName(), place(event.rowNumber(), event.seatNumber()), event.restoredSpentPoints(), event.deductedEarnedPoints(), event.currentPointsBalance());
            }
            default -> "Получено событие: " + eventType;
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
