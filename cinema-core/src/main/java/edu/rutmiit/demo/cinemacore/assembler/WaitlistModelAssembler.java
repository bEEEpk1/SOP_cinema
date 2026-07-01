package edu.rutmiit.demo.cinemacore.assembler;

import edu.rutmiit.demo.cinemaapicontract.dto.WaitlistResponse;
import edu.rutmiit.demo.cinemaapicontract.endpoints.CustomerApi;
import edu.rutmiit.demo.cinemaapicontract.endpoints.ShowApi;
import edu.rutmiit.demo.cinemaapicontract.endpoints.WaitlistApi;
import edu.rutmiit.demo.cinemacore.entity.WaitlistEntryEntity;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class WaitlistModelAssembler implements RepresentationModelAssembler<WaitlistEntryEntity, EntityModel<WaitlistResponse>> {
    @Override
    public EntityModel<WaitlistResponse> toModel(WaitlistEntryEntity entity) {
        WaitlistResponse response = WaitlistResponse.builder()
                .id(entity.getId())
                .showId(entity.getShowId())
                .seatId(entity.getSeatId())
                .customerId(entity.getCustomerId())
                .customerEmail(entity.getCustomerEmail())
                .status(entity.getStatus())
                .build();
        return EntityModel.of(response,
                linkTo(methodOn(WaitlistApi.class).getWaitlistEntryById(entity.getId())).withSelfRel(),
                linkTo(methodOn(WaitlistApi.class).getAllWaitlistEntries(null, null, null, 0, 20)).withRel("collection"),
                linkTo(methodOn(ShowApi.class).getShowById(entity.getShowId())).withRel("show"),
                linkTo(methodOn(CustomerApi.class).getCustomerById(entity.getCustomerId())).withRel("customer"));
    }
}
