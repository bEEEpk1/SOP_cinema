package edu.rutmiit.demo.cinemaapicontract.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статус билета")
public enum TicketStatus {
    ACTIVE,
    INVALID,
    USED
}
