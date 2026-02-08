package com.appointment.booking.config;

import com.appointment.booking.entity.AppointmentService;
import com.appointment.booking.entity.Doctor;
import com.appointment.booking.entity.User;
import com.appointment.booking.repository.AppointmentServiceRepository;
import com.appointment.booking.repository.DoctorRepository;
import com.appointment.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AppointmentServiceRepository appointmentServiceRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("admin@booking.com").isEmpty()) {
            userRepository.save(User.builder()
                    .email("admin@booking.com")
                    .password(passwordEncoder.encode("admin123"))
                    .name("Admin")
                    .phone("+1234567890")
                    .role(User.Role.ADMIN)
                    .build());
        }
        if (appointmentServiceRepository.count() == 0) {
            appointmentServiceRepository.saveAll(List.of(
                    AppointmentService.builder().name("General Consultation").description("General health check-up").build(),
                    AppointmentService.builder().name("Dental").description("Dental examination and cleaning").build(),
                    AppointmentService.builder().name("Physiotherapy").description("Physical therapy session").build()
            ));
        }
        if (doctorRepository.count() == 0) {
            List<AppointmentService> services = appointmentServiceRepository.findAll();
            if (services.size() >= 3) {
                AppointmentService general = services.get(0);   // General Consultation
                AppointmentService dental = services.get(1);    // Dental
                AppointmentService physio = services.get(2);     // Physiotherapy
                // Each service has 2 doctors. Weekdays: 4-6 slots per doctor; Weekends: 2-3 per doctor (varying).
                doctorRepository.saveAll(List.of(
                        // General Consultation – 2 doctors
                        Doctor.builder().name("Dr. Sarah Smith").title("MD").service(general).weekdaySlotCount(5).weekendSlotCount(3).build(),
                        Doctor.builder().name("Dr. James Wilson").title("MD").service(general).weekdaySlotCount(6).weekendSlotCount(2).build(),
                        // Dental – 2 doctors
                        Doctor.builder().name("Dr. Teresa Chevez").title("MD").service(dental).weekdaySlotCount(5).weekendSlotCount(2).build(),
                        Doctor.builder().name("Dr. Emily Brown").title("DDS").service(dental).weekdaySlotCount(4).weekendSlotCount(3).build(),
                        // Physiotherapy – 2 doctors
                        Doctor.builder().name("Dr. Michael Lee").title("PT").service(physio).weekdaySlotCount(6).weekendSlotCount(2).build(),
                        Doctor.builder().name("Dr. Anna Martinez").title("PT").service(physio).weekdaySlotCount(4).weekendSlotCount(3).build()
                ));
            }
        }
    }
}
