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

import java.util.List;

@Tag(name = "Movies", description = "Управление фильмами")
@RequestMapping(value = "/api/movies", produces = MediaType.APPLICATION_JSON_VALUE)
public interface MovieApi {

    @Operation(summary = "Получить фильм по ID", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "200", description = "Фильм найден")
    @ApiResponse(responseCode = "404", description = "Фильм не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    EntityModel<MovieResponse> getMovieById(@Parameter(description = "ID фильма", required = true, example = "1") @PathVariable Long id);

    @Operation(summary = "Список фильмов", description = "Возвращает постраничный список фильмов с HATEOAS-ссылками.", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "200", description = "Постраничный список фильмов")
    @GetMapping
    PagedModel<EntityModel<MovieResponse>> getAllMovies(
            @Parameter(description = "Фильтр по жанру", example = "Sci-Fi") @RequestParam(required = false) String genre,
            @Parameter(description = "Поиск по названию", example = "Интер") @RequestParam(required = false) String titleSearch,
            @Parameter(description = "Номер страницы", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20") @RequestParam(defaultValue = "20") int size
    );


    @Operation(summary = "Рекомендации фильмов по жанру", description = "Возвращает реальные активные фильмы из каталога с тем же жанром. Используется enrichment-client для ticket.enriched.", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "200", description = "Список рекомендуемых фильмов")
    @GetMapping("/recommendations")
    List<MovieRecommendationResponse> getMovieRecommendations(
            @Parameter(description = "Жанр фильма", example = "SCI_FI") @RequestParam String genre,
            @Parameter(description = "ID текущего фильма, который нужно исключить", example = "1") @RequestParam(required = false) Long excludeMovieId,
            @Parameter(description = "Максимальное количество рекомендаций", example = "3") @RequestParam(defaultValue = "3") int limit
    );

    @Operation(summary = "Создать фильм", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "201", description = "Фильм создан")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<EntityModel<MovieResponse>> createMovie(@Valid @RequestBody MovieRequest request);

    @Operation(summary = "Полное обновление фильма (PUT)", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "200", description = "Фильм обновлён")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Фильм не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    EntityModel<MovieResponse> updateMovie(@Parameter(description = "ID фильма", required = true, example = "1") @PathVariable Long id,
                                           @Valid @RequestBody UpdateMovieRequest request);

    @Operation(summary = "Частичное обновление фильма (PATCH)", description = "Изменяет только переданные поля. null = не менять поле.", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "200", description = "Фильм обновлён")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Фильм не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    EntityModel<MovieResponse> patchMovie(@Parameter(description = "ID фильма", required = true, example = "1") @PathVariable Long id,
                                          @Valid @RequestBody PatchMovieRequest request);

    @Operation(summary = "Удалить фильм", security = @SecurityRequirement(name = CinemaApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "204", description = "Фильм удалён")
    @ApiResponse(responseCode = "404", description = "Фильм не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteMovie(@Parameter(description = "ID фильма", required = true, example = "1") @PathVariable Long id);
}
