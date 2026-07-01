package edu.rutmiit.demo.cinemaapicontract.dto;

import edu.rutmiit.demo.cinemaapicontract.validation.ValidPhone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на создание пользователя")
public record CustomerRequest(
        @Schema(description = "Основной email пользователя", example = "ivan.petrov@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email обязателен")
        @Email(message = "Некорректный формат email")
        @Size(max = 255, message = "Email не может превышать 255 символов")
        String email,

        @Schema(description = "Номер телефона пользователя в международном формате", example = "+79991234567", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Телефон обязателен")
        @ValidPhone
        @Size(max = 20, message = "Телефон не может превышать 20 символов")
        String phone,

        @Schema(description = "Признак зарегистрированного пользователя", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Признак registered обязателен")
        Boolean registered,

        @Schema(description = "Хэш пароля для зарегистрированного пользователя", example = "$2a$10$abcdefghijklmnopqrstuv", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 255, message = "Хэш пароля не может превышать 255 символов")
        String passwordHash
) {
}
