package com.fastappoint.dto;

import java.util.List;
import java.util.UUID;

public class BusinessDTO {
    private UUID id;
    private String name;
    private String slug;
    private String chatPhoneNumber;
    private String description;
    private BusinessConfirmationSettingsDTO confirmationSettings;
    private BusinessConfirmationSettingsDTO reminderSettings;
    private List<ServiceDTO> services;
    private List<ResourceDTO> resources;

    public BusinessDTO() {}

    public BusinessDTO(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public BusinessDTO(UUID id, String name, String slug) {
        this.id = id;
        this.name = name;
        this.slug = slug;
    }

    public BusinessDTO(UUID id, String name, List<ServiceDTO> services, List<ResourceDTO> resources) {
        this.id = id;
        this.name = name;
        this.services = services;
        this.resources = resources;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getChatPhoneNumber() { return chatPhoneNumber; }
    public void setChatPhoneNumber(String chatPhoneNumber) { this.chatPhoneNumber = chatPhoneNumber; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BusinessConfirmationSettingsDTO getConfirmationSettings() { return confirmationSettings; }
    public void setConfirmationSettings(BusinessConfirmationSettingsDTO confirmationSettings) {
        this.confirmationSettings = confirmationSettings;
    }

    public BusinessConfirmationSettingsDTO getReminderSettings() { return reminderSettings; }
    public void setReminderSettings(BusinessConfirmationSettingsDTO reminderSettings) {
        this.reminderSettings = reminderSettings;
    }

    public List<ServiceDTO> getServices() { return services; }
    public void setServices(List<ServiceDTO> services) { this.services = services; }

    public List<ResourceDTO> getResources() { return resources; }
    public void setResources(List<ResourceDTO> resources) { this.resources = resources; }
}