package edu.rutmiit.demo.cinemaapicontract.dto;


import edu.rutmiit.demo.cinemaapicontract.enums.HallType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на создание зала")
public record HallRequest(
        @Schema(description = "Название или номер зала", example = "Зал 1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Название зала обязательно")
        @Size(max = 100, message = "Название зала не может превышать 100 символов")
        String name,

        @Schema(description = "Тип зала", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Тип зала обязателен")
        HallType hallType,

        @Schema(description = "Вместимость зала", example = "120", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Вместимость обязательна")
        @Positive(message = "Вместимость должна быть положительной")
        Integer capacity
) {
}
