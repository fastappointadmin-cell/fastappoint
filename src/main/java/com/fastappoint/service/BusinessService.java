package com.fastappoint.service;

import com.fastappoint.domain.Business;
import com.fastappoint.dto.BusinessConfirmationSettingsDTO;
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
    private final BusinessSlugService slugService;
    private final BusinessPhoneNumberService businessPhoneNumberService;

    public BusinessService(BusinessRepository businessRepository, BusinessSlugService slugService,
                           BusinessPhoneNumberService businessPhoneNumberService) {
        this.businessRepository = businessRepository;
        this.slugService = slugService;
        this.businessPhoneNumberService = businessPhoneNumberService;
    }

    /**
     * Create a new business
     */
    public BusinessDTO createBusiness(CreateBusinessRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new InvalidAppointmentException("Business name cannot be empty");
        }

        String name = request.getName().trim();
        Business business = new Business(
                name,
                slugService.generateUniqueSlug(name),
                resolveBusinessPhoneNumber(request.getChatPhoneNumber(), null),
                normalizeDescription(request.getDescription()));
        applyConfirmationSettings(business, request.getConfirmationSettings(), true);
        applyReminderSettings(business, request.getReminderSettings(), true);
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

    /** Used to scope the business list to a caller's own memberships instead of every business. */
    @Transactional(readOnly = true)
    public List<BusinessDTO> getBusinessesByIds(List<UUID> ids) {
        return businessRepository.findAllById(ids).stream()
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
        business.updateChatIdentity(
                resolveBusinessPhoneNumber(request.getChatPhoneNumber(), business.getId()),
                normalizeDescription(request.getDescription()));
        applyConfirmationSettings(business, request.getConfirmationSettings(), false);
        applyReminderSettings(business, request.getReminderSettings(), false);
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

    /** Resolve a business by its booking-subdomain slug -- backs the public tenant-resolution endpoint. */
    @Transactional(readOnly = true)
    public BusinessDTO getBusinessBySlug(String slug) {
        Business business = businessRepository.findBySlug(slug)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found with slug: " + slug));
        return convertToDTO(business);
    }

    @Transactional(readOnly = true)
    public Business getBusinessEntityByChatPhoneNumber(String chatPhoneNumber) {
        String normalized = businessPhoneNumberService.normalize(chatPhoneNumber);
        return businessRepository.findByChatPhoneNumber(normalized)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found for phone number: " + normalized));
    }

    /**
     * Convert Business entity to DTO
     */
    private BusinessDTO convertToDTO(Business business) {
        BusinessDTO dto = new BusinessDTO(business.getId(), business.getName(), business.getSlug());
        dto.setChatPhoneNumber(business.getChatPhoneNumber());
        dto.setDescription(business.getDescription());
        dto.setConfirmationSettings(toConfirmationSettingsDTO(business));
        dto.setReminderSettings(toReminderSettingsDTO(business));
        return dto;
    }

    private void applyConfirmationSettings(Business business, BusinessConfirmationSettingsDTO requestedSettings,
                                           boolean useDefaultsWhenMissing) {
        if (requestedSettings == null && !useDefaultsWhenMissing) {
            return;
        }
        BusinessConfirmationSettingsDTO normalized = normalizeConfirmationSettings(requestedSettings);
        business.updateConfirmationSettings(
                normalized.getMessage(),
                normalized.getLocationInfo(),
                normalized.getGoogleMapsLink(),
                Boolean.TRUE.equals(normalized.getEnabled()),
                Boolean.TRUE.equals(normalized.getIncludeLocationInfo()),
                Boolean.TRUE.equals(normalized.getIncludeTime()),
                Boolean.TRUE.equals(normalized.getIncludeBookingSlot()));
    }

    private void applyReminderSettings(Business business, BusinessConfirmationSettingsDTO requestedSettings,
                                       boolean useDefaultsWhenMissing) {
        if (requestedSettings == null && !useDefaultsWhenMissing) {
            return;
        }
        BusinessConfirmationSettingsDTO normalized = normalizeReminderSettings(requestedSettings);
        business.updateReminderSettings(
                normalized.getMessage(),
                normalized.getLocationInfo(),
                normalized.getGoogleMapsLink(),
                Boolean.TRUE.equals(normalized.getEnabled()),
                Boolean.TRUE.equals(normalized.getIncludeLocationInfo()),
                Boolean.TRUE.equals(normalized.getIncludeTime()),
                Boolean.TRUE.equals(normalized.getIncludeBookingSlot()),
                normalized.getReminderLeadTimeMinutes());
    }

    private BusinessConfirmationSettingsDTO normalizeConfirmationSettings(BusinessConfirmationSettingsDTO settings) {
        if (settings == null) {
            return defaultConfirmationSettings();
        }
        return new BusinessConfirmationSettingsDTO(
                normalizeText(settings.getMessage()),
                normalizeText(settings.getLocationInfo()),
                normalizeText(settings.getGoogleMapsLink()),
                settings.getEnabled() == null ? Boolean.TRUE : settings.getEnabled(),
                Boolean.TRUE.equals(settings.getIncludeLocationInfo()),
                settings.getIncludeTime() == null ? Boolean.TRUE : settings.getIncludeTime(),
                settings.getIncludeBookingSlot() == null ? Boolean.TRUE : settings.getIncludeBookingSlot(),
                normalizeReminderLeadTimeMinutes(settings.getReminderLeadTimeMinutes()));
    }

    private BusinessConfirmationSettingsDTO normalizeReminderSettings(BusinessConfirmationSettingsDTO settings) {
        if (settings == null) {
            return defaultReminderSettings();
        }
        return new BusinessConfirmationSettingsDTO(
                normalizeText(settings.getMessage()),
                normalizeText(settings.getLocationInfo()),
                normalizeText(settings.getGoogleMapsLink()),
                settings.getEnabled() == null ? Boolean.TRUE : settings.getEnabled(),
                Boolean.TRUE.equals(settings.getIncludeLocationInfo()),
                settings.getIncludeTime() == null ? Boolean.TRUE : settings.getIncludeTime(),
                settings.getIncludeBookingSlot() == null ? Boolean.TRUE : settings.getIncludeBookingSlot(),
                normalizeReminderLeadTimeMinutes(settings.getReminderLeadTimeMinutes()));
    }

    private BusinessConfirmationSettingsDTO toConfirmationSettingsDTO(Business business) {
        return new BusinessConfirmationSettingsDTO(
                business.getConfirmationMessage(),
                business.getConfirmationLocationInfo(),
                business.getConfirmationGoogleMapsLink(),
                business.isConfirmationEnabled(),
                business.isConfirmationIncludeLocationInfo(),
                business.isConfirmationIncludeTime(),
                business.isConfirmationIncludeBookingSlot(),
                business.getReminderLeadTimeMinutes());
    }

    private BusinessConfirmationSettingsDTO toReminderSettingsDTO(Business business) {
        return new BusinessConfirmationSettingsDTO(
                business.getReminderMessage(),
                business.getReminderLocationInfo(),
                business.getReminderGoogleMapsLink(),
                business.isReminderEnabled(),
                business.isReminderIncludeLocationInfo(),
                business.isReminderIncludeTime(),
                business.isReminderIncludeBookingSlot(),
                business.getReminderLeadTimeMinutes());
    }

    private BusinessConfirmationSettingsDTO defaultConfirmationSettings() {
        return new BusinessConfirmationSettingsDTO("", "", "", true, false, true, true, 1440);
    }

    private BusinessConfirmationSettingsDTO defaultReminderSettings() {
        return new BusinessConfirmationSettingsDTO("", "", "", true, false, true, true, 1440);
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    private int normalizeReminderLeadTimeMinutes(Integer value) {
        if (value == null || value < 5) {
            return 1440;
        }
        return value;
    }

    private String normalizeDescription(String description) {
        return description == null ? "" : description.trim();
    }

    private String resolveBusinessPhoneNumber(String requestedPhoneNumber, UUID currentBusinessId) {
        String normalized = businessPhoneNumberService.normalizeOrGenerate(requestedPhoneNumber);
        businessRepository.findByChatPhoneNumber(normalized).ifPresent(existing -> {
            if (currentBusinessId == null || !existing.getId().equals(currentBusinessId)) {
                throw new InvalidAppointmentException("Business phone number is already assigned: " + normalized);
            }
        });
        return normalized;
    }
}