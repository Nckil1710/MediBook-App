package com.appointment.booking.repository;

import com.appointment.booking.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {
    List<Slot> findByDoctorIdAndSlotDateAndAvailableTrue(Long doctorId, LocalDate slotDate);

    List<Slot> findByDoctorIdAndAvailableTrue(Long doctorId);

    List<Slot> findBySlotDateAndAvailableTrue(LocalDate slotDate);

    List<Slot> findByAvailableTrue();

    List<Slot> findByDoctorIdAndSlotDateOrderByStartTime(Long doctorId, LocalDate slotDate);

    boolean existsByDoctorIdAndSlotDateAndStartTime(Long doctorId, LocalDate slotDate, LocalTime startTime);
}
