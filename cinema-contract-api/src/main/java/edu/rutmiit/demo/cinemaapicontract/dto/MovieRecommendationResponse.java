package edu.rutmiit.demo.cinemaapicontract.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Краткая рекомендация фильма для enrichment-сценария")
public record MovieRecommendationResponse(
        @Schema(description = "ID фильма", example = "4")
        Long id,

        @Schema(description = "Название фильма", example = "Дюна: Часть вторая")
        String title,

        @Schema(description = "Жанр фильма", example = "SCI_FI")
        String genre,

        @Schema(description = "Длительность фильма в минутах", example = "166")
        Integer durationMinutes
) {
}
