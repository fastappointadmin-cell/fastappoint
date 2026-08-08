package com.fastappoint.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_conversation_message")
public class ChatConversationMessage {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatConversation conversation;

    @Column(nullable = false, length = 16)
    private String role;

    @Column(nullable = false, length = 8000)
    private String body;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected ChatConversationMessage() {
    }

    public ChatConversationMessage(ChatConversation conversation, String role, String body) {
        this.id = UUID.randomUUID();
        this.conversation = conversation;
        this.role = role;
        this.body = body == null ? "" : body;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public ChatConversation getConversation() {
        return conversation;
    }

    public String getRole() {
        return role;
    }

    public String getBody() {
        return body;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
