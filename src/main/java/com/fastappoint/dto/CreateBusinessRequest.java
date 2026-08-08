package com.fastappoint.dto;

public class CreateBusinessRequest {
    private String name;
    private String chatPhoneNumber;
    private String description;
    private BusinessConfirmationSettingsDTO confirmationSettings;
    private BusinessConfirmationSettingsDTO reminderSettings;

    public CreateBusinessRequest() {}

    public CreateBusinessRequest(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

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
}