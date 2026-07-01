package edu.rutmiit.demo.cinemaapicontract.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.time.OffsetDateTime;

@Getter
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(collectionRelation = "customers", itemRelation = "customer")
@Schema(description = "Информация о пользователе")
public class CustomerResponse extends RepresentationModel<CustomerResponse> {

    @Schema(description = "ID пользователя", example = "1001")
    private final Long id;

    @Schema(description = "Основной email пользователя", example = "ivan.petrov@example.com")
    private final String email;

    @Schema(description = "Номер телефона пользователя", example = "+79991234567")
    private final String phone;

    @Schema(description = "Признак зарегистрированного пользователя", example = "true")
    private final Boolean registered;

    @Schema(description = "Дата и время создания пользователя", example = "2026-05-10T18:15:00+03:00")
    private final OffsetDateTime createdAt;
}
