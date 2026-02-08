package com.appointment.booking.repository;

import com.appointment.booking.entity.Appointment;
import com.appointment.booking.entity.Appointment.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Appointment> findAllByOrderByCreatedAtDesc();
    boolean existsBySlotIdAndStatusIn(Long slotId, List<AppointmentStatus> statuses);
}
