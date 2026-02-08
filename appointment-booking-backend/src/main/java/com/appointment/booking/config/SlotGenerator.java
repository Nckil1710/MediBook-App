package com.appointment.booking.config;

import com.appointment.booking.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Generates fixed calendar slots for all doctors after data is initialized.
 * Weekdays: 4-6 slots per doctor; Weekends: 2-3 slots per doctor (configurable per doctor).
 */
@Component
@Order(2)
@RequiredArgsConstructor
public class SlotGenerator implements CommandLineRunner {

    private final SlotService slotService;

    @Value("${app.slot-generation.days-ahead:60}")
    private int daysAhead;

    @Override
    public void run(String... args) {
        slotService.generateSlotsForDoctors(daysAhead);
    }
}
