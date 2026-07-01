package edu.rutmiit.demo.cinemaapicontract.dto;

import edu.rutmiit.demo.cinemaapicontract.enums.SeatType;
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
@Relation(collectionRelation = "seats", itemRelation = "seat")
@Schema(description = "Место в зале или в контексте конкретного сеанса")
public class SeatResponse extends RepresentationModel<SeatResponse> {

    @Schema(description = "ID места", example = "101")
    private final Long id;

    @Schema(description = "ID зала", example = "2")
    private final Long hallId;

    @Schema(description = "Номер ряда", example = "5")
    private final Integer rowNumber;

    @Schema(description = "Номер места в ряду", example = "8")
    private final Integer seatNumber;

    @Schema(description = "Тип места")
    private final SeatType seatType;

    @Schema(description = "Активно ли место", example = "true")
    private final Boolean active;

    @Schema(description = "Статус места в контексте сеанса", example = "FREE")
    private final String availabilityStatus;
}
