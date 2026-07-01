package edu.rutmiit.demo.cinemacore.graphql.types;

import edu.rutmiit.demo.cinemaapicontract.dto.HallResponse;
import java.util.List;

public record HallConnectionGql(List<HallResponse> content, PageInfoGql pageInfo, int totalElements) {}
