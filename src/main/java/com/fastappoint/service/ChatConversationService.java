package com.fastappoint.service;

import com.fastappoint.domain.Business;
import com.fastappoint.domain.ChatConversation;
import com.fastappoint.domain.ChatConversationMessage;
import com.fastappoint.repository.ChatConversationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatConversationService {

    private final ChatConversationRepository chatConversationRepository;

    public ChatConversationService(ChatConversationRepository chatConversationRepository) {
        this.chatConversationRepository = chatConversationRepository;
    }

    public ChatConversation getOrCreateConversation(Business business, String customerPhone) {
        String normalizedPhone = customerPhone == null ? "" : customerPhone.trim();
        Optional<ChatConversation> existing = chatConversationRepository.findByBusiness_IdAndCustomerPhone(business.getId(), normalizedPhone);
        if (existing.isPresent()) {
            return existing.get();
        }
        ChatConversation conversation = new ChatConversation(business, normalizedPhone);
        return chatConversationRepository.save(conversation);
    }

    public void recordUserMessage(Business business, String customerPhone, String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        ChatConversation conversation = getOrCreateConversation(business, customerPhone);
        conversation.addMessage("user", message.trim());
        chatConversationRepository.save(conversation);
    }

    public void recordAssistantMessage(Business business, String customerPhone, String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        ChatConversation conversation = getOrCreateConversation(business, customerPhone);
        conversation.addMessage("assistant", message.trim());
        chatConversationRepository.save(conversation);
    }

    @Transactional(readOnly = true)
    public List<String> getRecentHistory(Business business, String customerPhone, int limit) {
        ChatConversation conversation = chatConversationRepository.findByBusiness_IdAndCustomerPhone(business.getId(), customerPhone == null ? "" : customerPhone.trim())
                .orElse(null);
        if (conversation == null) {
            return List.of();
        }

        List<ChatConversationMessage> messages = new ArrayList<>(conversation.getMessages());
        messages.sort(Comparator.comparing(ChatConversationMessage::getCreatedAt));
        List<String> history = new ArrayList<>();
        int startIndex = Math.max(0, messages.size() - limit);
        for (int i = startIndex; i < messages.size(); i++) {
            ChatConversationMessage message = messages.get(i);
            if (message.getBody() == null || message.getBody().isBlank()) {
                continue;
            }
            history.add(message.getRole() + ": " + message.getBody());
        }
        return history;
    }
}
