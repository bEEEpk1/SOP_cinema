package edu.rutmiit.demo.cinemacore.repository;

import edu.rutmiit.demo.cinemacore.entity.TicketEntity;
import edu.rutmiit.demo.cinemacore.repository.base.AbstractBaseRepository;
import edu.rutmiit.demo.cinemacore.repository.base.PageSlice;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class TicketRepository extends AbstractBaseRepository<TicketEntity> {

    public TicketRepository() {
        super(TicketEntity.class);
    }

    public Optional<TicketEntity> findByBookingId(Long bookingId) {
        return entityManager.createQuery(
                        "select t from TicketEntity t where t.bookingId = :bookingId",
                        TicketEntity.class
                )
                .setParameter("bookingId", bookingId)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }

    public PageSlice<TicketEntity> search(Long bookingId, Long customerId, Long showId, int page, int size) {
        StringBuilder from = new StringBuilder(
                " from TicketEntity t join BookingEntity b on b.id = t.bookingId where 1=1"
        );
        Map<String, Object> params = new HashMap<>();

        if (bookingId != null) {
            from.append(" and t.bookingId = :bookingId");
            params.put("bookingId", bookingId);
        }

        if (customerId != null) {
            from.append(" and b.customerId = :customerId");
            params.put("customerId", customerId);
        }

        if (showId != null) {
            from.append(" and b.showId = :showId");
            params.put("showId", showId);
        }

        String orderBy = " order by t.id desc";

        TypedQuery<TicketEntity> query = entityManager.createQuery(
                "select t" + from + orderBy,
                TicketEntity.class
        );

        TypedQuery<Long> countQuery = entityManager.createQuery(
                "select count(t.id)" + from,
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
}