package edu.rutmiit.demo.cinemaapicontract.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статус бронирования")
public enum BookingStatus {
    PENDING_PAYMENT,
    PAID,
    CANCELLED,
    EXPIRED
}
