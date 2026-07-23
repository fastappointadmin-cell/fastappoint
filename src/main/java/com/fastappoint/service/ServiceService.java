package com.fastappoint.service;

import com.fastappoint.core.AllocationMode;
import com.fastappoint.domain.Business;
import com.fastappoint.domain.BusinessService;
import com.fastappoint.domain.Capability;
import com.fastappoint.domain.ResourceType;
import com.fastappoint.domain.ServiceRequirement;
import com.fastappoint.dto.AddServiceRequirementRequest;
import com.fastappoint.dto.CapabilityRefDTO;
import com.fastappoint.dto.CreateServiceRequest;
import com.fastappoint.dto.ServiceDTO;
import com.fastappoint.dto.ServiceRequirementDTO;
import com.fastappoint.dto.UpdateServiceRequest;
import com.fastappoint.exception.BusinessNotFoundException;
import com.fastappoint.exception.InvalidAppointmentException;
import com.fastappoint.exception.ServiceNotFoundException;
import com.fastappoint.repository.BusinessRepository;
import com.fastappoint.repository.CapabilityRepository;
import com.fastappoint.repository.ServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final BusinessRepository businessRepository;
    private final CapabilityRepository capabilityRepository;

    public ServiceService(ServiceRepository serviceRepository, BusinessRepository businessRepository,
                         CapabilityRepository capabilityRepository) {
        this.serviceRepository = serviceRepository;
        this.businessRepository = businessRepository;
        this.capabilityRepository = capabilityRepository;
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

        if (request.getResourceTypeIds() == null || request.getResourceTypeIds().isEmpty()) {
            throw new InvalidAppointmentException("At least one resource type must be selected");
        }
        if (request.getAllocationMode() == null || request.getAllocationMode().trim().isEmpty()) {
            throw new InvalidAppointmentException("Allocation mode is required");
        }

        Business business = businessService.getBusiness();
        AllocationMode allocationMode = parseAllocationMode(request.getAllocationMode());
        List<ResourceType> selectedTypes = resolveResourceTypes(business, request.getResourceTypeIds());

        if (allocationMode == AllocationMode.MULTIPLE && selectedTypes.size() < 2) {
            throw new InvalidAppointmentException("MULTIPLE requirements must select at least two resource types");
        }

        if (allocationMode != AllocationMode.MULTIPLE && selectedTypes.size() != 1) {
            throw new InvalidAppointmentException("SINGLE and MERGE requirements support exactly one resource type");
        }

        int quantity = request.getQuantity() != null ? request.getQuantity() : 1;
        if (allocationMode != AllocationMode.MERGE && quantity <= 0) {
            throw new InvalidAppointmentException("Quantity must be positive");
        }

        String demandParameter = request.getDemandParameter() != null ? request.getDemandParameter().trim() : "";
        Set<Capability> requiredCapabilities = resolveCapabilities(request.getRequiredCapabilityIds());

        for (ResourceType resourceType : selectedTypes) {
            ServiceRequirement requirement;
            if (allocationMode == AllocationMode.MERGE) {
                if (demandParameter.isEmpty()) {
                    throw new InvalidAppointmentException("Demand parameter is required for MERGE");
                }
                requirement = businessService.requireMerged(resourceType, demandParameter);
            } else {
                requirement = businessService.require(resourceType, allocationMode, quantity);
            }

            for (Capability capability : requiredCapabilities) {
                requirement.withCapability(capability);
            }

            if (request.getOccupationDurationSeconds() != null) {
                if (request.getOccupationDurationSeconds() <= 0) {
                    throw new InvalidAppointmentException("Occupation duration must be positive");
                }
                requirement.withOccupationDuration(Duration.ofSeconds(request.getOccupationDurationSeconds()));
            }
        }

        BusinessService updated = serviceRepository.save(businessService);
        businessRepository.save(business);
        return convertToDTO(updated);
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
        Set<CapabilityRefDTO> capabilityRefs = requirement.getRequiredCapabilities().stream()
                .map(cap -> new CapabilityRefDTO(cap.getId(), cap.getName()))
                .collect(Collectors.toSet());

        return new ServiceRequirementDTO(
                requirement.getId(),
                requirement.getService().getId(),
                requirement.getResourceType().getName(),
                requirement.getMode().name(),
                requirement.getQuantity(),
                requirement.getDemandParameter(),
                capabilityRefs,
                requirement.getOccupationDuration()
        );
    }

    private AllocationMode parseAllocationMode(String rawMode) {
        try {
            return AllocationMode.valueOf(rawMode.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new InvalidAppointmentException("Unsupported allocation mode: " + rawMode, ex);
        }
    }

    private List<ResourceType> resolveResourceTypes(Business business, List<UUID> requestedIds) {
        List<ResourceType> resolved = new ArrayList<>();
        for (UUID requestedId : requestedIds) {
            ResourceType match = business.getResourceTypes().stream()
                    .filter(type -> type.getId().equals(requestedId))
                    .findFirst()
                    .orElseThrow(() -> new InvalidAppointmentException("Resource type is not available for this business: " + requestedId));
            resolved.add(match);
        }
        return resolved;
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
}