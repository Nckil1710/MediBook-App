package com.appointment.booking.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceResponse {
    private Long id;
    private String name;
    private String description;
}
