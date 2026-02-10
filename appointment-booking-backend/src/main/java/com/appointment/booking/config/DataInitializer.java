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
                    AppointmentService.builder().name("General").description("General health check-up").build(),
                    AppointmentService.builder().name("ENT").description("Ear, Nose and Throat consultation").build(),
                    AppointmentService.builder().name("Cardiology").description("Heart and cardiovascular care").build(),
                    AppointmentService.builder().name("Dermatology").description("Skin, hair and nail care").build(),
                    AppointmentService.builder().name("Orthopedics").description("Bones, joints and spine care").build()
            ));
        }
        if (doctorRepository.count() == 0) {
            List<AppointmentService> services = appointmentServiceRepository.findAll();
            if (services.size() >= 5) {
                AppointmentService general = services.get(0);
                AppointmentService ent = services.get(1);
                AppointmentService cardio = services.get(2);
                AppointmentService derm = services.get(3);
                AppointmentService ortho = services.get(4);
                doctorRepository.saveAll(List.of(
                        Doctor.builder().name("Dr. Aarav Sharma").title("MD").service(general).weekdaySlotCount(5).weekendSlotCount(3).build(),
                        Doctor.builder().name("Dr. Priya Iyer").title("MD").service(general).weekdaySlotCount(6).weekendSlotCount(2).build(),
                        Doctor.builder().name("Dr. Rohan Mehta").title("MS (ENT)").service(ent).weekdaySlotCount(5).weekendSlotCount(2).build(),
                        Doctor.builder().name("Dr. Ananya Nair").title("MS (ENT)").service(ent).weekdaySlotCount(4).weekendSlotCount(3).build(),
                        Doctor.builder().name("Dr. Vivek Reddy").title("DM (Cardio)").service(cardio).weekdaySlotCount(6).weekendSlotCount(2).build(),
                        Doctor.builder().name("Dr. Sneha Kulkarni").title("DM (Cardio)").service(cardio).weekdaySlotCount(5).weekendSlotCount(3).build(),
                        Doctor.builder().name("Dr. Karthik Menon").title("MD (Derm)").service(derm).weekdaySlotCount(5).weekendSlotCount(2).build(),
                        Doctor.builder().name("Dr. Meera Joshi").title("MD (Derm)").service(derm).weekdaySlotCount(4).weekendSlotCount(3).build(),
                        Doctor.builder().name("Dr. Arjun Singh").title("MS (Ortho)").service(ortho).weekdaySlotCount(6).weekendSlotCount(2).build(),
                        Doctor.builder().name("Dr. Kavya Rao").title("MS (Ortho)").service(ortho).weekdaySlotCount(5).weekendSlotCount(3).build()
                ));
            }
        }
    }
}
