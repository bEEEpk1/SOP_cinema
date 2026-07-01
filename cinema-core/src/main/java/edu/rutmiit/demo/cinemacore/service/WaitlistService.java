package edu.rutmiit.demo.cinemacore.service;

import edu.rutmiit.demo.cinemaapicontract.dto.WaitlistRequest;
import edu.rutmiit.demo.cinemaapicontract.enums.WaitlistStatus;
import edu.rutmiit.demo.cinemaapicontract.exception.BookingStateConflictException;
import edu.rutmiit.demo.cinemaapicontract.exception.ResourceNotFoundException;
import edu.rutmiit.demo.cinemacore.entity.CustomerEntity;
import edu.rutmiit.demo.cinemacore.entity.HallEntity;
import edu.rutmiit.demo.cinemacore.entity.MovieEntity;
import edu.rutmiit.demo.cinemacore.entity.SeatEntity;
import edu.rutmiit.demo.cinemacore.entity.ShowEntity;
import edu.rutmiit.demo.cinemacore.entity.WaitlistEntryEntity;
import edu.rutmiit.demo.cinemacore.integration.DomainEventPublisher;
import edu.rutmiit.demo.cinemacore.repository.SeatRepository;
import edu.rutmiit.demo.cinemacore.repository.WaitlistRepository;
import edu.rutmiit.demo.cinemacore.repository.base.PageSlice;
import edu.rutmiit.demo.cinemaeventscontract.WaitlistEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import static edu.rutmiit.demo.cinemaeventscontract.RoutingKeys.WAITLIST_USER_NOTIFIED;

@Service
@RequiredArgsConstructor
public class WaitlistService {

    private final WaitlistRepository waitlistRepository;
    private final ShowService showService;
    private final MovieService movieService;
    private final HallService hallService;
    private final SeatRepository seatRepository;
    private final CustomerService customerService;
    private final DomainEventPublisher eventPublisher;
    private final TaskScheduler taskScheduler;

    @Value("${cinema.waitlist.first-delay-seconds:30}")
    private long firstDelaySeconds;

    @Value("${cinema.waitlist.second-delay-seconds:60}")
    private long secondDelaySeconds;

    public WaitlistEntryEntity findById(Long id) {
        return waitlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Waitlist entry with id=" + id + " not found"));
    }

    public PageSlice<WaitlistEntryEntity> findAll(Long showId, Long customerId, WaitlistStatus status, int page, int size) {
        return waitlistRepository.search(showId, customerId, status, page, size);
    }

    @Transactional
    public WaitlistEntryEntity create(WaitlistRequest request) {
        showService.findById(request.showId());
        if (request.seatId() != null) {
            seatRepository.findById(request.seatId())
                    .orElseThrow(() -> new ResourceNotFoundException("Seat with id=" + request.seatId() + " not found"));
        }
        CustomerEntity customer = customerService.findById(request.customerId());
        if (!Boolean.TRUE.equals(customer.getRegistered())) {
            throw new BookingStateConflictException("Waitlist is available only for registered customers");
        }

        WaitlistEntryEntity entry = new WaitlistEntryEntity();
        entry.setShowId(request.showId());
        entry.setSeatId(request.seatId());
        entry.setCustomerId(request.customerId());
        entry.setCustomerEmail(request.customerEmail());
        entry.setStatus(WaitlistStatus.ACTIVE);
        return waitlistRepository.save(entry);
    }

    @Transactional
    public void cancel(Long id) {
        WaitlistEntryEntity entry = findById(id);
        entry.setStatus(WaitlistStatus.CANCELLED);
        waitlistRepository.update(entry);
    }

    public void handleSeatReleased(Long showId, Long seatId) {
        List<WaitlistEntryEntity> eligible = waitlistRepository.findEligibleForReleasedSeat(showId, seatId);
        if (eligible.isEmpty()) {
            return;
        }

        notifyEntry(eligible.get(0), seatId, 0);
        if (eligible.size() > 1) {
            notifyEntry(eligible.get(1), seatId, firstDelaySeconds);
        }
        if (eligible.size() > 2) {
            notifyEntry(eligible.get(2), seatId, secondDelaySeconds);
        }
        if (eligible.size() > 3) {
            taskScheduler.schedule(
                    () -> eligible.subList(3, eligible.size()).forEach(entry -> notifyEntry(entry, seatId, 0)),
                    Instant.now().plusSeconds(secondDelaySeconds)
            );
        }
    }

    private void notifyEntry(WaitlistEntryEntity entry, Long releasedSeatId, long delaySeconds) {
        taskScheduler.schedule(() -> {
            WaitlistEntryEntity actual = findById(entry.getId());
            if (actual.getStatus() != WaitlistStatus.ACTIVE) {
                return;
            }

            actual.setStatus(WaitlistStatus.NOTIFIED);
            waitlistRepository.update(actual);

            Long effectiveSeatId = actual.getSeatId() != null ? actual.getSeatId() : releasedSeatId;
            WaitlistContext context = buildWaitlistContext(actual.getShowId(), effectiveSeatId);

            eventPublisher.publish(
                    WAITLIST_USER_NOTIFIED,
                    new WaitlistEvent.UserNotified(
                            actual.getId(),
                            actual.getShowId(),
                            effectiveSeatId,
                            actual.getCustomerId(),
                            actual.getCustomerEmail(),
                            context.movieTitle(),
                            context.showStartTime(),
                            context.showEndTime(),
                            context.hallName(),
                            context.rowNumber(),
                            context.seatNumber()
                    )
            );
        }, Instant.now().plusSeconds(delaySeconds));
    }

    private WaitlistContext buildWaitlistContext(Long showId, Long seatId) {
        ShowEntity show = showService.findById(showId);
        MovieEntity movie = movieService.findById(show.getMovieId());
        HallEntity hall = hallService.findById(show.getHallId());
        Integer rowNumber = null;
        Integer seatNumber = null;
        if (seatId != null) {
            SeatEntity seat = seatRepository.findById(seatId).orElse(null);
            if (seat != null) {
                rowNumber = seat.getRowNumber();
                seatNumber = seat.getSeatNumber();
            }
        }
        return new WaitlistContext(movie.getTitle(), show.getStartTime(), show.getEndTime(), hall.getName(), rowNumber, seatNumber);
    }

    private record WaitlistContext(
            String movieTitle,
            OffsetDateTime showStartTime,
            OffsetDateTime showEndTime,
            String hallName,
            Integer rowNumber,
            Integer seatNumber
    ) {}
}
