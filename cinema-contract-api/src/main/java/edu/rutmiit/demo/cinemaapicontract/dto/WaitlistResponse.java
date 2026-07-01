package edu.rutmiit.demo.cinemaapicontract.dto;

import edu.rutmiit.demo.cinemaapicontract.enums.WaitlistStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

@Getter
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(collectionRelation = "waitlistEntries", itemRelation = "waitlistEntry")
@Schema(description = "Запись в листе ожидания")
public class WaitlistResponse extends RepresentationModel<WaitlistResponse> {

    @Schema(description = "ID записи листа ожидания", example = "700")
    private final Long id;

    @Schema(description = "ID сеанса", example = "10")
    private final Long showId;

    @Schema(description = "ID конкретного места, если подписка оформлена на одно место", example = "101")
    private final Long seatId;

    @Schema(description = "ID пользователя", example = "1001")
    private final Long customerId;

    @Schema(description = "Email для уведомлений", example = "ivan.petrov@example.com")
    private final String customerEmail;

    @Schema(description = "Статус записи листа ожидания")
    private final WaitlistStatus status;
}
