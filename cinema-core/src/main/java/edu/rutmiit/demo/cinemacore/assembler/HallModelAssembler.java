package edu.rutmiit.demo.cinemacore.assembler;

import edu.rutmiit.demo.cinemaapicontract.dto.HallResponse;
import edu.rutmiit.demo.cinemaapicontract.endpoints.HallApi;
import edu.rutmiit.demo.cinemaapicontract.endpoints.ShowApi;
import edu.rutmiit.demo.cinemacore.entity.HallEntity;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class HallModelAssembler implements RepresentationModelAssembler<HallEntity, EntityModel<HallResponse>> {
    @Override
    public EntityModel<HallResponse> toModel(HallEntity entity) {
        HallResponse response = HallResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .hallType(entity.getHallType())
                .capacity(entity.getCapacity())
                .active(entity.getActive())
                .build();
        return EntityModel.of(response,
                linkTo(methodOn(HallApi.class).getHallById(entity.getId())).withSelfRel(),
                linkTo(methodOn(HallApi.class).getAllHalls(null, 0, 20)).withRel("collection"),
                linkTo(methodOn(ShowApi.class).getAllShows(null, entity.getId(), null, null, 0, 20)).withRel("shows"));
    }
}
