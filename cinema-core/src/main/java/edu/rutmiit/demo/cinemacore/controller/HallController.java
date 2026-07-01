package edu.rutmiit.demo.cinemacore.controller;

import edu.rutmiit.demo.cinemaapicontract.dto.HallRequest;
import edu.rutmiit.demo.cinemaapicontract.dto.HallResponse;
import edu.rutmiit.demo.cinemaapicontract.endpoints.HallApi;
import edu.rutmiit.demo.cinemaapicontract.enums.HallType;
import edu.rutmiit.demo.cinemacore.assembler.HallModelAssembler;
import edu.rutmiit.demo.cinemacore.service.HallService;
import edu.rutmiit.demo.cinemacore.util.PagedModelFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HallController implements HallApi {
    private final HallService hallService;
    private final HallModelAssembler assembler;
    private final PagedModelFactory pagedModelFactory;

    @Override
    public EntityModel<HallResponse> getHallById(Long id) { return assembler.toModel(hallService.findById(id)); }

    @Override
    public PagedModel<EntityModel<HallResponse>> getAllHalls(String hallType, int page, int size) {
        HallType parsed = hallType == null ? null : HallType.valueOf(hallType);
        return pagedModelFactory.toPagedModel(hallService.findAll(parsed, page, size), assembler);
    }

    @Override
    public ResponseEntity<EntityModel<HallResponse>> createHall(HallRequest request) {
        EntityModel<HallResponse> model = assembler.toModel(hallService.create(request));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }
}
