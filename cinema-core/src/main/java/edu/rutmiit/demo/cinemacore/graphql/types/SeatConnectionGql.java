package edu.rutmiit.demo.cinemacore.graphql.types;

import edu.rutmiit.demo.cinemaapicontract.dto.SeatResponse;
import java.util.List;

public record SeatConnectionGql(List<SeatResponse> content, PageInfoGql pageInfo, int totalElements) {}
