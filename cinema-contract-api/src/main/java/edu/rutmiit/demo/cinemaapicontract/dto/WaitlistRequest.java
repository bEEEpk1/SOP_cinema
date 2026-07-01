package edu.rutmiit.demo.cinemaapicontract.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Запрос на подписку в лист ожидания")
public record WaitlistRequest(
        @Schema(description = "ID сеанса", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "ID сеанса обязателен")
        Long showId,

        @Schema(description = "ID конкретного места. null означает подписку на любой билет сеанса", example = "101")
        Long seatId,

        @Schema(description = "ID пользователя", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "ID пользователя обязателен")
        Long customerId,

        @Schema(description = "Email для уведомления", example = "ivan.petrov@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email обязателен")
        @Email(message = "Некорректный формат email")
        String customerEmail
) {
}
