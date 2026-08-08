package com.fastappoint.dto;

public class BusinessConfirmationSettingsDTO {
    private String message;
    private String locationInfo;
    private String googleMapsLink;
    private Boolean enabled;
    private Boolean includeLocationInfo;
    private Boolean includeTime;
    private Boolean includeBookingSlot;
    private Integer reminderLeadTimeMinutes;

    public BusinessConfirmationSettingsDTO() {}

    public BusinessConfirmationSettingsDTO(String message, String locationInfo, String googleMapsLink,
                                           Boolean enabled, Boolean includeLocationInfo, Boolean includeTime,
                                           Boolean includeBookingSlot, Integer reminderLeadTimeMinutes) {
        this.message = message;
        this.locationInfo = locationInfo;
        this.googleMapsLink = googleMapsLink;
        this.enabled = enabled;
        this.includeLocationInfo = includeLocationInfo;
        this.includeTime = includeTime;
        this.includeBookingSlot = includeBookingSlot;
        this.reminderLeadTimeMinutes = reminderLeadTimeMinutes;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getLocationInfo() { return locationInfo; }
    public void setLocationInfo(String locationInfo) { this.locationInfo = locationInfo; }

    public String getGoogleMapsLink() { return googleMapsLink; }
    public void setGoogleMapsLink(String googleMapsLink) { this.googleMapsLink = googleMapsLink; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Boolean getIncludeLocationInfo() { return includeLocationInfo; }
    public void setIncludeLocationInfo(Boolean includeLocationInfo) { this.includeLocationInfo = includeLocationInfo; }

    public Boolean getIncludeTime() { return includeTime; }
    public void setIncludeTime(Boolean includeTime) { this.includeTime = includeTime; }

    public Boolean getIncludeBookingSlot() { return includeBookingSlot; }
    public void setIncludeBookingSlot(Boolean includeBookingSlot) { this.includeBookingSlot = includeBookingSlot; }

    public Integer getReminderLeadTimeMinutes() { return reminderLeadTimeMinutes; }
    public void setReminderLeadTimeMinutes(Integer reminderLeadTimeMinutes) {
        this.reminderLeadTimeMinutes = reminderLeadTimeMinutes;
    }
}
