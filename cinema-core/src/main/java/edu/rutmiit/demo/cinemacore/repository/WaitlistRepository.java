package edu.rutmiit.demo.cinemacore.repository;

import edu.rutmiit.demo.cinemaapicontract.enums.WaitlistStatus;
import edu.rutmiit.demo.cinemacore.entity.WaitlistEntryEntity;
import edu.rutmiit.demo.cinemacore.repository.base.AbstractBaseRepository;
import edu.rutmiit.demo.cinemacore.repository.base.PageSlice;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class WaitlistRepository extends AbstractBaseRepository<WaitlistEntryEntity> {

    public WaitlistRepository() {
        super(WaitlistEntryEntity.class);
    }

    public PageSlice<WaitlistEntryEntity> search(Long showId, Long customerId, WaitlistStatus status, int page, int size) {
        StringBuilder from = new StringBuilder(" from WaitlistEntryEntity w where 1=1");
        Map<String, Object> params = new HashMap<>();

        if (showId != null) {
            from.append(" and w.showId = :showId");
            params.put("showId", showId);
        }

        if (customerId != null) {
            from.append(" and w.customerId = :customerId");
            params.put("customerId", customerId);
        }

        if (status != null) {
            from.append(" and w.status = :status");
            params.put("status", status);
        }

        String orderBy = " order by w.id";

        TypedQuery<WaitlistEntryEntity> query = entityManager.createQuery(
                "select w" + from + orderBy,
                WaitlistEntryEntity.class
        );

        TypedQuery<Long> countQuery = entityManager.createQuery(
                "select count(w.id)" + from,
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

    public List<WaitlistEntryEntity> findEligibleForReleasedSeat(Long showId, Long seatId) {
        return entityManager.createQuery(
                        "select w from WaitlistEntryEntity w " +
                                "where w.showId = :showId and w.status = :status and (w.seatId is null or w.seatId = :seatId) " +
                                "order by w.id",
                        WaitlistEntryEntity.class
                )
                .setParameter("showId", showId)
                .setParameter("seatId", seatId)
                .setParameter("status", WaitlistStatus.ACTIVE)
                .getResultList();
    }
}