package edu.rutmiit.demo.cinemacore.service;

import edu.rutmiit.demo.cinemaapicontract.enums.TicketStatus;
import edu.rutmiit.demo.cinemaapicontract.exception.ResourceNotFoundException;
import edu.rutmiit.demo.cinemacore.entity.BookingEntity;
import edu.rutmiit.demo.cinemacore.entity.HallEntity;
import edu.rutmiit.demo.cinemacore.entity.MovieEntity;
import edu.rutmiit.demo.cinemacore.entity.SeatEntity;
import edu.rutmiit.demo.cinemacore.entity.ShowEntity;
import edu.rutmiit.demo.cinemacore.entity.TicketEntity;
import edu.rutmiit.demo.cinemacore.integration.DomainEventPublisher;
import edu.rutmiit.demo.cinemacore.repository.HallRepository;
import edu.rutmiit.demo.cinemacore.repository.MovieRepository;
import edu.rutmiit.demo.cinemacore.repository.SeatRepository;
import edu.rutmiit.demo.cinemacore.repository.ShowRepository;
import edu.rutmiit.demo.cinemacore.repository.TicketRepository;
import edu.rutmiit.demo.cinemacore.repository.base.PageSlice;
import edu.rutmiit.demo.cinemaeventscontract.TicketEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static edu.rutmiit.demo.cinemaeventscontract.RoutingKeys.TICKET_CREATED;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final HallRepository hallRepository;
    private final SeatRepository seatRepository;
    private final DomainEventPublisher eventPublisher;

    public TicketEntity findById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket with id=" + id + " not found"));
    }

    public Optional<TicketEntity> findOptionalByBookingId(Long bookingId) {
        return ticketRepository.findByBookingId(bookingId);
    }

    public TicketEntity findByBookingId(Long bookingId) {
        return ticketRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket for booking id=" + bookingId + " not found"));
    }

    public PageSlice<TicketEntity> findAll(Long bookingId, Long customerId, Long showId, int page, int size) {
        return ticketRepository.search(bookingId, customerId, showId, page, size);
    }

    @Transactional
    public TicketEntity createForBooking(BookingEntity booking) {
        return ticketRepository.findByBookingId(booking.getId()).orElseGet(() -> {
            TicketEntity ticket = new TicketEntity();
            ticket.setBookingId(booking.getId());
            ticket.setTicketNumber("CIN-" + booking.getId());
            ticket.setQrCode("PENDING");
            ticket.setStatus(TicketStatus.ACTIVE);

            TicketEntity saved = ticketRepository.save(ticket);
            saved.setQrCode(buildQrPayload(saved, booking));
            ticketRepository.update(saved);

            ShowEntity show = showRepository.findById(booking.getShowId())
                    .orElseThrow(() -> new ResourceNotFoundException("Show with id=" + booking.getShowId() + " not found"));
            MovieEntity movie = movieRepository.findById(show.getMovieId())
                    .orElseThrow(() -> new ResourceNotFoundException("Movie with id=" + show.getMovieId() + " not found"));
            HallEntity hall = hallRepository.findById(show.getHallId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hall with id=" + show.getHallId() + " not found"));
            SeatEntity seat = seatRepository.findById(booking.getSeatId())
                    .orElseThrow(() -> new ResourceNotFoundException("Seat with id=" + booking.getSeatId() + " not found"));

            eventPublisher.publish(
                    TICKET_CREATED,
                    new TicketEvent.Created(
                            saved.getId(),
                            booking.getId(),
                            saved.getTicketNumber(),
                            booking.getCustomerEmail(),
                            saved.getQrCode(),

                            movie.getId(),
                            movie.getTitle(),
                            movie.getGenre(),
                            movie.getDurationMinutes(),

                            show.getId(),
                            show.getStartTime(),
                            show.getEndTime(),

                            hall.getName(),
                            seat.getRowNumber(),
                            seat.getSeatNumber(),

                            booking.getFinalPrice(),
                            booking.getCurrency()
                    )
            );
            return saved;
        });
    }

    private String buildQrPayload(TicketEntity ticket, BookingEntity booking) {
        return "CINEMA-TICKET|ticketId=%d|bookingId=%d|ticketNumber=%s|showId=%d|seatId=%d"
                .formatted(
                        ticket.getId(),
                        booking.getId(),
                        ticket.getTicketNumber(),
                        booking.getShowId(),
                        booking.getSeatId()
                );
    }

    @Transactional
    public void invalidate(TicketEntity ticket) {
        ticket.setStatus(TicketStatus.INVALID);
        ticketRepository.update(ticket);
    }
}
