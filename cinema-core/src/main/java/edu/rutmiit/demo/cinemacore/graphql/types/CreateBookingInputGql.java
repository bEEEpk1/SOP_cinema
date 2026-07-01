package edu.rutmiit.demo.cinemacore.graphql.types;

public record CreateBookingInputGql(String showId, String seatId, String customerId, String customerEmail, Integer loyaltyPointsUsed) {}
