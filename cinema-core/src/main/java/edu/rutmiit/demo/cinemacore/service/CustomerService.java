package edu.rutmiit.demo.cinemacore.service;

import edu.rutmiit.demo.cinemaapicontract.dto.CustomerRequest;
import edu.rutmiit.demo.cinemaapicontract.dto.PatchCustomerRequest;
import edu.rutmiit.demo.cinemaapicontract.exception.CustomerAlreadyExistsException;
import edu.rutmiit.demo.cinemaapicontract.exception.ResourceNotFoundException;
import edu.rutmiit.demo.cinemacore.entity.CustomerEntity;
import edu.rutmiit.demo.cinemacore.integration.LoyaltyClient;
import edu.rutmiit.demo.cinemacore.repository.CustomerRepository;
import edu.rutmiit.demo.cinemacore.repository.base.PageSlice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final LoyaltyClient loyaltyClient;

    public CustomerEntity findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer with id=" + id + " not found"));
    }

    public PageSlice<CustomerEntity> findAll(String emailSearch, String phoneSearch, int page, int size) {
        return customerRepository.search(emailSearch, phoneSearch, page, size);
    }

    public int getLoyaltyBalance(Long id) {
        CustomerEntity customer = findById(id);
        if (!Boolean.TRUE.equals(customer.getRegistered())) {
            return 0;
        }
        return loyaltyClient.getBalance(customer.getId());
    }

    @Transactional
    public CustomerEntity create(CustomerRequest request) {
        ensureUnique(request.email(), request.phone(), null);

        CustomerEntity customer = new CustomerEntity();
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        customer.setRegistered(request.registered());
        customer.setPasswordHash(request.passwordHash());
        customer.setCreatedAt(request.registered() ? OffsetDateTime.now() : null);

        CustomerEntity saved = customerRepository.save(customer);
        if (Boolean.TRUE.equals(saved.getRegistered())) {
            loyaltyClient.registerCustomer(saved.getId());
        }
        return saved;
    }

    @Transactional
    public CustomerEntity patch(Long id, PatchCustomerRequest request) {
        CustomerEntity customer = findById(id);

        String nextEmail = request.email() != null ? request.email() : customer.getEmail();
        String nextPhone = request.phone() != null ? request.phone() : customer.getPhone();

        ensureUnique(nextEmail, nextPhone, id);

        customer.setEmail(nextEmail);
        customer.setPhone(nextPhone);

        return customerRepository.update(customer);
    }

    private void ensureUnique(String email, String phone, Long currentId) {
        customerRepository.findByEmail(email)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new CustomerAlreadyExistsException("Customer with email=" + email + " already exists");
                });

        customerRepository.findByPhone(phone)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new CustomerAlreadyExistsException("Customer with phone=" + phone + " already exists");
                });
    }
}