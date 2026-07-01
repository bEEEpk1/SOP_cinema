package edu.rutmiit.demo.cinemacore.repository;

import edu.rutmiit.demo.cinemaapicontract.enums.BookingStatus;
import edu.rutmiit.demo.cinemacore.entity.BookingEntity;
import edu.rutmiit.demo.cinemacore.repository.base.AbstractBaseRepository;
import edu.rutmiit.demo.cinemacore.repository.base.PageSlice;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class BookingRepository extends AbstractBaseRepository<BookingEntity> {

    private static final Set<BookingStatus> ACTIVE_STATUSES =
            Set.of(BookingStatus.PENDING_PAYMENT, BookingStatus.PAID);

    public BookingRepository() {
        super(BookingEntity.class);
    }

    public PageSlice<BookingEntity> search(Long customerId, Long showId, BookingStatus status, int page, int size) {
        StringBuilder from = new StringBuilder(" from BookingEntity b where 1=1");
        Map<String, Object> params = new HashMap<>();

        if (customerId != null) {
            from.append(" and b.customerId = :customerId");
            params.put("customerId", customerId);
        }

        if (showId != null) {
            from.append(" and b.showId = :showId");
            params.put("showId", showId);
        }

        if (status != null) {
            from.append(" and b.status = :status");
            params.put("status", status);
        }

        String orderBy = " order by b.id desc";

        TypedQuery<BookingEntity> query = entityManager.createQuery(
                "select b" + from + orderBy,
                BookingEntity.class
        );

        TypedQuery<Long> countQuery = entityManager.createQuery(
                "select count(b.id)" + from,
                Long.class
        );

        params.forEach((key, value) -> {
            query.setParameter(key, value);
            countQuery.setParameter(key, value);
        });

        query.setFirstResult(page * size);
        query.setMaxResults(size);

        long total = countQuery.getSingleResult();

        return new PageSlice<>(query.getResultList(), page, size, total);
    }

    public Optional<BookingEntity> findActiveByShowIdAndSeatId(Long showId, Long seatId) {
        return entityManager.createQuery(
                        "select b from BookingEntity b " +
                                "where b.showId = :showId and b.seatId = :seatId and b.status in :statuses " +
                                "order by b.id desc",
                        BookingEntity.class
                )
                .setParameter("showId", showId)
                .setParameter("seatId", seatId)
                .setParameter("statuses", ACTIVE_STATUSES)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }


    public long countActiveByShowId(Long showId) {
        return entityManager.createQuery(
                        "select count(b.id) from BookingEntity b " +
                                "where b.showId = :showId and b.status in :statuses",
                        Long.class
                )
                .setParameter("showId", showId)
                .setParameter("statuses", ACTIVE_STATUSES)
                .getSingleResult();
    }


    public void flush() {
        entityManager.flush();
    }

    public List<BookingEntity> findExpirable(OffsetDateTime now) {
        return entityManager.createQuery(
                        "select b from BookingEntity b " +
                                "where b.status = :status and b.reservedUntil < :now " +
                                "order by b.id",
                        BookingEntity.class
                )
                .setParameter("status", BookingStatus.PENDING_PAYMENT)
                .setParameter("now", now)
                .getResultList();
    }
}