package com.appointment.booking.repository;

import com.appointment.booking.entity.AppointmentService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentServiceRepository extends JpaRepository<AppointmentService, Long> {
}
