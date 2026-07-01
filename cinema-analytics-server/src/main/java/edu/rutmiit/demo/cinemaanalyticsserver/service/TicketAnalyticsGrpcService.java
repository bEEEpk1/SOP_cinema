package edu.rutmiit.demo.cinemaanalyticsserver.service;

import edu.rutmiit.demo.cinemagrpc.analytics.EnrichTicketRequest;
import edu.rutmiit.demo.cinemagrpc.analytics.EnrichTicketResponse;
import edu.rutmiit.demo.cinemagrpc.analytics.RecommendedMovie;
import edu.rutmiit.demo.cinemagrpc.analytics.TicketAnalyticsServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Slf4j
@Service
public class TicketAnalyticsGrpcService extends TicketAnalyticsServiceGrpc.TicketAnalyticsServiceImplBase {

    @Override
    public void enrichTicket(
            EnrichTicketRequest request,
            StreamObserver<EnrichTicketResponse> responseObserver
    ) {
        String seatInfo = "Ряд " + request.getRowNumber() + ", место " + request.getSeatNumber();
        String showTimeInfo = formatShowTime(request.getShowStartTime(), request.getShowEndTime());
        List<RecommendedMovie> recommendations = selectRecommendations(request.getCandidateMoviesList(), request.getMovieId());

        EnrichTicketResponse response = EnrichTicketResponse.newBuilder()
                .setTicketId(request.getTicketId())
                .setBookingId(request.getBookingId())
                .setTicketNumber(request.getTicketNumber())
                .setCustomerEmail(request.getCustomerEmail())

                .setMovieId(request.getMovieId())
                .setMovieTitle(request.getMovieTitle())
                .setMovieGenre(request.getMovieGenre())
                .setMovieDurationMinutes(request.getMovieDurationMinutes())

                .setShowId(request.getShowId())
                .setShowStartTime(request.getShowStartTime())
                .setShowEndTime(request.getShowEndTime())
                .setShowTimeInfo(showTimeInfo)

                .setHallName(request.getHallName())
                .setSeatInfo(seatInfo)
                .addAllRecommendedMovies(recommendations)

                .setEntryInstruction("Покажите QR-код билета на входе в зал.")
                .setRefundDeadlineInfo("Возврат возможен не позднее чем за 45 минут до начала сеанса.")
                .build();

        log.info(
                "ticket enriched: ticketId={} bookingId={} movie='{}' genre={} duration={} candidateMovies={} selectedRecommendations={}",
                request.getTicketId(),
                request.getBookingId(),
                request.getMovieTitle(),
                request.getMovieGenre(),
                request.getMovieDurationMinutes(),
                request.getCandidateMoviesCount(),
                recommendations.size()
        );

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private String formatShowTime(String startTime, String endTime) {
        try {
            OffsetDateTime start = OffsetDateTime.parse(startTime);
            OffsetDateTime end = OffsetDateTime.parse(endTime);
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            return dateFormatter.format(start) + "–" + timeFormatter.format(end);
        } catch (Exception ignored) {
            return startTime == null || startTime.isBlank() ? "Время сеанса не указано" : startTime;
        }
    }

    private List<RecommendedMovie> selectRecommendations(List<RecommendedMovie> candidates, long currentMovieId) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        return candidates.stream()
                .filter(movie -> movie.getMovieId() != currentMovieId)
                .limit(3)
                .toList();
    }
}
