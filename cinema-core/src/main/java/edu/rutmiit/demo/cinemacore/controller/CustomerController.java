package edu.rutmiit.demo.cinemacore.controller;

import edu.rutmiit.demo.cinemaapicontract.dto.CustomerRequest;
import edu.rutmiit.demo.cinemaapicontract.dto.CustomerResponse;
import edu.rutmiit.demo.cinemaapicontract.dto.PatchCustomerRequest;
import edu.rutmiit.demo.cinemaapicontract.dto.LoyaltySummaryResponse;
import edu.rutmiit.demo.cinemaapicontract.endpoints.CustomerApi;
import edu.rutmiit.demo.cinemacore.assembler.CustomerModelAssembler;
import edu.rutmiit.demo.cinemacore.service.CustomerService;
import edu.rutmiit.demo.cinemacore.util.PagedModelFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CustomerController implements CustomerApi {
    private final CustomerService customerService;
    private final CustomerModelAssembler assembler;
    private final PagedModelFactory pagedModelFactory;

    @Override
    public EntityModel<CustomerResponse> getCustomerById(Long id) {
        return assembler.toModel(customerService.findById(id));
    }

    @Override
    public PagedModel<EntityModel<CustomerResponse>> getAllCustomers(String emailSearch, String phoneSearch, int page, int size) {
        return pagedModelFactory.toPagedModel(customerService.findAll(emailSearch, phoneSearch, page, size), assembler);
    }

    @Override
    public ResponseEntity<EntityModel<CustomerResponse>> createCustomer(CustomerRequest request) {
        EntityModel<CustomerResponse> model = assembler.toModel(customerService.create(request));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @Override
    public EntityModel<CustomerResponse> patchCustomer(Long id, PatchCustomerRequest request) {
        return assembler.toModel(customerService.patch(id, request));
    }
    @Override
    public LoyaltySummaryResponse getCustomerLoyalty(Long id) {
        var customer = customerService.findById(id);
        int balance = Boolean.TRUE.equals(customer.getRegistered())
                ? customerService.getLoyaltyBalance(id)
                : 0;
        return new LoyaltySummaryResponse(customer.getId(), customer.getEmail(), customer.getRegistered(), balance);
    }

}
