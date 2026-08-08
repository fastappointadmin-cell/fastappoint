package com.fastappoint.service;

import com.fastappoint.domain.Appointment;
import com.fastappoint.domain.AppointmentStatus;
import com.fastappoint.domain.Business;
import com.fastappoint.domain.BusinessService;
import com.fastappoint.domain.Customer;
import com.fastappoint.domain.Resource;
import com.fastappoint.domain.ResourceAllocation;
import com.fastappoint.dto.AppointmentDTO;
import com.fastappoint.dto.CreateAppointmentRequest;
import com.fastappoint.dto.ResourceAllocationDTO;
import com.fastappoint.exception.AppointmentNotFoundException;
import com.fastappoint.exception.BusinessNotFoundException;
import com.fastappoint.exception.InvalidAppointmentException;
import com.fastappoint.exception.ResourceNotFoundException;
import com.fastappoint.exception.ServiceNotFoundException;
import com.fastappoint.repository.AppointmentRepository;
import com.fastappoint.repository.BusinessRepository;
import com.fastappoint.repository.CustomerRepository;
import com.fastappoint.repository.ResourceAllocationRepository;
import com.fastappoint.repository.ResourceRepository;
import com.fastappoint.repository.ServiceRepository;
import com.fastappoint.solver.AllocationPlan;
import com.fastappoint.solver.PlannedAllocation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final BusinessRepository businessRepository;
    private final ServiceRepository serviceRepository;
    private final ResourceRepository resourceRepository;
    private final ResourceAllocationRepository resourceAllocationRepository;
    private final CustomerRepository customerRepository;
    private final SchedulingService schedulingService;
    private final ChatMessageService chatMessageService;
    private final BusinessPhoneNumberService phoneNumbers;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            BusinessRepository businessRepository,
            ServiceRepository serviceRepository,
            ResourceRepository resourceRepository,
            ResourceAllocationRepository resourceAllocationRepository,
            CustomerRepository customerRepository,
            SchedulingService schedulingService,
            ChatMessageService chatMessageService,
            BusinessPhoneNumberService phoneNumbers) {
        this.appointmentRepository = appointmentRepository;
        this.businessRepository = businessRepository;
        this.serviceRepository = serviceRepository;
        this.resourceRepository = resourceRepository;
        this.resourceAllocationRepository = resourceAllocationRepository;
        this.customerRepository = customerRepository;
        this.schedulingService = schedulingService;
        this.chatMessageService = chatMessageService;
        this.phoneNumbers = phoneNumbers;
    }

    public AppointmentDTO createAppointment(CreateAppointmentRequest request) {
        validateCommon(request);

        // An explicit interval (resources + end time) always wins, whether or not a serviceId also came along --
        // that's how a booking overrides the service's own duration with a custom one. Only when no explicit
        // interval is given do we fall back to the solver, which needs a serviceId to know what to schedule.
        boolean explicitInterval = request.getResourceIds() != null && !request.getResourceIds().isEmpty()
                && request.getEndTime() != null;
        if (explicitInterval) {
            return createManualAppointment(request);
        }
        if (request.getServiceId() != null) {
            return createServiceAppointment(request);
        }
        throw new InvalidAppointmentException("Either resourceIds and endTime, or a serviceId, must be provided");
    }

    private AppointmentDTO createServiceAppointment(CreateAppointmentRequest request) {
        validateServiceRequest(request);

        Business business = requireBusiness(request.getBusinessId());
        BusinessService businessService = requireService(request.getServiceId());
        validateServiceOwnership(business, businessService);

        AllocationPlan plan = solveOrThrow(
                businessService.getId(),
                request.getStartTime(),
                safePreferred(request.getPreferredResourceIds()),
                safeInputs(request.getInputs()));

        Customer customer = resolveCustomer(business, request.getCustomerName(), request.getCustomerPhone());

        Appointment appointment = buildAppointment(
                business, businessService, request.getStartTime(), customer, plan);

        Appointment saved = appointmentRepository.saveAndFlush(appointment);
        chatMessageService.scheduleMessagesForServiceBooking(saved);
        return convertToDTO(saved);
    }

    private AppointmentDTO createManualAppointment(CreateAppointmentRequest request) {
        validateManualRequest(request);

        Business business = requireBusiness(request.getBusinessId());
        LocalDateTime start = request.getStartTime();
        LocalDateTime end = request.getEndTime();

        // An explicit interval can optionally still be tagged with a service (customer picked a service but then
        // dragged a custom-length slot) -- purely for reporting/reference, it plays no role in duration or
        // resource selection here, both of which stay fully manual.
        BusinessService businessService = null;
        if (request.getServiceId() != null) {
            businessService = requireService(request.getServiceId());
            validateServiceOwnership(business, businessService);
        }

        List<Resource> resources = new ArrayList<>();
        for (UUID resourceId : request.getResourceIds()) {
            // Locking read: holds the row lock until this transaction commits, so a concurrent request creating
            // another (possibly overlapping) manual booking for the SAME resource has to wait for this one to
            // finish first -- otherwise both could read "not busy" via findBusy before either had saved, and
            // both would succeed even though their intervals overlap.
            Resource resource = resourceRepository.findWithLockingById(resourceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + resourceId));
            if (!resource.getBusiness().getId().equals(business.getId())) {
                throw new InvalidAppointmentException("Resource does not belong to the specified business");
            }
            if (businessService != null) {
                validateResourceMatchesService(businessService, resource);
            }
            if (!resourceAllocationRepository.findBusy(resourceId, start, end).isEmpty()) {
                throw new InvalidAppointmentException(
                        "Resource " + resource.getName() + " is already booked in that interval");
            }
            resources.add(resource);
        }

        Customer customer = resolveCustomer(business, request.getCustomerName(), request.getCustomerPhone());
        Appointment appointment = businessService != null
                ? new Appointment(business, businessService, start, end, customer, request.getManualLabel())
                : new Appointment(business, start, end, customer, request.getManualLabel());
        for (Resource resource : resources) {
            appointment.allocate(resource, start, end);
        }

        Appointment saved = appointmentRepository.saveAndFlush(appointment);
        return convertToDTO(saved);
    }


    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsByBusiness(UUID businessId) {
        businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found with ID: " + businessId));
        return appointmentRepository.findByBusinessId(businessId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsByService(UUID serviceId) {
        serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ServiceNotFoundException("Service not found with ID: " + serviceId));
        return appointmentRepository.findByBusinessService_Id(serviceId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsByResource(UUID resourceId) {
        resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + resourceId));
        return appointmentRepository.findByAllocations_Resource_Id(resourceId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getActiveAppointmentsByBusinessAndCustomerPhone(UUID businessId, String customerPhone) {
        businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found with ID: " + businessId));
        if (customerPhone == null || customerPhone.isBlank()) {
            throw new InvalidAppointmentException("Customer phone is required");
        }
        return appointmentRepository.findByBusiness_IdAndCustomer_PhoneAndStatusInOrderByStartTimeAsc(
                        businessId,
                        customerPhone.trim(),
                        Set.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED))
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AppointmentDTO getAppointmentById(UUID id) {
        return convertToDTO(getAppointmentEntityById(id));
    }

    @Transactional(readOnly = true)
    public Appointment getAppointmentEntityById(UUID id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with ID: " + id));
    }

    public AppointmentDTO confirmAppointment(UUID id) {
        Appointment appointment = getAppointmentEntityById(id);
        appointment.confirm();
        return convertToDTO(appointmentRepository.save(appointment));
    }

    public AppointmentDTO cancelAppointment(UUID id) {
        Appointment appointment = getAppointmentEntityById(id);
        appointment.cancel();
        return convertToDTO(appointmentRepository.save(appointment));
    }

    public AppointmentDTO completeAppointment(UUID id) {
        Appointment appointment = getAppointmentEntityById(id);
        appointment.complete();
        return convertToDTO(appointmentRepository.save(appointment));
    }

    public void deleteAppointment(UUID id) {
        appointmentRepository.delete(getAppointmentEntityById(id));
    }

    public AppointmentDTO rescheduleAppointment(UUID id, LocalDateTime newStart, LocalDateTime newEnd) {
        if (newStart == null || newEnd == null || !newEnd.isAfter(newStart)) {
            throw new InvalidAppointmentException("End time must be after start time");
        }

        Appointment appointment = getAppointmentEntityById(id);
        for (ResourceAllocation allocation : appointment.getAllocations()) {
            UUID resourceId = allocation.getResource().getId();
            boolean conflict = resourceAllocationRepository.findBusy(resourceId, newStart, newEnd).stream()
                    .anyMatch(busy -> !busy.getAppointment().getId().equals(id));
            if (conflict) {
                throw new InvalidAppointmentException(
                        "Resource " + allocation.getResource().getName() + " is already booked in that interval");
            }
        }

        appointment.reschedule(newStart, newEnd);
        return convertToDTO(appointmentRepository.save(appointment));
    }

    // --- private helpers ---

    private void validateCommon(CreateAppointmentRequest request) {
        if (request.getBusinessId() == null)
            throw new InvalidAppointmentException("Business ID is required");
        if (request.getStartTime() == null)
            throw new InvalidAppointmentException("Start time is required");
        if (request.getCustomerName() == null || request.getCustomerName().trim().isEmpty())
            throw new InvalidAppointmentException("Customer name is required");
        if (request.getCustomerPhone() == null || request.getCustomerPhone().trim().isEmpty())
            throw new InvalidAppointmentException("Customer phone is required");
    }

    private void validateServiceRequest(CreateAppointmentRequest request) {
        if (request.getServiceId() == null)
            throw new InvalidAppointmentException("Service ID is required");
    }

    private void validateManualRequest(CreateAppointmentRequest request) {
        if (request.getResourceIds() == null || request.getResourceIds().isEmpty())
            throw new InvalidAppointmentException("At least one resource is required for a manual booking");
        if (request.getEndTime() == null)
            throw new InvalidAppointmentException("End time is required for a manual booking");
        if (!request.getEndTime().isAfter(request.getStartTime()))
            throw new InvalidAppointmentException("End time must be after start time");
    }

    private Business requireBusiness(UUID businessId) {
        return businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found with ID: " + businessId));
    }

    private BusinessService requireService(UUID serviceId) {
        return serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ServiceNotFoundException("Service not found with ID: " + serviceId));
    }

    private void validateServiceOwnership(Business business, BusinessService businessService) {
        if (!businessService.getBusiness().getId().equals(business.getId())) {
            throw new InvalidAppointmentException("Service does not belong to the specified business");
        }
    }

    /** Defensive check for a manually-picked resource on a service-tagged booking: the client (dropdown) already
     * filters to eligible resources, but the server shouldn't trust that. */
    private void validateResourceMatchesService(BusinessService businessService, Resource resource) {
        boolean matches = businessService.getRequirements().stream()
                .anyMatch(requirement -> requirement.getResourceType().equals(resource.getType()));
        if (!matches) {
            throw new InvalidAppointmentException(
                    "Resource " + resource.getName() + " does not match any requirement of service " + businessService.getName());
        }
    }

    private AllocationPlan solveOrThrow(UUID serviceId, LocalDateTime start, Set<UUID> preferred, java.util.Map<String, Integer> inputs) {
        Optional<AllocationPlan> plan = schedulingService.plan(serviceId, start, preferred, inputs);
        return plan.orElseThrow(() -> new InvalidAppointmentException(
                "No feasible resource allocation for service " + serviceId + " at " + start));
    }

    private Customer resolveCustomer(Business business, String name, String phone) {
        String normalized = phoneNumbers.validateAndNormalize(phone);
        return customerRepository.findByBusiness_IdAndPhone(business.getId(), normalized)
                .orElseGet(() -> customerRepository.save(new Customer(business, name, normalized)));
    }

    private Appointment buildAppointment(Business business, BusinessService businessService,
                                         LocalDateTime start, Customer customer,
                                         AllocationPlan plan) {
        LocalDateTime endTime = start.plus(businessService.getDuration());
        Appointment appointment = new Appointment(business, businessService, start, endTime, customer);
        for (PlannedAllocation allocation : plan.allocations()) {
            appointment.allocate(allocation.resource(),
                    allocation.interval().start(), allocation.interval().end());
        }
        return appointment;
    }

    private Set<UUID> safePreferred(Set<UUID> preferred) {
        return preferred == null ? Set.of() : preferred;
    }

    private java.util.Map<String, Integer> safeInputs(java.util.Map<String, Integer> inputs) {
        return inputs == null ? java.util.Map.of() : java.util.Map.copyOf(inputs);
    }

    private AppointmentDTO convertToDTO(Appointment appointment) {
        List<ResourceAllocationDTO> allocations = appointment.getAllocations().stream()
                .map(this::convertAllocationToDTO)
                .collect(Collectors.toList());
        BusinessService businessService = appointment.getService();
        return new AppointmentDTO(
                appointment.getId(),
                appointment.getBusiness().getId(),
                businessService != null ? businessService.getId() : null,
                businessService != null ? businessService.getName() : null,
                appointment.getManualLabel(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus().name(),
                appointment.getCustomer().getName(),
                appointment.getCustomer().getPhone(),
                allocations);
    }

    private ResourceAllocationDTO convertAllocationToDTO(ResourceAllocation allocation) {
        return new ResourceAllocationDTO(
                allocation.getId(),
                allocation.getAppointment().getId(),
                allocation.getResource().getId(),
                allocation.getResource().getName(),
                allocation.getStartTime(),
                allocation.getEndTime());
    }
}