package com.appointment.booking.controller;

import com.appointment.booking.dto.DoctorResponse;
import com.appointment.booking.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorRepository doctorRepository;

    @GetMapping
    public ResponseEntity<List<DoctorResponse>> listAllDoctors() {
        List<DoctorResponse> list = doctorRepository.findAllByOrderByServiceIdAscNameAsc().stream()
                .map(d -> DoctorResponse.builder()
                        .id(d.getId())
                        .name(d.getName())
                        .title(d.getTitle())
                        .serviceId(d.getService().getId())
                        .serviceName(d.getService().getName())
                        .weekdaySlotCount(d.getWeekdaySlotCount())
                        .weekendSlotCount(d.getWeekendSlotCount())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/by-service/{serviceId}")
    public ResponseEntity<List<DoctorResponse>> listByService(@PathVariable Long serviceId) {
        List<DoctorResponse> list = doctorRepository.findByServiceIdOrderByName(serviceId).stream()
                .map(d -> DoctorResponse.builder()
                        .id(d.getId())
                        .name(d.getName())
                        .title(d.getTitle())
                        .serviceId(d.getService().getId())
                        .serviceName(d.getService().getName())
                        .weekdaySlotCount(d.getWeekdaySlotCount())
                        .weekendSlotCount(d.getWeekendSlotCount())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
