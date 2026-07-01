package edu.rutmiit.demo.cinemaeventscontract;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;


public sealed interface TicketEvent {

    record Created(
            Long ticketId,
            Long bookingId,
            String ticketNumber,
            String customerEmail,
            String qrCode,

            Long movieId,
            String movieTitle,
            String movieGenre,
            Integer movieDurationMinutes,

            Long showId,
            OffsetDateTime showStartTime,
            OffsetDateTime showEndTime,

            String hallName,
            Integer rowNumber,
            Integer seatNumber,

            BigDecimal finalPrice,
            String currency
    ) implements TicketEvent {}

    /**
     * Recommended movie item used by ticket.enriched.
     * Recommendations are selected by the genre of the purchased ticket's movie.
     */
    record RecommendedMovie(
            Long movieId,
            String title,
            String genre,
            Integer durationMinutes
    ) {}

    /**
     * Asynchronous post-processing event produced after ticket.created.
     * It enriches the customer experience with useful movie/session context
     * and genre-based recommendations.
     */
    record Enriched(
            Long ticketId,
            Long bookingId,
            String ticketNumber,
            String customerEmail,
            String qrCode,

            Long movieId,
            String movieTitle,
            String movieGenre,
            Integer movieDurationMinutes,

            Long showId,
            OffsetDateTime showStartTime,
            OffsetDateTime showEndTime,
            String showTimeInfo,

            String hallName,
            String seatInfo,

            List<RecommendedMovie> recommendedMovies,

            String entryInstruction,
            String refundDeadlineInfo
    ) implements TicketEvent {}
}
