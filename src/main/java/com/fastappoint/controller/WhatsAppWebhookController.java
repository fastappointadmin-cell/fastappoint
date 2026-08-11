package com.fastappoint.controller;

import com.fastappoint.service.ChatAgentService;
import com.fastappoint.service.WhatsAppCloudApiClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * The one webhook URL registered with Meta for the whole platform -- every message for every
 * connected business's number arrives here (see {@link ChatAgentService#handleWhatsAppWebhook}
 * for how it's routed to the right business by the number that received it). Public: Meta calls
 * this unauthenticated, verified instead by the shared secret in {@code hub.verify_token}.
 */
@RestController
@RequestMapping("/api/public/whatsapp/webhook")
public class WhatsAppWebhookController {

    private final ChatAgentService chatAgentService;
    private final WhatsAppCloudApiClient cloudApiClient;

    public WhatsAppWebhookController(ChatAgentService chatAgentService, WhatsAppCloudApiClient cloudApiClient) {
        this.chatAgentService = chatAgentService;
        this.cloudApiClient = cloudApiClient;
    }

    /** Meta calls this once, at webhook-setup time in the developer console, to prove the URL is real. */
    @GetMapping
    public ResponseEntity<String> verify(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String verifyToken,
            @RequestParam(name = "hub.challenge", required = false) String challenge) {
        boolean tokenConfigured = cloudApiClient.getWebhookVerifyToken() != null
                && !cloudApiClient.getWebhookVerifyToken().isBlank();
        if ("subscribe".equals(mode) && tokenConfigured && cloudApiClient.getWebhookVerifyToken().equals(verifyToken)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody Map<String, Object> payload) {
        chatAgentService.handleWhatsAppWebhook(payload);
        return ResponseEntity.ok().build();
    }
}
