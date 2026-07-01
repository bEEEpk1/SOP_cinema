package edu.rutmiit.demo.cinemacore.repository;

import edu.rutmiit.demo.cinemacore.entity.SeatEntity;
import edu.rutmiit.demo.cinemacore.repository.base.AbstractBaseRepository;
import edu.rutmiit.demo.cinemacore.repository.base.PageSlice;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SeatRepository extends AbstractBaseRepository<SeatEntity> {

    public SeatRepository() {
        super(SeatEntity.class);
    }

    public List<SeatEntity> findByHallId(Long hallId) {
        return entityManager.createQuery(
                        "select s from SeatEntity s where s.hallId = :hallId order by s.rowNumber, s.seatNumber",
                        SeatEntity.class
                )
                .setParameter("hallId", hallId)
                .getResultList();
    }

    public PageSlice<SeatEntity> findByHallId(Long hallId, int page, int size) {
        List<SeatEntity> content = entityManager.createQuery(
                        "select s from SeatEntity s where s.hallId = :hallId order by s.rowNumber, s.seatNumber",
                        SeatEntity.class
                )
                .setParameter("hallId", hallId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

        long total = entityManager.createQuery(
                        "select count(s.id) from SeatEntity s where s.hallId = :hallId",
                        Long.class
                )
                .setParameter("hallId", hallId)
                .getSingleResult();

        return new PageSlice<>(content, page, size, total);
    }
}