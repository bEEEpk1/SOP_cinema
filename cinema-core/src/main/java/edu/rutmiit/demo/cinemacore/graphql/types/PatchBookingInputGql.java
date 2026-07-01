package edu.rutmiit.demo.cinemacore.graphql.types;

import edu.rutmiit.demo.cinemaapicontract.enums.BookingStatus;

public record PatchBookingInputGql(String customerEmail, BookingStatus status, String paymentReference, Integer loyaltyPointsUsed) {}
