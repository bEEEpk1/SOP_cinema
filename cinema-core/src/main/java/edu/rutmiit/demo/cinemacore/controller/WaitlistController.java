package edu.rutmiit.demo.cinemacore.controller;

import edu.rutmiit.demo.cinemaapicontract.dto.WaitlistRequest;
import edu.rutmiit.demo.cinemaapicontract.dto.WaitlistResponse;
import edu.rutmiit.demo.cinemaapicontract.endpoints.WaitlistApi;
import edu.rutmiit.demo.cinemaapicontract.enums.WaitlistStatus;
import edu.rutmiit.demo.cinemacore.assembler.WaitlistModelAssembler;
import edu.rutmiit.demo.cinemacore.service.WaitlistService;
import edu.rutmiit.demo.cinemacore.util.PagedModelFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WaitlistController implements WaitlistApi {
    private final WaitlistService waitlistService;
    private final WaitlistModelAssembler assembler;
    private final PagedModelFactory pagedModelFactory;

    @Override
    public EntityModel<WaitlistResponse> getWaitlistEntryById(Long id) { return assembler.toModel(waitlistService.findById(id)); }

    @Override
    public PagedModel<EntityModel<WaitlistResponse>> getAllWaitlistEntries(Long showId, Long customerId, String status, int page, int size) {
        WaitlistStatus parsed = status == null ? null : WaitlistStatus.valueOf(status);
        return pagedModelFactory.toPagedModel(waitlistService.findAll(showId, customerId, parsed, page, size), assembler);
    }

    @Override
    public ResponseEntity<EntityModel<WaitlistResponse>> createWaitlistEntry(WaitlistRequest request) {
        EntityModel<WaitlistResponse> model = assembler.toModel(waitlistService.create(request));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @Override
    public void cancelWaitlistEntry(Long id) { waitlistService.cancel(id); }
}
