package com.appointment.booking.controller;

import com.appointment.booking.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> send(@RequestBody Map<String, String> body) {
        String toEmail = body.get("toEmail");
        String toPhone = body.get("toPhone");
        String subject = body.getOrDefault("subject", "Notification");
        String message = body.getOrDefault("message", "");
        notificationService.sendInstantNotification(toEmail, toPhone, subject, message);
        return ResponseEntity.ok(Map.of("status", "sent"));
    }
}
