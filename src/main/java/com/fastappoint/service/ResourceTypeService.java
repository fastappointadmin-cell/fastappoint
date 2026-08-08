package com.fastappoint.service;

import com.fastappoint.domain.Business;
import com.fastappoint.domain.ResourceAttributeDefinition;
import com.fastappoint.domain.ResourceAttributeType;
import com.fastappoint.domain.ResourceType;
import com.fastappoint.dto.CreateResourceAttributeDefinitionRequest;
import com.fastappoint.dto.CreateResourceTypeRequest;
import com.fastappoint.dto.ResourceAttributeDefinitionDTO;
import com.fastappoint.dto.ResourceTypeDTO;
import com.fastappoint.dto.UpdateResourceAttributeDefinitionRequest;
import com.fastappoint.dto.UpdateResourceTypeRequest;
import com.fastappoint.exception.BusinessNotFoundException;
import com.fastappoint.exception.InvalidAppointmentException;
import com.fastappoint.repository.BusinessRepository;
import com.fastappoint.repository.ResourceTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ResourceTypeService {

    private final ResourceTypeRepository resourceTypeRepository;
    private final BusinessRepository businessRepository;

    public ResourceTypeService(ResourceTypeRepository resourceTypeRepository, BusinessRepository businessRepository) {
        this.resourceTypeRepository = resourceTypeRepository;
        this.businessRepository = businessRepository;
    }

    public ResourceTypeDTO createResourceType(UUID businessId, CreateResourceTypeRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new InvalidAppointmentException("Resource type name cannot be empty");
        }

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found with ID: " + businessId));

        String normalizedName = request.getName().trim();
        if (resourceTypeRepository.findByBusinessIdAndNameIgnoreCase(businessId, normalizedName).isPresent()) {
            throw new InvalidAppointmentException("Resource type already exists for this business: " + normalizedName);
        }

        ResourceType created = business.resourceTypeNamed(normalizedName);
        Business saved = businessRepository.save(business);

        ResourceType savedType = saved.getResourceTypes().stream()
                .filter(type -> type.getName().equalsIgnoreCase(normalizedName))
                .findFirst()
                .orElse(created);

        return toDTO(savedType);
    }

    @Transactional(readOnly = true)
    public List<ResourceTypeDTO> getResourceTypesByBusiness(UUID businessId) {
        businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found with ID: " + businessId));

        return resourceTypeRepository.findByBusinessId(businessId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ResourceType getResourceTypeEntityById(UUID id) {
        return resourceTypeRepository.findById(id)
                .orElseThrow(() -> new InvalidAppointmentException("Resource type not found with ID: " + id));
    }

    public ResourceTypeDTO updateResourceType(UUID id, UpdateResourceTypeRequest request) {
        ResourceType type = getResourceTypeEntityById(id);

        if (request.getName() != null) {
            String normalizedName = request.getName().trim();
            if (normalizedName.isEmpty()) {
                throw new InvalidAppointmentException("Resource type name cannot be empty");
            }

            resourceTypeRepository.findByBusinessIdAndNameIgnoreCase(type.getBusiness().getId(), normalizedName)
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new InvalidAppointmentException(
                                    "Resource type already exists for this business: " + normalizedName);
                        }
                    });

            type.rename(normalizedName);
        }

        ResourceType updated = resourceTypeRepository.save(type);
        return toDTO(updated);
    }

    public ResourceAttributeDefinitionDTO createAttributeDefinition(
            UUID resourceTypeId,
            CreateResourceAttributeDefinitionRequest request
    ) {
        ResourceType resourceType = getResourceTypeEntityById(resourceTypeId);
        String normalizedName = normalizeDefinitionName(request.getName());
        ensureUniqueDefinitionName(resourceType, normalizedName, null);

        ResourceAttributeType type = requireDefinitionType(request.getType());
        boolean required = Boolean.TRUE.equals(request.getRequired());
        List<String> options = ResourceAttributeValidation.normalizeOptions(type, request.getOptions());

        ResourceAttributeDefinition created = resourceType.addAttributeDefinition(normalizedName, type, required, options);
        ResourceType updated = resourceTypeRepository.save(resourceType);
        ResourceAttributeDefinition saved = updated.findAttributeDefinition(created.getId()).orElse(created);
        return toAttributeDefinitionDTO(saved);
    }

    public ResourceAttributeDefinitionDTO updateAttributeDefinition(
            UUID resourceTypeId,
            UUID attributeDefinitionId,
            UpdateResourceAttributeDefinitionRequest request
    ) {
        ResourceType resourceType = getResourceTypeEntityById(resourceTypeId);
        ResourceAttributeDefinition definition = resourceType.findAttributeDefinition(attributeDefinitionId)
                .orElseThrow(() -> new InvalidAppointmentException("Attribute definition not found with ID: " + attributeDefinitionId));

        if (request.getName() != null) {
            String normalizedName = normalizeDefinitionName(request.getName());
            ensureUniqueDefinitionName(resourceType, normalizedName, definition.getId());
            definition.rename(normalizedName);
        }

        ResourceAttributeType nextType = request.getType() != null ? request.getType() : definition.getType();
        definition.changeType(nextType);
        definition.setRequired(request.getRequired() != null ? request.getRequired() : definition.isRequired());
        List<String> options = ResourceAttributeValidation.normalizeOptions(nextType, request.getOptions());
        definition.replaceOptions(options);

        ResourceType updated = resourceTypeRepository.save(resourceType);
        ResourceAttributeDefinition saved = updated.findAttributeDefinition(attributeDefinitionId).orElse(definition);
        return toAttributeDefinitionDTO(saved);
    }

    public void deleteAttributeDefinition(UUID resourceTypeId, UUID attributeDefinitionId) {
        ResourceType resourceType = getResourceTypeEntityById(resourceTypeId);
        boolean removed = resourceType.removeAttributeDefinition(attributeDefinitionId);
        if (!removed) {
            throw new InvalidAppointmentException("Attribute definition not found with ID: " + attributeDefinitionId);
        }
        resourceTypeRepository.save(resourceType);
    }

    public void deleteResourceType(UUID id) {
        ResourceType type = getResourceTypeEntityById(id);
        resourceTypeRepository.delete(type);
    }

    private ResourceTypeDTO toDTO(ResourceType type) {
        List<ResourceAttributeDefinitionDTO> attributeDefinitions = type.getAttributeDefinitions().stream()
                .map(this::toAttributeDefinitionDTO)
                .collect(Collectors.toList());
        return new ResourceTypeDTO(type.getId(), type.getBusiness().getId(), type.getName(), attributeDefinitions);
    }

    private ResourceAttributeDefinitionDTO toAttributeDefinitionDTO(ResourceAttributeDefinition definition) {
        return new ResourceAttributeDefinitionDTO(
                definition.getId(),
                definition.getResourceType().getId(),
                definition.getName(),
                definition.getType(),
                definition.isRequired(),
                List.copyOf(definition.getOptions())
        );
    }

    private String normalizeDefinitionName(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) {
            throw new InvalidAppointmentException("Attribute name cannot be empty");
        }
        return rawName.trim();
    }

    private ResourceAttributeType requireDefinitionType(ResourceAttributeType type) {
        if (type == null) {
            throw new InvalidAppointmentException("Attribute type is required");
        }
        return type;
    }

    private void ensureUniqueDefinitionName(ResourceType resourceType, String candidateName, UUID currentDefinitionId) {
        resourceType.getAttributeDefinitions().forEach(existing -> {
            if (existing.getName().equalsIgnoreCase(candidateName)
                    && (currentDefinitionId == null || !existing.getId().equals(currentDefinitionId))) {
                throw new InvalidAppointmentException("Attribute already exists for this resource type: " + candidateName);
            }
        });
    }
}
