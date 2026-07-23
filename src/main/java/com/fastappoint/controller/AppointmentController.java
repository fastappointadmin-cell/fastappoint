package com.fastappoint.controller;

import com.fastappoint.dto.AppointmentDTO;
import com.fastappoint.dto.CreateAppointmentRequest;
import com.fastappoint.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "http://localhost:4200")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Create a new appointment
     * POST /api/appointments
     */
    @PostMapping
    public ResponseEntity<AppointmentDTO> createAppointment(@RequestBody CreateAppointmentRequest request) {
        AppointmentDTO appointment = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
    }

    /**
     * Get all appointments for a business
     * GET /api/appointments?businessId={id}
     */
    @GetMapping
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByBusiness(@RequestParam UUID businessId) {
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByBusiness(businessId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Get appointments by service
     * GET /api/appointments/service/{serviceId}
     */
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByService(@PathVariable UUID serviceId) {
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByService(serviceId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Get an appointment by ID
     * GET /api/appointments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable UUID id) {
        AppointmentDTO appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(appointment);
    }

    /**
     * Confirm an appointment
     * PATCH /api/appointments/{id}/confirm
     */
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<AppointmentDTO> confirmAppointment(@PathVariable UUID id) {
        AppointmentDTO appointment = appointmentService.confirmAppointment(id);
        return ResponseEntity.ok(appointment);
    }

    /**
     * Cancel an appointment
     * PATCH /api/appointments/{id}/cancel
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentDTO> cancelAppointment(@PathVariable UUID id) {
        AppointmentDTO appointment = appointmentService.cancelAppointment(id);
        return ResponseEntity.ok(appointment);
    }

    /**
     * Complete an appointment
     * PATCH /api/appointments/{id}/complete
     */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<AppointmentDTO> completeAppointment(@PathVariable UUID id) {
        AppointmentDTO appointment = appointmentService.completeAppointment(id);
        return ResponseEntity.ok(appointment);
    }

    /**
     * Delete an appointment
     * DELETE /api/appointments/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable UUID id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
}