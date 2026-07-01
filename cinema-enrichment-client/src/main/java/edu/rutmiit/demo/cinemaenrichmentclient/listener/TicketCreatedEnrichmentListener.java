package edu.rutmiit.demo.cinemaenrichmentclient.listener;

import edu.rutmiit.demo.cinemaapicontract.dto.MovieRecommendationResponse;
import edu.rutmiit.demo.cinemaenrichmentclient.client.CoreMovieCatalogClient;
import edu.rutmiit.demo.cinemaenrichmentclient.config.RabbitMqEnrichmentConfig;
import edu.rutmiit.demo.cinemaenrichmentclient.publisher.EnrichmentEventPublisher;
import edu.rutmiit.demo.cinemaeventscontract.EventMetadata;
import edu.rutmiit.demo.cinemaeventscontract.RoutingKeys;
import edu.rutmiit.demo.cinemaeventscontract.TicketEvent;
import edu.rutmiit.demo.cinemagrpc.analytics.EnrichTicketRequest;
import edu.rutmiit.demo.cinemagrpc.analytics.EnrichTicketResponse;
import edu.rutmiit.demo.cinemagrpc.analytics.RecommendedMovie;
import edu.rutmiit.demo.cinemagrpc.analytics.TicketAnalyticsServiceGrpc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.OffsetDateTime;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class TicketCreatedEnrichmentListener {

    private static final int RECOMMENDATIONS_LIMIT = 3;

    private final JsonMapper jsonMapper;
    private final TicketAnalyticsServiceGrpc.TicketAnalyticsServiceBlockingStub analyticsStub;
    private final CoreMovieCatalogClient movieCatalogClient;
    private final EnrichmentEventPublisher eventPublisher;

    @RabbitListener(queues = RabbitMqEnrichmentConfig.TICKET_ENRICHMENT_QUEUE)
    public void onTicketCreated(Message message) {
        try {
            JsonNode root = jsonMapper.readTree(message.getBody());
            EventMetadata metadata = jsonMapper.treeToValue(root.get("metadata"), EventMetadata.class);
            TicketEvent.Created ticketCreated = jsonMapper.treeToValue(root.get("payload"), TicketEvent.Created.class);

            log.info("received ticket.created for enrichment: eventId={} ticketId={} bookingId={} movie='{}' genre={}",
                    metadata.eventId(),
                    ticketCreated.ticketId(),
                    ticketCreated.bookingId(),
                    ticketCreated.movieTitle(),
                    ticketCreated.movieGenre());

            List<RecommendedMovie> candidateMovies = movieCatalogClient
                    .findRecommendationsByGenre(ticketCreated.movieGenre(), ticketCreated.movieId(), RECOMMENDATIONS_LIMIT)
                    .stream()
                    .map(this::toGrpcRecommendedMovie)
                    .toList();

            EnrichTicketRequest request = EnrichTicketRequest.newBuilder()
                    .setTicketId(ticketCreated.ticketId())
                    .setBookingId(ticketCreated.bookingId())
                    .setTicketNumber(ticketCreated.ticketNumber())
                    .setCustomerEmail(ticketCreated.customerEmail())

                    .setMovieId(ticketCreated.movieId())
                    .setMovieTitle(ticketCreated.movieTitle())
                    .setMovieGenre(ticketCreated.movieGenre())
                    .setMovieDurationMinutes(ticketCreated.movieDurationMinutes())

                    .setShowId(ticketCreated.showId())
                    .setShowStartTime(ticketCreated.showStartTime().toString())
                    .setShowEndTime(ticketCreated.showEndTime().toString())

                    .setHallName(ticketCreated.hallName())
                    .setRowNumber(ticketCreated.rowNumber())
                    .setSeatNumber(ticketCreated.seatNumber())
                    .addAllCandidateMovies(candidateMovies)
                    .build();

            EnrichTicketResponse response = analyticsStub.enrichTicket(request);

            List<TicketEvent.RecommendedMovie> recommendedMovies = response.getRecommendedMoviesList()
                    .stream()
                    .map(movie -> new TicketEvent.RecommendedMovie(
                            movie.getMovieId(),
                            movie.getTitle(),
                            movie.getGenre(),
                            movie.getDurationMinutes()
                    ))
                    .toList();

            TicketEvent.Enriched enriched = new TicketEvent.Enriched(
                    response.getTicketId(),
                    response.getBookingId(),
                    response.getTicketNumber(),
                    response.getCustomerEmail(),
                    ticketCreated.qrCode(),

                    response.getMovieId(),
                    response.getMovieTitle(),
                    response.getMovieGenre(),
                    response.getMovieDurationMinutes(),

                    response.getShowId(),
                    OffsetDateTime.parse(response.getShowStartTime()),
                    OffsetDateTime.parse(response.getShowEndTime()),
                    response.getShowTimeInfo(),

                    response.getHallName(),
                    response.getSeatInfo(),

                    recommendedMovies,

                    response.getEntryInstruction(),
                    response.getRefundDeadlineInfo()
            );

            eventPublisher.publish(RoutingKeys.TICKET_ENRICHED, enriched);
        } catch (Exception e) {
            log.error("failed to enrich ticket.created event: {}", e.getMessage(), e);
            throw new RuntimeException("Could not enrich ticket.created event", e);
        }
    }

    private RecommendedMovie toGrpcRecommendedMovie(MovieRecommendationResponse movie) {
        return RecommendedMovie.newBuilder()
                .setMovieId(movie.id())
                .setTitle(movie.title())
                .setGenre(movie.genre())
                .setDurationMinutes(movie.durationMinutes())
                .build();
    }
}
