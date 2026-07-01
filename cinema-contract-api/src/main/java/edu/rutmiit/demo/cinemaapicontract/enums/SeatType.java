package edu.rutmiit.demo.cinemaapicontract.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Тип места")
public enum SeatType {
    STANDARD,
    VIP,
    SOFA
}
