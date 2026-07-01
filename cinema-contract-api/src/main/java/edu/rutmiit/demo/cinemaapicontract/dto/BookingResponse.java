package edu.rutmiit.demo.cinemaapicontract.dto;

import edu.rutmiit.demo.cinemaapicontract.enums.BookingStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Relation(collectionRelation = "bookings", itemRelation = "booking")
@Schema(description = "Информация о бронировании")
public class BookingResponse extends RepresentationModel<BookingResponse> {

    @Schema(description = "ID бронирования", example = "500")
    private final Long id;

    @Schema(description = "ID сеанса", example = "10")
    private final Long showId;

    @Schema(description = "ID места", example = "101")
    private final Long seatId;

    @Schema(description = "ID пользователя", example = "1001")
    private final Long customerId;

    @Schema(description = "Контактный email на момент брони", example = "ivan.petrov@example.com")
    private final String customerEmail;

    @Schema(description = "Статус бронирования")
    private final BookingStatus status;

    @Schema(description = "До какого времени бронь удерживает место без оплаты", example = "2026-05-10T19:40:00+03:00")
    private final OffsetDateTime reservedUntil;

    @Schema(description = "Итоговая зафиксированная цена брони", example = "390.00")
    private final BigDecimal finalPrice;

    @Schema(description = "Валюта", example = "RUB")
    private final String currency;

    @Schema(description = "Идентификатор платежа", example = "pay_01JQ6N9A6H8S1XYZ")
    private final String paymentReference;

    @Schema(description = "Количество использованных бонусов", example = "150")
    private final Integer loyaltyPointsUsed;
}
