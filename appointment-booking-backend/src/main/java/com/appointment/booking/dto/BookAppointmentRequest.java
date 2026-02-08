package com.appointment.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookAppointmentRequest {
    @NotNull(message = "Slot ID is required")
    private Long slotId;
    private String notes;
}
