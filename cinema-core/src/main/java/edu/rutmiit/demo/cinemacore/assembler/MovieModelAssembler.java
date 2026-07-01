package edu.rutmiit.demo.cinemacore.assembler;

import edu.rutmiit.demo.cinemaapicontract.dto.MovieResponse;
import edu.rutmiit.demo.cinemaapicontract.endpoints.MovieApi;
import edu.rutmiit.demo.cinemaapicontract.endpoints.ShowApi;
import edu.rutmiit.demo.cinemacore.entity.MovieEntity;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class MovieModelAssembler implements RepresentationModelAssembler<MovieEntity, EntityModel<MovieResponse>> {
    @Override
    public EntityModel<MovieResponse> toModel(MovieEntity entity) {
        MovieResponse response = MovieResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .durationMinutes(entity.getDurationMinutes())
                .ageRating(entity.getAgeRating())
                .genre(entity.getGenre())
                .active(entity.getActive())
                .build();
        return EntityModel.of(response,
                linkTo(methodOn(MovieApi.class).getMovieById(entity.getId())).withSelfRel(),
                linkTo(methodOn(MovieApi.class).getAllMovies(null, null, 0, 20)).withRel("collection"),
                linkTo(methodOn(ShowApi.class).getAllShows(entity.getId(), null, null, null, 0, 20)).withRel("shows"));
    }
}
