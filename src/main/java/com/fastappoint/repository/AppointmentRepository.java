package com.fastappoint.repository;

import com.fastappoint.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    List<Appointment> findByBusinessId(UUID businessId);
    List<Appointment> findByBusinessService_Id(UUID serviceId);
}
