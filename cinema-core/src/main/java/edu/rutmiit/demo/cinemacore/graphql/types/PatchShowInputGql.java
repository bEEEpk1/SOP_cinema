package edu.rutmiit.demo.cinemacore.graphql.types;

import edu.rutmiit.demo.cinemaapicontract.enums.ShowStatus;
import java.time.OffsetDateTime;

public record PatchShowInputGql(String movieId, String hallId, OffsetDateTime startTime, OffsetDateTime endTime, Double basePrice, String currency, ShowStatus status) {}
