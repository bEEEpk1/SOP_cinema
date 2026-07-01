package edu.rutmiit.demo.cinemaeventscontract;

import java.math.BigDecimal;
import java.time.OffsetDateTime;


public sealed interface LoyaltyEvent {

    record PointsEarned(
            Long customerId,
            String customerEmail,
            Long bookingId,
            BigDecimal finalPrice,
            String currency,
            Integer earnedPoints,
            Integer currentPointsBalance,

            String movieTitle,
            OffsetDateTime showStartTime,
            String hallName,
            Integer rowNumber,
            Integer seatNumber
    ) implements LoyaltyEvent {}

    record PointsRollbackApplied(
            Long customerId,
            String customerEmail,
            Long bookingId,
            Integer restoredSpentPoints,
            Integer deductedEarnedPoints,
            Integer currentPointsBalance,

            String movieTitle,
            OffsetDateTime showStartTime,
            String hallName,
            Integer rowNumber,
            Integer seatNumber
    ) implements LoyaltyEvent {}
}
