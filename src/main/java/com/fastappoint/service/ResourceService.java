package com.fastappoint.service;

import com.fastappoint.domain.Business;
import com.fastappoint.domain.ResourceAttributeDefinition;
import com.fastappoint.domain.ResourceAttributeValue;
import com.fastappoint.domain.Resource;
import com.fastappoint.domain.ResourceAvailability;
import com.fastappoint.domain.ResourceType;
import com.fastappoint.dto.CreateResourceRequest;
import com.fastappoint.dto.ResourceAttributeValueDTO;
import com.fastappoint.dto.ResourceAttributeValueInput;
import com.fastappoint.dto.ResourceAvailabilityDTO;
import com.fastappoint.dto.ResourceDTO;
import com.fastappoint.dto.UpdateResourceRequest;
import com.fastappoint.exception.BusinessNotFoundException;
import com.fastappoint.exception.InvalidAppointmentException;
import com.fastappoint.exception.ResourceNotFoundException;
import com.fastappoint.repository.BusinessRepository;
import com.fastappoint.repository.ResourceRepository;
import com.fastappoint.repository.ResourceTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final BusinessRepository businessRepository;
    private final ResourceTypeRepository resourceTypeRepository;

    public ResourceService(ResourceRepository resourceRepository, BusinessRepository businessRepository,
                          ResourceTypeRepository resourceTypeRepository) {
        this.resourceRepository = resourceRepository;
        this.businessRepository = businessRepository;
        this.resourceTypeRepository = resourceTypeRepository;
    }

    @Transactional
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
        resource.withMergeGroup(normalizeMergeGroup(request.getMergeGroup()));
        applyAttributeValues(resource, resourceType, request.getAttributeValues());

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

    @Transactional(readOnly = true)
    public List<ResourceAvailabilityDTO> getResourceAvailabilityByResourceId(UUID id) {
        return this.resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + id)).getAvailability()
                .stream().map(this::convertAvailabilityToDTO).collect(Collectors.toList());
    }

    @Transactional
    public ResourceAvailabilityDTO addAvailabilityWindow(UUID resourceId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (date == null) {
            throw new InvalidAppointmentException("Date is required");
        }
        if (startTime == null || endTime == null) {
            throw new InvalidAppointmentException("Start time and end time are required");
        }
        if (!endTime.isAfter(startTime)) {
            throw new InvalidAppointmentException("End time must be after start time");
        }

        Resource resource = getResourceEntityById(resourceId);
        ResourceAvailability availability = resource.addAvailability(date, startTime, endTime);
        resourceRepository.save(resource);
        return convertAvailabilityToDTO(availability);
    }

    @Transactional
    public ResourceAvailabilityDTO updateAvailabilityWindow(
            UUID resourceId, UUID availabilityId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (date == null) {
            throw new InvalidAppointmentException("Date is required");
        }
        if (startTime == null || endTime == null) {
            throw new InvalidAppointmentException("Start time and end time are required");
        }
        if (!endTime.isAfter(startTime)) {
            throw new InvalidAppointmentException("End time must be after start time");
        }

        Resource resource = getResourceEntityById(resourceId);
        ResourceAvailability availability = resource.getAvailability().stream()
                .filter(existing -> existing.getId().equals(availabilityId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Availability window not found with ID: " + availabilityId));

        availability.setDate(date);
        availability.setStartTime(startTime);
        availability.setEndTime(endTime);
        resourceRepository.save(resource);
        return convertAvailabilityToDTO(availability);
    }

    @Transactional
    public void deleteAvailabilityWindow(UUID resourceId, UUID availabilityId) {
        Resource resource = getResourceEntityById(resourceId);
        ResourceAvailability availability = resource.getAvailability().stream()
                .filter(existing -> existing.getId().equals(availabilityId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Availability window not found with ID: " + availabilityId));

        resource.getAvailability().remove(availability);
        resourceRepository.save(resource);
    }


    @Transactional
    public ResourceDTO updateResource(UUID id, UpdateResourceRequest request) {
        Resource resource = getResourceEntityById(id);

        if (request.getName() != null) {
            String normalizedName = request.getName().trim();
            if (normalizedName.isEmpty()) {
                throw new InvalidAppointmentException("Resource name cannot be empty");
            }
            resource.rename(normalizedName);
        }

        if (request.getTypeId() != null) {
            ResourceType resourceType = resourceTypeRepository.findById(request.getTypeId())
                    .orElseThrow(() -> new InvalidAppointmentException("Resource type not found with ID: " + request.getTypeId()));
            if (!resourceType.getBusiness().getId().equals(resource.getBusiness().getId())) {
                throw new InvalidAppointmentException("Resource type does not belong to this business");
            }
            resource.changeType(resourceType);
            if (request.getAttributeValues() == null) {
                resource.clearAttributeValues();
            }
        }

        if (request.getCapacity() != null && request.getCapacity() <= 0) {
            throw new InvalidAppointmentException("Capacity must be positive when provided");
        }
        resource.withCapacity(request.getCapacity());
        resource.withMergeGroup(normalizeMergeGroup(request.getMergeGroup()));
        if (request.getAttributeValues() != null) {
            applyAttributeValues(resource, resource.getType(), request.getAttributeValues());
        }

        Resource updated = resourceRepository.save(resource);
        return convertToDTO(updated);
    }

    @Transactional
    public void deleteResource(UUID id) {
        resourceRepository.delete(getResourceEntityById(id));
    }


    private ResourceDTO convertToDTO(Resource resource) {
        List<ResourceAttributeValueDTO> attributeValues = resource.getType().getAttributeDefinitions().stream()
                .map(definition -> {
                    ResourceAttributeValue matchingValue = resource.getAttributeValues().stream()
                            .filter(attributeValue -> attributeValue.getAttributeDefinition().getId().equals(definition.getId()))
                            .findFirst()
                            .orElse(null);
                    return new ResourceAttributeValueDTO(
                            definition.getId(),
                            definition.getName(),
                            definition.getType(),
                            definition.isRequired(),
                            List.copyOf(definition.getOptions()),
                            matchingValue != null ? matchingValue.getValue() : null
                    );
                })
                .collect(Collectors.toList());
        return new ResourceDTO(
                resource.getId(),
                resource.getBusiness().getId(),
                resource.getType().getId(),
                resource.getName(),
                resource.getType().getName(),
                resource.getCapacity(),
                resource.getMergeGroup(),
                attributeValues
        );
    }

    private ResourceAvailabilityDTO convertAvailabilityToDTO(ResourceAvailability availability) {
        return new ResourceAvailabilityDTO(
                availability.getId(),
                availability.getResource().getId(),
                availability.getDate(),
                availability.getStartTime(),
                availability.getEndTime()
        );
    }

    private void applyAttributeValues(Resource resource, ResourceType resourceType, List<ResourceAttributeValueInput> inputs) {
        Map<UUID, ResourceAttributeDefinition> definitionsById = resourceType.getAttributeDefinitions().stream()
                .collect(Collectors.toMap(ResourceAttributeDefinition::getId, definition -> definition));
        Map<UUID, String> normalizedValuesByDefinitionId = new java.util.LinkedHashMap<>();

        for (ResourceAttributeValueInput input : inputs == null ? List.<ResourceAttributeValueInput>of() : inputs) {
            if (input.getAttributeDefinitionId() == null) {
                throw new InvalidAppointmentException("Attribute definition is required");
            }
            ResourceAttributeDefinition definition = definitionsById.get(input.getAttributeDefinitionId());
            if (definition == null) {
                throw new InvalidAppointmentException("Attribute does not belong to the selected resource type: " + input.getAttributeDefinitionId());
            }
            if (normalizedValuesByDefinitionId.containsKey(definition.getId())) {
                throw new InvalidAppointmentException("Attribute values must be unique per definition");
            }

            String normalizedValue = ResourceAttributeValidation.normalizeStoredValue(
                    definition,
                    input.getValue(),
                    definition.isRequired(),
                    "Attribute \"" + definition.getName() + "\""
            );
            if (normalizedValue != null) {
                normalizedValuesByDefinitionId.put(definition.getId(), normalizedValue);
            }
        }

        for (ResourceAttributeDefinition definition : definitionsById.values()) {
            if (definition.isRequired() && !normalizedValuesByDefinitionId.containsKey(definition.getId())) {
                throw new InvalidAppointmentException("Attribute \"" + definition.getName() + "\" is required");
            }
        }

        resource.clearAttributeValues();
        for (ResourceAttributeDefinition definition : resourceType.getAttributeDefinitions()) {
            String normalizedValue = normalizedValuesByDefinitionId.get(definition.getId());
            if (normalizedValue != null) {
                resource.setAttributeValue(definition, normalizedValue);
            }
        }
    }

    private String normalizeMergeGroup(String mergeGroup) {
        if (mergeGroup == null) {
            return null;
        }
        String normalized = mergeGroup.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
