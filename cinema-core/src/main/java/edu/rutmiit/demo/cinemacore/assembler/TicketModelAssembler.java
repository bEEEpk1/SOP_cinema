package edu.rutmiit.demo.cinemacore.assembler;

import edu.rutmiit.demo.cinemaapicontract.dto.TicketResponse;
import edu.rutmiit.demo.cinemaapicontract.endpoints.BookingApi;
import edu.rutmiit.demo.cinemaapicontract.endpoints.TicketApi;
import edu.rutmiit.demo.cinemacore.entity.TicketEntity;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class TicketModelAssembler implements RepresentationModelAssembler<TicketEntity, EntityModel<TicketResponse>> {
    @Override
    public EntityModel<TicketResponse> toModel(TicketEntity entity) {
        TicketResponse response = TicketResponse.builder()
                .id(entity.getId())
                .bookingId(entity.getBookingId())
                .ticketNumber(entity.getTicketNumber())
                .status(entity.getStatus())
                .qrCode(entity.getQrCode())
                .build();
        return EntityModel.of(response,
                linkTo(methodOn(TicketApi.class).getTicketById(entity.getId())).withSelfRel(),
                linkTo(methodOn(TicketApi.class).getAllTickets(null, null, null, 0, 20)).withRel("collection"),
                linkTo(methodOn(BookingApi.class).getBookingById(entity.getBookingId())).withRel("booking"));
    }
}
