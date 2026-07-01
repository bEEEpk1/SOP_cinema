package edu.rutmiit.demo.cinemacore.graphql.types;

import java.time.OffsetDateTime;

public record CreateShowInputGql(String movieId, String hallId, OffsetDateTime startTime, OffsetDateTime endTime, Double basePrice, String currency) {}
