package edu.rutmiit.demo.cinemacore.graphql.types;

import edu.rutmiit.demo.cinemaapicontract.dto.MovieResponse;
import java.util.List;

public record MovieConnectionGql(List<MovieResponse> content, PageInfoGql pageInfo, int totalElements) {}
