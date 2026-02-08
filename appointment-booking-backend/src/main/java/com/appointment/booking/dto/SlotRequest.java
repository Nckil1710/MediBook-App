package com.appointment.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotRequest {
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Date is required")
    private LocalDate slotDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;
}
