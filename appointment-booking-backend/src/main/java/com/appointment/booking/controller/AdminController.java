package com.appointment.booking.controller;

import com.appointment.booking.dto.*;
import com.appointment.booking.service.AppointmentService;
import com.appointment.booking.service.SlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SlotService slotService;
    private final AppointmentService appointmentService;

    @PostMapping("/slots")
    public ResponseEntity<SlotResponse> createSlot(@Valid @RequestBody SlotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(slotService.createSlot(request));
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointmentsForAdmin());
    }

    @PutMapping("/appointments/{appointmentId}/status")
    public ResponseEntity<AppointmentResponse> updateAppointmentStatus(
            @PathVariable Long appointmentId,
            @Valid @RequestBody AppointmentStatusRequest request) {
        return ResponseEntity.ok(appointmentService.updateStatusByAdmin(appointmentId, request));
    }
}
