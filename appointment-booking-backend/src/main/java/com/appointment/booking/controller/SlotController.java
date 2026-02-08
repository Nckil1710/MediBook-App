package com.appointment.booking.controller;

import com.appointment.booking.dto.SlotResponse;
import com.appointment.booking.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;

    @GetMapping("/available")
    public ResponseEntity<List<SlotResponse>> getAvailableSlots(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(slotService.getAvailableSlots(doctorId, date));
    }

    /** All slots for a doctor on a date; available=false means slot is already booked. */
    @GetMapping("/by-date")
    public ResponseEntity<List<SlotResponse>> getSlotsByDate(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(slotService.getSlotsForDate(doctorId, date));
    }
}
