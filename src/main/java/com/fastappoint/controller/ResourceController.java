package com.fastappoint.controller;

import com.fastappoint.dto.CreateResourceRequest;
import com.fastappoint.dto.ResourceAvailabilityDTO;
import com.fastappoint.dto.ResourceDTO;
import com.fastappoint.service.ResourceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resources")
@CrossOrigin(origins = "http://localhost:4200")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping
    public ResponseEntity<ResourceDTO> createResource(
            @RequestParam UUID businessId,
            @RequestBody CreateResourceRequest request) {
        ResourceDTO resource = resourceService.createResource(businessId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    @GetMapping
    public ResponseEntity<List<ResourceDTO>> getResourcesByBusiness(@RequestParam UUID businessId) {
        return ResponseEntity.ok(resourceService.getResourcesByBusiness(businessId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceDTO> getResourceById(@PathVariable UUID id) {
        return ResponseEntity.ok(resourceService.getResourceById(id));
    }

    @PostMapping("/{resourceId}/availability")
    public ResponseEntity<ResourceAvailabilityDTO> addAvailability(
            @PathVariable UUID resourceId,
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam LocalTime startTime,
            @RequestParam LocalTime endTime) {
        ResourceAvailabilityDTO availability = resourceService.addAvailabilityWindow(resourceId, dayOfWeek, startTime, endTime);
        return ResponseEntity.status(HttpStatus.CREATED).body(availability);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable UUID id) {
        resourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }
}

