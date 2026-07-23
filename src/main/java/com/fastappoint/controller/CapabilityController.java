package com.fastappoint.controller;

import com.fastappoint.dto.CapabilityDTO;
import com.fastappoint.service.CapabilityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/capabilities")
@CrossOrigin(origins = "http://localhost:4200")
public class CapabilityController {

    private final CapabilityService capabilityService;

    public CapabilityController(CapabilityService capabilityService) {
        this.capabilityService = capabilityService;
    }

    @GetMapping
    public ResponseEntity<List<CapabilityDTO>> getCapabilitiesByBusiness(@RequestParam UUID businessId) {
        return ResponseEntity.ok(capabilityService.getCapabilitiesByBusiness(businessId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CapabilityDTO> getCapabilityById(@PathVariable UUID id) {
        return ResponseEntity.ok(capabilityService.getCapabilityById(id));
    }

    @PostMapping
    public ResponseEntity<CapabilityDTO> createCapability(
            @RequestParam UUID businessId,
            @RequestParam String name,
            @RequestParam(required = false) String description) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(capabilityService.createCapability(businessId, name, description));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CapabilityDTO> updateCapability(
            @PathVariable UUID id,
            @RequestParam(required = false) String description) {
        return ResponseEntity.ok(capabilityService.updateCapability(id, description));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCapability(@PathVariable UUID id) {
        capabilityService.deleteCapability(id);
        return ResponseEntity.noContent().build();
    }
}

