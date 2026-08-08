package com.fastappoint.dto;

public class ChatWebSocketResponse {

    /** Matches the conversationId sent by the client so it can correlate replies. */
    private String conversationId;

    /** "typing" | "reply" | "error" */
    private String type;

    private String reply;
    private String error;

    public ChatWebSocketResponse() {}

    public ChatWebSocketResponse(String conversationId, String type, String reply, String error) {
        this.conversationId = conversationId;
        this.type = type;
        this.reply = reply;
        this.error = error;
    }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
