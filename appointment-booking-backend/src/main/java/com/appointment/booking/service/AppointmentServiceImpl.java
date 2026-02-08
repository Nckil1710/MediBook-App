package com.appointment.booking.service;

import com.appointment.booking.dto.*;
import com.appointment.booking.entity.Appointment;
import com.appointment.booking.entity.Appointment.AppointmentStatus;
import com.appointment.booking.entity.Slot;
import com.appointment.booking.entity.User;
import com.appointment.booking.exception.BadRequestException;
import com.appointment.booking.exception.ResourceNotFoundException;
import com.appointment.booking.repository.AppointmentRepository;
import com.appointment.booking.repository.SlotRepository;
import com.appointment.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final SlotRepository slotRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public AppointmentResponse book(Long userId, BookAppointmentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Slot slot = slotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + request.getSlotId()));
        boolean alreadyBooked = appointmentRepository.existsBySlotIdAndStatusIn(
                slot.getId(), Arrays.asList(AppointmentStatus.PENDING, AppointmentStatus.APPROVED));
        if (alreadyBooked) {
            throw new BadRequestException("This slot is no longer available");
        }
        Appointment appointment = Appointment.builder()
                .user(user)
                .slot(slot)
                .notes(request.getNotes())
                .status(AppointmentStatus.PENDING)
                .build();
        appointment = appointmentRepository.save(appointment);
        notificationService.scheduleReminders(appointment);
        return toResponse(appointment);
    }

    @Override
    public List<AppointmentResponse> getMyAppointments(Long userId) {
        return appointmentRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancel(Long userId, Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));
        if (!appointment.getUser().getId().equals(userId)) {
            throw new BadRequestException("Not authorized to cancel this appointment");
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Appointment is already cancelled");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse reschedule(Long userId, Long appointmentId, RescheduleRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));
        if (!appointment.getUser().getId().equals(userId)) {
            throw new BadRequestException("Not authorized to reschedule this appointment");
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Cannot reschedule a cancelled appointment");
        }
        Slot newSlot = slotRepository.findById(request.getNewSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + request.getNewSlotId()));
        boolean alreadyBooked = appointmentRepository.existsBySlotIdAndStatusIn(
                newSlot.getId(), Arrays.asList(AppointmentStatus.PENDING, AppointmentStatus.APPROVED));
        if (alreadyBooked) {
            throw new BadRequestException("The selected slot is no longer available");
        }
        appointment.setSlot(newSlot);
        if (request.getNotes() != null) appointment.setNotes(request.getNotes());
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment = appointmentRepository.save(appointment);
        notificationService.scheduleReminders(appointment);
        return toResponse(appointment);
    }

    @Override
    public List<AppointmentResponse> getAllAppointmentsForAdmin() {
        return appointmentRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AppointmentResponse updateStatusByAdmin(Long appointmentId, AppointmentStatusRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));
        String status = request.getStatus().toUpperCase();
        if ("APPROVED".equals(status)) {
            appointment.setStatus(AppointmentStatus.APPROVED);
        } else if ("REJECTED".equals(status)) {
            appointment.setStatus(AppointmentStatus.REJECTED);
        } else {
            throw new BadRequestException("Status must be APPROVED or REJECTED");
        }
        appointment = appointmentRepository.save(appointment);
        return toResponse(appointment);
    }

    private AppointmentResponse toResponse(Appointment a) {
        Slot slot = a.getSlot();
        User user = a.getUser();
        return AppointmentResponse.builder()
                .id(a.getId())
                .userId(user.getId())
                .userName(user.getName())
                .userEmail(user.getEmail())
                .slotId(slot.getId())
                .serviceId(slot.getDoctor().getService().getId())
                .doctorId(slot.getDoctor().getId())
                .serviceName(slot.getDoctor().getService().getName())
                .doctorName(slot.getDoctor().getName())
                .slotDate(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .notes(a.getNotes())
                .status(a.getStatus().name())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
