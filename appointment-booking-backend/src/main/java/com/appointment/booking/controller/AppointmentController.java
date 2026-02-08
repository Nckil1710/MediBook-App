package com.appointment.booking.controller;

import com.appointment.booking.dto.*;
import com.appointment.booking.security.UserPrincipal;
import com.appointment.booking.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/book")
    public ResponseEntity<AppointmentResponse> book(@AuthenticationPrincipal UserPrincipal principal,
                                                    @Valid @RequestBody BookAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.book(principal.getUserId(), request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<AppointmentResponse>> myAppointments(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(appointmentService.getMyAppointments(principal.getUserId()));
    }

    @PutMapping("/cancel/{appointmentId}")
    public ResponseEntity<Void> cancel(@AuthenticationPrincipal UserPrincipal principal,
                                       @PathVariable Long appointmentId) {
        appointmentService.cancel(principal.getUserId(), appointmentId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/reschedule/{appointmentId}")
    public ResponseEntity<AppointmentResponse> reschedule(@AuthenticationPrincipal UserPrincipal principal,
                                                          @PathVariable Long appointmentId,
                                                          @Valid @RequestBody RescheduleRequest request) {
        return ResponseEntity.ok(appointmentService.reschedule(principal.getUserId(), appointmentId, request));
    }
}
