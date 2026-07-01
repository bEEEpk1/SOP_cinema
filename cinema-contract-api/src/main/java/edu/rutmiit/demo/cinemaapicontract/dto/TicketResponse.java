package edu.rutmiit.demo.cinemaapicontract.dto;

import edu.rutmiit.demo.cinemaapicontract.enums.TicketStatus;
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
@Relation(collectionRelation = "tickets", itemRelation = "ticket")
@Schema(description = "Информация о билете")
public class TicketResponse extends RepresentationModel<TicketResponse> {

    @Schema(description = "ID билета", example = "900")
    private final Long id;

    @Schema(description = "ID бронирования", example = "500")
    private final Long bookingId;

    @Schema(description = "Номер билета", example = "CIN-2026-000900")
    private final String ticketNumber;

    @Schema(description = "Статус билета")
    private final TicketStatus status;

    @Schema(description = "Данные QR-кода", example = "ticket:900:secure-payload")
    private final String qrCode;
}
