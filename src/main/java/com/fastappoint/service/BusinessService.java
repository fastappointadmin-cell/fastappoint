package com.fastappoint.service;

import com.fastappoint.domain.Business;
import com.fastappoint.dto.BusinessDTO;
import com.fastappoint.dto.CreateBusinessRequest;
import com.fastappoint.exception.BusinessNotFoundException;
import com.fastappoint.exception.InvalidAppointmentException;
import com.fastappoint.repository.BusinessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class BusinessService {

    private final BusinessRepository businessRepository;

    public BusinessService(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }

    /**
     * Create a new business
     */
    public BusinessDTO createBusiness(CreateBusinessRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new InvalidAppointmentException("Business name cannot be empty");
        }

        Business business = new Business(request.getName().trim());
        Business saved = businessRepository.save(business);
        return convertToDTO(saved);
    }

    /**
     * Get all businesses
     */
    @Transactional(readOnly = true)
    public List<BusinessDTO> getAllBusinesses() {
        return businessRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get business by ID
     */
    @Transactional(readOnly = true)
    public BusinessDTO getBusinessById(UUID id) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found with ID: " + id));
        return convertToDTO(business);
    }

    /**
     * Get business entity by ID (internal use)
     */
    @Transactional(readOnly = true)
    public Business getBusinessEntityById(UUID id) {
        return businessRepository.findById(id)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found with ID: " + id));
    }

    /**
     * Update business
     */
    public BusinessDTO updateBusiness(UUID id, CreateBusinessRequest request) {
        Business business = getBusinessEntityById(id);
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new InvalidAppointmentException("Business name cannot be empty");
        }

        business.rename(request.getName().trim());
        Business updated = businessRepository.save(business);
        return convertToDTO(updated);
    }

    /**
     * Delete business
     */
    public void deleteBusiness(UUID id) {
        Business business = getBusinessEntityById(id);
        businessRepository.delete(business);
    }

    /**
     * Convert Business entity to DTO
     */
    private BusinessDTO convertToDTO(Business business) {
        return new BusinessDTO(business.getId(), business.getName());
    }
}