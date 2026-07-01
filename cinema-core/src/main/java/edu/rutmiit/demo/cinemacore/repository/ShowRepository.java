package edu.rutmiit.demo.cinemacore.repository;

import edu.rutmiit.demo.cinemaapicontract.enums.ShowStatus;
import edu.rutmiit.demo.cinemacore.entity.ShowEntity;
import edu.rutmiit.demo.cinemacore.repository.base.AbstractBaseRepository;
import edu.rutmiit.demo.cinemacore.repository.base.PageSlice;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Repository
public class ShowRepository extends AbstractBaseRepository<ShowEntity> {

    public ShowRepository() {
        super(ShowEntity.class);
    }

    public PageSlice<ShowEntity> search(Long movieId, Long hallId, ShowStatus status, LocalDate showDate, int page, int size) {
        StringBuilder from = new StringBuilder(" from ShowEntity s where 1=1");
        Map<String, Object> params = new HashMap<>();

        if (movieId != null) {
            from.append(" and s.movieId = :movieId");
            params.put("movieId", movieId);
        }

        if (hallId != null) {
            from.append(" and s.hallId = :hallId");
            params.put("hallId", hallId);
        }

        if (status != null) {
            from.append(" and s.status = :status");
            params.put("status", status);
        }

        if (showDate != null) {
            OffsetDateTime fromDate = showDate.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
            OffsetDateTime toDate = showDate.plusDays(1).atStartOfDay().atOffset(OffsetDateTime.now().getOffset());

            from.append(" and s.startTime >= :fromDate and s.startTime < :toDate");
            params.put("fromDate", fromDate);
            params.put("toDate", toDate);
        }

        String orderBy = " order by s.startTime, s.id";

        TypedQuery<ShowEntity> query = entityManager.createQuery(
                "select s" + from + orderBy,
                ShowEntity.class
        );

        TypedQuery<Long> countQuery = entityManager.createQuery(
                "select count(s.id)" + from,
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