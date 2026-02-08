package com.appointment.booking.dto;

import com.appointment.booking.entity.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String email;
    private String name;
    private String role;
    private Long userId;
}
