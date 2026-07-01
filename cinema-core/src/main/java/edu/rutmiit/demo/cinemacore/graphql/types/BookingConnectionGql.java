package edu.rutmiit.demo.cinemacore.graphql.types;

import edu.rutmiit.demo.cinemaapicontract.dto.BookingResponse;
import java.util.List;

public record BookingConnectionGql(List<BookingResponse> content, PageInfoGql pageInfo, int totalElements) {}
