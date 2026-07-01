package edu.rutmiit.demo.cinemaeventscontract;

import java.time.OffsetDateTime;


public sealed interface WaitlistEvent {

    record UserNotified(
            Long waitlistEntryId,
            Long showId,
            Long seatId,
            Long customerId,
            String customerEmail,

            String movieTitle,
            OffsetDateTime showStartTime,
            OffsetDateTime showEndTime,
            String hallName,
            Integer rowNumber,
            Integer seatNumber
    ) implements WaitlistEvent {}
}
