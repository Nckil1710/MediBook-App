package com.appointment.booking.service;

import com.appointment.booking.entity.Appointment;

public interface NotificationService {
    void sendInstantNotification(String toEmail, String toPhone, String subject, String message);
    void scheduleReminders(Appointment appointment);
}
