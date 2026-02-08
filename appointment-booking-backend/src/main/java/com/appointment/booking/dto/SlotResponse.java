package com.appointment.booking.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotResponse {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private Long serviceId;
    private String serviceName;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available;
}
