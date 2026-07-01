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

@Tag(name = "Shows", description = "Управление сеансами")
@RequestMapping(value = "/api/shows", produces = MediaType.APPLICATION_JSON_VALUE)
public interface ShowApi {

    @Operation(summary = "Получить сеанс по ID", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "200", description = "Сеанс найден")
    @ApiResponse(responseCode = "404", description = "Сеанс не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    EntityModel<ShowResponse> getShowById(@Parameter(description = "ID сеанса", required = true, example = "10") @PathVariable Long id);

    @Operation(summary = "Список сеансов", description = "Фильтрация по фильму, залу и статусу. Возвращает постраничный список сеансов.", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "200", description = "Постраничный список сеансов")
    @GetMapping
    PagedModel<EntityModel<ShowResponse>> getAllShows(
            @Parameter(description = "Фильтр по ID фильма", example = "1") @RequestParam(required = false) Long movieId,
            @Parameter(description = "Фильтр по ID зала", example = "2") @RequestParam(required = false) Long hallId,
            @Parameter(description = "Статус сеанса", example = "SCHEDULED") @RequestParam(required = false) String status,
            @Parameter(description = "Дата сеанса", example = "2026-05-10") @RequestParam(required = false) String showDate,
            @Parameter(description = "Номер страницы", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20") @RequestParam(defaultValue = "20") int size
    );

    @Operation(summary = "Создать сеанс", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "201", description = "Сеанс создан")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Фильм или зал не найдены", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<EntityModel<ShowResponse>> createShow(@Valid @RequestBody ShowRequest request);

    @Operation(summary = "Частично обновить сеанс (PATCH)", description = "Изменяет только переданные поля сеанса.", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "200", description = "Сеанс обновлён")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Сеанс не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    EntityModel<ShowResponse> patchShow(@Parameter(description = "ID сеанса", required = true, example = "10") @PathVariable Long id,
                                        @Valid @RequestBody PatchShowRequest request);

    @Operation(summary = "Места сеанса", description = "Возвращает список мест зала в контексте конкретного seанса с информацией о доступности.", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "200", description = "Список мест сеанса")
    @ApiResponse(responseCode = "404", description = "Сеанс не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}/seats")
    PagedModel<EntityModel<SeatResponse>> getShowSeats(
            @Parameter(description = "ID сеанса", required = true, example = "10") @PathVariable Long id,
            @Parameter(description = "Статус доступности места", example = "FREE") @RequestParam(required = false) String availabilityStatus,
            @Parameter(description = "Номер страницы", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "50") @RequestParam(defaultValue = "50") int size
    );
}
