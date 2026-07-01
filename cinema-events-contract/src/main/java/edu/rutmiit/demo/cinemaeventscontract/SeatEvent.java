package edu.rutmiit.demo.cinemaeventscontract;

import java.time.OffsetDateTime;


public sealed interface SeatEvent {

    record Released(
            Long showId,
            Long seatId,
            Long bookingId,

            String movieTitle,
            OffsetDateTime showStartTime,
            String hallName,
            Integer rowNumber,
            Integer seatNumber
    ) implements SeatEvent {}
}
