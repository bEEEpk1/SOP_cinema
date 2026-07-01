package edu.rutmiit.demo.cinemacore.service;

import edu.rutmiit.demo.cinemaapicontract.dto.MovieRequest;
import edu.rutmiit.demo.cinemaapicontract.dto.MovieRecommendationResponse;
import edu.rutmiit.demo.cinemaapicontract.dto.PatchMovieRequest;
import edu.rutmiit.demo.cinemaapicontract.dto.UpdateMovieRequest;
import edu.rutmiit.demo.cinemaapicontract.exception.ResourceNotFoundException;
import edu.rutmiit.demo.cinemacore.entity.MovieEntity;
import edu.rutmiit.demo.cinemacore.repository.MovieRepository;
import edu.rutmiit.demo.cinemacore.repository.base.PageSlice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository movieRepository;

    public MovieEntity findById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie with id=" + id + " not found"));
    }

    public PageSlice<MovieEntity> findAll(String genre, String titleSearch, int page, int size) {
        return movieRepository.search(genre, titleSearch, page, size);
    }



    public List<MovieRecommendationResponse> findRecommendations(String genre, Long excludeMovieId, int limit) {
        return movieRepository.findRecommendationsByGenre(genre, excludeMovieId, limit)
                .stream()
                .map(movie -> new MovieRecommendationResponse(
                        movie.getId(),
                        movie.getTitle(),
                        movie.getGenre(),
                        movie.getDurationMinutes()
                ))
                .toList();
    }

    @Transactional
    public MovieEntity create(MovieRequest request) {
        MovieEntity entity = new MovieEntity();
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setDurationMinutes(request.durationMinutes());
        entity.setAgeRating(request.ageRating());
        entity.setGenre(request.genre());
        entity.setActive(true);
        return movieRepository.save(entity);
    }

    @Transactional
    public MovieEntity update(Long id, UpdateMovieRequest request) {
        MovieEntity entity = findById(id);
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setDurationMinutes(request.durationMinutes());
        entity.setAgeRating(request.ageRating());
        entity.setGenre(request.genre());
        entity.setActive(request.active());
        return movieRepository.update(entity);
    }

    @Transactional
    public MovieEntity patch(Long id, PatchMovieRequest request) {
        MovieEntity entity = findById(id);
        if (request.title() != null) entity.setTitle(request.title());
        if (request.description() != null) entity.setDescription(request.description());
        if (request.durationMinutes() != null) entity.setDurationMinutes(request.durationMinutes());
        if (request.ageRating() != null) entity.setAgeRating(request.ageRating());
        if (request.genre() != null) entity.setGenre(request.genre());
        if (request.active() != null) entity.setActive(request.active());
        return movieRepository.update(entity);
    }

    @Transactional
    public void delete(Long id) {
        movieRepository.delete(findById(id));
    }
}
