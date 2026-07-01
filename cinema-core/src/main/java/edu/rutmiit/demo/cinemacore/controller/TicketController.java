package edu.rutmiit.demo.cinemacore.controller;

import edu.rutmiit.demo.cinemaapicontract.dto.TicketResponse;
import edu.rutmiit.demo.cinemaapicontract.endpoints.TicketApi;
import edu.rutmiit.demo.cinemacore.assembler.TicketModelAssembler;
import edu.rutmiit.demo.cinemacore.service.TicketService;
import edu.rutmiit.demo.cinemacore.util.PagedModelFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TicketController implements TicketApi {
    private final TicketService ticketService;
    private final TicketModelAssembler assembler;
    private final PagedModelFactory pagedModelFactory;

    @Override
    public EntityModel<TicketResponse> getTicketById(Long id) { return assembler.toModel(ticketService.findById(id)); }

    @Override
    public PagedModel<EntityModel<TicketResponse>> getAllTickets(Long bookingId, Long customerId, Long showId, int page, int size) {
        return pagedModelFactory.toPagedModel(ticketService.findAll(bookingId, customerId, showId, page, size), assembler);
    }
}
