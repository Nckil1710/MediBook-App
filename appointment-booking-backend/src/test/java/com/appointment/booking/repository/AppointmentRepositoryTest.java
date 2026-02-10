package com.appointment.booking.repository;

import com.appointment.booking.entity.*;
import com.appointment.booking.entity.Appointment.AppointmentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AppointmentRepositoryTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentServiceRepository appointmentServiceRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private SlotRepository slotRepository;

    @Test
    void existsBySlotIdAndStatusIn_trueForPending() {
        AppointmentService service = appointmentServiceRepository.save(AppointmentService.builder()
                .name("General")
                .description("General")
                .build());

        Doctor doctor = doctorRepository.save(Doctor.builder()
                .name("Dr A")
                .service(service)
                .weekdaySlotCount(4)
                .weekendSlotCount(2)
                .build());

        Slot slot = slotRepository.save(Slot.builder()
                .doctor(doctor)
                .slotDate(LocalDate.now())
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .available(true)
                .build());

        User user = userRepository.save(User.builder()
                .email("u1@example.com")
                .password("pw")
                .name("User One")
                .role(User.Role.USER)
                .build());

        appointmentRepository.save(Appointment.builder()
                .user(user)
                .slot(slot)
                .status(AppointmentStatus.PENDING)
                .build());

        boolean exists = appointmentRepository.existsBySlotIdAndStatusIn(slot.getId(), List.of(AppointmentStatus.PENDING, AppointmentStatus.APPROVED));
        assertThat(exists).isTrue();
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_returnsAppointments() {
        AppointmentService service = appointmentServiceRepository.save(AppointmentService.builder()
                .name("General")
                .description("General")
                .build());

        Doctor doctor = doctorRepository.save(Doctor.builder()
                .name("Dr A")
                .service(service)
                .weekdaySlotCount(4)
                .weekendSlotCount(2)
                .build());

        Slot slot1 = slotRepository.save(Slot.builder()
                .doctor(doctor)
                .slotDate(LocalDate.now())
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .available(true)
                .build());

        Slot slot2 = slotRepository.save(Slot.builder()
                .doctor(doctor)
                .slotDate(LocalDate.now())
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .available(true)
                .build());

        User user = userRepository.save(User.builder()
                .email("u2@example.com")
                .password("pw")
                .name("User Two")
                .role(User.Role.USER)
                .build());

        appointmentRepository.save(Appointment.builder().user(user).slot(slot1).status(AppointmentStatus.PENDING).build());
        appointmentRepository.save(Appointment.builder().user(user).slot(slot2).status(AppointmentStatus.PENDING).build());

        List<Appointment> list = appointmentRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        assertThat(list).hasSize(2);
        assertThat(list.get(0).getId()).isNotNull();
    }
}
