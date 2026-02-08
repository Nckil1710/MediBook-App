package com.appointment.booking.service;

import com.appointment.booking.dto.*;
import com.appointment.booking.entity.Appointment;

import java.util.List;

public interface AppointmentService {
    AppointmentResponse book(Long userId, BookAppointmentRequest request);
    List<AppointmentResponse> getMyAppointments(Long userId);
    void cancel(Long userId, Long appointmentId);
    AppointmentResponse reschedule(Long userId, Long appointmentId, RescheduleRequest request);
    List<AppointmentResponse> getAllAppointmentsForAdmin();
    AppointmentResponse updateStatusByAdmin(Long appointmentId, AppointmentStatusRequest request);
}
