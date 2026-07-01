package edu.rutmiit.demo.cinemaapicontract.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Тип кинозала")
public enum HallType {
    STANDARD,
    IMAX,
    VIP,
    DOLBY_ATMOS
}
