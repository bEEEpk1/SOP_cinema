package edu.rutmiit.demo.cinemacore.controller;

import edu.rutmiit.demo.cinemaapicontract.dto.*;
import edu.rutmiit.demo.cinemaapicontract.endpoints.BookingApi;
import edu.rutmiit.demo.cinemaapicontract.enums.BookingStatus;
import edu.rutmiit.demo.cinemacore.assembler.BookingModelAssembler;
import edu.rutmiit.demo.cinemacore.assembler.TicketModelAssembler;
import edu.rutmiit.demo.cinemacore.service.BookingService;
import edu.rutmiit.demo.cinemacore.service.TicketService;
import edu.rutmiit.demo.cinemacore.util.PagedModelFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BookingController implements BookingApi {
    private final BookingService bookingService;
    private final TicketService ticketService;
    private final BookingModelAssembler assembler;
    private final TicketModelAssembler ticketAssembler;
    private final PagedModelFactory pagedModelFactory;

    @Override
    public EntityModel<BookingResponse> getBookingById(Long id) { return assembler.toModel(bookingService.findById(id)); }

    @Override
    public PagedModel<EntityModel<BookingResponse>> getAllBookings(Long customerId, Long showId, String status, int page, int size) {
        BookingStatus parsed = status == null ? null : BookingStatus.valueOf(status);
        return pagedModelFactory.toPagedModel(bookingService.findAll(customerId, showId, parsed, page, size), assembler);
    }

    @Override
    public ResponseEntity<EntityModel<BookingResponse>> createBooking(BookingRequest request) {
        EntityModel<BookingResponse> model = assembler.toModel(bookingService.create(request));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @Override
    public EntityModel<BookingResponse> patchBooking(Long id, PatchBookingRequest request) { return assembler.toModel(bookingService.patch(id, request)); }

    @Override
    public void cancelBooking(Long id) { bookingService.cancel(id); }

    @Override
    public EntityModel<TicketResponse> getTicketByBookingId(Long id) { return ticketAssembler.toModel(ticketService.findByBookingId(id)); }
}
