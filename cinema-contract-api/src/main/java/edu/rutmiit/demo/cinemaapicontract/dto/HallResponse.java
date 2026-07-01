package edu.rutmiit.demo.cinemaapicontract.dto;

import edu.rutmiit.demo.cinemaapicontract.enums.HallType;
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
@Relation(collectionRelation = "halls", itemRelation = "hall")
@Schema(description = "Информация о зале")
public class HallResponse extends RepresentationModel<HallResponse> {

    @Schema(description = "ID зала", example = "2")
    private final Long id;

    @Schema(description = "Название или номер зала", example = "Зал 1")
    private final String name;

    @Schema(description = "Тип зала")
    private final HallType hallType;

    @Schema(description = "Вместимость зала", example = "120")
    private final Integer capacity;

    @Schema(description = "Активен ли зал", example = "true")
    private final Boolean active;
}
