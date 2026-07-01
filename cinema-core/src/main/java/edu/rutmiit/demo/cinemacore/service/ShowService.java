package edu.rutmiit.demo.cinemacore.service;

import edu.rutmiit.demo.cinemaapicontract.dto.PatchShowRequest;
import edu.rutmiit.demo.cinemaapicontract.dto.SeatResponse;
import edu.rutmiit.demo.cinemaapicontract.dto.ShowRequest;
import edu.rutmiit.demo.cinemaapicontract.enums.ShowStatus;
import edu.rutmiit.demo.cinemaapicontract.exception.ResourceNotFoundException;
import edu.rutmiit.demo.cinemacore.entity.SeatEntity;
import edu.rutmiit.demo.cinemacore.entity.ShowEntity;
import edu.rutmiit.demo.cinemacore.repository.BookingRepository;
import edu.rutmiit.demo.cinemacore.repository.SeatRepository;
import edu.rutmiit.demo.cinemacore.repository.ShowRepository;
import edu.rutmiit.demo.cinemacore.repository.base.PageSlice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ShowService {
    private final ShowRepository showRepository;
    private final MovieService movieService;
    private final HallService hallService;
    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;

    public ShowEntity findById(Long id) {
        return showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show with id=" + id + " not found"));
    }

    public PageSlice<ShowEntity> findAll(Long movieId, Long hallId, ShowStatus status, LocalDate showDate, int page, int size) {
        return showRepository.search(movieId, hallId, status, showDate, page, size);
    }

    @Transactional
    public ShowEntity create(ShowRequest request) {
        movieService.findById(request.movieId());
        hallService.findById(request.hallId());
        ShowEntity show = new ShowEntity();
        show.setMovieId(request.movieId());
        show.setHallId(request.hallId());
        show.setStartTime(request.startTime());
        show.setEndTime(request.endTime());
        show.setBasePrice(request.basePrice());
        show.setCurrency(request.currency());
        show.setStatus(ShowStatus.SCHEDULED);
        return showRepository.save(show);
    }

    @Transactional
    public ShowEntity patch(Long id, PatchShowRequest request) {
        ShowEntity show = findById(id);
        if (request.startTime() != null) show.setStartTime(request.startTime());
        if (request.endTime() != null) show.setEndTime(request.endTime());
        if (request.basePrice() != null) show.setBasePrice(request.basePrice());
        if (request.currency() != null) show.setCurrency(request.currency());
        if (request.status() != null) show.setStatus(request.status());
        return showRepository.update(show);
    }

    public PageSlice<SeatResponse> getShowSeats(Long showId, String availabilityStatus, int page, int size) {
        ShowEntity show = findById(showId);
        PageSlice<SeatEntity> seatSlice = seatRepository.findByHallId(show.getHallId(), page, size);
        var content = seatSlice.content().stream().map(seat -> {
            String status = bookingRepository.findActiveByShowIdAndSeatId(showId, seat.getId()).isPresent() ? "RESERVED" : "FREE";
            return SeatResponse.builder()
                    .id(seat.getId())
                    .hallId(seat.getHallId())
                    .rowNumber(seat.getRowNumber())
                    .seatNumber(seat.getSeatNumber())
                    .seatType(seat.getSeatType())
                    .active(seat.getActive())
                    .availabilityStatus(status)
                    .build();
        }).filter(resp -> availabilityStatus == null || availabilityStatus.equalsIgnoreCase(resp.getAvailabilityStatus())).toList();
        return new PageSlice<>(content, page, size, seatSlice.totalElements());
    }
}
