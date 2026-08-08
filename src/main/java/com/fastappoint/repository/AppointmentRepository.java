package com.fastappoint.repository;

import com.fastappoint.domain.Appointment;
import com.fastappoint.domain.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    List<Appointment> findByBusinessId(UUID businessId);
    List<Appointment> findByBusinessService_Id(UUID serviceId);
    List<Appointment> findByAllocations_Resource_Id(UUID resourceId);
    List<Appointment> findByBusiness_IdAndCustomer_PhoneAndStatusInOrderByStartTimeAsc(UUID businessId, String customerPhone,
                                                                                        Collection<AppointmentStatus> statuses);
}
