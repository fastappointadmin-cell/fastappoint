package com.fastappoint.repository;

import com.fastappoint.domain.WhatsAppConnection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WhatsAppConnectionRepository extends JpaRepository<WhatsAppConnection, UUID> {
    Optional<WhatsAppConnection> findByBusiness_Id(UUID businessId);

    Optional<WhatsAppConnection> findByMetaPhoneNumberId(String metaPhoneNumberId);
}
