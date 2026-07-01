package edu.rutmiit.demo.cinemacore.graphql.types;

import edu.rutmiit.demo.cinemaapicontract.dto.ShowResponse;
import java.util.List;

public record ShowConnectionGql(List<ShowResponse> content, PageInfoGql pageInfo, int totalElements) {}
