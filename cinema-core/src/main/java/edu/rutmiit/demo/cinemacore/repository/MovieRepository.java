package edu.rutmiit.demo.cinemacore.repository;

import edu.rutmiit.demo.cinemacore.entity.MovieEntity;
import edu.rutmiit.demo.cinemacore.repository.base.AbstractBaseRepository;
import edu.rutmiit.demo.cinemacore.repository.base.PageSlice;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MovieRepository extends AbstractBaseRepository<MovieEntity> {

    public MovieRepository() {
        super(MovieEntity.class);
    }

    public PageSlice<MovieEntity> search(String genre, String titleSearch, int page, int size) {
        StringBuilder from = new StringBuilder(" from MovieEntity m where 1=1");
        Map<String, Object> params = new HashMap<>();

        if (genre != null && !genre.isBlank()) {
            from.append(" and m.genre = :genre");
            params.put("genre", genre);
        }

        if (titleSearch != null && !titleSearch.isBlank()) {
            from.append(" and lower(m.title) like :titleSearch");
            params.put("titleSearch", "%" + titleSearch.toLowerCase() + "%");
        }

        String orderBy = " order by m.id";

        TypedQuery<MovieEntity> query = entityManager.createQuery(
                "select m" + from + orderBy,
                MovieEntity.class
        );

        TypedQuery<Long> countQuery = entityManager.createQuery(
                "select count(m.id)" + from,
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


    public List<MovieEntity> findRecommendationsByGenre(String genre, Long excludeMovieId, int limit) {
        if (genre == null || genre.isBlank() || limit <= 0) {
            return List.of();
        }

        StringBuilder jpql = new StringBuilder("""
                select m
                from MovieEntity m
                where m.active = true
                  and upper(m.genre) = upper(:genre)
                """);

        if (excludeMovieId != null) {
            jpql.append(" and m.id <> :excludeMovieId");
        }

        jpql.append(" order by m.id");

        TypedQuery<MovieEntity> query = entityManager.createQuery(jpql.toString(), MovieEntity.class);
        query.setParameter("genre", genre.trim());
        if (excludeMovieId != null) {
            query.setParameter("excludeMovieId", excludeMovieId);
        }
        query.setMaxResults(Math.min(Math.max(limit, 1), 10));
        return query.getResultList();
    }

}
