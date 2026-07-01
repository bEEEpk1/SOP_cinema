package edu.rutmiit.demo.cinemacore.service;

import edu.rutmiit.demo.cinemaapicontract.dto.BookingRequest;
import edu.rutmiit.demo.cinemaapicontract.dto.PatchBookingRequest;
import edu.rutmiit.demo.cinemaapicontract.enums.BookingStatus;
import edu.rutmiit.demo.cinemaapicontract.exception.BookingStateConflictException;
import edu.rutmiit.demo.cinemaapicontract.exception.ResourceNotFoundException;
import edu.rutmiit.demo.cinemaapicontract.exception.SeatAlreadyReservedException;
import edu.rutmiit.demo.cinemacore.entity.BookingEntity;
import edu.rutmiit.demo.cinemacore.entity.CustomerEntity;
import edu.rutmiit.demo.cinemacore.entity.HallEntity;
import edu.rutmiit.demo.cinemacore.entity.MovieEntity;
import edu.rutmiit.demo.cinemacore.entity.SeatEntity;
import edu.rutmiit.demo.cinemacore.entity.ShowEntity;
import edu.rutmiit.demo.cinemacore.integration.DomainEventPublisher;
import edu.rutmiit.demo.cinemacore.integration.LoyaltyClient;
import edu.rutmiit.demo.cinemacore.integration.PricingClient;
import edu.rutmiit.demo.cinemacore.repository.BookingRepository;
import edu.rutmiit.demo.cinemacore.repository.SeatRepository;
import edu.rutmiit.demo.cinemacore.repository.base.PageSlice;
import edu.rutmiit.demo.cinemaeventscontract.BookingEvent;
import edu.rutmiit.demo.cinemaeventscontract.LoyaltyEvent;
import edu.rutmiit.demo.cinemaeventscontract.SeatEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;

import static edu.rutmiit.demo.cinemaeventscontract.RoutingKeys.BOOKING_CANCELLED;
import static edu.rutmiit.demo.cinemaeventscontract.RoutingKeys.BOOKING_CREATED;
import static edu.rutmiit.demo.cinemaeventscontract.RoutingKeys.BOOKING_EXPIRED;
import static edu.rutmiit.demo.cinemaeventscontract.RoutingKeys.BOOKING_PAID;
import static edu.rutmiit.demo.cinemaeventscontract.RoutingKeys.LOYALTY_POINTS_ROLLBACK_APPLIED;
import static edu.rutmiit.demo.cinemaeventscontract.RoutingKeys.SEAT_RELEASED;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowService showService;
    private final HallService hallService;
    private final MovieService movieService;
    private final SeatRepository seatRepository;
    private final CustomerService customerService;
    private final TicketService ticketService;
    private final PricingClient pricingClient;
    private final LoyaltyClient loyaltyClient;
    private final DomainEventPublisher eventPublisher;
    private final WaitlistService waitlistService;

    @Value("${cinema.booking.reserve-minutes:10}")
    private long reserveMinutes;

    private static final Duration REFUND_DEADLINE_BEFORE_SHOW = Duration.ofMinutes(45);

    public BookingEntity findById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking with id=" + id + " not found"));
    }

    public PageSlice<BookingEntity> findAll(Long customerId, Long showId, BookingStatus status, int page, int size) {
        return bookingRepository.search(customerId, showId, status, page, size);
    }

    @Transactional
    public BookingEntity create(BookingRequest request) {
        ShowEntity show = showService.findById(request.showId());
        ensureShowAvailableForBooking(show);
        SeatEntity seat = seatRepository.findById(request.seatId())
                .orElseThrow(() -> new ResourceNotFoundException("Seat with id=" + request.seatId() + " not found"));
        CustomerEntity customer = customerService.findById(request.customerId());
        HallEntity hall = hallService.findById(show.getHallId());

        bookingRepository.findActiveByShowIdAndSeatId(request.showId(), request.seatId())
                .ifPresent(existing -> {
                    throw new SeatAlreadyReservedException("Seat already reserved for show");
                });

        long occupiedSeats = bookingRepository.countActiveByShowId(show.getId());
        BigDecimal priceBeforeLoyalty = pricingClient.calculateFinalPrice(show, seat, hall, customer, occupiedSeats);
        int loyaltyPointsUsed = resolveLoyaltyPoints(customer, request.loyaltyPointsUsed());
        BigDecimal finalPrice = applyLoyaltyDiscount(priceBeforeLoyalty, loyaltyPointsUsed);

        BookingEntity booking = new BookingEntity();
        booking.setShowId(request.showId());
        booking.setSeatId(request.seatId());
        booking.setCustomerId(request.customerId());
        booking.setCustomerEmail(request.customerEmail());
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setReservedUntil(OffsetDateTime.now().plusMinutes(reserveMinutes));
        booking.setFinalPrice(finalPrice);
        booking.setCurrency(show.getCurrency());
        booking.setLoyaltyPointsUsed(loyaltyPointsUsed);

        try {
            BookingEntity saved = bookingRepository.save(booking);
            bookingRepository.flush();
            publishBookingCreated(saved);
            return saved;
        } catch (DataIntegrityViolationException ex) {
            throw new SeatAlreadyReservedException("Seat already reserved for show");
        }
    }

    @Transactional
    public BookingEntity patch(Long id, PatchBookingRequest request) {
        BookingEntity booking = findById(id);

        if (request.customerEmail() != null) {
            booking.setCustomerEmail(request.customerEmail());
        }
        if (request.paymentReference() != null) {
            booking.setPaymentReference(request.paymentReference());
        }
        if (request.loyaltyPointsUsed() != null) {
            if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
                throw new BookingStateConflictException("Loyalty points can be changed only before payment");
            }
            CustomerEntity customer = customerService.findById(booking.getCustomerId());
            int normalizedPoints = resolveLoyaltyPoints(customer, request.loyaltyPointsUsed());
            BigDecimal priceBeforeLoyalty = recalculatePriceBeforeLoyalty(booking, customer);
            booking.setLoyaltyPointsUsed(normalizedPoints);
            booking.setFinalPrice(applyLoyaltyDiscount(priceBeforeLoyalty, normalizedPoints));
        }
        if (request.status() != null && request.status() != booking.getStatus()) {
            transitionStatus(booking, request.status());
        }

        return bookingRepository.update(booking);
    }

    @Transactional
    public void cancel(Long id) {
        BookingEntity booking = findById(id);
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT && booking.getStatus() != BookingStatus.PAID) {
            throw new BookingStateConflictException("Booking cannot be cancelled from status=" + booking.getStatus());
        }

        if (booking.getStatus() == BookingStatus.PAID) {
            ensureRefundAllowed(booking);
        }

        BookingStatus previousStatus = booking.getStatus();
        LoyaltyClient.RollbackResult rollbackResult = rollbackLoyaltyIfPaidRegistered(booking, previousStatus);
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.update(booking);

        invalidateTicketIfExists(booking.getId());
        publishBookingCancelled(booking, previousStatus);
        publishLoyaltyRollbackApplied(booking, rollbackResult);
        publishSeatReleased(booking);
        waitlistService.handleSeatReleased(booking.getShowId(), booking.getSeatId());
    }

    @Transactional
    public void expirePendingBookings() {
        for (BookingEntity booking : bookingRepository.findExpirable(OffsetDateTime.now())) {
            booking.setStatus(BookingStatus.EXPIRED);
            bookingRepository.update(booking);

            publishBookingExpired(booking);
            publishSeatReleased(booking);
            waitlistService.handleSeatReleased(booking.getShowId(), booking.getSeatId());
        }
    }

    private void transitionStatus(BookingEntity booking, BookingStatus target) {
        switch (booking.getStatus()) {
            case PENDING_PAYMENT -> handleFromPending(booking, target);
            case PAID -> {
                if (target != BookingStatus.CANCELLED) {
                    throw new BookingStateConflictException("Invalid transition from PAID to " + target);
                }
                ensureRefundAllowed(booking);
                BookingStatus previousStatus = booking.getStatus();
                LoyaltyClient.RollbackResult rollbackResult = rollbackLoyaltyIfPaidRegistered(booking, previousStatus);
                booking.setStatus(BookingStatus.CANCELLED);
                invalidateTicketIfExists(booking.getId());
                publishBookingCancelled(booking, previousStatus);
                publishLoyaltyRollbackApplied(booking, rollbackResult);
                publishSeatReleased(booking);
                waitlistService.handleSeatReleased(booking.getShowId(), booking.getSeatId());
            }
            default -> throw new BookingStateConflictException(
                    "Invalid transition from " + booking.getStatus() + " to " + target
            );
        }
    }

    private void handleFromPending(BookingEntity booking, BookingStatus target) {
        if (target == BookingStatus.PAID) {
            ensureShowNotStarted(booking);
            booking.setStatus(BookingStatus.PAID);
            CustomerEntity customer = customerService.findById(booking.getCustomerId());
            if (Boolean.TRUE.equals(customer.getRegistered())) {
                loyaltyClient.applyPointsOnPayment(booking.getCustomerId(), booking.getId(), booking.getLoyaltyPointsUsed());
            } else {
                booking.setLoyaltyPointsUsed(0);
            }
            ticketService.createForBooking(booking);
            publishBookingPaid(booking);
            return;
        }

        if (target == BookingStatus.CANCELLED) {
            BookingStatus previousStatus = booking.getStatus();
            booking.setStatus(BookingStatus.CANCELLED);
            publishBookingCancelled(booking, previousStatus);
            publishSeatReleased(booking);
            waitlistService.handleSeatReleased(booking.getShowId(), booking.getSeatId());
            return;
        }

        if (target == BookingStatus.EXPIRED) {
            booking.setStatus(BookingStatus.EXPIRED);
            publishBookingExpired(booking);
            publishSeatReleased(booking);
            waitlistService.handleSeatReleased(booking.getShowId(), booking.getSeatId());
            return;
        }

        throw new BookingStateConflictException("Invalid transition from PENDING_PAYMENT to " + target);
    }

    private int resolveLoyaltyPoints(CustomerEntity customer, Integer requestedPoints) {
        if (!Boolean.TRUE.equals(customer.getRegistered())) {
            return 0;
        }
        return loyaltyClient.normalizeRequestedPoints(customer.getId(), requestedPoints);
    }

    private BigDecimal applyLoyaltyDiscount(BigDecimal priceBeforeLoyalty, int loyaltyPointsUsed) {
        BigDecimal discount = BigDecimal.valueOf(Math.max(loyaltyPointsUsed, 0));
        BigDecimal amountToPay = priceBeforeLoyalty.subtract(discount);
        return amountToPay.max(BigDecimal.ZERO);
    }

    private BigDecimal recalculatePriceBeforeLoyalty(BookingEntity booking, CustomerEntity customer) {
        ShowEntity show = showService.findById(booking.getShowId());
        SeatEntity seat = seatRepository.findById(booking.getSeatId())
                .orElseThrow(() -> new ResourceNotFoundException("Seat with id=" + booking.getSeatId() + " not found"));
        HallEntity hall = hallService.findById(show.getHallId());
        long occupiedSeats = bookingRepository.countActiveByShowId(show.getId());
        return pricingClient.calculateFinalPrice(show, seat, hall, customer, occupiedSeats);
    }

    private void invalidateTicketIfExists(Long bookingId) {
        ticketService.findOptionalByBookingId(bookingId).ifPresent(ticketService::invalidate);
    }

    private LoyaltyClient.RollbackResult rollbackLoyaltyIfPaidRegistered(BookingEntity booking, BookingStatus previousStatus) {
        if (previousStatus != BookingStatus.PAID) {
            return null;
        }

        CustomerEntity customer = customerService.findById(booking.getCustomerId());
        if (!Boolean.TRUE.equals(customer.getRegistered())) {
            return null;
        }

        return loyaltyClient.rollbackBookingPoints(booking.getCustomerId(), booking.getId());
    }

    private void publishBookingCreated(BookingEntity booking) {
        BookingContext context = buildBookingContext(booking);
        eventPublisher.publish(
                BOOKING_CREATED,
                new BookingEvent.Created(
                        booking.getId(),
                        booking.getShowId(),
                        booking.getSeatId(),
                        booking.getCustomerId(),
                        booking.getCustomerEmail(),
                        booking.getReservedUntil(),
                        context.movieTitle(),
                        context.showStartTime(),
                        context.showEndTime(),
                        context.hallName(),
                        context.rowNumber(),
                        context.seatNumber(),
                        booking.getFinalPrice(),
                        booking.getCurrency()
                )
        );
    }

    private void publishBookingPaid(BookingEntity booking) {
        BookingContext context = buildBookingContext(booking);
        eventPublisher.publish(
                BOOKING_PAID,
                new BookingEvent.Paid(
                        booking.getId(),
                        booking.getShowId(),
                        booking.getSeatId(),
                        booking.getCustomerId(),
                        booking.getCustomerEmail(),
                        customerService.findById(booking.getCustomerId()).getRegistered(),
                        booking.getFinalPrice(),
                        booking.getCurrency(),
                        booking.getPaymentReference(),
                        booking.getLoyaltyPointsUsed(),
                        context.movieTitle(),
                        context.showStartTime(),
                        context.showEndTime(),
                        context.hallName(),
                        context.rowNumber(),
                        context.seatNumber()
                )
        );
    }

    private void publishBookingExpired(BookingEntity booking) {
        BookingContext context = buildBookingContext(booking);
        eventPublisher.publish(
                BOOKING_EXPIRED,
                new BookingEvent.Expired(
                        booking.getId(),
                        booking.getShowId(),
                        booking.getSeatId(),
                        booking.getCustomerId(),
                        context.movieTitle(),
                        context.showStartTime(),
                        context.hallName(),
                        context.rowNumber(),
                        context.seatNumber()
                )
        );
    }

    private void publishBookingCancelled(BookingEntity booking, BookingStatus previousStatus) {
        BookingContext context = buildBookingContext(booking);
        eventPublisher.publish(
                BOOKING_CANCELLED,
                new BookingEvent.Cancelled(
                        booking.getId(),
                        booking.getShowId(),
                        booking.getSeatId(),
                        booking.getCustomerId(),
                        previousStatus.name(),
                        context.movieTitle(),
                        context.showStartTime(),
                        context.hallName(),
                        context.rowNumber(),
                        context.seatNumber()
                )
        );
    }

    private void publishLoyaltyRollbackApplied(BookingEntity booking, LoyaltyClient.RollbackResult rollbackResult) {
        if (rollbackResult == null) {
            return;
        }

        if (rollbackResult.restoredSpentPoints() == 0 && rollbackResult.deductedEarnedPoints() == 0) {
            return;
        }

        BookingContext context = buildBookingContext(booking);
        eventPublisher.publish(
                LOYALTY_POINTS_ROLLBACK_APPLIED,
                new LoyaltyEvent.PointsRollbackApplied(
                        booking.getCustomerId(),
                        booking.getCustomerEmail(),
                        booking.getId(),
                        rollbackResult.restoredSpentPoints(),
                        rollbackResult.deductedEarnedPoints(),
                        rollbackResult.currentPointsBalance(),
                        context.movieTitle(),
                        context.showStartTime(),
                        context.hallName(),
                        context.rowNumber(),
                        context.seatNumber()
                )
        );
    }

    private void publishSeatReleased(BookingEntity booking) {
        BookingContext context = buildBookingContext(booking);
        eventPublisher.publish(
                SEAT_RELEASED,
                new SeatEvent.Released(
                        booking.getShowId(),
                        booking.getSeatId(),
                        booking.getId(),
                        context.movieTitle(),
                        context.showStartTime(),
                        context.hallName(),
                        context.rowNumber(),
                        context.seatNumber()
                )
        );
    }

    private void ensureShowAvailableForBooking(ShowEntity show) {
        if (show.getStartTime().isBefore(OffsetDateTime.now()) || show.getStartTime().isEqual(OffsetDateTime.now())) {
            throw new BookingStateConflictException("Booking is not available for shows that already started or passed");
        }
    }

    private void ensureShowNotStarted(BookingEntity booking) {
        ShowEntity show = showService.findById(booking.getShowId());
        if (show.getStartTime().isBefore(OffsetDateTime.now()) || show.getStartTime().isEqual(OffsetDateTime.now())) {
            throw new BookingStateConflictException("Payment is not available for shows that already started or passed");
        }
    }

    private void ensureRefundAllowed(BookingEntity booking) {
        ShowEntity show = showService.findById(booking.getShowId());
        OffsetDateTime deadline = show.getStartTime().minus(REFUND_DEADLINE_BEFORE_SHOW);
        if (OffsetDateTime.now().isAfter(deadline) || OffsetDateTime.now().isEqual(deadline)) {
            throw new BookingStateConflictException("Ticket refund is available only until 45 minutes before show start");
        }
    }

    private BookingContext buildBookingContext(BookingEntity booking) {
        ShowEntity show = showService.findById(booking.getShowId());
        MovieEntity movie = movieService.findById(show.getMovieId());
        HallEntity hall = hallService.findById(show.getHallId());
        SeatEntity seat = seatRepository.findById(booking.getSeatId())
                .orElseThrow(() -> new ResourceNotFoundException("Seat with id=" + booking.getSeatId() + " not found"));

        return new BookingContext(
                movie.getTitle(),
                show.getStartTime(),
                show.getEndTime(),
                hall.getName(),
                seat.getRowNumber(),
                seat.getSeatNumber()
        );
    }

    private record BookingContext(
            String movieTitle,
            OffsetDateTime showStartTime,
            OffsetDateTime showEndTime,
            String hallName,
            Integer rowNumber,
            Integer seatNumber
    ) {}

}
