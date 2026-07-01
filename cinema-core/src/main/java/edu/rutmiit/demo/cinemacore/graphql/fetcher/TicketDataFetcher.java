package edu.rutmiit.demo.cinemacore.graphql.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import edu.rutmiit.demo.cinemaapicontract.dto.TicketResponse;
import edu.rutmiit.demo.cinemacore.assembler.TicketModelAssembler;
import edu.rutmiit.demo.cinemacore.entity.TicketEntity;
import edu.rutmiit.demo.cinemacore.graphql.types.TicketConnectionGql;
import edu.rutmiit.demo.cinemacore.graphql.types.TicketFilterGql;
import edu.rutmiit.demo.cinemacore.repository.base.PageSlice;
import edu.rutmiit.demo.cinemacore.service.TicketService;

import static edu.rutmiit.demo.cinemacore.graphql.util.GqlSupport.*;

@DgsComponent
public class TicketDataFetcher {
    private final TicketService ticketService;
    private final TicketModelAssembler assembler;

    public TicketDataFetcher(TicketService ticketService, TicketModelAssembler assembler) {
        this.ticketService = ticketService;
        this.assembler = assembler;
    }

    @DgsQuery
    public TicketResponse ticket(@InputArgument String id) {
        return assembler.toModel(ticketService.findById(id(id))).getContent();
    }

    @DgsQuery
    public TicketResponse ticketByBooking(@InputArgument String bookingId) {
        return assembler.toModel(ticketService.findByBookingId(id(bookingId))).getContent();
    }

    @DgsQuery
    public TicketConnectionGql tickets(@InputArgument TicketFilterGql filter,
                                       @InputArgument Integer page,
                                       @InputArgument Integer size) {
        Long bookingId = filter != null ? id(filter.bookingId()) : null;
        Long customerId = filter != null ? id(filter.customerId()) : null;
        Long showId = filter != null ? id(filter.showId()) : null;
        PageSlice<TicketEntity> slice = ticketService.findAll(bookingId, customerId, showId, page(page), size(size));
        return new TicketConnectionGql(content(slice, assembler), pageInfo(slice), (int) slice.totalElements());
    }
}
