package edu.rutmiit.demo.cinemacore.repository;

import edu.rutmiit.demo.cinemaapicontract.enums.HallType;
import edu.rutmiit.demo.cinemacore.entity.HallEntity;
import edu.rutmiit.demo.cinemacore.repository.base.AbstractBaseRepository;
import edu.rutmiit.demo.cinemacore.repository.base.PageSlice;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class HallRepository extends AbstractBaseRepository<HallEntity> {

    public HallRepository() {
        super(HallEntity.class);
    }

    public PageSlice<HallEntity> search(HallType hallType, int page, int size) {
        StringBuilder from = new StringBuilder(" from HallEntity h where 1=1");
        Map<String, Object> params = new HashMap<>();

        if (hallType != null) {
            from.append(" and h.hallType = :hallType");
            params.put("hallType", hallType);
        }

        String orderBy = " order by h.id";

        TypedQuery<HallEntity> query = entityManager.createQuery(
                "select h" + from + orderBy,
                HallEntity.class
        );

        TypedQuery<Long> countQuery = entityManager.createQuery(
                "select count(h.id)" + from,
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