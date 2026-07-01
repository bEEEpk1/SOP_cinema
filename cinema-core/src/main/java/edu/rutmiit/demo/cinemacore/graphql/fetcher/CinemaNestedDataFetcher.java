package edu.rutmiit.demo.cinemacore.graphql.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import graphql.schema.DataFetchingEnvironment;
import com.netflix.graphql.dgs.InputArgument;
import edu.rutmiit.demo.cinemaapicontract.dto.*;
import edu.rutmiit.demo.cinemaapicontract.enums.BookingStatus;
import edu.rutmiit.demo.cinemaapicontract.enums.WaitlistStatus;
import edu.rutmiit.demo.cinemaapicontract.exception.ResourceNotFoundException;
import edu.rutmiit.demo.cinemacore.assembler.*;
import edu.rutmiit.demo.cinemacore.entity.*;
import edu.rutmiit.demo.cinemacore.graphql.types.*;
import edu.rutmiit.demo.cinemacore.repository.SeatRepository;
import edu.rutmiit.demo.cinemacore.repository.base.PageSlice;
import edu.rutmiit.demo.cinemacore.service.*;

import static edu.rutmiit.demo.cinemacore.graphql.util.GqlSupport.*;

@DgsComponent
public class CinemaNestedDataFetcher {
    private final CustomerService customerService;
    private final MovieService movieService;
    private final HallService hallService;
    private final ShowService showService;
    private final BookingService bookingService;
    private final TicketService ticketService;
    private final WaitlistService waitlistService;
    private final SeatRepository seatRepository;

    private final CustomerModelAssembler customerAssembler;
    private final MovieModelAssembler movieAssembler;
    private final HallModelAssembler hallAssembler;
    private final ShowModelAssembler showAssembler;
    private final BookingModelAssembler bookingAssembler;
    private final TicketModelAssembler ticketAssembler;
    private final WaitlistModelAssembler waitlistAssembler;

    public CinemaNestedDataFetcher(CustomerService customerService,
                                   MovieService movieService,
                                   HallService hallService,
                                   ShowService showService,
                                   BookingService bookingService,
                                   TicketService ticketService,
                                   WaitlistService waitlistService,
                                   SeatRepository seatRepository,
                                   CustomerModelAssembler customerAssembler,
                                   MovieModelAssembler movieAssembler,
                                   HallModelAssembler hallAssembler,
                                   ShowModelAssembler showAssembler,
                                   BookingModelAssembler bookingAssembler,
                                   TicketModelAssembler ticketAssembler,
                                   WaitlistModelAssembler waitlistAssembler) {
        this.customerService = customerService;
        this.movieService = movieService;
        this.hallService = hallService;
        this.showService = showService;
        this.bookingService = bookingService;
        this.ticketService = ticketService;
        this.waitlistService = waitlistService;
        this.seatRepository = seatRepository;
        this.customerAssembler = customerAssembler;
        this.movieAssembler = movieAssembler;
        this.hallAssembler = hallAssembler;
        this.showAssembler = showAssembler;
        this.bookingAssembler = bookingAssembler;
        this.ticketAssembler = ticketAssembler;
        this.waitlistAssembler = waitlistAssembler;
    }

    @DgsData(parentType = "Customer", field = "bookings")
    public BookingConnectionGql customerBookings(DataFetchingEnvironment dfe,
                                                 @InputArgument Integer page,
                                                 @InputArgument Integer size) {
        CustomerResponse customer = dfe.getSource();
        PageSlice<BookingEntity> slice = bookingService.findAll(customer.getId(), null, null, page(page), size(size));
        return new BookingConnectionGql(content(slice, bookingAssembler), pageInfo(slice), (int) slice.totalElements());
    }

    @DgsData(parentType = "Customer", field = "waitlistEntries")
    public WaitlistEntryConnectionGql customerWaitlistEntries(DataFetchingEnvironment dfe,
                                                             @InputArgument Integer page,
                                                             @InputArgument Integer size) {
        CustomerResponse customer = dfe.getSource();
        PageSlice<WaitlistEntryEntity> slice = waitlistService.findAll(null, customer.getId(), null, page(page), size(size));
        return new WaitlistEntryConnectionGql(content(slice, waitlistAssembler), pageInfo(slice), (int) slice.totalElements());
    }

    @DgsData(parentType = "Movie", field = "shows")
    public ShowConnectionGql movieShows(DataFetchingEnvironment dfe,
                                        @InputArgument Integer page,
                                        @InputArgument Integer size) {
        MovieResponse movie = dfe.getSource();
        PageSlice<ShowEntity> slice = showService.findAll(movie.getId(), null, null, null, page(page), size(size));
        return new ShowConnectionGql(content(slice, showAssembler), pageInfo(slice), (int) slice.totalElements());
    }

    @DgsData(parentType = "Hall", field = "seats")
    public SeatConnectionGql hallSeats(DataFetchingEnvironment dfe,
                                       @InputArgument Integer page,
                                       @InputArgument Integer size) {
        HallResponse hall = dfe.getSource();
        PageSlice<SeatEntity> slice = seatRepository.findByHallId(hall.getId(), page(page), size(size));
        var seats = slice.content().stream().map(this::toSeatResponse).toList();
        return new SeatConnectionGql(seats, pageInfo(slice), (int) slice.totalElements());
    }

    @DgsData(parentType = "Hall", field = "shows")
    public ShowConnectionGql hallShows(DataFetchingEnvironment dfe,
                                       @InputArgument Integer page,
                                       @InputArgument Integer size) {
        HallResponse hall = dfe.getSource();
        PageSlice<ShowEntity> slice = showService.findAll(null, hall.getId(), null, null, page(page), size(size));
        return new ShowConnectionGql(content(slice, showAssembler), pageInfo(slice), (int) slice.totalElements());
    }

    @DgsData(parentType = "Seat", field = "hall")
    public HallResponse seatHall(DataFetchingEnvironment dfe) {
        SeatResponse seat = dfe.getSource();
        return hallAssembler.toModel(hallService.findById(seat.getHallId())).getContent();
    }

    @DgsData(parentType = "Show", field = "movie")
    public MovieResponse showMovie(DataFetchingEnvironment dfe) {
        ShowResponse show = dfe.getSource();
        return movieAssembler.toModel(movieService.findById(show.getMovieId())).getContent();
    }

    @DgsData(parentType = "Show", field = "hall")
    public HallResponse showHall(DataFetchingEnvironment dfe) {
        ShowResponse show = dfe.getSource();
        return hallAssembler.toModel(hallService.findById(show.getHallId())).getContent();
    }

    @DgsData(parentType = "Show", field = "seats")
    public SeatConnectionGql showSeats(DataFetchingEnvironment dfe,
                                       @InputArgument String availabilityStatus,
                                       @InputArgument Integer page,
                                       @InputArgument Integer size) {
        ShowResponse show = dfe.getSource();
        PageSlice<SeatResponse> slice = showService.getShowSeats(show.getId(), availabilityStatus, page(page), size(size));
        return new SeatConnectionGql(slice.content(), pageInfo(slice), (int) slice.totalElements());
    }

    @DgsData(parentType = "Show", field = "bookings")
    public BookingConnectionGql showBookings(DataFetchingEnvironment dfe,
                                             @InputArgument BookingStatus status,
                                             @InputArgument Integer page,
                                             @InputArgument Integer size) {
        ShowResponse show = dfe.getSource();
        PageSlice<BookingEntity> slice = bookingService.findAll(null, show.getId(), status, page(page), size(size));
        return new BookingConnectionGql(content(slice, bookingAssembler), pageInfo(slice), (int) slice.totalElements());
    }

    @DgsData(parentType = "Show", field = "waitlistEntries")
    public WaitlistEntryConnectionGql showWaitlistEntries(DataFetchingEnvironment dfe,
                                                         @InputArgument WaitlistStatus status,
                                                         @InputArgument Integer page,
                                                         @InputArgument Integer size) {
        ShowResponse show = dfe.getSource();
        PageSlice<WaitlistEntryEntity> slice = waitlistService.findAll(show.getId(), null, status, page(page), size(size));
        return new WaitlistEntryConnectionGql(content(slice, waitlistAssembler), pageInfo(slice), (int) slice.totalElements());
    }

    @DgsData(parentType = "Booking", field = "customer")
    public CustomerResponse bookingCustomer(DataFetchingEnvironment dfe) {
        BookingResponse booking = dfe.getSource();
        return customerAssembler.toModel(customerService.findById(booking.getCustomerId())).getContent();
    }

    @DgsData(parentType = "Booking", field = "show")
    public ShowResponse bookingShow(DataFetchingEnvironment dfe) {
        BookingResponse booking = dfe.getSource();
        return showAssembler.toModel(showService.findById(booking.getShowId())).getContent();
    }

    @DgsData(parentType = "Booking", field = "seat")
    public SeatResponse bookingSeat(DataFetchingEnvironment dfe) {
        BookingResponse booking = dfe.getSource();
        return toSeatResponse(seatRepository.findById(booking.getSeatId())
                .orElseThrow(() -> new ResourceNotFoundException("Seat with id=" + booking.getSeatId() + " not found")));
    }

    @DgsData(parentType = "Booking", field = "ticket")
    public TicketResponse bookingTicket(DataFetchingEnvironment dfe) {
        BookingResponse booking = dfe.getSource();
        return ticketService.findOptionalByBookingId(booking.getId())
                .map(ticket -> ticketAssembler.toModel(ticket).getContent())
                .orElse(null);
    }

    @DgsData(parentType = "Ticket", field = "booking")
    public BookingResponse ticketBooking(DataFetchingEnvironment dfe) {
        TicketResponse ticket = dfe.getSource();
        return bookingAssembler.toModel(bookingService.findById(ticket.getBookingId())).getContent();
    }

    @DgsData(parentType = "WaitlistEntry", field = "customer")
    public CustomerResponse waitlistCustomer(DataFetchingEnvironment dfe) {
        WaitlistResponse entry = dfe.getSource();
        return customerAssembler.toModel(customerService.findById(entry.getCustomerId())).getContent();
    }

    @DgsData(parentType = "WaitlistEntry", field = "show")
    public ShowResponse waitlistShow(DataFetchingEnvironment dfe) {
        WaitlistResponse entry = dfe.getSource();
        return showAssembler.toModel(showService.findById(entry.getShowId())).getContent();
    }

    @DgsData(parentType = "WaitlistEntry", field = "seat")
    public SeatResponse waitlistSeat(DataFetchingEnvironment dfe) {
        WaitlistResponse entry = dfe.getSource();
        if (entry.getSeatId() == null) {
            return null;
        }
        return toSeatResponse(seatRepository.findById(entry.getSeatId())
                .orElseThrow(() -> new ResourceNotFoundException("Seat with id=" + entry.getSeatId() + " not found")));
    }

    private SeatResponse toSeatResponse(SeatEntity seat) {
        return SeatResponse.builder()
                .id(seat.getId())
                .hallId(seat.getHallId())
                .rowNumber(seat.getRowNumber())
                .seatNumber(seat.getSeatNumber())
                .seatType(seat.getSeatType())
                .active(seat.getActive())
                .availabilityStatus(null)
                .build();
    }
}
