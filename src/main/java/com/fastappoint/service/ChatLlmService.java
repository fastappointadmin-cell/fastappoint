package com.fastappoint.service;

import com.fastappoint.domain.Business;
import com.fastappoint.dto.ServiceDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ChatLlmService {

    private static final Logger LOG = LoggerFactory.getLogger(ChatLlmService.class);

    private final RestClient restClient;
    private final ChatAgentToolService chatAgentToolService;
    private final ChatLlmHistoryService chatLlmHistoryService;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final String apiKey;
    private final String model;
    private final int maxToolRounds;

    public ChatLlmService(RestClient.Builder restClientBuilder,
                          ChatAgentToolService chatAgentToolService,
                          ChatLlmHistoryService chatLlmHistoryService,
                          ObjectMapper objectMapper,
                          @Value("${app.chat.llm.enabled:false}") boolean enabled,
                          @Value("${app.chat.llm.api-url:https://api.openai.com/v1/chat/completions}") String apiUrl,
                          @Value("${app.chat.llm.api-key:}") String apiKey,
                          @Value("${app.chat.llm.model:gpt-4o-mini}") String model,
                          @Value("${app.chat.llm.max-tool-rounds:8}") int maxToolRounds) {
        this.restClient = restClientBuilder.baseUrl(apiUrl).build();
        this.chatAgentToolService = chatAgentToolService;
        this.chatLlmHistoryService = chatLlmHistoryService;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.apiKey = apiKey;
        this.model = model;
        this.maxToolRounds = Math.max(1, maxToolRounds);
    }

    public String generateChatReply(Business business,
                                    List<ServiceDTO> catalog,
                                    String customerPhone,
                                    String customerName,
                                    String customerMessage,
                                    List<String> conversationHistory) {
        String interactionId = UUID.randomUUID().toString();
        if (!enabled || apiKey == null || apiKey.isBlank()) {
            chatLlmHistoryService.logEvent(interactionId, "llm-disabled", Map.of(
                    "businessId", business == null ? "" : String.valueOf(business.getId()),
                    "customerPhone", safe(customerPhone),
                    "message", safe(customerMessage)
            ));
            return "Serviciul de chat AI nu este disponibil acum.";
        }

        chatLlmHistoryService.logEvent(interactionId, "interaction-start", Map.of(
                "businessId", business == null ? "" : String.valueOf(business.getId()),
                "businessPhone", business == null ? "" : safe(business.getChatPhoneNumber()),
                "customerPhone", safe(customerPhone),
                "customerName", safe(customerName),
                "message", safe(customerMessage),
                "history", conversationHistory == null ? List.of() : conversationHistory
        ));

        List<Map<String, Object>> messages = buildMessages(
                business,
                catalog,
                customerPhone,
                customerName,
                customerMessage,
                conversationHistory);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("temperature", 0.7);
        body.put("messages", messages);
        body.put("tools", List.of(
                listServicesTool(),
                checkAvailabilityTool(),
                createBookingTool(),
                listMyAppointmentsTool()
        ));
        body.put("tool_choice", "auto");

        try {
            for (int round = 0; round < maxToolRounds; round++) {
                Map<String, Object> response = postChatCompletion(body);
                List<Map<String, Object>> toolCalls = extractToolCalls(response);
                chatLlmHistoryService.logEvent(interactionId, "llm-round-response", Map.of(
                        "round", round,
                        "toolCallsCount", toolCalls.size(),
                        "assistantContent", extractAssistantContent(response)
                ));
                if (toolCalls.isEmpty()) {
                    String reply = fallbackDirectReply(extractAssistantContent(response));
                    chatLlmHistoryService.logEvent(interactionId, "interaction-final-reply", Map.of(
                            "reply", reply,
                            "logPath", chatLlmHistoryService.getLogPath()
                    ));
                    return reply;
                }

                messages.add(buildAssistantToolCallMessage(toolCalls));
                for (Map<String, Object> toolCall : toolCalls) {
                    String toolName = asString(toolCall.get("name"));
                    Map<String, Object> arguments = parseArguments(asString(toolCall.get("arguments")));
                    Map<String, Object> toolCallLog = new LinkedHashMap<>();
                    toolCallLog.put("round", round);
                    toolCallLog.put("toolName", toolName);
                    toolCallLog.put("arguments", arguments);
                    chatLlmHistoryService.logEvent(interactionId, "tool-call", toolCallLog);
                    String toolResult = executeToolCall(business, customerPhone, toolName, arguments);
                    chatLlmHistoryService.logEvent(interactionId, "tool-result", Map.of(
                            "round", round,
                            "toolName", toolName,
                            "result", toolResult
                    ));
                    Map<String, Object> toolMsg = new LinkedHashMap<>();
                    toolMsg.put("role", "tool");
                    toolMsg.put("tool_call_id", asString(toolCall.get("id")));
                    toolMsg.put("name", toolName);
                    toolMsg.put("content", toolResult);
                    messages.add(toolMsg);
                }

                body.put("messages", messages);
            }
            chatLlmHistoryService.logEvent(interactionId, "interaction-max-rounds", Map.of(
                    "message", "Reached max tool rounds without final assistant reply",
                    "maxToolRounds", maxToolRounds
            ));
            return "Nu am putut finaliza cererea in acest moment.";
        } catch (RestClientException ex) {
            LOG.warn("LLM chat request failed: {}", ex.getMessage());
            chatLlmHistoryService.logEvent(interactionId, "interaction-error", Map.of(
                    "errorType", ex.getClass().getSimpleName(),
                    "errorMessage", safe(ex.getMessage())
            ));
            return "Momentan nu pot raspunde prin serviciul AI.";
        }
    }

    private List<Map<String, Object>> buildMessages(Business business,
                                                    List<ServiceDTO> catalog,
                                                    String customerPhone,
                                                    String customerName,
                                                    String customerMessage,
                                                    List<String> conversationHistory) {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "system",
                "content", buildSystemPrompt(business, catalog, customerPhone, customerName)
        ));
        if (conversationHistory != null) {
            for (String historyEntry : conversationHistory) {
                Map<String, Object> historyMessage = toChatMessage(historyEntry);
                if (historyMessage != null) {
                    messages.add(historyMessage);
                }
            }
        }
        messages.add(Map.of(
                "role", "user",
                "content", customerMessage == null ? "" : customerMessage.trim()
        ));
        return messages;
    }

    private String buildSystemPrompt(Business business,
                                     List<ServiceDTO> catalog,
                                     String customerPhone,
                                     String customerName) {
        List<String> serviceNames = new ArrayList<>();
        if (catalog != null) {
            for (ServiceDTO service : catalog) {
                if (service.getName() != null && !service.getName().isBlank()) {
                    serviceNames.add(service.getName());
                }
            }
        }

        return """
                You are the chat assistant for this business.
                Reply in Romanian. Keep the conversation natural and helpful.
                Use backend tools when the user asks to:
                - see available services
                - check service availability
                - create an appointment
                - see their active appointments
                If the user is just chatting or the request is not a backend action, answer directly without calling a tool.
                If information is missing for a backend action, ask a short clarifying question instead of calling a tool.
                Use business context and conversation history to understand follow-up messages.
                If the customer asks for their appointments, use the phone number already in context.

                Booking flow — follow this strictly:
                1. If the user wants to book but has not given a date, ask for the date. Do NOT suggest a date format; accept any natural expression.
                2. If the user gives a date that is in the past, tell them and ask for a different date. Do NOT silently pick another date.
                3. Once you have a valid date, call check_availability.
                4. Present the available slots and ask the user which time they prefer. Do NOT pick a time on their behalf.
                5. Once the user picks a time, if you do not already know their name, ask for it before booking.
                6. Only call create_booking after you have: service name, date, time chosen by the user, and customer name.
                7. Never use placeholder names like "Numele clientului". If the name is missing, ask for it.

                Before calling a tool, convert natural-language dates into exact ISO dates using today's date.
                Examples: "azi", "maine", "poimaine", weekday names like "luni", and dates like "7 august" must become yyyy-MM-dd.
                Never pass natural-language dates into a tool argument.
                If a tool returns a validation error, ask the user for the missing information rather than inventing values.
                Today's date is %s (%s).

                Business name: %s
                Business description: %s
                Available services: %s
                Customer phone number in context: %s
                Customer name in context: %s
                """.formatted(
                LocalDate.now(),
                LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                safe(business == null ? null : business.getName()),
                safe(business == null ? null : business.getDescription()),
                serviceNames.isEmpty() ? "none" : String.join(", ", serviceNames),
                safe(customerPhone),
                safe(customerName)
        );
    }

    private Map<String, Object> listServicesTool() {
        return Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "list_services",
                        "description", "List the services available for this business",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of()
                        )
                )
        );
    }

    private Map<String, Object> checkAvailabilityTool() {
        return Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "check_availability",
                        "description", "Check available time slots for a service on a specific date. requestedDate must be an exact yyyy-MM-dd date, not natural language like luni or maine.",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "serviceName", Map.of(
                                                "type", "string",
                                                "description", "The service name as requested by the customer"
                                        ),
                                        "requestedDate", Map.of(
                                                "type", "string",
                                                "description", "The requested date in exact yyyy-MM-dd format, for example 2026-08-10"
                                        )
                                ),
                                "required", List.of("serviceName", "requestedDate")
                        )
                )
        );
    }

    private Map<String, Object> createBookingTool() {
        return Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "create_booking",
                        "description", "Create an appointment for a service at a specific date and time",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "serviceName", Map.of(
                                                "type", "string",
                                                "description", "The service name as requested by the customer"
                                        ),
                                        "requestedDate", Map.of(
                                                "type", "string",
                                                "description", "The requested date in exact yyyy-MM-dd format, for example 2026-08-10"
                                        ),
                                        "requestedTime", Map.of(
                                                "type", "string",
                                                "description", "The requested time in HH:mm format"
                                        ),
                                        "customerName", Map.of(
                                                "type", "string",
                                                "description", "The customer's name"
                                        )
                                ),
                                "required", List.of("serviceName", "requestedDate", "requestedTime", "customerName")
                        )
                )
        );
    }

    private Map<String, Object> listMyAppointmentsTool() {
        return Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "list_my_appointments",
                        "description", "List the customer's active appointments using the phone number already in context",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of()
                        )
                )
        );
    }

    private Map<String, Object> buildAssistantToolCallMessage(List<Map<String, Object>> toolCalls) {
        List<Map<String, Object>> messageToolCalls = new ArrayList<>();
        for (Map<String, Object> toolCall : toolCalls) {
            messageToolCalls.add(Map.of(
                    "id", asString(toolCall.get("id")),
                    "type", "function",
                    "function", Map.of(
                            "name", asString(toolCall.get("name")),
                            "arguments", asString(toolCall.get("arguments"))
                    )
            ));
        }
        return Map.of(
                "role", "assistant",
                "content", "",
                "tool_calls", messageToolCalls
        );
    }

    private String executeToolCall(Business business,
                                   String customerPhone,
                                   String toolName,
                                   Map<String, Object> arguments) {
        try {
            Map<String, Object> result;
            switch (toolName) {
                case "list_services" ->
                        result = chatAgentToolService.listServicesForBusiness(business.getChatPhoneNumber());
                case "check_availability" ->
                        result = chatAgentToolService.checkAvailabilityForBusiness(business.getChatPhoneNumber(), arguments);
                case "create_booking" ->
                        result = chatAgentToolService.createBookingForBusiness(business.getChatPhoneNumber(), customerPhone, arguments);
                case "list_my_appointments" ->
                        result = chatAgentToolService.listMyAppointmentsForBusiness(business.getChatPhoneNumber(), customerPhone);
                default -> result = Map.of("kind", "unsupported", "error", "unsupported tool");
            }
            return objectMapper.writeValueAsString(result);
        } catch (Exception ex) {
            LOG.warn("Tool execution failed for {}: {}", toolName, ex.getMessage());
            return serializeToolError(ex);
        }
    }

    private String serializeToolError(Exception ex) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "kind", "tool_error",
                    "error", ex.getMessage() == null || ex.getMessage().isBlank() ? "tool execution failed" : ex.getMessage()
            ));
        } catch (Exception serializationEx) {
            return "{\"kind\":\"tool_error\",\"error\":\"tool execution failed\"}";
        }
    }

    private Map<String, Object> parseArguments(String rawArguments) {
        if (rawArguments == null || rawArguments.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(rawArguments, new TypeReference<>() {});
        } catch (Exception ex) {
            LOG.warn("Failed to parse tool arguments: {}", ex.getMessage());
            return Map.of();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> postChatCompletion(Map<String, Object> body) {
        return restClient.post()
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(Map.class);
    }

    private List<Map<String, Object>> extractToolCalls(Map<String, Object> response) {
        Object choicesObject = response == null ? null : response.get("choices");
        if (!(choicesObject instanceof List<?> choices) || choices.isEmpty()) {
            return List.of();
        }
        Object firstChoice = choices.getFirst();
        if (!(firstChoice instanceof Map<?, ?> firstChoiceMap)) {
            return List.of();
        }
        Object messageObject = firstChoiceMap.get("message");
        if (!(messageObject instanceof Map<?, ?> messageMap)) {
            return List.of();
        }
        Object toolCallsObject = messageMap.get("tool_calls");
        if (!(toolCallsObject instanceof List<?> rawToolCalls) || rawToolCalls.isEmpty()) {
            return List.of();
        }

        List<Map<String, Object>> toolCalls = new ArrayList<>();
        for (Object rawToolCall : rawToolCalls) {
            if (!(rawToolCall instanceof Map<?, ?> toolCallMap)) {
                continue;
            }
            Object functionObject = toolCallMap.get("function");
            if (!(functionObject instanceof Map<?, ?> functionMap)) {
                continue;
            }
            toolCalls.add(Map.of(
                    "id", asString(toolCallMap.get("id")),
                    "name", asString(functionMap.get("name")),
                    "arguments", asString(functionMap.get("arguments"))
            ));
        }
        return toolCalls;
    }

    private Map<String, Object> toChatMessage(String historyEntry) {
        if (historyEntry == null || historyEntry.isBlank()) {
            return null;
        }
        int separatorIndex = historyEntry.indexOf(':');
        if (separatorIndex <= 0 || separatorIndex + 1 >= historyEntry.length()) {
            return null;
        }
        String role = historyEntry.substring(0, separatorIndex).trim();
        String content = historyEntry.substring(separatorIndex + 1).trim();
        if (content.isBlank()) {
            return null;
        }
        if (!"user".equals(role) && !"assistant".equals(role)) {
            return null;
        }
        return Map.of("role", role, "content", content);
    }

    private String extractAssistantContent(Map<String, Object> response) {
        if (response == null) {
            return "";
        }
        Object choicesObject = response.get("choices");
        if (!(choicesObject instanceof List<?> choices) || choices.isEmpty()) {
            return "";
        }
        Object firstChoice = choices.get(0);
        if (!(firstChoice instanceof Map<?, ?> firstChoiceMap)) {
            return "";
        }
        Object messageObject = firstChoiceMap.get("message");
        if (!(messageObject instanceof Map<?, ?> messageMap)) {
            return "";
        }
        Object content = messageMap.get("content");
        return content == null ? "" : String.valueOf(content).trim();
    }

    private String fallbackDirectReply(String content) {
        if (content == null || content.isBlank()) {
            LOG.warn("LLM returned empty chat content");
            return "Nu am primit un raspuns de la serviciul AI.";
        }
        return content;
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
