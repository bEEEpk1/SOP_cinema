package edu.rutmiit.demo.cinemaeventscontract;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public sealed interface BookingEvent {

    record Created(
            Long bookingId,
            Long showId,
            Long seatId,
            Long customerId,
            String customerEmail,
            OffsetDateTime reservedUntil,

            String movieTitle,
            OffsetDateTime showStartTime,
            OffsetDateTime showEndTime,
            String hallName,
            Integer rowNumber,
            Integer seatNumber,
            BigDecimal finalPrice,
            String currency
    ) implements BookingEvent {}

    record Paid(
            Long bookingId,
            Long showId,
            Long seatId,
            Long customerId,
            String customerEmail,
            Boolean customerRegistered,
            BigDecimal finalPrice,
            String currency,
            String paymentReference,
            Integer loyaltyPointsUsed,

            String movieTitle,
            OffsetDateTime showStartTime,
            OffsetDateTime showEndTime,
            String hallName,
            Integer rowNumber,
            Integer seatNumber
    ) implements BookingEvent {}

    record Expired(
            Long bookingId,
            Long showId,
            Long seatId,
            Long customerId,

            String movieTitle,
            OffsetDateTime showStartTime,
            String hallName,
            Integer rowNumber,
            Integer seatNumber
    ) implements BookingEvent {}

    record Cancelled(
            Long bookingId,
            Long showId,
            Long seatId,
            Long customerId,
            String previousStatus,

            String movieTitle,
            OffsetDateTime showStartTime,
            String hallName,
            Integer rowNumber,
            Integer seatNumber
    ) implements BookingEvent {}
}
