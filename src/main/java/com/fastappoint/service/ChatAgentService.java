package com.fastappoint.service;

import com.fastappoint.domain.Business;
import com.fastappoint.dto.ChatAgentResponseDTO;
import com.fastappoint.dto.ChatInboundMessageRequest;
import com.fastappoint.dto.ServiceDTO;
import com.fastappoint.exception.InvalidAppointmentException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ChatAgentService {

    private final BusinessService businesses;
    private final BusinessServiceService services;
    private final BusinessPhoneNumberService phoneNumbers;
    private final ChatLlmService chatLlmService;
    private final ChatConversationService chatConversationService;

    public ChatAgentService(BusinessService businesses,
                            BusinessServiceService services,
                            BusinessPhoneNumberService phoneNumbers,
                            ChatLlmService chatLlmService,
                            ChatConversationService chatConversationService) {
        this.businesses = businesses;
        this.services = services;
        this.phoneNumbers = phoneNumbers;
        this.chatLlmService = chatLlmService;
        this.chatConversationService = chatConversationService;
    }

    public ChatAgentResponseDTO handleInbound(ChatInboundMessageRequest request) {
        if (request.getToPhoneNumber() == null || request.getToPhoneNumber().trim().isEmpty()) {
            throw new InvalidAppointmentException("Destination business phone number is required");
        }
        if (request.getFromPhoneNumber() == null || request.getFromPhoneNumber().trim().isEmpty()) {
            throw new InvalidAppointmentException("Sender customer phone number is required");
        }
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new InvalidAppointmentException("Message body is required");
        }

        Business business = businesses.getBusinessEntityByChatPhoneNumber(request.getToPhoneNumber());
        String customerPhone = phoneNumbers.validateAndNormalize(request.getFromPhoneNumber());
        List<ServiceDTO> catalog = services.getServicesByBusiness(business.getId());
        List<String> conversationHistory = chatConversationService.getRecentHistory(business, customerPhone, 12);

        chatConversationService.recordUserMessage(business, customerPhone, request.getMessage());

        String reply = chatLlmService.generateChatReply(
                business,
                catalog,
                customerPhone,
                request.getCustomerName(),
                request.getMessage(),
                conversationHistory);

        ChatAgentResponseDTO response = baseResponse(business);
        response.setReply(reply);

        chatConversationService.recordAssistantMessage(business, customerPhone, reply);
        return response;
    }

    public List<ChatAgentResponseDTO> handleWhatsAppWebhook(Map<String, Object> payload) {
        List<Map<String, Object>> entries = asObjectList(payload == null ? null : payload.get("entry"));
        if (entries.isEmpty()) {
            throw new InvalidAppointmentException("Invalid WhatsApp webhook payload: missing entry array");
        }

        List<ChatAgentResponseDTO> responses = new ArrayList<>();
        for (Map<String, Object> entry : entries) {
            List<Map<String, Object>> changes = asObjectList(entry.get("changes"));
            if (changes.isEmpty()) {
                continue;
            }
            for (Map<String, Object> change : changes) {
                Map<String, Object> value = asObjectMap(change.get("value"));
                if (value.isEmpty()) {
                    continue;
                }
                List<Map<String, Object>> messages = asObjectList(value.get("messages"));
                if (messages.isEmpty()) {
                    continue;
                }
                String toPhoneNumber = extractToPhoneNumber(value);
                for (Map<String, Object> message : messages) {
                    String fromPhoneNumber = asString(message.get("from"));
                    Map<String, Object> text = asObjectMap(message.get("text"));
                    String messageBody = asString(text.get("body"));
                    if (fromPhoneNumber.isBlank() || messageBody.isBlank()) {
                        continue;
                    }

                    ChatInboundMessageRequest request = new ChatInboundMessageRequest();
                    request.setToPhoneNumber(toPhoneNumber);
                    request.setFromPhoneNumber(fromPhoneNumber);
                    request.setCustomerName(resolveCustomerName(value, fromPhoneNumber));
                    request.setMessage(messageBody);
                    responses.add(handleInbound(request));
                }
            }
        }
        return responses;
    }

    private ChatAgentResponseDTO baseResponse(Business business) {
        ChatAgentResponseDTO response = new ChatAgentResponseDTO();
        response.setBusinessId(business.getId());
        response.setBusinessName(business.getName());
        response.setBusinessPhoneNumber(business.getChatPhoneNumber());
        response.setBusinessDescription(business.getDescription());
        return response;
    }

    private String extractToPhoneNumber(Map<String, Object> value) {
        Map<String, Object> metadata = asObjectMap(value.get("metadata"));
        if (metadata.isEmpty()) {
            throw new InvalidAppointmentException("Invalid WhatsApp webhook payload: missing metadata");
        }
        String displayPhoneNumber = asString(metadata.get("display_phone_number"));
        if (displayPhoneNumber.isBlank()) {
            throw new InvalidAppointmentException("Invalid WhatsApp webhook payload: missing metadata.display_phone_number");
        }
        return displayPhoneNumber;
    }

    private String resolveCustomerName(Map<String, Object> value, String fromPhoneNumber) {
        List<Map<String, Object>> contacts = asObjectList(value.get("contacts"));
        if (contacts.isEmpty()) {
            return null;
        }
        for (Map<String, Object> contact : contacts) {
            if (fromPhoneNumber.equals(asString(contact.get("wa_id")))) {
                Map<String, Object> profile = asObjectMap(contact.get("profile"));
                String name = asString(profile.get("name"));
                return name.isBlank() ? null : name;
            }
        }
        return null;
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asObjectMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Collections.emptyMap();
    }

    private List<Map<String, Object>> asObjectList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            Map<String, Object> asMap = asObjectMap(item);
            if (!asMap.isEmpty()) {
                result.add(asMap);
            }
        }
        return result;
    }
}
