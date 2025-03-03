package com.spribe.bookingsystem.payload.response.data;

import com.spribe.bookingsystem.entity.EventStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record EventData(
    Integer id,
    Integer userId,
    Integer unitId,
    Integer paymentId,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal totalPrice,
    EventStatus status,
    LocalDateTime createdAt
) {}
