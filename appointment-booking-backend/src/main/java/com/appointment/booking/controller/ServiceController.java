package com.appointment.booking.controller;

import com.appointment.booking.dto.ServiceResponse;
import com.appointment.booking.entity.AppointmentService;
import com.appointment.booking.repository.AppointmentServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final AppointmentServiceRepository appointmentServiceRepository;

    @GetMapping
    public ResponseEntity<List<ServiceResponse>> listServices() {
        List<ServiceResponse> list = appointmentServiceRepository.findAll().stream()
                .map(s -> ServiceResponse.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .description(s.getDescription())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
