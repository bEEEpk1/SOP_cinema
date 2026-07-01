package edu.rutmiit.demo.cinemacore.assembler;

import edu.rutmiit.demo.cinemaapicontract.dto.BookingResponse;
import edu.rutmiit.demo.cinemaapicontract.endpoints.BookingApi;
import edu.rutmiit.demo.cinemaapicontract.endpoints.CustomerApi;
import edu.rutmiit.demo.cinemaapicontract.endpoints.ShowApi;
import edu.rutmiit.demo.cinemacore.entity.BookingEntity;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class BookingModelAssembler implements RepresentationModelAssembler<BookingEntity, EntityModel<BookingResponse>> {
    @Override
    public EntityModel<BookingResponse> toModel(BookingEntity entity) {
        BookingResponse response = BookingResponse.builder()
                .id(entity.getId())
                .showId(entity.getShowId())
                .seatId(entity.getSeatId())
                .customerId(entity.getCustomerId())
                .customerEmail(entity.getCustomerEmail())
                .status(entity.getStatus())
                .reservedUntil(entity.getReservedUntil())
                .finalPrice(entity.getFinalPrice())
                .currency(entity.getCurrency())
                .paymentReference(entity.getPaymentReference())
                .loyaltyPointsUsed(entity.getLoyaltyPointsUsed())
                .build();
        return EntityModel.of(response,
                linkTo(methodOn(BookingApi.class).getBookingById(entity.getId())).withSelfRel(),
                linkTo(methodOn(BookingApi.class).getAllBookings(null, null, null, 0, 20)).withRel("collection"),
                linkTo(methodOn(ShowApi.class).getShowById(entity.getShowId())).withRel("show"),
                linkTo(methodOn(CustomerApi.class).getCustomerById(entity.getCustomerId())).withRel("customer"),
                linkTo(methodOn(BookingApi.class).getTicketByBookingId(entity.getId())).withRel("ticket"));
    }
}
