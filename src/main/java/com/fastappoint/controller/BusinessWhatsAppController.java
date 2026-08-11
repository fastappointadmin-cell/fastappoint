package com.fastappoint.controller;

import com.fastappoint.dto.StartWhatsAppConnectionRequest;
import com.fastappoint.dto.SubmitWhatsAppOtpRequest;
import com.fastappoint.dto.WhatsAppConnectionDTO;
import com.fastappoint.security.AuthPrincipal;
import com.fastappoint.service.MembershipService;
import com.fastappoint.service.WhatsAppConnectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/businesses/{businessId}/whatsapp")
public class BusinessWhatsAppController {

    private final WhatsAppConnectionService whatsAppConnectionService;
    private final MembershipService membershipService;

    public BusinessWhatsAppController(WhatsAppConnectionService whatsAppConnectionService,
                                      MembershipService membershipService) {
        this.whatsAppConnectionService = whatsAppConnectionService;
        this.membershipService = membershipService;
    }

    @GetMapping
    public ResponseEntity<WhatsAppConnectionDTO> getConnection(
            @PathVariable UUID businessId, @AuthenticationPrincipal AuthPrincipal principal) {
        membershipService.requireMembership(principal.userId(), businessId);
        return ResponseEntity.ok(whatsAppConnectionService.getConnection(businessId));
    }

    @PostMapping("/connect")
    public ResponseEntity<WhatsAppConnectionDTO> startConnection(
            @PathVariable UUID businessId,
            @RequestBody StartWhatsAppConnectionRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        membershipService.requireMembership(principal.userId(), businessId);
        return ResponseEntity.ok(whatsAppConnectionService.startConnection(businessId, request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<WhatsAppConnectionDTO> submitOtp(
            @PathVariable UUID businessId,
            @RequestBody SubmitWhatsAppOtpRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        membershipService.requireMembership(principal.userId(), businessId);
        return ResponseEntity.ok(whatsAppConnectionService.submitOtp(businessId, request));
    }

    @DeleteMapping
    public ResponseEntity<WhatsAppConnectionDTO> disconnect(
            @PathVariable UUID businessId, @AuthenticationPrincipal AuthPrincipal principal) {
        membershipService.requireMembership(principal.userId(), businessId);
        return ResponseEntity.ok(whatsAppConnectionService.disconnect(businessId));
    }
}
