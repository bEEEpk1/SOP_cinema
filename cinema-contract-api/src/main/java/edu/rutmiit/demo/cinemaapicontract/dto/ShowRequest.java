package edu.rutmiit.demo.cinemaapicontract.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "Запрос на создание сеанса")
public record ShowRequest(
        @Schema(description = "ID фильма", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "ID фильма обязателен")
        Long movieId,

        @Schema(description = "ID зала", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "ID зала обязателен")
        Long hallId,

        @Schema(description = "Дата и время начала сеанса", example = "2026-05-10T19:30:00+03:00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Время начала сеанса обязательно")
        OffsetDateTime startTime,

        @Schema(description = "Дата и время окончания сеанса", example = "2026-05-10T22:19:00+03:00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Время окончания сеанса обязательно")
        OffsetDateTime endTime,

        @Schema(description = "Базовая цена сеанса", example = "450.00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Базовая цена обязательна")
        @DecimalMin(value = "0.0", inclusive = true, message = "Базовая цена не может быть отрицательной")
        BigDecimal basePrice,

        @Schema(description = "Валюта", example = "RUB", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Валюта обязательна")
        String currency
) {
}
