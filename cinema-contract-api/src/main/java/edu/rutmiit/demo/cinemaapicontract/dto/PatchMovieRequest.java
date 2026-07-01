package edu.rutmiit.demo.cinemaapicontract.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на частичное обновление фильма (PATCH). null = не менять поле")
public record PatchMovieRequest(
        @Schema(description = "Название фильма", example = "Интерстеллар")
        @Size(max = 255, message = "Название фильма не может превышать 255 символов")
        String title,

        @Schema(description = "Описание фильма")
        @Size(max = 4000, message = "Описание не может превышать 4000 символов")
        String description,

        @Schema(description = "Длительность фильма в минутах", example = "169")
        @Positive(message = "Длительность фильма должна быть положительной")
        Integer durationMinutes,

        @Schema(description = "Возрастное ограничение", example = "12+")
        @Size(max = 10, message = "Возрастное ограничение не может превышать 10 символов")
        String ageRating,

        @Schema(description = "Жанр фильма", example = "Sci-Fi")
        @Size(max = 100, message = "Жанр не может превышать 100 символов")
        String genre,

        @Schema(description = "Активен ли фильм", example = "true")
        Boolean active
) {
}
