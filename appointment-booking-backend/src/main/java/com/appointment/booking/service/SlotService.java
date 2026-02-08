package com.appointment.booking.service;

import com.appointment.booking.dto.SlotRequest;
import com.appointment.booking.dto.SlotResponse;

import java.time.LocalDate;
import java.util.List;

public interface SlotService {
    List<SlotResponse> getAvailableSlots(Long doctorId, LocalDate date);
    /** Returns all slots for a doctor on a date; each slot has available=false if already booked. */
    List<SlotResponse> getSlotsForDate(Long doctorId, LocalDate date);
    SlotResponse createSlot(SlotRequest request);
    void generateSlotsForDoctors(int daysAhead);
}
