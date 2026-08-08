package com.fastappoint.controller;

import com.fastappoint.dto.CreateResourceTypeRequest;
import com.fastappoint.dto.CreateResourceAttributeDefinitionRequest;
import com.fastappoint.dto.ResourceAttributeDefinitionDTO;
import com.fastappoint.dto.ResourceTypeDTO;
import com.fastappoint.dto.UpdateResourceAttributeDefinitionRequest;
import com.fastappoint.dto.UpdateResourceTypeRequest;
import com.fastappoint.security.AuthPrincipal;
import com.fastappoint.service.MembershipService;
import com.fastappoint.service.ResourceTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resource-types")
public class ResourceTypeController {

    private final ResourceTypeService resourceTypeService;
    private final MembershipService membershipService;

    public ResourceTypeController(ResourceTypeService resourceTypeService, MembershipService membershipService) {
        this.resourceTypeService = resourceTypeService;
        this.membershipService = membershipService;
    }

    @PostMapping
    public ResponseEntity<ResourceTypeDTO> createResourceType(
            @RequestParam UUID businessId,
            @RequestBody CreateResourceTypeRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        membershipService.requireMembership(principal.userId(), businessId);
        ResourceTypeDTO created = resourceTypeService.createResourceType(businessId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ResourceTypeDTO>> getResourceTypesByBusiness(
            @RequestParam UUID businessId, @AuthenticationPrincipal AuthPrincipal principal) {
        membershipService.requireMembership(principal.userId(), businessId);
        return ResponseEntity.ok(resourceTypeService.getResourceTypesByBusiness(businessId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResourceTypeDTO> updateResourceType(
            @PathVariable UUID id,
            @RequestBody UpdateResourceTypeRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        requireMembershipForResourceType(principal, id);
        return ResponseEntity.ok(resourceTypeService.updateResourceType(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResourceType(@PathVariable UUID id, @AuthenticationPrincipal AuthPrincipal principal) {
        requireMembershipForResourceType(principal, id);
        resourceTypeService.deleteResourceType(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/attributes")
    public ResponseEntity<ResourceAttributeDefinitionDTO> createAttributeDefinition(
            @PathVariable UUID id,
            @RequestBody CreateResourceAttributeDefinitionRequest request,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        requireMembershipForResourceType(principal, id);
        ResourceAttributeDefinitionDTO created = resourceTypeService.createAttributeDefinition(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/attributes/{attributeId}")
    public ResponseEntity<ResourceAttributeDefinitionDTO> updateAttributeDefinition(
            @PathVariable UUID id,
            @PathVariable UUID attributeId,
            @RequestBody UpdateResourceAttributeDefinitionRequest request,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        requireMembershipForResourceType(principal, id);
        return ResponseEntity.ok(resourceTypeService.updateAttributeDefinition(id, attributeId, request));
    }

    @DeleteMapping("/{id}/attributes/{attributeId}")
    public ResponseEntity<Void> deleteAttributeDefinition(
            @PathVariable UUID id,
            @PathVariable UUID attributeId,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        requireMembershipForResourceType(principal, id);
        resourceTypeService.deleteAttributeDefinition(id, attributeId);
        return ResponseEntity.noContent().build();
    }

    private void requireMembershipForResourceType(AuthPrincipal principal, UUID resourceTypeId) {
        UUID businessId = resourceTypeService.getResourceTypeEntityById(resourceTypeId).getBusiness().getId();
        membershipService.requireMembership(principal.userId(), businessId);
    }
}
