package com.appointment.booking.service;

import com.appointment.booking.dto.*;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
