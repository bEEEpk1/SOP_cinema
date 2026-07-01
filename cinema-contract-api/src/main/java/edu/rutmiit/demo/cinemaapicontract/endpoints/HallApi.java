package edu.rutmiit.demo.cinemaapicontract.endpoints;

import edu.rutmiit.demo.cinemaapicontract.config.CinemaApiContractConfig;
import edu.rutmiit.demo.cinemaapicontract.dto.ErrorResponse;
import edu.rutmiit.demo.cinemaapicontract.dto.HallRequest;
import edu.rutmiit.demo.cinemaapicontract.dto.HallResponse;
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

@Tag(name = "Halls", description = "Управление кинозалами")
@RequestMapping(value = "/api/halls", produces = MediaType.APPLICATION_JSON_VALUE)
public interface HallApi {

    @Operation(summary = "Получить зал по ID", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "200", description = "Зал найден")
    @ApiResponse(responseCode = "404", description = "Зал не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    EntityModel<HallResponse> getHallById(@Parameter(description = "ID зала", required = true, example = "2") @PathVariable Long id);

    @Operation(summary = "Список залов", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "200", description = "Постраничный список залов")
    @GetMapping
    PagedModel<EntityModel<HallResponse>> getAllHalls(
            @Parameter(description = "Тип зала", example = "VIP") @RequestParam(required = false) String hallType,
            @Parameter(description = "Номер страницы", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20") @RequestParam(defaultValue = "20") int size
    );

    @Operation(summary = "Создать зал", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "201", description = "Зал создан")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<EntityModel<HallResponse>> createHall(@Valid @RequestBody HallRequest request);
}
