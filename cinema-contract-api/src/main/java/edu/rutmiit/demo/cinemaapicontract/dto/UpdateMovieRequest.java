package edu.rutmiit.demo.cinemaapicontract.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на полное обновление фильма (PUT)")
public record UpdateMovieRequest(
        @Schema(description = "Название фильма", example = "Интерстеллар", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Название фильма не может быть пустым")
        @Size(max = 255, message = "Название фильма не может превышать 255 символов")
        String title,

        @Schema(description = "Описание фильма")
        @Size(max = 4000, message = "Описание не может превышать 4000 символов")
        String description,

        @Schema(description = "Длительность фильма в минутах", example = "169", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Длительность фильма обязательна")
        @Positive(message = "Длительность фильма должна быть положительной")
        Integer durationMinutes,

        @Schema(description = "Возрастное ограничение", example = "12+", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Возрастное ограничение обязательно")
        @Size(max = 10, message = "Возрастное ограничение не может превышать 10 символов")
        String ageRating,

        @Schema(description = "Жанр фильма", example = "Sci-Fi", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Жанр обязателен")
        @Size(max = 100, message = "Жанр не может превышать 100 символов")
        String genre,

        @Schema(description = "Активен ли фильм", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Флаг active обязателен")
        Boolean active
) {
}
