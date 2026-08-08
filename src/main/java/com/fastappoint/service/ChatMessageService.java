package com.fastappoint.service;

import com.fastappoint.domain.Appointment;
import com.fastappoint.domain.AppointmentStatus;
import com.fastappoint.domain.Business;
import com.fastappoint.domain.OutboundChatMessage;
import com.fastappoint.domain.OutboundChatMessageKind;
import com.fastappoint.domain.OutboundChatMessageStatus;
import com.fastappoint.dto.BusinessConfirmationSettingsDTO;
import com.fastappoint.dto.OutboundChatMessageDTO;
import com.fastappoint.repository.OutboundChatMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ChatMessageService {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final OutboundChatMessageRepository outboundChatMessageRepository;

    public ChatMessageService(OutboundChatMessageRepository outboundChatMessageRepository) {
        this.outboundChatMessageRepository = outboundChatMessageRepository;
    }

    public void scheduleMessagesForServiceBooking(Appointment appointment) {
        if (appointment.getService() == null) {
            return;
        }

        Business business = appointment.getBusiness();
        LocalDateTime now = LocalDateTime.now();

        if (business.isConfirmationEnabled()) {
            OutboundChatMessage confirmation = new OutboundChatMessage(
                    business,
                    appointment,
                    OutboundChatMessageKind.CONFIRMATION,
                    appointment.getCustomer().getPhone(),
                    business.getChatPhoneNumber(),
                    buildMessageBody(appointment, true),
                    now
            );
            confirmation.markDispatched(now);
            outboundChatMessageRepository.save(confirmation);
        }

        if (business.isReminderEnabled()) {
            LocalDateTime reminderSendAt = appointment.getStartTime().minusMinutes(business.getReminderLeadTimeMinutes());
            if (reminderSendAt.isBefore(now)) {
                reminderSendAt = now;
            }
            OutboundChatMessage reminder = new OutboundChatMessage(
                    business,
                    appointment,
                    OutboundChatMessageKind.REMINDER,
                    appointment.getCustomer().getPhone(),
                    business.getChatPhoneNumber(),
                    buildMessageBody(appointment, false),
                    reminderSendAt
            );
            outboundChatMessageRepository.save(reminder);
        }
    }

    public List<OutboundChatMessageDTO> dispatchDueReminders(LocalDateTime asOf) {
        LocalDateTime dispatchTime = asOf == null ? LocalDateTime.now() : asOf;
        List<OutboundChatMessage> dueMessages = outboundChatMessageRepository
                .findByStatusAndSendAtLessThanEqualOrderBySendAtAsc(OutboundChatMessageStatus.PENDING, dispatchTime);
        List<OutboundChatMessageDTO> dispatched = new ArrayList<>();
        for (OutboundChatMessage message : dueMessages) {
            if (message.getKind() == OutboundChatMessageKind.REMINDER
                    && (message.getAppointment().getStatus() == AppointmentStatus.CANCELLED
                    || message.getAppointment().getStatus() == AppointmentStatus.COMPLETED)) {
                message.markSkipped(dispatchTime);
            } else {
                message.markDispatched(dispatchTime);
                dispatched.add(convertToDTO(message));
            }
        }
        return dispatched;
    }

    @Transactional(readOnly = true)
    public OutboundChatMessageDTO findLatestMessageForAppointment(UUID appointmentId, OutboundChatMessageKind kind) {
        Optional<OutboundChatMessage> message = outboundChatMessageRepository
                .findTopByAppointment_IdAndKindOrderBySendAtDesc(appointmentId, kind);
        return message.map(this::convertToDTO).orElse(null);
    }

    private String buildMessageBody(Appointment appointment, boolean confirmation) {
        Business business = appointment.getBusiness();
        BusinessConfirmationSettingsDTO settings = confirmation
                ? new BusinessConfirmationSettingsDTO(
                business.getConfirmationMessage(),
                business.getConfirmationLocationInfo(),
                business.getConfirmationGoogleMapsLink(),
                business.isConfirmationEnabled(),
                business.isConfirmationIncludeLocationInfo(),
                business.isConfirmationIncludeTime(),
                business.isConfirmationIncludeBookingSlot(),
                business.getReminderLeadTimeMinutes())
                : new BusinessConfirmationSettingsDTO(
                business.getReminderMessage(),
                business.getReminderLocationInfo(),
                business.getReminderGoogleMapsLink(),
                business.isReminderEnabled(),
                business.isReminderIncludeLocationInfo(),
                business.isReminderIncludeTime(),
                business.isReminderIncludeBookingSlot(),
                business.getReminderLeadTimeMinutes());

        List<String> parts = new ArrayList<>();
        if (!settings.getMessage().isBlank()) {
            parts.add(settings.getMessage());
        }
        if (Boolean.TRUE.equals(settings.getIncludeBookingSlot()) && appointment.getService() != null) {
            parts.add("Serviciu: " + appointment.getService().getName());
        }
        if (Boolean.TRUE.equals(settings.getIncludeTime())) {
            parts.add("Ora: " + appointment.getStartTime().format(DATE_TIME_FORMAT));
        }
        if (Boolean.TRUE.equals(settings.getIncludeLocationInfo()) && !settings.getLocationInfo().isBlank()) {
            parts.add("Locatie: " + settings.getLocationInfo());
            if (!settings.getGoogleMapsLink().isBlank()) {
                parts.add("Harta: " + settings.getGoogleMapsLink());
            }
        }
        if (parts.isEmpty()) {
            return confirmation
                    ? "Programarea ta a fost inregistrata."
                    : "Reminder: programarea ta se apropie.";
        }
        return String.join("\n", parts);
    }

    private OutboundChatMessageDTO convertToDTO(OutboundChatMessage message) {
        return new OutboundChatMessageDTO(
                message.getId(),
                message.getBusiness().getId(),
                message.getAppointment().getId(),
                message.getKind().name(),
                message.getStatus().name(),
                message.getToPhoneNumber(),
                message.getFromPhoneNumber(),
                message.getBody(),
                message.getSendAt(),
                message.getSentAt());
    }
}
