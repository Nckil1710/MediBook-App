package com.appointment.booking.repository;

import com.appointment.booking.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findByServiceIdOrderByName(Long serviceId);
    List<Doctor> findAllByOrderByServiceIdAscNameAsc();
}
