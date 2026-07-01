package edu.rutmiit.demo.cinemaapicontract.dto;

import edu.rutmiit.demo.cinemaapicontract.enums.ShowStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "Запрос на частичное обновление сеанса (PATCH). null = не менять поле")
public record PatchShowRequest(
        @Schema(description = "ID фильма", example = "1")
        Long movieId,

        @Schema(description = "ID зала", example = "2")
        Long hallId,

        @Schema(description = "Дата и время начала сеанса", example = "2026-05-10T19:30:00+03:00")
        OffsetDateTime startTime,

        @Schema(description = "Дата и время окончания сеанса", example = "2026-05-10T22:19:00+03:00")
        OffsetDateTime endTime,

        @Schema(description = "Базовая цена сеанса", example = "450.00")
        @DecimalMin(value = "0.0", inclusive = true, message = "Базовая цена не может быть отрицательной")
        BigDecimal basePrice,

        @Schema(description = "Валюта", example = "RUB")
        String currency,

        @Schema(description = "Статус сеанса")
        ShowStatus status
) {
}
