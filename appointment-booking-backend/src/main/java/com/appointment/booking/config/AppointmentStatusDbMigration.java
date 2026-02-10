package com.appointment.booking.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * One-time compatibility migration:
 * Older DB rows may contain status=CANCELLED while code enum no longer has CANCELLED.
 * Convert those rows to REJECTED so Hibernate can read them.
 */
@Component
@Order(0)
@RequiredArgsConstructor
public class AppointmentStatusDbMigration implements CommandLineRunner {

    private final DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE appointments SET status = 'REJECTED' WHERE status = 'CANCELLED'")
        ) {
            ps.executeUpdate();
        }
    }
}
