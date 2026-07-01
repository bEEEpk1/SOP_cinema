package edu.rutmiit.demo.cinemacore.graphql.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import edu.rutmiit.demo.cinemaapicontract.dto.HallRequest;
import edu.rutmiit.demo.cinemaapicontract.dto.HallResponse;
import edu.rutmiit.demo.cinemacore.assembler.HallModelAssembler;
import edu.rutmiit.demo.cinemacore.entity.HallEntity;
import edu.rutmiit.demo.cinemacore.graphql.types.CreateHallInputGql;
import edu.rutmiit.demo.cinemacore.graphql.types.HallConnectionGql;
import edu.rutmiit.demo.cinemacore.graphql.types.HallFilterGql;
import edu.rutmiit.demo.cinemacore.repository.base.PageSlice;
import edu.rutmiit.demo.cinemacore.service.HallService;

import static edu.rutmiit.demo.cinemacore.graphql.util.GqlSupport.*;

@DgsComponent
public class HallDataFetcher {
    private final HallService hallService;
    private final HallModelAssembler assembler;

    public HallDataFetcher(HallService hallService, HallModelAssembler assembler) {
        this.hallService = hallService;
        this.assembler = assembler;
    }

    @DgsQuery
    public HallResponse hall(@InputArgument String id) {
        return assembler.toModel(hallService.findById(id(id))).getContent();
    }

    @DgsQuery
    public HallConnectionGql halls(@InputArgument HallFilterGql filter,
                                   @InputArgument Integer page,
                                   @InputArgument Integer size) {
        PageSlice<HallEntity> slice = hallService.findAll(filter != null ? filter.hallType() : null, page(page), size(size));
        return new HallConnectionGql(content(slice, assembler), pageInfo(slice), (int) slice.totalElements());
    }

    @DgsMutation
    public HallResponse createHall(@InputArgument CreateHallInputGql input) {
        HallRequest request = new HallRequest(input.name(), input.hallType(), input.capacity());
        return assembler.toModel(hallService.create(request)).getContent();
    }
}
