package com.fastappoint.controller;

import com.fastappoint.config.IpCapturingHandshakeInterceptor;
import com.fastappoint.dto.ChatAgentResponseDTO;
import com.fastappoint.dto.ChatInboundMessageRequest;
import com.fastappoint.dto.ChatWebSocketMessage;
import com.fastappoint.dto.ChatWebSocketResponse;
import com.fastappoint.exception.InvalidAppointmentException;
import com.fastappoint.service.ChatAgentService;
import com.fastappoint.service.ChatRateLimiterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
public class ChatWebSocketController {

    private static final Logger LOG = LoggerFactory.getLogger(ChatWebSocketController.class);

    private final ChatAgentService chatAgentService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRateLimiterService rateLimiter;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public ChatWebSocketController(ChatAgentService chatAgentService,
                                   SimpMessagingTemplate messagingTemplate,
                                   ChatRateLimiterService rateLimiter) {
        this.chatAgentService = chatAgentService;
        this.messagingTemplate = messagingTemplate;
        this.rateLimiter = rateLimiter;
    }

    @MessageMapping("/chat.send")
    public void handleChatMessage(ChatWebSocketMessage incoming, SimpMessageHeaderAccessor headerAccessor) {
        String conversationId = incoming.getConversationId();
        String topic = "/topic/chat/" + conversationId;

        String clientIp = resolveIp(headerAccessor);
        if (!rateLimiter.isAllowed(clientIp)) {
            LOG.warn("Rate limit exceeded for IP {} on conversation {}", clientIp, conversationId);
            messagingTemplate.convertAndSend(topic,
                    new ChatWebSocketResponse(conversationId, "error", null, "rate_limit_exceeded"));
            return;
        }

        messagingTemplate.convertAndSend(topic,
                new ChatWebSocketResponse(conversationId, "typing", null, null));

        executor.submit(() -> {
            try {
                ChatInboundMessageRequest request = new ChatInboundMessageRequest();
                request.setToPhoneNumber(incoming.getToPhoneNumber());
                request.setFromPhoneNumber(incoming.getFromPhoneNumber());
                request.setCustomerName(incoming.getCustomerName());
                request.setMessage(incoming.getMessage());

                ChatAgentResponseDTO response = chatAgentService.handleInbound(request);

                messagingTemplate.convertAndSend(topic,
                        new ChatWebSocketResponse(conversationId, "reply", response.getReply(), null));
            } catch (Exception e) {
                String errorCode = resolveErrorCode(e);
                if ("send_failed".equals(errorCode)) {
                    LOG.error("WebSocket chat processing failed for conversation {}", conversationId, e);
                } else {
                    LOG.warn("WebSocket chat rejected for conversation {}: {}", conversationId, e.getMessage());
                }
                messagingTemplate.convertAndSend(topic,
                        new ChatWebSocketResponse(conversationId, "error", null, errorCode));
            }
        });
    }

    private String resolveIp(SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> attrs = headerAccessor.getSessionAttributes();
        if (attrs != null) {
            Object ip = attrs.get(IpCapturingHandshakeInterceptor.IP_ATTR);
            if (ip instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        return "unknown";
    }

    private String resolveErrorCode(Exception e) {
        if (e instanceof InvalidAppointmentException) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("phone")) {
                return "invalid_phone";
            }
            return "invalid_request";
        }
        return "send_failed";
    }
}
