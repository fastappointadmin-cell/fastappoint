package com.fastappoint.service;

import com.fastappoint.domain.Business;
import com.fastappoint.domain.ResourceType;
import com.fastappoint.dto.CreateResourceTypeRequest;
import com.fastappoint.dto.ResourceTypeDTO;
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

    private ResourceTypeDTO toDTO(ResourceType type) {
        return new ResourceTypeDTO(type.getId(), type.getBusiness().getId(), type.getName());
    }
}

