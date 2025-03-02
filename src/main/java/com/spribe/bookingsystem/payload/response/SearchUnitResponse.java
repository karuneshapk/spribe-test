package com.spribe.bookingsystem.payload.response;

import com.spribe.bookingsystem.payload.response.data.UnitData;
import java.util.List;
import lombok.Builder;

@Builder
public record SearchUnitResponse(long total, List<UnitData> units) {}
