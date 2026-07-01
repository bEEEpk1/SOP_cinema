package edu.rutmiit.demo.cinemacore.graphql.types;

import edu.rutmiit.demo.cinemaapicontract.dto.WaitlistResponse;
import java.util.List;

public record WaitlistEntryConnectionGql(List<WaitlistResponse> content, PageInfoGql pageInfo, int totalElements) {}
