package com.fastappoint.service;

import com.fastappoint.domain.Business;
import com.fastappoint.dto.AppointmentDTO;
import com.fastappoint.dto.CreateAppointmentRequest;
import com.fastappoint.dto.ServiceDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class ChatAgentToolService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final BusinessService businesses;
    private final BusinessServiceService services;
    private final SchedulingService schedulingService;
    private final AppointmentService appointments;
    private final BusinessPhoneNumberService phoneNumbers;

    public ChatAgentToolService(BusinessService businesses,
                                BusinessServiceService services,
                                SchedulingService schedulingService,
                                AppointmentService appointments,
                                BusinessPhoneNumberService phoneNumbers) {
        this.businesses = businesses;
        this.services = services;
        this.schedulingService = schedulingService;
        this.appointments = appointments;
        this.phoneNumbers = phoneNumbers;
    }

    public Map<String, Object> listServicesForBusiness(String businessPhoneNumber) {
        Business business = businesses.getBusinessEntityByChatPhoneNumber(businessPhoneNumber);
        List<ServiceDTO> catalog = services.getServicesByBusiness(business.getId());
        List<Map<String, Object>> servicePayload = new ArrayList<>();
        for (ServiceDTO service : catalog) {
            servicePayload.add(Map.of(
                    "name", service.getName(),
                    "durationMinutes", Math.max(1, service.getDurationSeconds() / 60)
            ));
        }
        return Map.of(
                "kind", "services",
                "services", servicePayload
        );
    }

    public Map<String, Object> checkAvailabilityForBusiness(String businessPhoneNumber, Map<String, Object> args) {
        Business business = businesses.getBusinessEntityByChatPhoneNumber(businessPhoneNumber);
        List<ServiceDTO> catalog = services.getServicesByBusiness(business.getId());
        return checkAvailabilityForBusiness(businessPhoneNumber, catalog, args);
    }

    public Map<String, Object> checkAvailabilityForBusiness(String businessPhoneNumber, List<ServiceDTO> catalog, Map<String, Object> args) {
        String serviceName = asString(args.get("serviceName"));
        String requestedDateValue = asString(args.get("requestedDate"));
        if (serviceName.isBlank() || requestedDateValue.isBlank()) {
            return Map.of("kind", "availability", "error", "serviceName and requestedDate are required");
        }
        ServiceDTO service = findService(catalog, serviceName);
        if (service == null) {
            return Map.of("kind", "availability", "error", "service not found");
        }
        LocalDate requestedDate;
        try {
            requestedDate = LocalDate.parse(requestedDateValue, DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            return Map.of("kind", "availability", "error", "requestedDate must be in yyyy-MM-dd format");
        }
        List<LocalDateTime> slots = schedulingService.availableStarts(
                service.getId(),
                requestedDate,
                Set.of(),
                Duration.ofMinutes(15),
                Map.of());
        List<String> slotStrings = new ArrayList<>();
        for (LocalDateTime slot : slots) {
            slotStrings.add(slot.format(DATE_TIME_FORMAT));
        }
        return Map.of(
                "kind", "availability",
                "serviceName", service.getName(),
                "requestedDate", requestedDate.format(DATE_FORMAT),
                "slots", slotStrings,
                "availableStarts", slots
        );
    }

    public Map<String, Object> createBookingForBusiness(String businessPhoneNumber, String customerPhone, Map<String, Object> args) {
        Business business = businesses.getBusinessEntityByChatPhoneNumber(businessPhoneNumber);
        List<ServiceDTO> catalog = services.getServicesByBusiness(business.getId());
        String serviceName = asString(args.get("serviceName"));
        String requestedDateValue = asString(args.get("requestedDate"));
        String requestedTimeValue = asString(args.get("requestedTime"));
        String customerName = asString(args.get("customerName"));
        if (serviceName.isBlank() || requestedDateValue.isBlank() || requestedTimeValue.isBlank() || customerName.isBlank()) {
            return Map.of(
                    "kind", "booking",
                    "created", false,
                    "error", "serviceName, requestedDate, requestedTime and customerName are required"
            );
        }
        if (customerPhone == null || customerPhone.isBlank()) {
            return Map.of("kind", "booking", "created", false, "error", "customer phone is required");
        }

        ServiceDTO service = findService(catalog, serviceName);
        if (service == null) {
            return Map.of("kind", "booking", "created", false, "error", "service not found");
        }

        LocalDate requestedDate;
        LocalTime requestedTime;
        try {
            requestedDate = LocalDate.parse(requestedDateValue, DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            return Map.of("kind", "booking", "created", false, "error", "requestedDate must be in yyyy-MM-dd format");
        }
        try {
            requestedTime = LocalTime.parse(requestedTimeValue, DateTimeFormatter.ofPattern("H:mm"));
        } catch (DateTimeParseException ex) {
            return Map.of("kind", "booking", "created", false, "error", "requestedTime must be in HH:mm format");
        }

        LocalDateTime requestedStart = LocalDateTime.of(requestedDate, requestedTime);
        List<LocalDateTime> availableSlots = schedulingService.availableStarts(
                service.getId(),
                requestedDate,
                Set.of(),
                Duration.ofMinutes(15),
                Map.of());
        if (!availableSlots.contains(requestedStart)) {
            List<String> slotStrings = new ArrayList<>();
            for (LocalDateTime slot : availableSlots) {
                slotStrings.add(slot.format(DATE_TIME_FORMAT));
            }
            return Map.of(
                    "kind", "booking",
                    "created", false,
                    "error", "slot unavailable",
                    "requestedStart", requestedStart.format(DATE_TIME_FORMAT),
                    "alternatives", slotStrings
            );
        }

        CreateAppointmentRequest request = new CreateAppointmentRequest();
        request.setBusinessId(business.getId());
        request.setServiceId(service.getId());
        request.setStartTime(requestedStart);
        request.setCustomerName(customerName.trim());
        request.setCustomerPhone(phoneNumbers.normalize(customerPhone));
        request.setInputs(Map.of());

        AppointmentDTO booking = appointments.createAppointment(request);
        return Map.of(
                "kind", "booking",
                "created", true,
                "booking", toBookingPayload(booking)
        );
    }

    public Map<String, Object> listMyAppointmentsForBusiness(String businessPhoneNumber, String customerPhone) {
        Business business = businesses.getBusinessEntityByChatPhoneNumber(businessPhoneNumber);
        if (customerPhone == null || customerPhone.isBlank()) {
            return Map.of("kind", "appointments", "error", "customer phone is required");
        }
        String normalizedPhone = phoneNumbers.normalize(customerPhone);
        List<AppointmentDTO> bookings = appointments.getActiveAppointmentsByBusinessAndCustomerPhone(business.getId(), normalizedPhone);
        List<Map<String, Object>> bookingPayload = new ArrayList<>();
        for (AppointmentDTO booking : bookings) {
            bookingPayload.add(toBookingPayload(booking));
        }
        return Map.of(
                "kind", "appointments",
                "customerPhone", normalizedPhone,
                "count", bookingPayload.size(),
                "appointments", bookingPayload
        );
    }

    private Map<String, Object> toBookingPayload(AppointmentDTO booking) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", booking.getId());
        payload.put("serviceName", booking.getServiceName());
        payload.put("status", booking.getStatus());
        payload.put("startTime", booking.getStartTime() == null ? null : booking.getStartTime().format(DATE_TIME_FORMAT));
        payload.put("endTime", booking.getEndTime() == null ? null : booking.getEndTime().format(DATE_TIME_FORMAT));
        payload.put("customerName", booking.getCustomerName());
        payload.put("customerPhone", booking.getCustomerPhone());
        return payload;
    }

    private ServiceDTO findService(List<ServiceDTO> catalog, String serviceName) {
        String normalizedTarget = normalize(serviceName);
        for (ServiceDTO service : catalog) {
            String normalizedService = normalize(service.getName());
            if (normalizedService.equals(normalizedTarget)
                    || normalizedService.contains(normalizedTarget)
                    || normalizedTarget.contains(normalizedService)) {
                return service;
            }
        }
        return null;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase();
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
