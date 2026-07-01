package edu.rutmiit.demo.cinemacore.graphql.types;

import edu.rutmiit.demo.cinemaapicontract.dto.CustomerResponse;
import java.util.List;

public record CustomerConnectionGql(List<CustomerResponse> content, PageInfoGql pageInfo, int totalElements) {}
