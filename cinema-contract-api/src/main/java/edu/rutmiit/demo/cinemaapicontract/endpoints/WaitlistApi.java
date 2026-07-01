package edu.rutmiit.demo.cinemaapicontract.endpoints;

import edu.rutmiit.demo.cinemaapicontract.config.CinemaApiContractConfig;
import edu.rutmiit.demo.cinemaapicontract.dto.ErrorResponse;
import edu.rutmiit.demo.cinemaapicontract.dto.WaitlistRequest;
import edu.rutmiit.demo.cinemaapicontract.dto.WaitlistResponse;
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

@Tag(name = "Waitlist", description = "Лист ожидания по сеансам и местам")
@RequestMapping(value = "/api/waitlist", produces = MediaType.APPLICATION_JSON_VALUE)
public interface WaitlistApi {

    @Operation(summary = "Получить запись листа ожидания по ID", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "200", description = "Запись найдена")
    @ApiResponse(responseCode = "404", description = "Запись не найдена", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    EntityModel<WaitlistResponse> getWaitlistEntryById(@Parameter(description = "ID записи листа ожидания", required = true, example = "700") @PathVariable Long id);

    @Operation(summary = "Список записей листа ожидания", description = "Фильтрация по пользователю, сеансу и статусу.", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "200", description = "Постраничный список записей")
    @GetMapping
    PagedModel<EntityModel<WaitlistResponse>> getAllWaitlistEntries(
            @Parameter(description = "ID сеанса", example = "10") @RequestParam(required = false) Long showId,
            @Parameter(description = "ID пользователя", example = "1001") @RequestParam(required = false) Long customerId,
            @Parameter(description = "Статус записи", example = "ACTIVE") @RequestParam(required = false) String status,
            @Parameter(description = "Номер страницы", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20") @RequestParam(defaultValue = "20") int size
    );

    @Operation(summary = "Подписаться в лист ожидания", description = "Создаёт запись листа ожидания на весь сеанс или на конкретное место. Очередь определяется порядком ID.", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "201", description = "Запись листа ожидания создана")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Сеанс, место или пользователь не найдены", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<EntityModel<WaitlistResponse>> createWaitlistEntry(@Valid @RequestBody WaitlistRequest request);

    @Operation(summary = "Отменить подписку в листе ожидания", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "204", description = "Подписка отменена")
    @ApiResponse(responseCode = "404", description = "Запись не найдена", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cancelWaitlistEntry(@Parameter(description = "ID записи листа ожидания", required = true, example = "700") @PathVariable Long id);
}
