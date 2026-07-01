package edu.rutmiit.demo.cinemaapicontract.dto;

import edu.rutmiit.demo.cinemaapicontract.validation.ValidPhone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на частичное обновление пользователя. null = не менять поле")
public record PatchCustomerRequest(
        @Schema(description = "Новый email пользователя", example = "new.address@example.com")
        @Email(message = "Некорректный формат email")
        @Size(max = 255, message = "Email не может превышать 255 символов")
        String email,

        @Schema(description = "Новый номер телефона", example = "+79990001122")
        @ValidPhone
        @Size(max = 20, message = "Телефон не может превышать 20 символов")
        String phone
) {
}
