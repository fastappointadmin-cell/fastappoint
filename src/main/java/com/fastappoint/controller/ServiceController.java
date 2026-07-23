package com.fastappoint.controller;

import com.fastappoint.dto.AddServiceRequirementRequest;
import com.fastappoint.dto.CreateServiceRequest;
import com.fastappoint.dto.ServiceDTO;
import com.fastappoint.dto.UpdateServiceRequest;
import com.fastappoint.service.ServiceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "http://localhost:4200")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    /**
     * Create a new service for a business
     * POST /api/services?businessId={id}
     */
    @PostMapping
    public ResponseEntity<ServiceDTO> createService(
            @RequestParam UUID businessId,
            @RequestBody CreateServiceRequest request) {
        ServiceDTO service = serviceService.createService(businessId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(service);
    }

    /**
     * Get all services for a business
     * GET /api/services?businessId={id}
     */
    @GetMapping
    public ResponseEntity<List<ServiceDTO>> getServicesByBusiness(@RequestParam UUID businessId) {
        List<ServiceDTO> services = serviceService.getServicesByBusiness(businessId);
        return ResponseEntity.ok(services);
    }

    /**
     * Get a service by ID
     * GET /api/services/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceDTO> getServiceById(@PathVariable UUID id) {
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
            @RequestBody UpdateServiceRequest request) {
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
            @RequestBody AddServiceRequirementRequest request) {
        ServiceDTO service = serviceService.addRequirementToService(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(service);
    }

    /**
     * Delete a service
     * DELETE /api/services/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable UUID id) {
        serviceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}