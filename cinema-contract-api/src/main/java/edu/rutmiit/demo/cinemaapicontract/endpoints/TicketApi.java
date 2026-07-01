package edu.rutmiit.demo.cinemaapicontract.endpoints;

import edu.rutmiit.demo.cinemaapicontract.config.CinemaApiContractConfig;
import edu.rutmiit.demo.cinemaapicontract.dto.ErrorResponse;
import edu.rutmiit.demo.cinemaapicontract.dto.TicketResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Tickets", description = "Просмотр билетов")
@RequestMapping(value = "/api/tickets", produces = MediaType.APPLICATION_JSON_VALUE)
public interface TicketApi {

    @Operation(summary = "Получить билет по ID", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "200", description = "Билет найден")
    @ApiResponse(responseCode = "404", description = "Билет не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    EntityModel<TicketResponse> getTicketById(@Parameter(description = "ID билета", required = true, example = "900") @PathVariable Long id);

    @Operation(summary = "Список билетов", description = "Фильтрация по бронированию, пользователю и сеансу.", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "200", description = "Постраничный список билетов")
    @GetMapping
    PagedModel<EntityModel<TicketResponse>> getAllTickets(
            @Parameter(description = "ID бронирования", example = "500") @RequestParam(required = false) Long bookingId,
            @Parameter(description = "ID пользователя", example = "1001") @RequestParam(required = false) Long customerId,
            @Parameter(description = "ID сеанса", example = "10") @RequestParam(required = false) Long showId,
            @Parameter(description = "Номер страницы", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20") @RequestParam(defaultValue = "20") int size
    );
}
