package edu.rutmiit.demo.cinemacore.repository.base;

import java.util.List;
import java.util.Optional;

public interface BaseRepository<T, ID> {
    Optional<T> findById(ID id);
    List<T> findAll();
    PageSlice<T> findAll(int page, int size);
    T save(T entity);
    T update(T entity);
    void delete(T entity);
    void deleteById(ID id);
}
