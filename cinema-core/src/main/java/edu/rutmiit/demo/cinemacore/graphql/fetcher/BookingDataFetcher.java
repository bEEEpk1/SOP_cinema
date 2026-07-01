package edu.rutmiit.demo.cinemacore.graphql.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import edu.rutmiit.demo.cinemaapicontract.dto.BookingRequest;
import edu.rutmiit.demo.cinemaapicontract.dto.BookingResponse;
import edu.rutmiit.demo.cinemaapicontract.dto.PatchBookingRequest;
import edu.rutmiit.demo.cinemacore.assembler.BookingModelAssembler;
import edu.rutmiit.demo.cinemacore.entity.BookingEntity;
import edu.rutmiit.demo.cinemacore.graphql.types.*;
import edu.rutmiit.demo.cinemacore.repository.base.PageSlice;
import edu.rutmiit.demo.cinemacore.service.BookingService;

import static edu.rutmiit.demo.cinemacore.graphql.util.GqlSupport.*;

@DgsComponent
public class BookingDataFetcher {
    private final BookingService bookingService;
    private final BookingModelAssembler assembler;

    public BookingDataFetcher(BookingService bookingService, BookingModelAssembler assembler) {
        this.bookingService = bookingService;
        this.assembler = assembler;
    }

    @DgsQuery
    public BookingResponse booking(@InputArgument String id) {
        return assembler.toModel(bookingService.findById(id(id))).getContent();
    }

    @DgsQuery
    public BookingConnectionGql bookings(@InputArgument BookingFilterGql filter,
                                         @InputArgument Integer page,
                                         @InputArgument Integer size) {
        Long customerId = filter != null ? id(filter.customerId()) : null;
        Long showId = filter != null ? id(filter.showId()) : null;
        PageSlice<BookingEntity> slice = bookingService.findAll(customerId, showId,
                filter != null ? filter.status() : null,
                page(page), size(size));
        return new BookingConnectionGql(content(slice, assembler), pageInfo(slice), (int) slice.totalElements());
    }

    @DgsMutation
    public BookingResponse createBooking(@InputArgument CreateBookingInputGql input) {
        BookingRequest request = new BookingRequest(
                id(input.showId()),
                id(input.seatId()),
                id(input.customerId()),
                input.customerEmail(),
                input.loyaltyPointsUsed()
        );
        return assembler.toModel(bookingService.create(request)).getContent();
    }

    @DgsMutation
    public BookingResponse patchBooking(@InputArgument String id, @InputArgument PatchBookingInputGql input) {
        PatchBookingRequest request = new PatchBookingRequest(input.customerEmail(), input.status(), input.paymentReference(), input.loyaltyPointsUsed());
        return assembler.toModel(bookingService.patch(id(id), request)).getContent();
    }

    @DgsMutation
    public boolean cancelBooking(@InputArgument String id) {
        bookingService.cancel(id(id));
        return true;
    }
}
