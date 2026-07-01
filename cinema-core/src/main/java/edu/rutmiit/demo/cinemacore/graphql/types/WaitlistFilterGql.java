package edu.rutmiit.demo.cinemacore.graphql.types;

import edu.rutmiit.demo.cinemaapicontract.enums.WaitlistStatus;

public record WaitlistFilterGql(String showId, String customerId, WaitlistStatus status) {}
