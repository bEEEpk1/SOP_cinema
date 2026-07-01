package edu.rutmiit.demo.cinemaapicontract.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

@Getter
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(collectionRelation = "movies", itemRelation = "movie")
@Schema(description = "Информация о фильме")
public class MovieResponse extends RepresentationModel<MovieResponse> {

    @Schema(description = "ID фильма", example = "1")
    private final Long id;

    @Schema(description = "Название фильма", example = "Интерстеллар")
    private final String title;

    @Schema(description = "Описание фильма")
    private final String description;

    @Schema(description = "Длительность фильма в минутах", example = "169")
    private final Integer durationMinutes;

    @Schema(description = "Возрастное ограничение", example = "12+")
    private final String ageRating;

    @Schema(description = "Жанр фильма", example = "Sci-Fi")
    private final String genre;

    @Schema(description = "Признак активности фильма", example = "true")
    private final Boolean active;
}
