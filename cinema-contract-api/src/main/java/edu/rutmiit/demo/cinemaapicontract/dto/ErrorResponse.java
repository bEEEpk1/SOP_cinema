package edu.rutmiit.demo.cinemaapicontract.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Стандартный ответ об ошибке (RFC 7807 Problem Details)")
public record ErrorResponse(
        @Schema(description = "HTTP статус-код", example = "404")
        int status,

        @Schema(description = "URI-идентификатор типа ошибки", example = "https://api.cinema.local/problems/resource-not-found")
        String type,

        @Schema(description = "Краткое название ошибки", example = "Ресурс не найден")
        String title,

        @Schema(description = "Подробности конкретного случая", example = "Show with id=42 not found")
        String detail,

        @Schema(description = "URI запроса, приведшего к ошибке", example = "/api/shows/42")
        String instance,

        @Schema(description = "Момент возникновения ошибки (UTC)", example = "2026-03-03T10:15:30Z")
        Instant timestamp,

        @Schema(description = "Ошибки по отдельным полям (для 400 Bad Request)")
        List<FieldError> fieldErrors
) {
    @Schema(description = "Ошибка валидации поля")
    public record FieldError(
            @Schema(description = "Имя поля", example = "email")
            String field,
            @Schema(description = "Отклонённое значение", example = "bad-email")
            Object rejectedValue,
            @Schema(description = "Причина отклонения", example = "Некорректный формат email")
            String message
    ) {
    }
}
