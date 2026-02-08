package com.appointment.booking.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorResponse {
    private Long id;
    private String name;
    private String title;
    private Long serviceId;
    private String serviceName;
    private int weekdaySlotCount;
    private int weekendSlotCount;
}
