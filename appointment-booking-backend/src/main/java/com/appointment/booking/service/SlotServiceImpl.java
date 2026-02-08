package com.appointment.booking.service;

import com.appointment.booking.dto.SlotRequest;
import com.appointment.booking.dto.SlotResponse;
import com.appointment.booking.entity.Doctor;
import com.appointment.booking.entity.Slot;
import com.appointment.booking.exception.ResourceNotFoundException;
import com.appointment.booking.repository.AppointmentRepository;
import com.appointment.booking.repository.DoctorRepository;
import com.appointment.booking.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.appointment.booking.entity.Appointment.AppointmentStatus;

@Service
@RequiredArgsConstructor
public class SlotServiceImpl implements SlotService {

    private static final LocalTime[] WEEKDAY_TIMES = {
            LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0),
            LocalTime.of(12, 0), LocalTime.of(14, 0), LocalTime.of(15, 0), LocalTime.of(16, 0)
    };
    private static final LocalTime[] WEEKEND_TIMES = {
            LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(14, 0)
    };

    private final SlotRepository slotRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    @Override
    public List<SlotResponse> getAvailableSlots(Long doctorId, LocalDate date) {

        List<Slot> slots;

        if (doctorId != null && date != null) {
            slots = slotRepository
                    .findByDoctorIdAndSlotDateAndAvailableTrue(doctorId, date);

        } else if (doctorId != null) {
            slots = slotRepository
                    .findByDoctorIdAndAvailableTrue(doctorId);

        } else if (date != null) {
            slots = slotRepository
                    .findBySlotDateAndAvailableTrue(date);

        } else {
            slots = slotRepository
                    .findByAvailableTrue();
        }

        return slots.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SlotResponse> getSlotsForDate(Long doctorId, LocalDate date) {
        List<Slot> slots = slotRepository.findByDoctorIdAndSlotDateOrderByStartTime(doctorId, date);
        return slots.stream()
                .map(slot -> {
                    boolean booked = appointmentRepository.existsBySlotIdAndStatusIn(
                            slot.getId(), Arrays.asList(AppointmentStatus.PENDING, AppointmentStatus.APPROVED));
                    SlotResponse r = toResponse(slot);
                    r.setAvailable(!booked);
                    return r;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SlotResponse createSlot(SlotRequest request) {
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + request.getDoctorId()));
        Slot slot = Slot.builder()
                .doctor(doctor)
                .slotDate(request.getSlotDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .available(true)
                .build();
        slot = slotRepository.save(slot);
        return toResponse(slot);
    }

    /**
     * For each calendar day, generate slots for every doctor.
     * On the same day: doctor 1 gets weekdaySlotCount (4-6) or weekendSlotCount (2-3), doctor 2 gets their own count, etc.
     */
    @Override
    @Transactional
    public void generateSlotsForDoctors(int daysAhead) {
        LocalDate today = LocalDate.now();
        List<Doctor> doctors = doctorRepository.findAll();
        for (int i = 0; i < daysAhead; i++) {
            LocalDate date = today.plusDays(i);
            for (Doctor doctor : doctors) {
                List<LocalTime> times = getTimesForDay(date, doctor);
                for (LocalTime start : times) {
                    if (slotRepository.existsByDoctorIdAndSlotDateAndStartTime(doctor.getId(), date, start)) {
                        continue;
                    }
                    LocalTime end = start.plusMinutes(30);
                    Slot slot = Slot.builder()
                            .doctor(doctor)
                            .slotDate(date)
                            .startTime(start)
                            .endTime(end)
                            .available(true)
                            .build();
                    slotRepository.save(slot);
                }
            }
        }
    }

    private List<LocalTime> getTimesForDay(LocalDate date, Doctor doctor) {
        DayOfWeek day = date.getDayOfWeek();
        boolean weekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
        int count = weekend ? doctor.getWeekendSlotCount() : doctor.getWeekdaySlotCount();
        LocalTime[] pool = weekend ? WEEKEND_TIMES : WEEKDAY_TIMES;
        int take = Math.min(count, pool.length);
        List<LocalTime> result = new ArrayList<>(take);
        for (int i = 0; i < take; i++) {
            result.add(pool[i]);
        }
        return result;
    }

    private SlotResponse toResponse(Slot slot) {
        Doctor doc = slot.getDoctor();
        return SlotResponse.builder()
                .id(slot.getId())
                .doctorId(doc.getId())
                .doctorName(doc.getName())
                .serviceId(doc.getService().getId())
                .serviceName(doc.getService().getName())
                .slotDate(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .available(slot.isAvailable())
                .build();
    }
}
