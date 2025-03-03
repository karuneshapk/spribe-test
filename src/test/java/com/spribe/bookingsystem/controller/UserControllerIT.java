package com.spribe.bookingsystem.controller;

import com.spribe.bookingsystem.config.IntegrationTest;
import com.spribe.bookingsystem.config.PaymentProperties;
import com.spribe.bookingsystem.entity.UserEntity;
import com.spribe.bookingsystem.repository.EventRepository;
import com.spribe.bookingsystem.repository.PaymentRepository;
import com.spribe.bookingsystem.repository.UnitRepository;
import com.spribe.bookingsystem.repository.UserRepository;
import com.spribe.bookingsystem.service.BookingService;
import com.spribe.bookingsystem.service.PaymentService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static java.time.LocalDateTime.now;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
public class UserControllerIT {

    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UnitRepository unitRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        paymentRepository.deleteAll();
        unitRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Transactional
    @Test
    void shouldCreateUser() throws Exception {
        // Given
        String userJson = """
                {
                    "name": "John2 Doe2",
                    "email": "johndoetest@exampletest.com"
                }
                """;

        // When
        ResultActions perform = mockMvc.perform(post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(userJson));

        // Then
        perform
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("John2 Doe2"))
            .andExpect(jsonPath("$.email").value("johndoetest@exampletest.com"));
    }

    @Transactional
    @Test
    void shouldGetUserById() throws Exception {
        // Given
        UserEntity user = UserEntity.builder().name("Mark Malko").email("mmalko@gmai.com").createdAt(now()).build();
        UserEntity savedUser = userRepository.save(user);

        // When
        ResultActions perform = mockMvc.perform(get("/users/{id}", savedUser.getId()));

        // Then
        perform
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", equalTo("Mark Malko")))
            .andExpect(jsonPath("$.email", equalTo("mmalko@gmai.com")));
    }

    @Transactional
    @Test
    void shouldGetAllUsers() throws Exception {
        // Given
        UserEntity user1 = UserEntity.builder().name("User1").email("user1@example.com").createdAt(now()).build();
        UserEntity user2 = UserEntity.builder().name("User2").email("user2@example.com").createdAt(now()).build();
        userRepository.save(user1);
        userRepository.save(user2);

        // When
        ResultActions perform = mockMvc.perform(get("/users"));

        // Then
        perform
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Transactional
    @Test
    void shouldReturnNotFoundForInvalidUserId() throws Exception {
        mockMvc.perform(get("/users/{id}", 999))
            .andExpect(status().isNotFound());
    }
}
