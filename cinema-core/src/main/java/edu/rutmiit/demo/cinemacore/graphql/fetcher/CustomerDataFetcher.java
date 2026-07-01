package edu.rutmiit.demo.cinemacore.graphql.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import edu.rutmiit.demo.cinemaapicontract.dto.CustomerRequest;
import edu.rutmiit.demo.cinemaapicontract.dto.CustomerResponse;
import edu.rutmiit.demo.cinemaapicontract.dto.PatchCustomerRequest;
import edu.rutmiit.demo.cinemacore.assembler.CustomerModelAssembler;
import edu.rutmiit.demo.cinemacore.graphql.types.CustomerConnectionGql;
import edu.rutmiit.demo.cinemacore.graphql.types.CustomerFilterGql;
import edu.rutmiit.demo.cinemacore.graphql.types.CreateCustomerInputGql;
import edu.rutmiit.demo.cinemacore.graphql.types.PatchCustomerInputGql;
import edu.rutmiit.demo.cinemacore.repository.base.PageSlice;
import edu.rutmiit.demo.cinemacore.service.CustomerService;

import static edu.rutmiit.demo.cinemacore.graphql.util.GqlSupport.*;

@DgsComponent
public class CustomerDataFetcher {
    private final CustomerService customerService;
    private final CustomerModelAssembler assembler;

    public CustomerDataFetcher(CustomerService customerService, CustomerModelAssembler assembler) {
        this.customerService = customerService;
        this.assembler = assembler;
    }

    @DgsQuery
    public CustomerResponse customer(@InputArgument String id) {
        return assembler.toModel(customerService.findById(id(id))).getContent();
    }

    @DgsQuery
    public CustomerConnectionGql customers(@InputArgument CustomerFilterGql filter,
                                           @InputArgument Integer page,
                                           @InputArgument Integer size) {
        String emailSearch = filter != null ? filter.emailSearch() : null;
        String phoneSearch = filter != null ? filter.phoneSearch() : null;
        PageSlice<?> slice = customerService.findAll(emailSearch, phoneSearch, page(page), size(size));
        return new CustomerConnectionGql(content((PageSlice) slice, assembler), pageInfo(slice), (int) slice.totalElements());
    }

    @DgsMutation
    public CustomerResponse createCustomer(@InputArgument CreateCustomerInputGql input) {
        CustomerRequest request = new CustomerRequest(input.email(), input.phone(), input.registered(), input.passwordHash());
        return assembler.toModel(customerService.create(request)).getContent();
    }

    @DgsMutation
    public CustomerResponse patchCustomer(@InputArgument String id, @InputArgument PatchCustomerInputGql input) {
        PatchCustomerRequest request = new PatchCustomerRequest(input.email(), input.phone());
        return assembler.toModel(customerService.patch(id(id), request)).getContent();
    }
}
