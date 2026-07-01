package edu.rutmiit.demo.cinemacore.service;

import edu.rutmiit.demo.cinemaapicontract.dto.HallRequest;
import edu.rutmiit.demo.cinemaapicontract.enums.HallType;
import edu.rutmiit.demo.cinemaapicontract.enums.SeatType;
import edu.rutmiit.demo.cinemaapicontract.exception.ResourceNotFoundException;
import edu.rutmiit.demo.cinemacore.entity.HallEntity;
import edu.rutmiit.demo.cinemacore.entity.SeatEntity;
import edu.rutmiit.demo.cinemacore.repository.HallRepository;
import edu.rutmiit.demo.cinemacore.repository.SeatRepository;
import edu.rutmiit.demo.cinemacore.repository.base.PageSlice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HallService {
    private final HallRepository hallRepository;
    private final SeatRepository seatRepository;

    public HallEntity findById(Long id) {
        return hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall with id=" + id + " not found"));
    }

    public PageSlice<HallEntity> findAll(HallType hallType, int page, int size) {
        return hallRepository.search(hallType, page, size);
    }

    @Transactional
    public HallEntity create(HallRequest request) {
        HallEntity hall = new HallEntity();
        hall.setName(request.name());
        hall.setHallType(request.hallType());
        hall.setCapacity(request.capacity());
        hall.setActive(true);
        HallEntity saved = hallRepository.save(hall);
        bootstrapSeats(saved);
        return saved;
    }

    private void bootstrapSeats(HallEntity hall) {
        int rows = Math.max(1, hall.getCapacity() / 10);
        int perRow = Math.max(1, hall.getCapacity() / rows);
        int created = 0;
        for (int row = 1; row <= rows && created < hall.getCapacity(); row++) {
            for (int num = 1; num <= perRow && created < hall.getCapacity(); num++) {
                SeatEntity seat = new SeatEntity();
                seat.setHallId(hall.getId());
                seat.setRowNumber(row);
                seat.setSeatNumber(num);
                seat.setSeatType(hall.getHallType() == HallType.VIP ? SeatType.VIP : SeatType.STANDARD);
                seat.setActive(true);
                seatRepository.save(seat);
                created++;
            }
        }
    }
}
