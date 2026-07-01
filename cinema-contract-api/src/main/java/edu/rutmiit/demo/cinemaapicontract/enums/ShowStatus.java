package edu.rutmiit.demo.cinemaapicontract.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статус сеанса")
public enum ShowStatus {
    SCHEDULED,
    CANCELLED,
    FINISHED
}
