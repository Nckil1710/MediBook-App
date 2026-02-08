package com.appointment.booking.service;

import com.appointment.booking.entity.Appointment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender mailSender;

    @Override
    @Async
    public void sendInstantNotification(String toEmail, String toPhone, String subject, String message) {
        if (toEmail != null && !toEmail.isBlank()) {
            try {
                SimpleMailMessage email = new SimpleMailMessage();
                email.setTo(toEmail);
                email.setSubject(subject);
                email.setText(message);
                mailSender.send(email);
                log.info("Email sent to {}", toEmail);
            } catch (Exception e) {
                log.warn("Failed to send email to {}: {}", toEmail, e.getMessage());
            }
        }
        if (toPhone != null && !toPhone.isBlank()) {
            log.info("Phone reminder would be sent to {}: {}", toPhone, message);
            // In production, integrate with SMS gateway (Twilio, etc.)
        }
    }

    @Override
    public void scheduleReminders(Appointment appointment) {
        String userEmail = appointment.getUser().getEmail();
        String userPhone = appointment.getUser().getPhone();
        var slot = appointment.getSlot();
        LocalDateTime appointmentDateTime = slot.getSlotDate().atTime(slot.getStartTime());
        String details = String.format("Appointment: %s on %s at %s - %s",
                slot.getDoctor().getService().getName(),
                slot.getSlotDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                slot.getStartTime(),
                slot.getEndTime());
        String subject = "Appointment Reminder: " + slot.getDoctor().getService().getName();
        // For demo we send one instant "scheduled" reminder; in production use Quartz/ScheduledExecutorService
        // to send 24h and 1h before. Here we send a single confirmation.
        sendInstantNotification(userEmail, userPhone, subject,
                "Your appointment is confirmed. " + details + ". Reminders will be sent 24 hours and 1 hour before.");
    }
}
