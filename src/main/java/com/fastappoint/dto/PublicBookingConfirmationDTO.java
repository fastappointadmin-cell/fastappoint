package com.fastappoint.dto;

public class PublicBookingConfirmationDTO extends AppointmentDTO {
    private BusinessConfirmationSettingsDTO confirmationSettings;

    public PublicBookingConfirmationDTO() {}

    public BusinessConfirmationSettingsDTO getConfirmationSettings() { return confirmationSettings; }
    public void setConfirmationSettings(BusinessConfirmationSettingsDTO confirmationSettings) {
        this.confirmationSettings = confirmationSettings;
    }
}
