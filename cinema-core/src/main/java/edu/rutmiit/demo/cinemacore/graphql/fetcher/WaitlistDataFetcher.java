package edu.rutmiit.demo.cinemacore.graphql.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import edu.rutmiit.demo.cinemaapicontract.dto.WaitlistRequest;
import edu.rutmiit.demo.cinemaapicontract.dto.WaitlistResponse;
import edu.rutmiit.demo.cinemacore.assembler.WaitlistModelAssembler;
import edu.rutmiit.demo.cinemacore.entity.WaitlistEntryEntity;
import edu.rutmiit.demo.cinemacore.graphql.types.CreateWaitlistInputGql;
import edu.rutmiit.demo.cinemacore.graphql.types.WaitlistEntryConnectionGql;
import edu.rutmiit.demo.cinemacore.graphql.types.WaitlistFilterGql;
import edu.rutmiit.demo.cinemacore.repository.base.PageSlice;
import edu.rutmiit.demo.cinemacore.service.WaitlistService;

import static edu.rutmiit.demo.cinemacore.graphql.util.GqlSupport.*;

@DgsComponent
public class WaitlistDataFetcher {
    private final WaitlistService waitlistService;
    private final WaitlistModelAssembler assembler;

    public WaitlistDataFetcher(WaitlistService waitlistService, WaitlistModelAssembler assembler) {
        this.waitlistService = waitlistService;
        this.assembler = assembler;
    }

    @DgsQuery
    public WaitlistResponse waitlistEntry(@InputArgument String id) {
        return assembler.toModel(waitlistService.findById(id(id))).getContent();
    }

    @DgsQuery
    public WaitlistEntryConnectionGql waitlistEntries(@InputArgument WaitlistFilterGql filter,
                                                     @InputArgument Integer page,
                                                     @InputArgument Integer size) {
        Long showId = filter != null ? id(filter.showId()) : null;
        Long customerId = filter != null ? id(filter.customerId()) : null;
        PageSlice<WaitlistEntryEntity> slice = waitlistService.findAll(showId, customerId,
                filter != null ? filter.status() : null,
                page(page), size(size));
        return new WaitlistEntryConnectionGql(content(slice, assembler), pageInfo(slice), (int) slice.totalElements());
    }

    @DgsMutation
    public WaitlistResponse createWaitlistEntry(@InputArgument CreateWaitlistInputGql input) {
        WaitlistRequest request = new WaitlistRequest(id(input.showId()), id(input.seatId()), id(input.customerId()), input.customerEmail());
        return assembler.toModel(waitlistService.create(request)).getContent();
    }

    @DgsMutation
    public boolean cancelWaitlistEntry(@InputArgument String id) {
        waitlistService.cancel(id(id));
        return true;
    }
}
