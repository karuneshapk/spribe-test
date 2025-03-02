package com.spribe.bookingsystem.payload.response.data;

import com.spribe.bookingsystem.entity.AccommodationType;
import java.math.BigDecimal;

public record UnitData(
    int id,
    int numRooms,
    AccommodationType type,
    int floor,
    String description,
    BigDecimal totalCost
) {}
