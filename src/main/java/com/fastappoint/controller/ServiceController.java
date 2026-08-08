package com.fastappoint.controller;

import com.fastappoint.dto.AddServiceRequirementRequest;
import com.fastappoint.dto.CreateServiceRequest;
import com.fastappoint.dto.ServiceDTO;
import com.fastappoint.dto.ServiceRequirementDTO;
import com.fastappoint.dto.UpdateServiceRequest;
import com.fastappoint.dto.UpdateServiceRequirementRequest;
import com.fastappoint.security.AuthPrincipal;
import com.fastappoint.service.BusinessServiceService;
import com.fastappoint.service.MembershipService;
import com.fastappoint.service.SchedulingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final BusinessServiceService serviceService;
    private final SchedulingService schedulingService;
    private final MembershipService membershipService;

    public ServiceController(BusinessServiceService serviceService, SchedulingService schedulingService,
                              MembershipService membershipService) {
        this.serviceService = serviceService;
        this.schedulingService = schedulingService;
        this.membershipService = membershipService;
    }

    /**
     * Create a new service for a business
     * POST /api/services?businessId={id}
     */
    @PostMapping
    public ResponseEntity<ServiceDTO> createService(
            @RequestParam UUID businessId,
            @RequestBody CreateServiceRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        membershipService.requireMembership(principal.userId(), businessId);
        ServiceDTO service = serviceService.createService(businessId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(service);
    }

    /**
     * Get all services for a business
     * GET /api/services?businessId={id}
     */
    @GetMapping
    public ResponseEntity<List<ServiceDTO>> getServicesByBusiness(
            @RequestParam UUID businessId, @AuthenticationPrincipal AuthPrincipal principal) {
        membershipService.requireMembership(principal.userId(), businessId);
        List<ServiceDTO> services = serviceService.getServicesByBusiness(businessId);
        return ResponseEntity.ok(services);
    }

    /**
     * Get a service by ID
     * GET /api/services/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceDTO> getServiceById(@PathVariable UUID id, @AuthenticationPrincipal AuthPrincipal principal) {
        requireMembershipForService(principal, id);
        ServiceDTO service = serviceService.getServiceById(id);
        return ResponseEntity.ok(service);
    }

    /**
     * Update an existing service
     * PUT /api/services/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ServiceDTO> updateService(
            @PathVariable UUID id,
            @RequestBody UpdateServiceRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        requireMembershipForService(principal, id);
        ServiceDTO service = serviceService.updateService(id, request);
        return ResponseEntity.ok(service);
    }

    /**
     * Add a requirement to a service
     * POST /api/services/{id}/requirements
     */
    @PostMapping("/{id}/requirements")
    public ResponseEntity<ServiceDTO> addRequirementToService(
            @PathVariable UUID id,
            @RequestBody AddServiceRequirementRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        requireMembershipForService(principal, id);
        ServiceDTO service = serviceService.addRequirementToService(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(service);
    }

    @PutMapping("/{id}/requirements/{requirementId}")
    public ResponseEntity<ServiceRequirementDTO> updateRequirementInService(
            @PathVariable UUID id,
            @PathVariable UUID requirementId,
            @RequestBody UpdateServiceRequirementRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        requireMembershipForService(principal, id);
        ServiceRequirementDTO requirement = serviceService.updateRequirementInService(id, requirementId, request);
        return ResponseEntity.ok(requirement);
    }

    @DeleteMapping("/{id}/requirements/{requirementId}")
    public ResponseEntity<Void> deleteRequirementFromService(
            @PathVariable UUID id,
            @PathVariable UUID requirementId,
            @AuthenticationPrincipal AuthPrincipal principal) {
        requireMembershipForService(principal, id);
        serviceService.deleteRequirementFromService(id, requirementId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Every bookable start time for this service on a date -- backs the "first available slot" UI.
     * GET /api/services/{id}/available-starts?date=2026-08-01&preferredResourceIds=...&stepMinutes=15
     */
    @GetMapping("/{id}/available-starts")
    public ResponseEntity<List<LocalDateTime>> getAvailableStarts(
            @PathVariable UUID id,
            @RequestParam LocalDate date,
            @RequestParam(required = false) Set<UUID> preferredResourceIds,
            @RequestParam(defaultValue = "15") int stepMinutes,
            @RequestParam Map<String, String> requestParams,
            @AuthenticationPrincipal AuthPrincipal principal) {
        requireMembershipForService(principal, id);
        List<LocalDateTime> starts = schedulingService.availableStarts(
                id,
                date,
                preferredResourceIds == null ? Set.of() : preferredResourceIds,
                Duration.ofMinutes(stepMinutes),
                extractInputs(requestParams));
        return ResponseEntity.ok(starts);
    }

    /**
     * Delete a service
     * DELETE /api/services/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable UUID id, @AuthenticationPrincipal AuthPrincipal principal) {
        requireMembershipForService(principal, id);
        serviceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }

    private void requireMembershipForService(AuthPrincipal principal, UUID serviceId) {
        UUID businessId = serviceService.getServiceEntityById(serviceId).getBusiness().getId();
        membershipService.requireMembership(principal.userId(), businessId);
    }

    private Map<String, Integer> extractInputs(Map<String, String> requestParams) {
        Map<String, Integer> inputs = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, String> entry : requestParams.entrySet()) {
            if (!entry.getKey().startsWith("input.")) {
                continue;
            }
            String inputKey = entry.getKey().substring("input.".length()).trim();
            if (inputKey.isEmpty()) {
                continue;
            }
            try {
                inputs.put(inputKey, Integer.parseInt(entry.getValue()));
            } catch (NumberFormatException ex) {
                throw new com.fastappoint.exception.InvalidAppointmentException(
                        "Booking input \"" + inputKey + "\" must be a whole number", ex);
            }
        }
        return inputs;
    }
}
