package edu.rutmiit.demo.cinemacore.assembler;

import edu.rutmiit.demo.cinemaapicontract.dto.CustomerResponse;
import edu.rutmiit.demo.cinemaapicontract.endpoints.CustomerApi;
import edu.rutmiit.demo.cinemacore.entity.CustomerEntity;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CustomerModelAssembler implements RepresentationModelAssembler<CustomerEntity, EntityModel<CustomerResponse>> {

    @Override
    public EntityModel<CustomerResponse> toModel(CustomerEntity entity) {
        CustomerResponse response = CustomerResponse.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .registered(entity.getRegistered())
                .createdAt(entity.getCreatedAt())
                .build();

        return EntityModel.of(
                response,
                linkTo(methodOn(CustomerApi.class).getCustomerById(entity.getId())).withSelfRel(),
                linkTo(methodOn(CustomerApi.class).getAllCustomers(null, null, 0, 20)).withRel("collection")
        );
    }
}