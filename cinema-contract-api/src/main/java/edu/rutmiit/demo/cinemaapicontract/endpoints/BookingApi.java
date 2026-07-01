package edu.rutmiit.demo.cinemaapicontract.endpoints;

import edu.rutmiit.demo.cinemaapicontract.config.CinemaApiContractConfig;
import edu.rutmiit.demo.cinemaapicontract.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Bookings", description = "Управление бронированиями")
@RequestMapping(value = "/api/bookings", produces = MediaType.APPLICATION_JSON_VALUE)
public interface BookingApi {

    @Operation(summary = "Получить бронирование по ID", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "200", description = "Бронирование найдено")
    @ApiResponse(responseCode = "404", description = "Бронирование не найдено", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    EntityModel<BookingResponse> getBookingById(@Parameter(description = "ID бронирования", required = true, example = "500") @PathVariable Long id);

    @Operation(summary = "Список бронирований", description = "Фильтрация по пользователю, сеансу и статусу.", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "200", description = "Постраничный список бронирований")
    @GetMapping
    PagedModel<EntityModel<BookingResponse>> getAllBookings(
            @Parameter(description = "ID пользователя", example = "1001") @RequestParam(required = false) Long customerId,
            @Parameter(description = "ID сеанса", example = "10") @RequestParam(required = false) Long showId,
            @Parameter(description = "Статус бронирования", example = "PAID") @RequestParam(required = false) String status,
            @Parameter(description = "Номер страницы", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20") @RequestParam(defaultValue = "20") int size
    );

    @Operation(summary = "Создать бронирование", description = "Создаёт бронирование в статусе PENDING_PAYMENT и резервирует место на ограниченное время.", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "201", description = "Бронирование создано")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Сеанс, место или пользователь не найдены", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Место уже занято или недоступно", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<EntityModel<BookingResponse>> createBooking(@Valid @RequestBody BookingRequest request);

    @Operation(summary = "Частично обновить бронирование", description = "Используется для фиксации оплаты, внешней ссылки на платёж или смены статуса доменной логикой.", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "200", description = "Бронирование обновлено")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Бронирование не найдено", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Некорректный переход статуса", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    EntityModel<BookingResponse> patchBooking(@Parameter(description = "ID бронирования", required = true, example = "500") @PathVariable Long id,
                                              @Valid @RequestBody PatchBookingRequest request);

    @Operation(summary = "Отменить бронирование", description = "Отменяет бронирование, освобождает место и запускает последующую доменную обработку (например, уведомления).", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "204", description = "Бронирование отменено")
    @ApiResponse(responseCode = "404", description = "Бронирование не найдено", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Бронирование нельзя отменить в текущем статусе", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cancelBooking(@Parameter(description = "ID бронирования", required = true, example = "500") @PathVariable Long id);

    @Operation(summary = "Билет по бронированию", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "200", description = "Билет найден")
    @ApiResponse(responseCode = "404", description = "Бронирование или билет не найдены", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}/ticket")
    EntityModel<TicketResponse> getTicketByBookingId(@Parameter(description = "ID бронирования", required = true, example = "500") @PathVariable Long id);
}
