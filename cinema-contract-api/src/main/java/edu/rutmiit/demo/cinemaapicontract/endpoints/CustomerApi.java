package edu.rutmiit.demo.cinemaapicontract.endpoints;

import edu.rutmiit.demo.cinemaapicontract.config.CinemaApiContractConfig;
import edu.rutmiit.demo.cinemaapicontract.dto.CustomerRequest;
import edu.rutmiit.demo.cinemaapicontract.dto.CustomerResponse;
import edu.rutmiit.demo.cinemaapicontract.dto.ErrorResponse;
import edu.rutmiit.demo.cinemaapicontract.dto.PatchCustomerRequest;
import edu.rutmiit.demo.cinemaapicontract.dto.LoyaltySummaryResponse;
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

@Tag(name = "Customers", description = "Управление пользователями кинотеатра")
@RequestMapping(value = "/api/customers", produces = MediaType.APPLICATION_JSON_VALUE)
public interface CustomerApi {

    @Operation(summary = "Получить пользователя по ID", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "200", description = "Пользователь найден")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    EntityModel<CustomerResponse> getCustomerById(@Parameter(description = "ID пользователя", required = true, example = "1001") @PathVariable Long id);

    @Operation(summary = "Список пользователей", description = "Возвращает постраничный список пользователей с необязательным поиском по email и телефону.", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "200", description = "Постраничный список пользователей")
    @GetMapping
    PagedModel<EntityModel<CustomerResponse>> getAllCustomers(
            @Parameter(description = "Поиск по email", example = "ivan") @RequestParam(required = false) String emailSearch,
            @Parameter(description = "Поиск по телефону", example = "+7999") @RequestParam(required = false) String phoneSearch,
            @Parameter(description = "Номер страницы", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20") @RequestParam(defaultValue = "20") int size
    );


    @Operation(summary = "Баланс бонусов пользователя", description = "Возвращает текущий loyalty-баланс. Для guest-пользователей баланс равен 0.", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "200", description = "Баланс получен")
    @GetMapping("/{id}/loyalty")
    LoyaltySummaryResponse getCustomerLoyalty(@Parameter(description = "ID пользователя", required = true, example = "4") @PathVariable Long id);

    @Operation(summary = "Создать пользователя", description = "Создаёт пользователя core-сервиса. Email и телефон должны быть уникальны.", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "201", description = "Пользователь создан")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Пользователь с таким email или телефоном уже существует", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<EntityModel<CustomerResponse>> createCustomer(@Valid @RequestBody CustomerRequest request);

    @Operation(summary = "Частично обновить пользователя", description = "Меняет только переданные поля пользователя. null = не менять поле.", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "200", description = "Пользователь обновлён")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Email или телефон уже заняты", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    EntityModel<CustomerResponse> patchCustomer(
            @Parameter(description = "ID пользователя", required = true, example = "1001") @PathVariable Long id,
            @Valid @RequestBody PatchCustomerRequest request
    );
}
