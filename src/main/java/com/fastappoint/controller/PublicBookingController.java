package com.fastappoint.controller;

import com.fastappoint.dto.AppointmentDTO;
import com.fastappoint.dto.BusinessDTO;
import com.fastappoint.dto.CreateAppointmentRequest;
import com.fastappoint.dto.PublicBookingConfirmationDTO;
import com.fastappoint.dto.PublicBookingRequest;
import com.fastappoint.dto.ServiceDTO;
import com.fastappoint.service.AppointmentService;
import com.fastappoint.service.BusinessService;
import com.fastappoint.service.BusinessServiceService;
import com.fastappoint.service.SchedulingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The unauthenticated surface a client hits from a shared booking link: enough to browse one
 * business's services and availability and place a booking, nothing that needs a login. Every
 * read here is already scoped to a single businessId/serviceId path segment, so there's no
 * cross-tenant leakage to guard against the way the membership-gated controllers do.
 */
@RestController
@RequestMapping("/api/public")
public class PublicBookingController {

    private final BusinessService businessService;
    private final BusinessServiceService serviceService;
    private final SchedulingService schedulingService;
    private final AppointmentService appointmentService;

    public PublicBookingController(BusinessService businessService, BusinessServiceService serviceService,
                                    SchedulingService schedulingService, AppointmentService appointmentService) {
        this.businessService = businessService;
        this.serviceService = serviceService;
        this.schedulingService = schedulingService;
        this.appointmentService = appointmentService;
    }

    /** GET /api/public/businesses/{id} -- name only, for the booking page header. */
    @GetMapping("/businesses/{id}")
    public ResponseEntity<BusinessDTO> getBusiness(@PathVariable UUID id) {
        return ResponseEntity.ok(businessService.getBusinessById(id));
    }

    /** GET /api/public/businesses/by-slug/{slug} -- how the booking page resolves a tenant subdomain
     * (e.g. `riverside.fastappoint.app`) to a business, before it knows the business's real id. */
    @GetMapping("/businesses/by-slug/{slug}")
    public ResponseEntity<BusinessDTO> getBusinessBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(businessService.getBusinessBySlug(slug));
    }

    /** GET /api/public/businesses/{id}/services -- what a client can pick from. */
    @GetMapping("/businesses/{id}/services")
    public ResponseEntity<List<ServiceDTO>> getServices(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceService.getServicesByBusiness(id));
    }

    /** GET /api/public/services/{id}/available-starts?date=2026-08-01 -- bookable start times. */
    @GetMapping("/services/{id}/available-starts")
    public ResponseEntity<List<LocalDateTime>> getAvailableStarts(
            @PathVariable UUID id,
            @RequestParam LocalDate date,
            @RequestParam Map<String, String> requestParams) {
        List<LocalDateTime> starts = schedulingService.availableStarts(
                id,
                date,
                Set.of(),
                Duration.ofMinutes(15),
                extractInputs(requestParams));
        return ResponseEntity.ok(starts);
    }

    /** POST /api/public/appointments -- places the booking; always a service booking, never manual. */
    @PostMapping("/appointments")
    public ResponseEntity<PublicBookingConfirmationDTO> createBooking(@RequestBody PublicBookingRequest request) {
        CreateAppointmentRequest appointmentRequest = new CreateAppointmentRequest();
        appointmentRequest.setBusinessId(request.getBusinessId());
        appointmentRequest.setServiceId(request.getServiceId());
        appointmentRequest.setStartTime(request.getStartTime());
        appointmentRequest.setCustomerName(request.getCustomerName());
        appointmentRequest.setCustomerPhone(request.getCustomerPhone());
        appointmentRequest.setInputs(request.getInputs());

        AppointmentDTO appointment = appointmentService.createAppointment(appointmentRequest);
        BusinessDTO business = businessService.getBusinessById(request.getBusinessId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toPublicBookingConfirmation(appointment, business));
    }

    private Map<String, Integer> extractInputs(Map<String, String> requestParams) {
        Map<String, Integer> inputs = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, String> entry : requestParams.entrySet()) {
            if (!entry.getKey().startsWith("input.")) {
                continue;
            }
            String inputKey = entry.getKey().substring("input.".length()).trim();
            if (inputKey.isEmpty()) {
                continue;
            }
            try {
                inputs.put(inputKey, Integer.parseInt(entry.getValue()));
            } catch (NumberFormatException ex) {
                throw new com.fastappoint.exception.InvalidAppointmentException(
                        "Booking input \"" + inputKey + "\" must be a whole number", ex);
            }
        }
        return inputs;
    }

    private PublicBookingConfirmationDTO toPublicBookingConfirmation(AppointmentDTO appointment, BusinessDTO business) {
        PublicBookingConfirmationDTO confirmation = new PublicBookingConfirmationDTO();
        confirmation.setId(appointment.getId());
        confirmation.setBusinessId(appointment.getBusinessId());
        confirmation.setServiceId(appointment.getServiceId());
        confirmation.setServiceName(appointment.getServiceName());
        confirmation.setManualLabel(appointment.getManualLabel());
        confirmation.setStartTime(appointment.getStartTime());
        confirmation.setEndTime(appointment.getEndTime());
        confirmation.setStatus(appointment.getStatus());
        confirmation.setCustomerName(appointment.getCustomerName());
        confirmation.setCustomerPhone(appointment.getCustomerPhone());
        confirmation.setAllocations(appointment.getAllocations());
        confirmation.setConfirmationSettings(business.getConfirmationSettings());
        return confirmation;
    }
}
