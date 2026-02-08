package com.appointment.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RescheduleRequest {
    @NotNull(message = "New slot ID is required")
    private Long newSlotId;
    private String notes;
}
