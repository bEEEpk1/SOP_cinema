package edu.rutmiit.demo.cinemaapicontract.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сводка по бонусному счёту пользователя")
public record LoyaltySummaryResponse(
        @Schema(description = "ID пользователя", example = "4")
        Long customerId,

        @Schema(description = "Email пользователя", example = "maria.ivanova@example.com")
        String email,

        @Schema(description = "Зарегистрирован ли пользователь", example = "true")
        Boolean registered,

        @Schema(description = "Текущий баланс бонусов", example = "116")
        Integer balance
) {
}
