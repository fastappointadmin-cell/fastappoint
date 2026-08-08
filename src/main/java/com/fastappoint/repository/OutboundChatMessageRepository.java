package com.fastappoint.repository;

import com.fastappoint.domain.OutboundChatMessage;
import com.fastappoint.domain.OutboundChatMessageKind;
import com.fastappoint.domain.OutboundChatMessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OutboundChatMessageRepository extends JpaRepository<OutboundChatMessage, UUID> {
    List<OutboundChatMessage> findByStatusAndSendAtLessThanEqualOrderBySendAtAsc(
            OutboundChatMessageStatus status, LocalDateTime sendAt);

    Optional<OutboundChatMessage> findTopByAppointment_IdAndKindOrderBySendAtDesc(
            UUID appointmentId, OutboundChatMessageKind kind);
}
