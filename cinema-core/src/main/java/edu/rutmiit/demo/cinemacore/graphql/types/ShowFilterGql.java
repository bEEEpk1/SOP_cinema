package edu.rutmiit.demo.cinemacore.graphql.types;

import edu.rutmiit.demo.cinemaapicontract.enums.ShowStatus;
import java.time.LocalDate;

public record ShowFilterGql(String movieId, String hallId, ShowStatus status, LocalDate showDate) {}
