package edu.rutmiit.demo.cinemacore.controller;

import edu.rutmiit.demo.cinemaapicontract.dto.*;
import edu.rutmiit.demo.cinemaapicontract.endpoints.MovieApi;
import edu.rutmiit.demo.cinemacore.assembler.MovieModelAssembler;
import edu.rutmiit.demo.cinemacore.service.MovieService;
import edu.rutmiit.demo.cinemacore.util.PagedModelFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MovieController implements MovieApi {
    private final MovieService movieService;
    private final MovieModelAssembler assembler;
    private final PagedModelFactory pagedModelFactory;

    @Override
    public EntityModel<MovieResponse> getMovieById(Long id) { return assembler.toModel(movieService.findById(id)); }

    @Override
    public PagedModel<EntityModel<MovieResponse>> getAllMovies(String genre, String titleSearch, int page, int size) {
        return pagedModelFactory.toPagedModel(movieService.findAll(genre, titleSearch, page, size), assembler);
    }



    @Override
    public List<MovieRecommendationResponse> getMovieRecommendations(String genre, Long excludeMovieId, int limit) {
        return movieService.findRecommendations(genre, excludeMovieId, limit);
    }

    @Override
    public ResponseEntity<EntityModel<MovieResponse>> createMovie(MovieRequest request) {
        EntityModel<MovieResponse> model = assembler.toModel(movieService.create(request));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @Override
    public EntityModel<MovieResponse> updateMovie(Long id, UpdateMovieRequest request) { return assembler.toModel(movieService.update(id, request)); }

    @Override
    public EntityModel<MovieResponse> patchMovie(Long id, PatchMovieRequest request) { return assembler.toModel(movieService.patch(id, request)); }

    @Override
    public void deleteMovie(Long id) { movieService.delete(id); }
}
