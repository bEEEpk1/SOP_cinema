package edu.rutmiit.demo.cinemaeventscontract;


public final class RoutingKeys {

    private RoutingKeys() {
    }

    public static final String EXCHANGE = "cinema.events.exchange";

    public static final String BOOKING_CREATED = "booking.created";
    public static final String BOOKING_PAID = "booking.paid";
    public static final String BOOKING_EXPIRED = "booking.expired";
    public static final String BOOKING_CANCELLED = "booking.cancelled";

    public static final String TICKET_CREATED = "ticket.created";
    public static final String TICKET_ENRICHED = "ticket.enriched";

    public static final String SEAT_RELEASED = "seat.released";

    public static final String WAITLIST_USER_NOTIFIED = "waitlist.user.notified";

    public static final String LOYALTY_POINTS_EARNED = "loyalty.points.earned";
    public static final String LOYALTY_POINTS_ROLLBACK_APPLIED = "loyalty.points.rollback.applied";

    public static final String ALL_BOOKING_EVENTS = "booking.*";
    public static final String ALL_TICKET_EVENTS = "ticket.*";
    public static final String ALL_WAITLIST_EVENTS = "waitlist.*";
    public static final String ALL_LOYALTY_EVENTS = "loyalty.*";
    public static final String ALL_EVENTS = "#";
}
