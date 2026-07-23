package com.fastappoint.controller;

import com.fastappoint.dto.CreateResourceTypeRequest;
import com.fastappoint.dto.ResourceTypeDTO;
import com.fastappoint.service.ResourceTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resource-types")
@CrossOrigin(origins = "http://localhost:4200")
public class ResourceTypeController {

    private final ResourceTypeService resourceTypeService;

    public ResourceTypeController(ResourceTypeService resourceTypeService) {
        this.resourceTypeService = resourceTypeService;
    }

    @PostMapping
    public ResponseEntity<ResourceTypeDTO> createResourceType(
            @RequestParam UUID businessId,
            @RequestBody CreateResourceTypeRequest request) {
        ResourceTypeDTO created = resourceTypeService.createResourceType(businessId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ResourceTypeDTO>> getResourceTypesByBusiness(@RequestParam UUID businessId) {
        return ResponseEntity.ok(resourceTypeService.getResourceTypesByBusiness(businessId));
    }
}

