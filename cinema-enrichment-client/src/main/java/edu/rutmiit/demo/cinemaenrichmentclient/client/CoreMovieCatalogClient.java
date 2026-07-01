package edu.rutmiit.demo.cinemaenrichmentclient.client;

import edu.rutmiit.demo.cinemaapicontract.dto.MovieRecommendationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;


@Slf4j
@Component
public class CoreMovieCatalogClient {

    private final RestClient restClient;

    public CoreMovieCatalogClient(
            RestClient.Builder builder,
            @Value("${cinema.core.base-url:http://localhost:8080}") String cinemaCoreBaseUrl
    ) {
        this.restClient = builder.baseUrl(cinemaCoreBaseUrl).build();
    }

    public List<MovieRecommendationResponse> findRecommendationsByGenre(String genre, Long excludeMovieId, int limit) {
        if (genre == null || genre.isBlank()) {
            return List.of();
        }

        try {
            List<MovieRecommendationResponse> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/movies/recommendations")
                            .queryParam("genre", genre)
                            .queryParamIfPresent("excludeMovieId", Optional.ofNullable(excludeMovieId))
                            .queryParam("limit", limit)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<MovieRecommendationResponse>>() {
                    });

            return response == null ? List.of() : response;
        } catch (RestClientException ex) {
            log.warn(
                    "Could not load movie recommendations from cinema-core: genre={} excludeMovieId={} reason={}",
                    genre,
                    excludeMovieId,
                    ex.getMessage()
            );
            return List.of();
        }
    }
}
