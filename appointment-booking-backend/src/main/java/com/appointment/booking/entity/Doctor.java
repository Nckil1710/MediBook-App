package com.appointment.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private AppointmentService service;

    /** Weekday slot count (4-6). Used when generating slots. */
    @Column(name = "weekday_slot_count")
    private int weekdaySlotCount;

    /** Weekend slot count (2-3). Used when generating slots. */
    @Column(name = "weekend_slot_count")
    private int weekendSlotCount;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Slot> slots = new ArrayList<>();
}
