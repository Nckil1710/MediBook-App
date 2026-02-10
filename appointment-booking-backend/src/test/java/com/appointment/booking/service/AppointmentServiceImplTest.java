package com.appointment.booking.service;

import com.appointment.booking.dto.BookAppointmentRequest;
import com.appointment.booking.dto.AppointmentResponse;
import com.appointment.booking.entity.Appointment;
import com.appointment.booking.entity.Appointment.AppointmentStatus;
import com.appointment.booking.entity.Doctor;
import com.appointment.booking.entity.Slot;
import com.appointment.booking.entity.User;
import com.appointment.booking.exception.BadRequestException;
import com.appointment.booking.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AppointmentServiceImplTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentServiceRepository appointmentServiceRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Test
    void book_createsPendingAppointment() {
        User user = userRepository.save(User.builder()
                .email("booker@example.com")
                .password("pw")
                .name("Booker")
                .role(User.Role.USER)
                .build());

        com.appointment.booking.entity.AppointmentService svc = appointmentServiceRepository.save(com.appointment.booking.entity.AppointmentService.builder()
                .name("General")
                .description("General")
                .build());

        Doctor doctor = doctorRepository.save(Doctor.builder()
                .name("Dr Test")
                .service(svc)
                .weekdaySlotCount(4)
                .weekendSlotCount(2)
                .build());

        Slot slot = slotRepository.save(Slot.builder()
                .doctor(doctor)
                .slotDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .available(true)
                .build());

        AppointmentResponse resp = appointmentService.book(user.getId(), BookAppointmentRequest.builder()
                .slotId(slot.getId())
                .build());

        assertThat(resp.getId()).isNotNull();
        assertThat(resp.getStatus()).isEqualTo("PENDING");
        assertThat(appointmentRepository.findById(resp.getId())).isPresent();
    }

    @Test
    void book_throwsWhenSlotAlreadyBookedPendingOrApproved() {
        User user1 = userRepository.save(User.builder()
                .email("u1@example.com")
                .password("pw")
                .name("U1")
                .role(User.Role.USER)
                .build());

        User user2 = userRepository.save(User.builder()
                .email("u2@example.com")
                .password("pw")
                .name("U2")
                .role(User.Role.USER)
                .build());

        com.appointment.booking.entity.AppointmentService svc = appointmentServiceRepository.save(com.appointment.booking.entity.AppointmentService.builder()
                .name("General")
                .description("General")
                .build());

        Doctor doctor = doctorRepository.save(Doctor.builder()
                .name("Dr Test")
                .service(svc)
                .weekdaySlotCount(4)
                .weekendSlotCount(2)
                .build());

        Slot slot = slotRepository.save(Slot.builder()
                .doctor(doctor)
                .slotDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(11, 30))
                .available(true)
                .build());

        appointmentRepository.save(Appointment.builder()
                .user(user1)
                .slot(slot)
                .status(AppointmentStatus.PENDING)
                .build());

        assertThatThrownBy(() -> appointmentService.book(user2.getId(), BookAppointmentRequest.builder().slotId(slot.getId()).build()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("slot is no longer available");
    }
}
