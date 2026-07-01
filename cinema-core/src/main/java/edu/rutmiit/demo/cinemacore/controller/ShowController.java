package edu.rutmiit.demo.cinemacore.controller;

import edu.rutmiit.demo.cinemaapicontract.dto.*;
import edu.rutmiit.demo.cinemaapicontract.endpoints.ShowApi;
import edu.rutmiit.demo.cinemaapicontract.enums.ShowStatus;
import edu.rutmiit.demo.cinemacore.assembler.ShowModelAssembler;
import edu.rutmiit.demo.cinemacore.service.ShowService;
import edu.rutmiit.demo.cinemacore.util.PagedModelFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class ShowController implements ShowApi {
    private final ShowService showService;
    private final ShowModelAssembler assembler;
    private final PagedModelFactory pagedModelFactory;

    @Override
    public EntityModel<ShowResponse> getShowById(Long id) { return assembler.toModel(showService.findById(id)); }

    @Override
    public PagedModel<EntityModel<ShowResponse>> getAllShows(Long movieId, Long hallId, String status, String showDate, int page, int size) {
        ShowStatus parsedStatus = status == null ? null : ShowStatus.valueOf(status);
        LocalDate parsedDate = showDate == null ? null : LocalDate.parse(showDate);
        return pagedModelFactory.toPagedModel(showService.findAll(movieId, hallId, parsedStatus, parsedDate, page, size), assembler);
    }

    @Override
    public ResponseEntity<EntityModel<ShowResponse>> createShow(ShowRequest request) {
        EntityModel<ShowResponse> model = assembler.toModel(showService.create(request));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @Override
    public EntityModel<ShowResponse> patchShow(Long id, PatchShowRequest request) { return assembler.toModel(showService.patch(id, request)); }

    @Override
    public PagedModel<EntityModel<SeatResponse>> getShowSeats(Long id, String availabilityStatus, int page, int size) {
        return pagedModelFactory.toPagedModel(showService.getShowSeats(id, availabilityStatus, page, size), seat -> EntityModel.of(seat,
                org.springframework.hateoas.Link.of("/api/shows/" + id + "/seats/" + seat.getId()).withSelfRel()));
    }
}
