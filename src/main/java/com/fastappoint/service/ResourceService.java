package com.fastappoint.service;

import com.fastappoint.domain.Business;
import com.fastappoint.domain.Capability;
import com.fastappoint.domain.Resource;
import com.fastappoint.domain.ResourceAvailability;
import com.fastappoint.domain.ResourceType;
import com.fastappoint.dto.CapabilityRefDTO;
import com.fastappoint.dto.CreateResourceRequest;
import com.fastappoint.dto.ResourceAvailabilityDTO;
import com.fastappoint.dto.ResourceDTO;
import com.fastappoint.exception.BusinessNotFoundException;
import com.fastappoint.exception.InvalidAppointmentException;
import com.fastappoint.exception.ResourceNotFoundException;
import com.fastappoint.repository.BusinessRepository;
import com.fastappoint.repository.CapabilityRepository;
import com.fastappoint.repository.ResourceRepository;
import com.fastappoint.repository.ResourceTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final BusinessRepository businessRepository;
    private final CapabilityRepository capabilityRepository;
    private final ResourceTypeRepository resourceTypeRepository;

    public ResourceService(ResourceRepository resourceRepository, BusinessRepository businessRepository,
                          CapabilityRepository capabilityRepository,
                          ResourceTypeRepository resourceTypeRepository) {
        this.resourceRepository = resourceRepository;
        this.businessRepository = businessRepository;
        this.capabilityRepository = capabilityRepository;
        this.resourceTypeRepository = resourceTypeRepository;
    }

    public ResourceDTO createResource(UUID businessId, CreateResourceRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new InvalidAppointmentException("Resource name cannot be empty");
        }
        if (request.getTypeId() == null) {
            throw new InvalidAppointmentException("Resource type is required");
        }
        if (request.getCapacity() != null && request.getCapacity() <= 0) {
            throw new InvalidAppointmentException("Capacity must be positive when provided");
        }

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found with ID: " + businessId));

        ResourceType resourceType = resourceTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new InvalidAppointmentException("Resource type not found with ID: " + request.getTypeId()));
        if (!resourceType.getBusiness().getId().equals(businessId)) {
            throw new InvalidAppointmentException("Resource type does not belong to this business");
        }

        Resource resource = business.addResource(request.getName().trim(), resourceType);
        if (request.getCapacity() != null) {
            resource.withCapacity(request.getCapacity());
        }
        
        Set<Capability> capabilities = resolveCapabilities(request.getCapabilityIds());
        for (Capability capability : capabilities) {
            resource.addCapability(capability);
        }

        // Persist through aggregate root so a newly inferred ResourceType is inserted
        // before Resource references it.
        Business savedBusiness = businessRepository.save(business);
        Resource savedResource = savedBusiness.getResources().stream()
                .filter(existing -> existing.getId().equals(resource.getId()))
                .findFirst()
                .orElse(resource);

        return convertToDTO(savedResource);
    }

    @Transactional(readOnly = true)
    public List<ResourceDTO> getResourcesByBusiness(UUID businessId) {
        businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found with ID: " + businessId));

        return resourceRepository.findByBusinessId(businessId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ResourceDTO getResourceById(UUID id) {
        return convertToDTO(getResourceEntityById(id));
    }

    @Transactional(readOnly = true)
    public Resource getResourceEntityById(UUID id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + id));
    }

    public ResourceAvailabilityDTO addAvailabilityWindow(UUID resourceId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        if (dayOfWeek == null) {
            throw new InvalidAppointmentException("Day of week is required");
        }
        if (startTime == null || endTime == null) {
            throw new InvalidAppointmentException("Start time and end time are required");
        }
        if (!endTime.isAfter(startTime)) {
            throw new InvalidAppointmentException("End time must be after start time");
        }

        Resource resource = getResourceEntityById(resourceId);
        ResourceAvailability availability = resource.addAvailability(dayOfWeek, startTime, endTime);
        resourceRepository.save(resource);
        return convertAvailabilityToDTO(availability);
    }

    public void deleteResource(UUID id) {
        resourceRepository.delete(getResourceEntityById(id));
    }


    private Set<Capability> resolveCapabilities(Set<UUID> capabilityIds) {
        Set<Capability> resolved = new HashSet<>();
        if (capabilityIds == null) {
            return resolved;
        }
        for (UUID capabilityId : capabilityIds) {
            Capability capability = capabilityRepository.findById(capabilityId)
                    .orElseThrow(() -> new InvalidAppointmentException("Capability not found with ID: " + capabilityId));
            resolved.add(capability);
        }
        return resolved;
    }

    private ResourceDTO convertToDTO(Resource resource) {
        Set<CapabilityRefDTO> capabilityRefs = resource.getCapabilities().stream()
                .map(cap -> new CapabilityRefDTO(cap.getId(), cap.getName()))
                .collect(Collectors.toSet());

        return new ResourceDTO(
                resource.getId(),
                resource.getBusiness().getId(),
                resource.getType().getId(),
                resource.getName(),
                resource.getType().getName(),
                resource.getCapacity(),
                capabilityRefs
        );
    }

    private ResourceAvailabilityDTO convertAvailabilityToDTO(ResourceAvailability availability) {
        return new ResourceAvailabilityDTO(
                availability.getId(),
                availability.getResource().getId(),
                availability.getDayOfWeek(),
                availability.getStartTime(),
                availability.getEndTime()
        );
    }
}

