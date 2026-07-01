package edu.rutmiit.demo.cinemaapicontract.dto;

import edu.rutmiit.demo.cinemaapicontract.enums.ShowStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(collectionRelation = "shows", itemRelation = "show")
@Schema(description = "Информация о сеансе")
public class ShowResponse extends RepresentationModel<ShowResponse> {

    @Schema(description = "ID сеанса", example = "10")
    private final Long id;

    @Schema(description = "ID фильма", example = "1")
    private final Long movieId;

    @Schema(description = "ID зала", example = "2")
    private final Long hallId;

    @Schema(description = "Дата и время начала сеанса", example = "2026-05-10T19:30:00+03:00")
    private final OffsetDateTime startTime;

    @Schema(description = "Дата и время окончания сеанса", example = "2026-05-10T22:19:00+03:00")
    private final OffsetDateTime endTime;

    @Schema(description = "Базовая цена сеанса", example = "450.00")
    private final BigDecimal basePrice;

    @Schema(description = "Валюта", example = "RUB")
    private final String currency;

    @Schema(description = "Статус сеанса")
    private final ShowStatus status;
}
