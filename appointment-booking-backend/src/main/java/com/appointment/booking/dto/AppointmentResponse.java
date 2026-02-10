package com.appointment.booking.dto;

import com.appointment.booking.entity.Appointment;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long slotId;
    private Long serviceId;
    private Long doctorId;
    private String serviceName;
    private String doctorName;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private Instant createdAt;
}
