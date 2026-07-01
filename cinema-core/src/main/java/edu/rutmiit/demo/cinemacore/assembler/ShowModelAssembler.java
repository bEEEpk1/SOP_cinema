package edu.rutmiit.demo.cinemacore.assembler;

import edu.rutmiit.demo.cinemaapicontract.dto.ShowResponse;
import edu.rutmiit.demo.cinemaapicontract.endpoints.MovieApi;
import edu.rutmiit.demo.cinemaapicontract.endpoints.ShowApi;
import edu.rutmiit.demo.cinemaapicontract.endpoints.WaitlistApi;
import edu.rutmiit.demo.cinemacore.entity.ShowEntity;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ShowModelAssembler implements RepresentationModelAssembler<ShowEntity, EntityModel<ShowResponse>> {
    @Override
    public EntityModel<ShowResponse> toModel(ShowEntity entity) {
        ShowResponse response = ShowResponse.builder()
                .id(entity.getId())
                .movieId(entity.getMovieId())
                .hallId(entity.getHallId())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .basePrice(entity.getBasePrice())
                .currency(entity.getCurrency())
                .status(entity.getStatus())
                .build();
        return EntityModel.of(response,
                linkTo(methodOn(ShowApi.class).getShowById(entity.getId())).withSelfRel(),
                linkTo(methodOn(ShowApi.class).getAllShows(null, null, null, null, 0, 20)).withRel("collection"),
                linkTo(methodOn(ShowApi.class).getShowSeats(entity.getId(), null, 0, 50)).withRel("seats"),
                linkTo(methodOn(WaitlistApi.class).getAllWaitlistEntries(entity.getId(), null, null, 0, 20)).withRel("waitlist"),
                linkTo(methodOn(MovieApi.class).getMovieById(entity.getMovieId())).withRel("movie"));
    }
}
