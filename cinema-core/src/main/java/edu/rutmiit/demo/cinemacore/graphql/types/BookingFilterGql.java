package edu.rutmiit.demo.cinemacore.graphql.types;

import edu.rutmiit.demo.cinemaapicontract.enums.BookingStatus;

public record BookingFilterGql(String customerId, String showId, BookingStatus status) {}
