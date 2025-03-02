package com.spribe.bookingsystem.payload.request.dto;

import com.spribe.bookingsystem.entity.AccommodationType;
import java.math.BigDecimal;

public record UnitDto(
    Integer userId,
    int numRooms,
    AccommodationType type,
    int floor,
    String description,
    BigDecimal baseCost
) {}
