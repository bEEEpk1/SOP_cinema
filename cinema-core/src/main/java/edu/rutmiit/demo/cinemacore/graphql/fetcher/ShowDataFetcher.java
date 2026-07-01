package edu.rutmiit.demo.cinemacore.graphql.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import edu.rutmiit.demo.cinemaapicontract.dto.PatchShowRequest;
import edu.rutmiit.demo.cinemaapicontract.dto.ShowRequest;
import edu.rutmiit.demo.cinemaapicontract.dto.ShowResponse;
import edu.rutmiit.demo.cinemacore.assembler.ShowModelAssembler;
import edu.rutmiit.demo.cinemacore.entity.ShowEntity;
import edu.rutmiit.demo.cinemacore.graphql.types.*;
import edu.rutmiit.demo.cinemacore.repository.base.PageSlice;
import edu.rutmiit.demo.cinemacore.service.ShowService;

import java.math.BigDecimal;

import static edu.rutmiit.demo.cinemacore.graphql.util.GqlSupport.*;

@DgsComponent
public class ShowDataFetcher {
    private final ShowService showService;
    private final ShowModelAssembler assembler;

    public ShowDataFetcher(ShowService showService, ShowModelAssembler assembler) {
        this.showService = showService;
        this.assembler = assembler;
    }

    @DgsQuery
    public ShowResponse show(@InputArgument String id) {
        return assembler.toModel(showService.findById(id(id))).getContent();
    }

    @DgsQuery
    public ShowConnectionGql shows(@InputArgument ShowFilterGql filter,
                                   @InputArgument Integer page,
                                   @InputArgument Integer size) {
        Long movieId = filter != null ? id(filter.movieId()) : null;
        Long hallId = filter != null ? id(filter.hallId()) : null;
        PageSlice<ShowEntity> slice = showService.findAll(movieId, hallId,
                filter != null ? filter.status() : null,
                filter != null ? filter.showDate() : null,
                page(page), size(size));
        return new ShowConnectionGql(content(slice, assembler), pageInfo(slice), (int) slice.totalElements());
    }

    @DgsMutation
    public ShowResponse createShow(@InputArgument CreateShowInputGql input) {
        ShowRequest request = new ShowRequest(
                id(input.movieId()),
                id(input.hallId()),
                input.startTime(),
                input.endTime(),
                BigDecimal.valueOf(input.basePrice()),
                input.currency()
        );
        return assembler.toModel(showService.create(request)).getContent();
    }

    @DgsMutation
    public ShowResponse patchShow(@InputArgument String id, @InputArgument PatchShowInputGql input) {
        PatchShowRequest request = new PatchShowRequest(
                id(input.movieId()),
                id(input.hallId()),
                input.startTime(),
                input.endTime(),
                input.basePrice() != null ? BigDecimal.valueOf(input.basePrice()) : null,
                input.currency(),
                input.status()
        );
        return assembler.toModel(showService.patch(id(id), request)).getContent();
    }
}
