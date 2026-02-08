package com.appointment.booking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentStatusRequest {
    @NotBlank(message = "Status is required (APPROVED or REJECTED)")
    private String status;
}
