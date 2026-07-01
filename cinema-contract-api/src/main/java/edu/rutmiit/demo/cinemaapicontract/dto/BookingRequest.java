package edu.rutmiit.demo.cinemaapicontract.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Запрос на создание бронирования")
public record BookingRequest(
        @Schema(description = "ID сеанса", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "ID сеанса обязателен")
        Long showId,

        @Schema(description = "ID места", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "ID места обязателен")
        Long seatId,

        @Schema(description = "ID пользователя", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "ID пользователя обязателен")
        Long customerId,

        @Schema(description = "Контактный email для билета и уведомлений", example = "ivan.petrov@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email обязателен")
        @Email(message = "Некорректный формат email")
        String customerEmail,

        @Schema(description = "Количество бонусных баллов, которые пользователь хочет использовать", example = "150")
        @Min(value = 0, message = "Количество бонусов не может быть отрицательным")
        Integer loyaltyPointsUsed
) {
}
