package com.fastappoint.controller;

import com.fastappoint.domain.MembershipRole;
import com.fastappoint.dto.BusinessDTO;
import com.fastappoint.dto.CreateBusinessRequest;
import com.fastappoint.security.AuthPrincipal;
import com.fastappoint.service.BusinessService;
import com.fastappoint.service.MembershipService;
import com.fastappoint.service.ResourceService;
import com.fastappoint.service.BusinessServiceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/businesses")
public class BusinessController {

    private final BusinessService businessService;
    private final BusinessServiceService serviceService;
    private final ResourceService resourceService;
    private final MembershipService membershipService;

    public BusinessController(BusinessService businessService, BusinessServiceService serviceService,
                               ResourceService resourceService, MembershipService membershipService) {
        this.businessService = businessService;
        this.serviceService = serviceService;
        this.resourceService = resourceService;
        this.membershipService = membershipService;
    }

    /**
     * Create a new business owned by the caller (e.g. "add another business" from an existing account
     * -- the FIRST business normally comes from /api/auth/register instead).
     * POST /api/businesses
     */
    @PostMapping
    public ResponseEntity<BusinessDTO> createBusiness(
            @RequestBody CreateBusinessRequest request, @AuthenticationPrincipal AuthPrincipal principal) {
        BusinessDTO business = businessService.createBusiness(request);
        membershipService.grantByUserId(principal.userId(), business.getId(), MembershipRole.OWNER);
        return ResponseEntity.status(HttpStatus.CREATED).body(business);
    }

    /**
     * The caller's own businesses (via their memberships) -- not every business in the database.
     * GET /api/businesses
     */
    @GetMapping
    public ResponseEntity<List<BusinessDTO>> getAllBusinesses(@AuthenticationPrincipal AuthPrincipal principal) {
        List<UUID> businessIds = membershipService.findByUser(principal.userId()).stream()
                .map(membership -> membership.getBusiness().getId())
                .collect(Collectors.toList());
        return ResponseEntity.ok(businessService.getBusinessesByIds(businessIds));
    }

    /**
     * Get a business by ID
     * GET /api/businesses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<BusinessDTO> getBusinessById(
            @PathVariable UUID id, @AuthenticationPrincipal AuthPrincipal principal) {
        membershipService.requireMembership(principal.userId(), id);
        BusinessDTO business = businessService.getBusinessById(id);
        business.setServices(serviceService.getServicesByBusiness(id));
        business.setResources(resourceService.getResourcesByBusiness(id));
        return ResponseEntity.ok(business);
    }

    /**
     * Update a business
     * PUT /api/businesses/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<BusinessDTO> updateBusiness(
            @PathVariable UUID id,
            @RequestBody CreateBusinessRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        membershipService.requireMembership(principal.userId(), id);
        BusinessDTO business = businessService.updateBusiness(id, request);
        return ResponseEntity.ok(business);
    }

    /**
     * Delete a business
     * DELETE /api/businesses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBusiness(
            @PathVariable UUID id, @AuthenticationPrincipal AuthPrincipal principal) {
        membershipService.requireMembership(principal.userId(), id);
        businessService.deleteBusiness(id);
        return ResponseEntity.noContent().build();
    }
}
