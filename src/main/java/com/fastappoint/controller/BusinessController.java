package com.fastappoint.controller;

import com.fastappoint.dto.BusinessDTO;
import com.fastappoint.dto.CreateBusinessRequest;
import com.fastappoint.service.BusinessService;
import com.fastappoint.service.ResourceService;
import com.fastappoint.service.ServiceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/businesses")
@CrossOrigin(origins = "http://localhost:4200")
public class BusinessController {

    private final BusinessService businessService;
    private final ServiceService serviceService;
    private final ResourceService resourceService;

    public BusinessController(BusinessService businessService, ServiceService serviceService, ResourceService resourceService) {
        this.businessService = businessService;
        this.serviceService = serviceService;
        this.resourceService = resourceService;
    }

    /**
     * Create a new business
     * POST /api/businesses
     */
    @PostMapping
    public ResponseEntity<BusinessDTO> createBusiness(@RequestBody CreateBusinessRequest request) {
        BusinessDTO business = businessService.createBusiness(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(business);
    }

    /**
     * Get all businesses
     * GET /api/businesses
     */
    @GetMapping
    public ResponseEntity<List<BusinessDTO>> getAllBusinesses() {
        List<BusinessDTO> businesses = businessService.getAllBusinesses();
        return ResponseEntity.ok(businesses);
    }

    /**
     * Get a business by ID
     * GET /api/businesses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<BusinessDTO> getBusinessById(@PathVariable UUID id) {
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
            @RequestBody CreateBusinessRequest request) {
        BusinessDTO business = businessService.updateBusiness(id, request);
        return ResponseEntity.ok(business);
    }

    /**
     * Delete a business
     * DELETE /api/businesses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBusiness(@PathVariable UUID id) {
        businessService.deleteBusiness(id);
        return ResponseEntity.noContent().build();
    }
}