package com.fastappoint.repository;

import com.fastappoint.domain.Business;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BusinessRepository extends JpaRepository<Business, UUID> {
    boolean existsBySlug(String slug);
    boolean existsByChatPhoneNumber(String chatPhoneNumber);
    Optional<Business> findBySlug(String slug);
    Optional<Business> findByChatPhoneNumber(String chatPhoneNumber);
}
