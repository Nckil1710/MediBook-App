package com.appointment.booking.controller;

import com.appointment.booking.dto.AppointmentResponse;
import com.appointment.booking.security.UserPrincipal;
import com.appointment.booking.service.AppointmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    @Test
    void book_returnsCreated() throws Exception {
        long userId = 10L;
        UserPrincipal principal = new UserPrincipal(userId, "u@example.com", "USER");
        var auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        AppointmentResponse resp = AppointmentResponse.builder()
                .id(1L)
                .userId(userId)
                .userName("User")
                .userEmail("u@example.com")
                .slotId(5L)
                .serviceId(2L)
                .doctorId(3L)
                .serviceName("General")
                .doctorName("Dr Test")
                .slotDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .status("PENDING")
                .createdAt(Instant.now())
                .build();

        when(appointmentService.book(eq(userId), any())).thenReturn(resp);

        mockMvc.perform(
                        post("/api/appointments/book")
                                .with(authentication(auth))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new java.util.HashMap<String, Object>() {{
                                    put("slotId", 5);
                                }}))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void myAppointments_requiresAuth() throws Exception {
        mockMvc.perform(get("/api/appointments/my"))
                .andExpect(status().isForbidden());
    }
}
