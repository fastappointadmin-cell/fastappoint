package com.fastappoint.dto;

public class StartWhatsAppConnectionRequest {
    /** "PROVISIONED" or "OWN_NUMBER" -- see {@link com.fastappoint.domain.WhatsAppConnectionSource}. */
    private String source;

    /** Required only when source is "OWN_NUMBER"; any format BusinessPhoneNumberService can parse. */
    private String ownPhoneNumber;

    public StartWhatsAppConnectionRequest() {
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getOwnPhoneNumber() {
        return ownPhoneNumber;
    }

    public void setOwnPhoneNumber(String ownPhoneNumber) {
        this.ownPhoneNumber = ownPhoneNumber;
    }
}
