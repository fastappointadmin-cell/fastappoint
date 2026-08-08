package com.fastappoint.repository;

import com.fastappoint.domain.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, UUID> {
    Optional<ChatConversation> findByBusiness_IdAndCustomerPhone(UUID businessId, String customerPhone);
}
