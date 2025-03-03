package com.spribe.bookingsystem.controller;

import com.spribe.bookingsystem.config.IntegrationTest;
import com.spribe.bookingsystem.entity.UnitEntity;
import com.spribe.bookingsystem.entity.UserEntity;
import com.spribe.bookingsystem.payload.response.data.EventData;
import com.spribe.bookingsystem.repository.EventRepository;
import com.spribe.bookingsystem.repository.PaymentRepository;
import com.spribe.bookingsystem.repository.UnitRepository;
import com.spribe.bookingsystem.repository.UserRepository;
import com.spribe.bookingsystem.service.BookingService;
import com.spribe.bookingsystem.service.PaymentService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.time.LocalDate.now;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
public class BookingControllerIT {

    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private PaymentService paymentService;
    @Autowired private BookingService bookingService;
    @Autowired private UserRepository userRepository;
    @Autowired private UnitRepository unitRepository;

    private MockMvc mockMvc;
    private UserEntity testUser;
    private UnitEntity testUnit;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        paymentRepository.deleteAll();
        unitRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = userRepository.save(UserEntity.builder()
            .name("John Doe")
            .email("john@example.com")
            .createdAt(LocalDateTime.now())
            .build());

        // Create test unit
        testUnit = unitRepository.save(UnitEntity.builder()
            .user(testUser)
            .numRooms(2)
            .type(com.spribe.bookingsystem.entity.AccommodationType.FLAT)
            .floor(3)
            .description("Modern city flat")
            .markup(BigDecimal.valueOf(0.15))
            .baseCost(new BigDecimal("100.00"))
            .totalCost(new BigDecimal("115.00")) // 15% markup
            .createdAt(LocalDateTime.now())
            .build());
    }

    @Test
    void shouldBookUnitSuccessfully() throws Exception {
        // Given
        LocalDate startDate = now().plusDays(1);
        LocalDate endDate = startDate.plusDays(3);

        // When
        ResultActions perform = mockMvc.perform(post("/bookings")
            .param("userId", String.valueOf(testUser.getId()))
            .param("unitId", String.valueOf(testUnit.getId()))
            .param("startDate", startDate.toString())
            .param("endDate", endDate.toString())
            .contentType(MediaType.APPLICATION_JSON));

        // Then
        perform
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId", equalTo(testUser.getId())))
            .andExpect(jsonPath("$.unitId", equalTo(testUnit.getId())))
            .andExpect(jsonPath("$.startDate", equalTo(startDate.toString())))
            .andExpect(jsonPath("$.endDate", equalTo(endDate.toString())));
    }

    @Test
    void shouldFailToBookIfUnitAlreadyBookedInPayedStatus() throws Exception {
        // Given - First booking already exists
        EventData existingEvent = bookingService.bookUnit(testUser.getId(), testUnit.getId(), now().plusDays(2),
            now().plusDays(5));
        paymentService.processPayment(existingEvent.paymentId(), true);

        // When - Try to book overlapping dates
        ResultActions perform = mockMvc.perform(post("/bookings")
            .param("userId", String.valueOf(testUser.getId()))
            .param("unitId", String.valueOf(testUnit.getId()))
            .param("startDate", now().plusDays(3).toString()) // Overlaps with existing
            .param("endDate", now().plusDays(6).toString())
            .contentType(MediaType.APPLICATION_JSON));

        // Then
        perform.andExpect(status().isConflict());
    }

    @Test
    void shouldPassToBookIfUnitAlreadyBookedInPayedDeclinedStatus() throws Exception {
        // Given - First booking already exists
        EventData existingEvent = bookingService.bookUnit(testUser.getId(), testUnit.getId(), now().plusDays(2),
            now().plusDays(5));
        paymentService.processPayment(existingEvent.paymentId(), false);

        // When - Try to book overlapping dates
        ResultActions perform = mockMvc.perform(post("/bookings")
            .param("userId", String.valueOf(testUser.getId()))
            .param("unitId", String.valueOf(testUnit.getId()))
            .param("startDate", now().plusDays(3).toString()) // Overlaps with existing
            .param("endDate", now().plusDays(6).toString())
            .contentType(MediaType.APPLICATION_JSON));

        // Then
        perform
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId", equalTo(testUser.getId())))
            .andExpect(jsonPath("$.unitId", equalTo(testUnit.getId())))
            .andExpect(jsonPath("$.startDate", equalTo(now().plusDays(3).toString())))
            .andExpect(jsonPath("$.endDate", equalTo(now().plusDays(6).toString())));
    }

    @Test
    void shouldFailToBookIfUnitAlreadyBookedInReadyToPayStatus() throws Exception {
        // Given - First booking already exists
        EventData existingEvent = bookingService.bookUnit(testUser.getId(), testUnit.getId(), now().plusDays(2),
            now().plusDays(5));

        // When - Try to book overlapping dates
        ResultActions perform = mockMvc.perform(post("/bookings")
            .param("userId", String.valueOf(testUser.getId()))
            .param("unitId", String.valueOf(testUnit.getId()))
            .param("startDate", now().plusDays(3).toString()) // Overlaps with existing
            .param("endDate", now().plusDays(6).toString())
            .contentType(MediaType.APPLICATION_JSON));

        // Then
        perform.andExpect(status().isConflict());
    }


    @Test
    void shouldFailToBookIfUserDoesNotExist() throws Exception {
        // Given - Invalid userId
        int invalidUserId = 999;

        // When
        ResultActions perform = mockMvc.perform(post("/bookings")
            .param("userId", String.valueOf(invalidUserId))
            .param("unitId", String.valueOf(testUnit.getId()))
            .param("startDate", now().plusDays(1).toString())
            .param("endDate", now().plusDays(3).toString())
            .contentType(MediaType.APPLICATION_JSON));

        // Then
        perform.andExpect(status().isNotFound());
    }

    @Test
    void shouldFailToBookIfUnitDoesNotExist() throws Exception {
        // Given - Invalid unitId
        int invalidUnitId = 999;

        // When
        ResultActions perform = mockMvc.perform(post("/bookings")
            .param("userId", String.valueOf(testUser.getId()))
            .param("unitId", String.valueOf(invalidUnitId))
            .param("startDate", now().plusDays(1).toString())
            .param("endDate", now().plusDays(3).toString())
            .contentType(MediaType.APPLICATION_JSON));

        // Then
        perform.andExpect(status().isNotFound());
    }
}
