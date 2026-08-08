package com.fastappoint.controller;

import com.fastappoint.dto.ChatAgentResponseDTO;
import com.fastappoint.dto.ChatInboundMessageRequest;
import com.fastappoint.service.ChatAgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/chat-agent")
public class PublicChatAgentController {

    private final ChatAgentService chatAgentService;

    public PublicChatAgentController(ChatAgentService chatAgentService) {
        this.chatAgentService = chatAgentService;
    }

    @PostMapping("/messages")
    public ResponseEntity<ChatAgentResponseDTO> handleInboundMessage(@RequestBody ChatInboundMessageRequest request) {
        return ResponseEntity.ok(chatAgentService.handleInbound(request));
    }
}
