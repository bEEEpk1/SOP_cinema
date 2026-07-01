package edu.rutmiit.demo.cinemacore.graphql.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import edu.rutmiit.demo.cinemaapicontract.dto.MovieRequest;
import edu.rutmiit.demo.cinemaapicontract.dto.MovieResponse;
import edu.rutmiit.demo.cinemaapicontract.dto.PatchMovieRequest;
import edu.rutmiit.demo.cinemaapicontract.dto.UpdateMovieRequest;
import edu.rutmiit.demo.cinemacore.assembler.MovieModelAssembler;
import edu.rutmiit.demo.cinemacore.entity.MovieEntity;
import edu.rutmiit.demo.cinemacore.graphql.types.*;
import edu.rutmiit.demo.cinemacore.repository.base.PageSlice;
import edu.rutmiit.demo.cinemacore.service.MovieService;

import static edu.rutmiit.demo.cinemacore.graphql.util.GqlSupport.*;

@DgsComponent
public class MovieDataFetcher {
    private final MovieService movieService;
    private final MovieModelAssembler assembler;

    public MovieDataFetcher(MovieService movieService, MovieModelAssembler assembler) {
        this.movieService = movieService;
        this.assembler = assembler;
    }

    @DgsQuery
    public MovieResponse movie(@InputArgument String id) {
        return assembler.toModel(movieService.findById(id(id))).getContent();
    }

    @DgsQuery
    public MovieConnectionGql movies(@InputArgument MovieFilterGql filter,
                                     @InputArgument Integer page,
                                     @InputArgument Integer size) {
        String genre = filter != null ? filter.genre() : null;
        String titleSearch = filter != null ? filter.titleSearch() : null;
        PageSlice<MovieEntity> slice = movieService.findAll(genre, titleSearch, page(page), size(size));
        return new MovieConnectionGql(content(slice, assembler), pageInfo(slice), (int) slice.totalElements());
    }

    @DgsMutation
    public MovieResponse createMovie(@InputArgument CreateMovieInputGql input) {
        MovieRequest request = new MovieRequest(input.title(), input.description(), input.durationMinutes(), input.ageRating(), input.genre());
        return assembler.toModel(movieService.create(request)).getContent();
    }

    @DgsMutation
    public MovieResponse updateMovie(@InputArgument String id, @InputArgument UpdateMovieInputGql input) {
        UpdateMovieRequest request = new UpdateMovieRequest(input.title(), input.description(), input.durationMinutes(), input.ageRating(), input.genre(), input.active());
        return assembler.toModel(movieService.update(id(id), request)).getContent();
    }

    @DgsMutation
    public MovieResponse patchMovie(@InputArgument String id, @InputArgument PatchMovieInputGql input) {
        PatchMovieRequest request = new PatchMovieRequest(input.title(), input.description(), input.durationMinutes(), input.ageRating(), input.genre(), input.active());
        return assembler.toModel(movieService.patch(id(id), request)).getContent();
    }

    @DgsMutation
    public boolean deleteMovie(@InputArgument String id) {
        movieService.delete(id(id));
        return true;
    }
}
