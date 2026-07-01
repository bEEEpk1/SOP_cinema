package edu.rutmiit.demo.cinemaapicontract.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Упрощённая модель страницы в контракте без зависимости от Spring Data")
public record PagedResponse<T>(
        @Schema(description = "Элементы текущей страницы")
        List<T> content,

        @Schema(description = "Номер страницы (0-based)", example = "0")
        int pageNumber,

        @Schema(description = "Размер страницы", example = "20")
        int pageSize,

        @Schema(description = "Общее количество элементов", example = "105")
        long totalElements,

        @Schema(description = "Общее количество страниц", example = "6")
        int totalPages,

        @Schema(description = "Признак последней страницы", example = "false")
        boolean last
) {
}
