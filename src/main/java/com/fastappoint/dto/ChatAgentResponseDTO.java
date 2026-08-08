package com.fastappoint.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ChatAgentResponseDTO {
    private UUID businessId;
    private String businessName;
    private String businessPhoneNumber;
    private String businessDescription;
    private String reply;
    private String nextAction;
    private UUID matchedServiceId;
    private String matchedServiceName;
    private List<String> knownActions;
    private List<String> knownEndpoints;
    private List<ServiceDTO> services;
    private List<LocalDateTime> availableStarts;
    private AppointmentDTO createdBooking;
    private OutboundChatMessageDTO confirmationMessage;
    private OutboundChatMessageDTO scheduledReminder;

    public UUID getBusinessId() { return businessId; }
    public void setBusinessId(UUID businessId) { this.businessId = businessId; }
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public String getBusinessPhoneNumber() { return businessPhoneNumber; }
    public void setBusinessPhoneNumber(String businessPhoneNumber) { this.businessPhoneNumber = businessPhoneNumber; }
    public String getBusinessDescription() { return businessDescription; }
    public void setBusinessDescription(String businessDescription) { this.businessDescription = businessDescription; }
    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
    public String getNextAction() { return nextAction; }
    public void setNextAction(String nextAction) { this.nextAction = nextAction; }
    public UUID getMatchedServiceId() { return matchedServiceId; }
    public void setMatchedServiceId(UUID matchedServiceId) { this.matchedServiceId = matchedServiceId; }
    public String getMatchedServiceName() { return matchedServiceName; }
    public void setMatchedServiceName(String matchedServiceName) { this.matchedServiceName = matchedServiceName; }
    public List<String> getKnownActions() { return knownActions; }
    public void setKnownActions(List<String> knownActions) { this.knownActions = knownActions; }
    public List<String> getKnownEndpoints() { return knownEndpoints; }
    public void setKnownEndpoints(List<String> knownEndpoints) { this.knownEndpoints = knownEndpoints; }
    public List<ServiceDTO> getServices() { return services; }
    public void setServices(List<ServiceDTO> services) { this.services = services; }
    public List<LocalDateTime> getAvailableStarts() { return availableStarts; }
    public void setAvailableStarts(List<LocalDateTime> availableStarts) { this.availableStarts = availableStarts; }
    public AppointmentDTO getCreatedBooking() { return createdBooking; }
    public void setCreatedBooking(AppointmentDTO createdBooking) { this.createdBooking = createdBooking; }
    public OutboundChatMessageDTO getConfirmationMessage() { return confirmationMessage; }
    public void setConfirmationMessage(OutboundChatMessageDTO confirmationMessage) { this.confirmationMessage = confirmationMessage; }
    public OutboundChatMessageDTO getScheduledReminder() { return scheduledReminder; }
    public void setScheduledReminder(OutboundChatMessageDTO scheduledReminder) { this.scheduledReminder = scheduledReminder; }
}
