package com.fastappoint.dto;

public class ChatWebSocketMessage {
    private String conversationId;
    private String toPhoneNumber;
    private String fromPhoneNumber;
    private String customerName;
    private String message;

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getToPhoneNumber() { return toPhoneNumber; }
    public void setToPhoneNumber(String toPhoneNumber) { this.toPhoneNumber = toPhoneNumber; }
    public String getFromPhoneNumber() { return fromPhoneNumber; }
    public void setFromPhoneNumber(String fromPhoneNumber) { this.fromPhoneNumber = fromPhoneNumber; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
