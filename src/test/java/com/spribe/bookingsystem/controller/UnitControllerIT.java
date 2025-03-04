package com.spribe.bookingsystem.controller;

import static com.spribe.bookingsystem.util.TestConstants.TWO_SECONDS;
import com.spribe.bookingsystem.config.IntegrationTest;
import com.spribe.bookingsystem.config.PaymentProperties;
import com.spribe.bookingsystem.entity.AccommodationType;
import com.spribe.bookingsystem.entity.UnitEntity;
import com.spribe.bookingsystem.entity.UserEntity;
import com.spribe.bookingsystem.repository.EventRepository;
import com.spribe.bookingsystem.repository.PaymentRepository;
import com.spribe.bookingsystem.repository.UnitRepository;
import com.spribe.bookingsystem.repository.UserRepository;
import jakarta.transaction.Transactional;
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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
public class UnitControllerIT {

    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private PaymentProperties paymentProperties;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UnitRepository unitRepository;

    private MockMvc mockMvc;
    private UserEntity testUser;
    private UnitEntity testUnit1;
    private UnitEntity testUnit2;
    private UnitEntity testUnit3;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        paymentRepository.deleteAll();
        eventRepository.deleteAll();
        unitRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(UserEntity.builder()
            .name("Test User")
            .email("testuser@example.com")
            .createdAt(LocalDateTime.now())
            .build());

        testUnit1 = UnitEntity.builder()
            .numRooms(2)
            .user(testUser)
            .type(AccommodationType.FLAT)
            .floor(1)
            .description("Modern flat")
            .baseCost(BigDecimal.valueOf(100))
            .totalCost(BigDecimal.valueOf(115))
            .createdAt(LocalDateTime.now())
            .build();
        unitRepository.save(testUnit1);

        testUnit2 = UnitEntity.builder()
            .numRooms(5)
            .user(testUser)
            .type(AccommodationType.HOME)
            .floor(5)
            .description("Big house with garden")
            .baseCost(BigDecimal.valueOf(200))
            .totalCost(BigDecimal.valueOf(230))
            .createdAt(LocalDateTime.now())
            .build();
        unitRepository.save(testUnit2);

        testUnit3 = UnitEntity.builder()
            .numRooms(1)
            .user(testUser)
            .type(AccommodationType.APARTMENTS)
            .floor(10)
            .description("five m2 apartments")
            .baseCost(BigDecimal.valueOf(60))
            .totalCost(BigDecimal.valueOf(69))
            .createdAt(LocalDateTime.now())
            .build();
        unitRepository.save(testUnit3);

    }

    @Transactional
    @Test
    void shouldCreateUnit() throws Exception {
        // Given
        String unitJson =
                "{\n"
                    + " \"userId\": \"" + testUser.getId()  + "\",\n"
                    + " \"numRooms\": 3,\n"
                    + " \"type\": \"HOME\",\n"
                    + " \"floor\": 2,\n"
                    + " \"description\": \"Big house with garden\",\n"
                    + " \"baseCost\": 200.00\n"
                    + "}";

        // When
        ResultActions perform = mockMvc.perform(post("/units")
            .contentType(MediaType.APPLICATION_JSON)
            .content(unitJson));

        // Then
        perform
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.numRooms").value(3))
            .andExpect(jsonPath("$.type").value("HOME"))
            .andExpect(jsonPath("$.floor").value(2))
            .andExpect(jsonPath("$.description").value("Big house with garden"))
            .andExpect(jsonPath("$.baseCost").value(200.00));
    }

    @Transactional
    @Test
    void shouldSearchUnitWithFullMatch() throws Exception {
        // When
        ResultActions perform = mockMvc.perform(get("/units/search")
            .param("type", "FLAT")
            .param("numRooms", "2")
            .param("floor", "1")
            .param("minCost", "100")
            .param("maxCost", "150")
            .param("page", "0")
            .param("size", "2")
            .param("sortBy", "type")
            .param("asc", "true"));

        // Then
        perform
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.units", hasSize(1)))
            .andExpect(jsonPath("$.units[0].type", equalTo("FLAT")));
    }

    @Transactional
    @Test
    void shouldSearchUnitByType() throws Exception {
        // When
        ResultActions perform = mockMvc.perform(get("/units/search")
            .param("type", "FLAT")
            .param("page", "0")
            .param("size", "2")
            .param("sortBy", "type")
            .param("asc", "true"));

        // Then
        perform
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.units", hasSize(1)))
            .andExpect(jsonPath("$.units[0].type", equalTo("FLAT")));
    }

    @Transactional
    @Test
    void shouldSearchUnitByNUmberRooms() throws Exception {
        // When
        ResultActions perform = mockMvc.perform(get("/units/search")
            .param("numRooms", "2")
            .param("page", "0")
            .param("size", "2")
            .param("sortBy", "type")
            .param("asc", "true"));

        // Then
        perform
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.units", hasSize(1)))
            .andExpect(jsonPath("$.units[0].type", equalTo("FLAT")));
    }

    @Transactional
    @Test
    void shouldSearchUnitByFloor() throws Exception {
        // When
        ResultActions perform = mockMvc.perform(get("/units/search")
            .param("floor", "5")
            .param("page", "0")
            .param("size", "2")
            .param("sortBy", "type")
            .param("asc", "true"));

        // Then
        perform
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.units", hasSize(1)))
            .andExpect(jsonPath("$.units[0].type", equalTo("HOME")));
    }

    @Transactional
    @Test
    void shouldSearchUnitByTotalPriceRange() throws Exception {
        // When
        ResultActions perform = mockMvc.perform(get("/units/search")
            .param("minCost", "200")
            .param("maxCost", "250")
            .param("page", "0")
            .param("size", "2")
            .param("sortBy", "type")
            .param("asc", "true"));

        // Then
        perform
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.units", hasSize(1)))
            .andExpect(jsonPath("$.units[0].type", equalTo("HOME")));
    }

    @Transactional
    @Test
    void shouldSearchFirstUnitPage() throws Exception {
        // When
        ResultActions perform = mockMvc.perform(get("/units/search")
            .param("page", "0")
            .param("size", "1")
            .param("sortBy", "floor")
            .param("asc", "true"));

        // Then
        perform
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.units", hasSize(1)))
            .andExpect(jsonPath("$.units[0].type", equalTo("FLAT")));
    }

    @Transactional
    @Test
    void shouldSearchSecondUnitPage() throws Exception {
        // When
        ResultActions perform = mockMvc.perform(get("/units/search")
            .param("page", "1")
            .param("size", "1")
            .param("sortBy", "floor")
            .param("asc", "true"));

        // Then
        perform
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.units", hasSize(1)))
            .andExpect(jsonPath("$.units[0].type", equalTo("HOME")));
    }

    @Transactional
    @Test
    void shouldSearchThidUnitPage() throws Exception {
        // When
        ResultActions perform = mockMvc.perform(get("/units/search")
            .param("page", "2")
            .param("size", "1")
            .param("sortBy", "floor")
            .param("asc", "true"));

        // Then
        perform
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.units", hasSize(1)))
            .andExpect(jsonPath("$.units[0].type", equalTo("APARTMENTS")));
    }

    @Transactional
    @Test
    void shouldNotFindBookedUnit() throws Exception {
        // Given
        mockMvc.perform(post("/bookings")
            .param("userId", String.valueOf(testUser.getId()))
            .param("unitId", String.valueOf(testUnit3.getId()))
            .param("startDate", "2025-03-10")
            .param("endDate", "2025-03-15")
            .contentType(MediaType.APPLICATION_JSON));

        // When
        ResultActions perform = mockMvc.perform(get("/units/search")
            .param("floor", "10")
            .param("startDate", "2025-03-12")
            .param("endDate", "2025-03-17")
            .param("page", "0")
            .param("size", "1")
            .param("sortBy", "floor")
            .param("asc", "true"));

        // Then
        perform
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.units", hasSize(0)));
    }

    @Transactional
    @Test
    void shouldFindNotConfirmedUnit() throws Exception {
        // Given
        mockMvc.perform(post("/bookings")
            .param("userId", String.valueOf(testUser.getId()))
            .param("unitId", String.valueOf(testUnit3.getId()))
            .param("startDate", "2025-03-10")
            .param("endDate", "2025-03-15")
            .contentType(MediaType.APPLICATION_JSON));

        // Simulate payment expiration delay (5 seconds)
        Thread.sleep(paymentProperties.expirationTime() * 1000 + TWO_SECONDS);

        // When
        ResultActions perform = mockMvc.perform(get("/units/search")
            .param("floor", "10")
            .param("startDate", "2025-03-12")
            .param("endDate", "2025-03-17")
            .param("page", "0")
            .param("size", "1")
            .param("sortBy", "floor")
            .param("asc", "true"));

        // Then
        perform
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.units", hasSize(0)));
    }

    @Transactional
    @Test
    void shouldNotFindUnitWithNotExistingFloor() throws Exception {
        // When
        ResultActions perform = mockMvc.perform(get("/units/search")
            .param("floor", "1000")
            .param("page", "1")
            .param("size", "1")
            .param("sortBy", "floor")
            .param("asc", "true"));

        // Then
        perform
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.units", hasSize(0)));
    }

    @Transactional
    @Test
    void shouldGetAvailableUnitsCountWithoutBookedUnit() throws Exception {
        // Given
        mockMvc.perform(post("/bookings")
            .param("userId", String.valueOf(testUser.getId()))
            .param("unitId", String.valueOf(testUnit3.getId()))
            .param("startDate", "2025-03-10")
            .param("endDate", "2025-03-15")
            .contentType(MediaType.APPLICATION_JSON));

        // When
        ResultActions perform = mockMvc.perform(get("/units/available-count"));

        // Then
        perform
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(2));
    }

    @Transactional
    @Test
    void shouldGetAvailableUnitsCount() throws Exception {
        // Given
        mockMvc.perform(post("/bookings")
            .param("userId", String.valueOf(testUser.getId()))
            .param("unitId", String.valueOf(testUnit3.getId()))
            .param("startDate", "2025-03-10")
            .param("endDate", "2025-03-15")
            .contentType(MediaType.APPLICATION_JSON));

        // Simulate payment expiration delay (5 seconds)
        Thread.sleep(paymentProperties.expirationTime() * 1000 + TWO_SECONDS);


        // When
        ResultActions perform = mockMvc.perform(get("/units/available-count"));

        // Then
        perform
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(3));
    }

}
