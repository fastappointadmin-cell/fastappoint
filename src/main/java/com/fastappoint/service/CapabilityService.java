package com.fastappoint.service;

import com.fastappoint.domain.Business;
import com.fastappoint.domain.Capability;
import com.fastappoint.dto.CapabilityDTO;
import com.fastappoint.exception.BusinessNotFoundException;
import com.fastappoint.exception.InvalidAppointmentException;
import com.fastappoint.repository.BusinessRepository;
import com.fastappoint.repository.CapabilityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CapabilityService {

    private final CapabilityRepository capabilityRepository;
    private final BusinessRepository businessRepository;

    public CapabilityService(CapabilityRepository capabilityRepository, BusinessRepository businessRepository) {
        this.capabilityRepository = capabilityRepository;
        this.businessRepository = businessRepository;
    }

    /**
     * Find-or-create a capability for a business by name.
     * Matching is case-insensitive; the first spelling seen is kept.
     */
    public Capability capabilityNamed(UUID businessId, String name) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found with ID: " + businessId));

        String normalized = name.trim();
        return capabilityRepository.findByBusinessIdAndNameIgnoreCase(businessId, normalized)
                .orElseGet(() -> {
                    Capability created = new Capability(business, normalized);
                    return capabilityRepository.save(created);
                });
    }

    /**
     * Create a new capability with optional description
     */
    public CapabilityDTO createCapability(UUID businessId, String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidAppointmentException("Capability name cannot be empty");
        }

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found with ID: " + businessId));

        String normalized = name.trim();

        // Check if it already exists
        if (capabilityRepository.findByBusinessIdAndNameIgnoreCase(businessId, normalized).isPresent()) {
            throw new InvalidAppointmentException("Capability '" + name + "' already exists for this business");
        }

        Capability capability = new Capability(business, normalized);
        if (description != null && !description.trim().isEmpty()) {
            capability.setDescription(description.trim());
        }

        Capability saved = capabilityRepository.save(capability);
        return convertToDTO(saved);
    }

    /**
     * Get all capabilities for a business
     */
    @Transactional(readOnly = true)
    public List<CapabilityDTO> getCapabilitiesByBusiness(UUID businessId) {
        businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found with ID: " + businessId));

        return capabilityRepository.findByBusinessId(businessId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get capability by ID
     */
    @Transactional(readOnly = true)
    public CapabilityDTO getCapabilityById(UUID id) {
        Capability capability = capabilityRepository.findById(id)
                .orElseThrow(() -> new InvalidAppointmentException("Capability not found with ID: " + id));
        return convertToDTO(capability);
    }

    /**
     * Update capability description
     */
    public CapabilityDTO updateCapability(UUID id, String description) {
        Capability capability = capabilityRepository.findById(id)
                .orElseThrow(() -> new InvalidAppointmentException("Capability not found with ID: " + id));

        if (description != null && !description.trim().isEmpty()) {
            capability.setDescription(description.trim());
        }

        Capability updated = capabilityRepository.save(capability);
        return convertToDTO(updated);
    }

    /**
     * Delete a capability
     */
    public void deleteCapability(UUID id) {
        Capability capability = capabilityRepository.findById(id)
                .orElseThrow(() -> new InvalidAppointmentException("Capability not found with ID: " + id));
        capabilityRepository.delete(capability);
    }

    private CapabilityDTO convertToDTO(Capability capability) {
        return new CapabilityDTO(
                capability.getId(),
                capability.getBusiness().getId(),
                capability.getName(),
                capability.getDescription()
        );
    }
}

