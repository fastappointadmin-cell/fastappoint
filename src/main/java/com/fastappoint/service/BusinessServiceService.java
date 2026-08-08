package com.fastappoint.service;

import com.fastappoint.domain.Business;
import com.fastappoint.domain.BusinessService;
import com.fastappoint.domain.ResourceAttributeDefinition;
import com.fastappoint.domain.ResourceType;
import com.fastappoint.domain.ServiceRequirement;
import com.fastappoint.domain.ServiceRequirementConstraint;
import com.fastappoint.domain.ServiceRequirementFulfillmentMode;
import com.fastappoint.dto.AddServiceRequirementRequest;
import com.fastappoint.dto.CreateServiceRequest;
import com.fastappoint.dto.ServiceRequirementConstraintDTO;
import com.fastappoint.dto.ServiceRequirementConstraintInput;
import com.fastappoint.dto.ServiceDTO;
import com.fastappoint.dto.ServiceRequirementDTO;
import com.fastappoint.dto.UpdateServiceRequest;
import com.fastappoint.dto.UpdateServiceRequirementRequest;
import com.fastappoint.exception.BusinessNotFoundException;
import com.fastappoint.exception.InvalidAppointmentException;
import com.fastappoint.exception.ServiceNotFoundException;
import com.fastappoint.repository.BusinessRepository;
import com.fastappoint.repository.ServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class BusinessServiceService {

    private final ServiceRepository serviceRepository;
    private final BusinessRepository businessRepository;

    public BusinessServiceService(ServiceRepository serviceRepository, BusinessRepository businessRepository) {
        this.serviceRepository = serviceRepository;
        this.businessRepository = businessRepository;
    }

    /**
     * Create a new service for a business
     */
    public ServiceDTO createService(UUID businessId, CreateServiceRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new InvalidAppointmentException("Service name cannot be empty");
        }

        if (request.getDurationSeconds() <= 0) {
            throw new InvalidAppointmentException("Duration must be positive");
        }

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found with ID: " + businessId));

        BusinessService businessService = business.addService(
                request.getName().trim(),
                Duration.ofSeconds(request.getDurationSeconds())
        );
        BusinessService saved = serviceRepository.save(businessService);
        businessRepository.save(business);

        return convertToDTO(saved);
    }

    /**
     * Get all services for a business
     */
    @Transactional(readOnly = true)
    public List<ServiceDTO> getServicesByBusiness(UUID businessId) {
        businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found with ID: " + businessId));

        return serviceRepository.findByBusinessId(businessId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get service by ID
     */
    @Transactional(readOnly = true)
    public ServiceDTO getServiceById(UUID id) {
        BusinessService businessService = serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException("Service not found with ID: " + id));
        return convertToDTO(businessService);
    }

    /**
     * Get service entity by ID (internal use)
     */
    @Transactional(readOnly = true)
    public BusinessService getServiceEntityById(UUID id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException("Service not found with ID: " + id));
    }

    public ServiceDTO updateService(UUID id, UpdateServiceRequest request) {
        BusinessService businessService = getServiceEntityById(id);

        if (request.getName() != null) {
            String normalizedName = request.getName().trim();
            if (normalizedName.isEmpty()) {
                throw new InvalidAppointmentException("Service name cannot be empty");
            }
            businessService.rename(normalizedName);
        }

        if (request.getDurationSeconds() != null) {
            if (request.getDurationSeconds() <= 0) {
                throw new InvalidAppointmentException("Duration must be positive");
            }
            businessService.changeDuration(Duration.ofSeconds(request.getDurationSeconds()));
        }

        BusinessService updated = serviceRepository.save(businessService);
        return convertToDTO(updated);
    }

    public ServiceDTO addRequirementToService(UUID serviceId, AddServiceRequirementRequest request) {
        BusinessService businessService = getServiceEntityById(serviceId);

        if (request.getResourceTypeId() == null) {
            throw new InvalidAppointmentException("Resource type is required");
        }

        Business business = businessService.getBusiness();
        ResourceType resourceType = business.getResourceTypes().stream()
                .filter(type -> type.getId().equals(request.getResourceTypeId()))
                .findFirst()
                .orElseThrow(() -> new InvalidAppointmentException(
                        "Resource type is not available for this business: " + request.getResourceTypeId()));

        int quantity = request.getQuantity() != null ? request.getQuantity() : 1;
        ServiceRequirement requirement = businessService.addRequirement(resourceType, quantity);
        applyRequirementFulfillment(
                requirement,
                request.getFulfillmentMode(),
                request.getQuantity(),
                request.getRequiredCapacity(),
                request.getCapacityInputKey()
        );
        applyConstraints(requirement, resourceType, request.getConstraints());

        BusinessService updated = serviceRepository.save(businessService);
        businessRepository.save(business);
        return convertToDTO(updated);
    }

    public ServiceRequirementDTO updateRequirementInService(UUID serviceId, UUID requirementId, UpdateServiceRequirementRequest request) {
        BusinessService businessService = getServiceEntityById(serviceId);
        ServiceRequirement requirement = businessService.findRequirement(requirementId)
                .orElseThrow(() -> new InvalidAppointmentException("Requirement not found with ID: " + requirementId));

        if (request.getResourceTypeId() != null) {
            ResourceType resourceType = businessService.getBusiness().getResourceTypes().stream()
                    .filter(type -> type.getId().equals(request.getResourceTypeId()))
                    .findFirst()
                    .orElseThrow(() -> new InvalidAppointmentException(
                            "Resource type is not available for this business: " + request.getResourceTypeId()));
            requirement.changeResourceType(resourceType);
            if (request.getConstraints() == null) {
                requirement.clearConstraints();
            }
        }

        applyRequirementFulfillment(
                requirement,
                request.getFulfillmentMode(),
                request.getQuantity(),
                request.getRequiredCapacity(),
                request.getCapacityInputKey()
        );

        if (request.getConstraints() != null) {
            applyConstraints(requirement, requirement.getResourceType(), request.getConstraints());
        }

        BusinessService updated = serviceRepository.save(businessService);
        ServiceRequirement updatedRequirement = updated.findRequirement(requirementId)
                .orElseThrow(() -> new InvalidAppointmentException("Requirement not found with ID: " + requirementId));
        return convertRequirementToDTO(updatedRequirement);
    }

    public void deleteRequirementFromService(UUID serviceId, UUID requirementId) {
        BusinessService businessService = getServiceEntityById(serviceId);
        boolean removed = businessService.removeRequirement(requirementId);
        if (!removed) {
            throw new InvalidAppointmentException("Requirement not found with ID: " + requirementId);
        }
        serviceRepository.save(businessService);
    }

    public void deleteService(UUID id) {
        BusinessService businessService = getServiceEntityById(id);
        serviceRepository.delete(businessService);
    }

    private ServiceDTO convertToDTO(BusinessService businessService) {
        List<ServiceRequirementDTO> requirements = businessService.getRequirements().stream()
                .map(this::convertRequirementToDTO)
                .collect(Collectors.toList());

        return new ServiceDTO(
                businessService.getId(),
                businessService.getBusiness().getId(),
                businessService.getName(),
                businessService.getDuration(),
                requirements
        );
    }

    private ServiceRequirementDTO convertRequirementToDTO(ServiceRequirement requirement) {
        List<ServiceRequirementConstraintDTO> constraints = requirement.getConstraints().stream()
                .map(this::convertConstraintToDTO)
                .collect(Collectors.toList());
        return new ServiceRequirementDTO(
                requirement.getId(),
                requirement.getService().getId(),
                requirement.getResourceType().getId(),
                requirement.getResourceType().getName(),
                requirement.getQuantity(),
                requirement.getFulfillmentMode(),
                requirement.getRequiredCapacity(),
                requirement.getCapacityInputKey(),
                constraints
        );
    }

    private ServiceRequirementConstraintDTO convertConstraintToDTO(ServiceRequirementConstraint constraint) {
        return new ServiceRequirementConstraintDTO(
                constraint.getId(),
                constraint.getAttributeDefinition().getId(),
                constraint.getAttributeDefinition().getName(),
                constraint.getAttributeDefinition().getType(),
                constraint.getOperator(),
                constraint.getExpectedValue(),
                List.copyOf(constraint.getAttributeDefinition().getOptions())
        );
    }

    private void applyConstraints(
            ServiceRequirement requirement,
            ResourceType resourceType,
            List<ServiceRequirementConstraintInput> inputs
    ) {
        Map<UUID, ResourceAttributeDefinition> definitionsById = resourceType.getAttributeDefinitions().stream()
                .collect(Collectors.toMap(ResourceAttributeDefinition::getId, definition -> definition));
        requirement.clearConstraints();

        if (inputs == null) {
            return;
        }

        java.util.Set<UUID> seenDefinitionIds = new java.util.HashSet<>();
        for (ServiceRequirementConstraintInput input : inputs) {
            if (input.getAttributeDefinitionId() == null) {
                throw new InvalidAppointmentException("Constraint attribute is required");
            }
            ResourceAttributeDefinition definition = definitionsById.get(input.getAttributeDefinitionId());
            if (definition == null) {
                throw new InvalidAppointmentException("Constraint attribute does not belong to the selected resource type: " + input.getAttributeDefinitionId());
            }
            if (!seenDefinitionIds.add(definition.getId())) {
                throw new InvalidAppointmentException("Only one constraint per attribute is allowed");
            }
            if (input.getOperator() == null) {
                throw new InvalidAppointmentException("Constraint operator is required");
            }

            ResourceAttributeValidation.validateOperator(definition.getType(), input.getOperator());
            String normalizedValue = ResourceAttributeValidation.normalizeStoredValue(
                    definition,
                    input.getExpectedValue(),
                    true,
                    "Constraint value for \"" + definition.getName() + "\""
            );
            requirement.addConstraint(definition, input.getOperator(), normalizedValue);
        }
    }

    private void applyRequirementFulfillment(
            ServiceRequirement requirement,
            ServiceRequirementFulfillmentMode requestedMode,
            Integer requestedQuantity,
            Integer requestedRequiredCapacity,
            String requestedCapacityInputKey
    ) {
        ServiceRequirementFulfillmentMode mode =
                requestedMode != null ? requestedMode : requirement.getFulfillmentMode();

        if (mode == ServiceRequirementFulfillmentMode.QUANTITY) {
            int quantity = requestedQuantity != null ? requestedQuantity : requirement.getQuantity();
            if (quantity <= 0) {
                throw new InvalidAppointmentException("Quantity must be positive");
            }
            requirement.configureQuantityMode(quantity);
            return;
        }

        Integer requiredCapacity = requestedRequiredCapacity != null
                ? requestedRequiredCapacity
                : requirement.getRequiredCapacity();
        String capacityInputKey = requestedCapacityInputKey != null
                ? normalizeCapacityInputKey(requestedCapacityInputKey)
                : requirement.getCapacityInputKey();

        if (requiredCapacity != null && requiredCapacity <= 0) {
            throw new InvalidAppointmentException("Required capacity must be positive");
        }
        if (requiredCapacity == null && capacityInputKey == null) {
            throw new InvalidAppointmentException("Capacity requirements need either a fixed capacity or a booking input key");
        }

        requirement.configureCapacityMode(requiredCapacity, capacityInputKey);
    }

    private String normalizeCapacityInputKey(String capacityInputKey) {
        if (capacityInputKey == null) {
            return null;
        }
        String normalized = capacityInputKey.trim();
        return normalized.isEmpty() ? null : normalized;
    }

}