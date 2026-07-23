package com.fastappoint.service;

import com.fastappoint.domain.Appointment;
import com.fastappoint.domain.Business;
import com.fastappoint.domain.BusinessService;
import com.fastappoint.domain.Customer;
import com.fastappoint.domain.ResourceAllocation;
import com.fastappoint.dto.AppointmentDTO;
import com.fastappoint.dto.CreateAppointmentRequest;
import com.fastappoint.dto.ResourceAllocationDTO;
import com.fastappoint.exception.AppointmentNotFoundException;
import com.fastappoint.exception.BusinessNotFoundException;
import com.fastappoint.exception.InvalidAppointmentException;
import com.fastappoint.exception.ServiceNotFoundException;
import com.fastappoint.repository.AppointmentRepository;
import com.fastappoint.repository.BusinessRepository;
import com.fastappoint.repository.ServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final BusinessRepository businessRepository;
    private final ServiceRepository serviceRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            BusinessRepository businessRepository,
            ServiceRepository serviceRepository) {
        this.appointmentRepository = appointmentRepository;
        this.businessRepository = businessRepository;
        this.serviceRepository = serviceRepository;
    }

    /**
     * Create a new appointment
     * Validates all required fields and verifies entities exist
     */
    public AppointmentDTO createAppointment(CreateAppointmentRequest request) {
        // Validate request
        validateCreateAppointmentRequest(request);

        // Fetch entities
        Business business = businessRepository.findById(request.getBusinessId())
                .orElseThrow(() -> new BusinessNotFoundException(
                        "Business not found with ID: " + request.getBusinessId()));

        BusinessService businessService = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ServiceNotFoundException(
                        "Service not found with ID: " + request.getServiceId()));

        // Verify service belongs to business
        if (!businessService.getBusiness().getId().equals(business.getId())) {
            throw new InvalidAppointmentException(
                    "Service does not belong to the specified business");
        }

        // Create appointment
        LocalDateTime endTime = request.getStartTime().plus(businessService.getDuration());
        Customer customer = new Customer(request.getCustomerName(), request.getCustomerPhone());
        Appointment appointment = new Appointment(business, businessService, request.getStartTime(), endTime, customer);

        Appointment saved = appointmentRepository.save(appointment);
        return convertToDTO(saved);
    }

    /**
     * Get all appointments for a business
     */
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsByBusiness(UUID businessId) {
        // Verify business exists
        businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException(
                        "Business not found with ID: " + businessId));

        return appointmentRepository.findByBusinessId(businessId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all appointments for a service
     */
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsByService(UUID serviceId) {
        serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ServiceNotFoundException(
                        "Service not found with ID: " + serviceId));

        return appointmentRepository.findByBusinessService_Id(serviceId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get appointment by ID
     */
    @Transactional(readOnly = true)
    public AppointmentDTO getAppointmentById(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Appointment not found with ID: " + id));
        return convertToDTO(appointment);
    }

    /**
     * Get appointment entity by ID (internal use)
     */
    @Transactional(readOnly = true)
    public Appointment getAppointmentEntityById(UUID id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Appointment not found with ID: " + id));
    }

    /**
     * Confirm an appointment
     */
    public AppointmentDTO confirmAppointment(UUID id) {
        Appointment appointment = getAppointmentEntityById(id);
        appointment.confirm();
        Appointment updated = appointmentRepository.save(appointment);
        return convertToDTO(updated);
    }

    /**
     * Cancel an appointment
     */
    public AppointmentDTO cancelAppointment(UUID id) {
        Appointment appointment = getAppointmentEntityById(id);
        appointment.cancel();
        Appointment updated = appointmentRepository.save(appointment);
        return convertToDTO(updated);
    }

    /**
     * Complete an appointment
     */
    public AppointmentDTO completeAppointment(UUID id) {
        Appointment appointment = getAppointmentEntityById(id);
        appointment.complete();
        Appointment updated = appointmentRepository.save(appointment);
        return convertToDTO(updated);
    }

    /**
     * Delete an appointment
     */
    public void deleteAppointment(UUID id) {
        Appointment appointment = getAppointmentEntityById(id);
        appointmentRepository.delete(appointment);
    }

    /**
     * Validate create appointment request
     */
    private void validateCreateAppointmentRequest(CreateAppointmentRequest request) {
        if (request.getBusinessId() == null) {
            throw new InvalidAppointmentException("Business ID is required");
        }
        if (request.getServiceId() == null) {
            throw new InvalidAppointmentException("Service ID is required");
        }
        if (request.getStartTime() == null) {
            throw new InvalidAppointmentException("Start time is required");
        }
        if (request.getCustomerName() == null || request.getCustomerName().trim().isEmpty()) {
            throw new InvalidAppointmentException("Customer name is required");
        }
        if (request.getCustomerPhone() == null || request.getCustomerPhone().trim().isEmpty()) {
            throw new InvalidAppointmentException("Customer phone is required");
        }
    }

    /**
     * Convert Appointment entity to DTO
     */
    private AppointmentDTO convertToDTO(Appointment appointment) {
        List<ResourceAllocationDTO> allocations = appointment.getAllocations().stream()
                .map(this::convertAllocationToDTO)
                .collect(Collectors.toList());

        return new AppointmentDTO(
                appointment.getId(),
                appointment.getBusiness().getId(),
                appointment.getService().getId(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus().name(),
                appointment.getCustomer().getName(),
                appointment.getCustomer().getPhone(),
                allocations
        );
    }

    private ResourceAllocationDTO convertAllocationToDTO(ResourceAllocation allocation) {
        return new ResourceAllocationDTO(
                allocation.getId(),
                allocation.getAppointment().getId(),
                allocation.getResource().getId(),
                allocation.getRequirement().getId(),
                allocation.getStartTime(),
                allocation.getEndTime()
        );
    }
}