package com.fastappoint.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ChatAgentToolExecutionResult {

    private String reply;
    private String nextAction;
    private List<LocalDateTime> availableStarts;
    private AppointmentDTO createdBooking;
    private OutboundChatMessageDTO confirmationMessage;
    private OutboundChatMessageDTO scheduledReminder;

    public static ChatAgentToolExecutionResult empty() {
        return new ChatAgentToolExecutionResult();
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getNextAction() {
        return nextAction;
    }

    public void setNextAction(String nextAction) {
        this.nextAction = nextAction;
    }

    public List<LocalDateTime> getAvailableStarts() {
        return availableStarts;
    }

    public void setAvailableStarts(List<LocalDateTime> availableStarts) {
        this.availableStarts = availableStarts;
    }

    public AppointmentDTO getCreatedBooking() {
        return createdBooking;
    }

    public void setCreatedBooking(AppointmentDTO createdBooking) {
        this.createdBooking = createdBooking;
    }

    public OutboundChatMessageDTO getConfirmationMessage() {
        return confirmationMessage;
    }

    public void setConfirmationMessage(OutboundChatMessageDTO confirmationMessage) {
        this.confirmationMessage = confirmationMessage;
    }

    public OutboundChatMessageDTO getScheduledReminder() {
        return scheduledReminder;
    }

    public void setScheduledReminder(OutboundChatMessageDTO scheduledReminder) {
        this.scheduledReminder = scheduledReminder;
    }
}
