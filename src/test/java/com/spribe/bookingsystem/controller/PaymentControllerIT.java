package com.spribe.bookingsystem.controller;

import static com.spribe.bookingsystem.util.TestConstants.TWO_SECONDS;
import static java.time.LocalDate.now;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.spribe.bookingsystem.config.IntegrationTest;
import com.spribe.bookingsystem.config.PaymentProperties;
import com.spribe.bookingsystem.entity.PaymentEntity;
import com.spribe.bookingsystem.entity.PaymentStatus;
import com.spribe.bookingsystem.entity.UnitEntity;
import com.spribe.bookingsystem.entity.UserEntity;
import com.spribe.bookingsystem.payload.response.data.EventData;
import com.spribe.bookingsystem.repository.EventRepository;
import com.spribe.bookingsystem.repository.PaymentRepository;
import com.spribe.bookingsystem.repository.UnitRepository;
import com.spribe.bookingsystem.repository.UserRepository;
import com.spribe.bookingsystem.service.BookingService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@IntegrationTest
public class PaymentControllerIT {

    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private PaymentProperties paymentProperties;
    @Autowired private EventRepository eventRepository;
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
        eventRepository.deleteAll();
        unitRepository.deleteAll();
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
    void shouldConfirmPayment() throws Exception {
        // Given
        EventData existingEvent = bookingService.bookUnit(testUser.getId(), testUnit.getId(), now().plusDays(2),
            now().plusDays(5));

        // When
        ResultActions perform = mockMvc.perform(put("/payments/{paymentId}/confirm", existingEvent.paymentId())
            .contentType(MediaType.APPLICATION_JSON));

        // Then
        perform.andExpect(status().isOk())
            .andExpect(jsonPath("$").value("Payment confirmed"));

        PaymentEntity updatedPayment = paymentRepository.findById(existingEvent.paymentId()).orElseThrow();
        assert updatedPayment.getStatus() == PaymentStatus.PAID;
    }


    @Test
    void shouldCannotConfirmExpiredPayment() throws Exception {
        // Given
        EventData existingEvent = bookingService.bookUnit(testUser.getId(), testUnit.getId(), now().plusDays(2),
            now().plusDays(5));

        // Simulate payment expiration delay (5 seconds)
        Thread.sleep(paymentProperties.expirationTime() * 1000 + TWO_SECONDS);

        // When
        ResultActions perform = mockMvc.perform(put("/payments/{paymentId}/confirm", existingEvent.paymentId())
            .contentType(MediaType.APPLICATION_JSON));

        // Then
        perform.andExpect(status().isConflict());

        PaymentEntity updatedPayment = paymentRepository.findById(existingEvent.paymentId()).orElseThrow();
        assert updatedPayment.getStatus() == PaymentStatus.FAILED;
    }

    @Test
    void shouldCannotConfirmCanceledPayment() throws Exception {
        // Given
        EventData existingEvent = bookingService.bookUnit(testUser.getId(), testUnit.getId(), now().plusDays(2),
            now().plusDays(5));

        // When
       mockMvc.perform(put("/payments/{paymentId}/cancel", existingEvent.paymentId())
            .contentType(MediaType.APPLICATION_JSON));
        ResultActions perform = mockMvc.perform(put("/payments/{paymentId}/confirm", existingEvent.paymentId())
            .contentType(MediaType.APPLICATION_JSON));

        // Then
        perform.andExpect(status().isConflict());

        PaymentEntity updatedPayment = paymentRepository.findById(existingEvent.paymentId()).orElseThrow();
        assert updatedPayment.getStatus() == PaymentStatus.FAILED;
    }

    @Test
    void shouldCancelReadyToPayPayment() throws Exception {
        // Given
        EventData existingEvent = bookingService.bookUnit(testUser.getId(), testUnit.getId(), now().plusDays(2),
            now().plusDays(5));

        // When
        ResultActions perform = mockMvc.perform(put("/payments/{paymentId}/cancel", existingEvent.paymentId())
            .contentType(MediaType.APPLICATION_JSON));

        // Then
        perform.andExpect(status().isOk())
            .andExpect(jsonPath("$").value("Payment canceled"));

        PaymentEntity updatedPayment = paymentRepository.findById(existingEvent.paymentId()).orElseThrow();
        assert updatedPayment.getStatus() == PaymentStatus.FAILED;
    }

    @Test
    void shouldNotCancelAlreadyPaidPayment() throws Exception {
        // Given
        EventData existingEvent = bookingService.bookUnit(testUser.getId(), testUnit.getId(), now().plusDays(2),
            now().plusDays(5));

        // When
        mockMvc.perform(put("/payments/{paymentId}/confirm", existingEvent.paymentId())
            .contentType(MediaType.APPLICATION_JSON));
        ResultActions perform = mockMvc.perform(put("/payments/{paymentId}/cancel", existingEvent.paymentId())
            .contentType(MediaType.APPLICATION_JSON));

        // Then
        perform.andExpect(status().isConflict());

        PaymentEntity updatedPayment = paymentRepository.findById(existingEvent.paymentId()).orElseThrow();
        assert updatedPayment.getStatus() == PaymentStatus.PAID;
    }

    @Test
    void shouldReturnNotFoundForInvalidPaymentId() throws Exception {
        mockMvc.perform(put("/payments/{paymentId}/confirm", 999))
            .andExpect(status().isNotFound());

        mockMvc.perform(put("/payments/{paymentId}/cancel", 999))
            .andExpect(status().isNotFound());
    }
}
