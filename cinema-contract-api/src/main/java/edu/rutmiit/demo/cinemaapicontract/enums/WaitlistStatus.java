package edu.rutmiit.demo.cinemaapicontract.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статус записи листа ожидания")
public enum WaitlistStatus {
    ACTIVE,
    NOTIFIED,
    FULFILLED,
    CANCELLED,
    EXPIRED
}
