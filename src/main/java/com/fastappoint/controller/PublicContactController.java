package com.fastappoint.controller;

import com.fastappoint.dto.PublicContactMessageRequest;
import com.fastappoint.service.PublicContactService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/contact")
public class PublicContactController {
    private final PublicContactService publicContactService;

    public PublicContactController(PublicContactService publicContactService) {
        this.publicContactService = publicContactService;
    }

    @PostMapping("/messages")
    public ResponseEntity<Void> sendContactMessage(@Valid @RequestBody PublicContactMessageRequest request) {
        publicContactService.send(request);
        return ResponseEntity.accepted().build();
    }
}
