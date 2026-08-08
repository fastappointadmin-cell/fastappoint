package com.fastappoint.controller;

import com.fastappoint.dto.AppointmentDTO;
import com.fastappoint.dto.CreateAppointmentRequest;
import com.fastappoint.dto.RescheduleAppointmentRequest;
import com.fastappoint.security.AuthPrincipal;
import com.fastappoint.service.AppointmentService;
import com.fastappoint.service.BusinessServiceService;
import com.fastappoint.service.MembershipService;
import com.fastappoint.service.ResourceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final BusinessServiceService businessServiceService;
    private final ResourceService resourceService;
    private final MembershipService membershipService;

    public AppointmentController(AppointmentService appointmentService, BusinessServiceService businessServiceService,
                                  ResourceService resourceService, MembershipService membershipService) {
        this.appointmentService = appointmentService;
        this.businessServiceService = businessServiceService;
        this.resourceService = resourceService;
        this.membershipService = membershipService;
    }

    /**
     * Create a new appointment
     * POST /api/appointments
     */
    @PostMapping
    public ResponseEntity<AppointmentDTO> createAppointment(
            @RequestBody CreateAppointmentRequest request, @AuthenticationPrincipal AuthPrincipal principal) {
        membershipService.requireMembership(principal.userId(), request.getBusinessId());
        AppointmentDTO appointment = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
    }

    /**
     * Get all appointments for a business
     * GET /api/appointments?businessId={id}
     */
    @GetMapping
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByBusiness(
            @RequestParam UUID businessId, @AuthenticationPrincipal AuthPrincipal principal) {
        membershipService.requireMembership(principal.userId(), businessId);
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByBusiness(businessId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Get appointments by service
     * GET /api/appointments/service/{serviceId}
     */
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByService(
            @PathVariable UUID serviceId, @AuthenticationPrincipal AuthPrincipal principal) {
        UUID businessId = businessServiceService.getServiceEntityById(serviceId).getBusiness().getId();
        membershipService.requireMembership(principal.userId(), businessId);
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByService(serviceId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Get appointments allocated to a resource
     * GET /api/appointments/resource/{resourceId}
     */
    @GetMapping("/resource/{resourceId}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByResource(
            @PathVariable UUID resourceId, @AuthenticationPrincipal AuthPrincipal principal) {
        UUID businessId = resourceService.getResourceEntityById(resourceId).getBusiness().getId();
        membershipService.requireMembership(principal.userId(), businessId);
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByResource(resourceId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Get an appointment by ID
     * GET /api/appointments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable UUID id, @AuthenticationPrincipal AuthPrincipal principal) {
        requireMembershipForAppointment(principal, id);
        AppointmentDTO appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(appointment);
    }

    /**
     * Confirm an appointment
     * PATCH /api/appointments/{id}/confirm
     */
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<AppointmentDTO> confirmAppointment(@PathVariable UUID id, @AuthenticationPrincipal AuthPrincipal principal) {
        requireMembershipForAppointment(principal, id);
        AppointmentDTO appointment = appointmentService.confirmAppointment(id);
        return ResponseEntity.ok(appointment);
    }

    /**
     * Cancel an appointment
     * PATCH /api/appointments/{id}/cancel
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentDTO> cancelAppointment(@PathVariable UUID id, @AuthenticationPrincipal AuthPrincipal principal) {
        requireMembershipForAppointment(principal, id);
        AppointmentDTO appointment = appointmentService.cancelAppointment(id);
        return ResponseEntity.ok(appointment);
    }

    /**
     * Complete an appointment
     * PATCH /api/appointments/{id}/complete
     */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<AppointmentDTO> completeAppointment(@PathVariable UUID id, @AuthenticationPrincipal AuthPrincipal principal) {
        requireMembershipForAppointment(principal, id);
        AppointmentDTO appointment = appointmentService.completeAppointment(id);
        return ResponseEntity.ok(appointment);
    }

    /**
     * Delete an appointment
     * DELETE /api/appointments/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable UUID id, @AuthenticationPrincipal AuthPrincipal principal) {
        requireMembershipForAppointment(principal, id);
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Move an appointment to a new start/end time
     * PATCH /api/appointments/{id}/reschedule
     */
    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<AppointmentDTO> rescheduleAppointment(
            @PathVariable UUID id,
            @RequestBody RescheduleAppointmentRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        requireMembershipForAppointment(principal, id);
        AppointmentDTO appointment = appointmentService.rescheduleAppointment(id, request.getStartTime(), request.getEndTime());
        return ResponseEntity.ok(appointment);
    }

    private void requireMembershipForAppointment(AuthPrincipal principal, UUID appointmentId) {
        UUID businessId = appointmentService.getAppointmentEntityById(appointmentId).getBusiness().getId();
        membershipService.requireMembership(principal.userId(), businessId);
    }
}
