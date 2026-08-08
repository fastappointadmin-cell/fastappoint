package com.fastappoint.controller;

import com.fastappoint.dto.CreateResourceRequest;
import com.fastappoint.dto.ResourceAvailabilityDTO;
import com.fastappoint.dto.ResourceDTO;
import com.fastappoint.dto.UpdateResourceRequest;
import com.fastappoint.security.AuthPrincipal;
import com.fastappoint.service.MembershipService;
import com.fastappoint.service.ResourceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;
    private final MembershipService membershipService;

    public ResourceController(ResourceService resourceService, MembershipService membershipService) {
        this.resourceService = resourceService;
        this.membershipService = membershipService;
    }

    @PostMapping
    public ResponseEntity<ResourceDTO> createResource(
            @RequestParam UUID businessId,
            @RequestBody CreateResourceRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        membershipService.requireMembership(principal.userId(), businessId);
        ResourceDTO resource = resourceService.createResource(businessId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    @GetMapping
    public ResponseEntity<List<ResourceDTO>> getResourcesByBusiness(
            @RequestParam UUID businessId, @AuthenticationPrincipal AuthPrincipal principal) {
        membershipService.requireMembership(principal.userId(), businessId);
        return ResponseEntity.ok(resourceService.getResourcesByBusiness(businessId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceDTO> getResourceById(@PathVariable UUID id, @AuthenticationPrincipal AuthPrincipal principal) {
        requireMembershipForResource(principal, id);
        return ResponseEntity.ok(resourceService.getResourceById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResourceDTO> updateResource(
            @PathVariable UUID id,
            @RequestBody UpdateResourceRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        requireMembershipForResource(principal, id);
        return ResponseEntity.ok(resourceService.updateResource(id, request));
    }

    @PostMapping("/{resourceId}/availability")
    public ResponseEntity<ResourceAvailabilityDTO> addAvailability(
            @PathVariable UUID resourceId,
            @RequestParam LocalDate date,
            @RequestParam LocalTime startTime,
            @RequestParam LocalTime endTime,
            @AuthenticationPrincipal AuthPrincipal principal) {
        requireMembershipForResource(principal, resourceId);
        ResourceAvailabilityDTO availability = resourceService.addAvailabilityWindow(resourceId, date, startTime, endTime);
        return ResponseEntity.status(HttpStatus.CREATED).body(availability);
    }

    @GetMapping("/{resourceId}/availability")
    public ResponseEntity<List<ResourceAvailabilityDTO>> getAvailability(
            @PathVariable UUID resourceId, @AuthenticationPrincipal AuthPrincipal principal) {
        requireMembershipForResource(principal, resourceId);
        List<ResourceAvailabilityDTO> availability = resourceService.getResourceAvailabilityByResourceId(resourceId);
        return ResponseEntity.status(HttpStatus.CREATED).body(availability);
    }

    @PutMapping("/{resourceId}/availability/{availabilityId}")
    public ResponseEntity<ResourceAvailabilityDTO> updateAvailability(
            @PathVariable UUID resourceId,
            @PathVariable UUID availabilityId,
            @RequestParam LocalDate date,
            @RequestParam LocalTime startTime,
            @RequestParam LocalTime endTime,
            @AuthenticationPrincipal AuthPrincipal principal) {
        requireMembershipForResource(principal, resourceId);
        ResourceAvailabilityDTO availability =
                resourceService.updateAvailabilityWindow(resourceId, availabilityId, date, startTime, endTime);
        return ResponseEntity.ok(availability);
    }

    @DeleteMapping("/{resourceId}/availability/{availabilityId}")
    public ResponseEntity<Void> deleteAvailability(
            @PathVariable UUID resourceId,
            @PathVariable UUID availabilityId,
            @AuthenticationPrincipal AuthPrincipal principal) {
        requireMembershipForResource(principal, resourceId);
        resourceService.deleteAvailabilityWindow(resourceId, availabilityId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable UUID id, @AuthenticationPrincipal AuthPrincipal principal) {
        requireMembershipForResource(principal, id);
        resourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }

    private void requireMembershipForResource(AuthPrincipal principal, UUID resourceId) {
        UUID businessId = resourceService.getResourceEntityById(resourceId).getBusiness().getId();
        membershipService.requireMembership(principal.userId(), businessId);
    }
}
