package edu.rutmiit.demo.cinemaapicontract.dto;

import edu.rutmiit.demo.cinemaapicontract.enums.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;

@Schema(description = "Запрос на частичное обновление бронирования (PATCH). Используется для смены статуса, фиксации оплаты и корректировки контактного email")
public record PatchBookingRequest(
        @Schema(description = "Новый контактный email", example = "ivan.petrov@example.com")
        @Email(message = "Некорректный формат email")
        String customerEmail,

        @Schema(description = "Новый статус бронирования")
        BookingStatus status,

        @Schema(description = "Внешний идентификатор платежа", example = "pay_01JQ6N9A6H8S1XYZ")
        String paymentReference,

        @Schema(description = "Фактически применённые бонусные баллы", example = "150")
        @Min(value = 0, message = "Количество бонусов не может быть отрицательным")
        Integer loyaltyPointsUsed
) {
}
