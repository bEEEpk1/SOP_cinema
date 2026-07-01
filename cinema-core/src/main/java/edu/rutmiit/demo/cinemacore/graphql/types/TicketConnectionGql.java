package edu.rutmiit.demo.cinemacore.graphql.types;

import edu.rutmiit.demo.cinemaapicontract.dto.TicketResponse;
import java.util.List;

public record TicketConnectionGql(List<TicketResponse> content, PageInfoGql pageInfo, int totalElements) {}
