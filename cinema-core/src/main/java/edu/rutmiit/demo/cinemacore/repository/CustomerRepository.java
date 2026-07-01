package edu.rutmiit.demo.cinemacore.repository;

import edu.rutmiit.demo.cinemacore.entity.CustomerEntity;
import edu.rutmiit.demo.cinemacore.repository.base.AbstractBaseRepository;
import edu.rutmiit.demo.cinemacore.repository.base.PageSlice;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class CustomerRepository extends AbstractBaseRepository<CustomerEntity> {

    public CustomerRepository() {
        super(CustomerEntity.class);
    }

    public Optional<CustomerEntity> findByEmail(String email) {
        List<CustomerEntity> list = entityManager.createQuery(
                        "select c from CustomerEntity c where lower(c.email) = lower(:email)",
                        CustomerEntity.class
                )
                .setParameter("email", email)
                .getResultList();
        return list.stream().findFirst();
    }

    public Optional<CustomerEntity> findByPhone(String phone) {
        List<CustomerEntity> list = entityManager.createQuery(
                        "select c from CustomerEntity c where c.phone = :phone",
                        CustomerEntity.class
                )
                .setParameter("phone", phone)
                .getResultList();
        return list.stream().findFirst();
    }

    public PageSlice<CustomerEntity> search(String emailSearch, String phoneSearch, int page, int size) {
        StringBuilder from = new StringBuilder(" from CustomerEntity c where 1=1");
        Map<String, Object> params = new HashMap<>();

        if (emailSearch != null && !emailSearch.isBlank()) {
            from.append(" and lower(c.email) like :emailSearch");
            params.put("emailSearch", "%" + emailSearch.toLowerCase() + "%");
        }

        if (phoneSearch != null && !phoneSearch.isBlank()) {
            from.append(" and c.phone like :phoneSearch");
            params.put("phoneSearch", "%" + phoneSearch + "%");
        }

        String orderBy = " order by c.id";

        TypedQuery<CustomerEntity> query = entityManager.createQuery(
                "select c" + from + orderBy,
                CustomerEntity.class
        );

        TypedQuery<Long> countQuery = entityManager.createQuery(
                "select count(c.id)" + from,
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