package edu.rutmiit.demo.cinemacore.scheduler;

import edu.rutmiit.demo.cinemacore.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingExpirationScheduler {

    private final BookingService bookingService;

    @Scheduled(fixedDelay = 30000)
    public void expirePendingBookings() {
        log.debug("running booking expiration scheduler");
        bookingService.expirePendingBookings();
    }
}
