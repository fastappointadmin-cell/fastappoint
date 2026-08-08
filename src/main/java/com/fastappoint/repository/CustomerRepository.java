package com.fastappoint.repository;

import com.fastappoint.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByBusiness_IdAndPhone(UUID businessId, String phone);
}
