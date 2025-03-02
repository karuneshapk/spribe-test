package com.spribe.bookingsystem.controller;

import com.spribe.bookingsystem.entity.AccommodationType;
import com.spribe.bookingsystem.entity.UnitEntity;
import com.spribe.bookingsystem.payload.response.SearchUnitResponse;
import com.spribe.bookingsystem.payload.response.UnitData;
import com.spribe.bookingsystem.service.UnitService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/units")
@RequiredArgsConstructor
public class UnitController {
    private final UnitService unitService;

    @PostMapping
    public ResponseEntity<UnitEntity> addUnit(@RequestBody UnitEntity unit) {
        return ResponseEntity.ok(unitService.addUnit(unit));
    }

    @GetMapping("/search")
    public SearchUnitResponse searchUnits(
        @RequestParam(required = false) AccommodationType type,
        @RequestParam(required = false) Integer numRooms,
        @RequestParam(required = false) Integer floor,
        @RequestParam(required = false) BigDecimal minCost,
        @RequestParam(required = false) BigDecimal maxCost,
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate,
        @RequestParam int page,
        @RequestParam int size,
        @RequestParam String sortBy,
        @RequestParam boolean asc) {
        return unitService.searchUnits(type, numRooms, floor, minCost, maxCost, startDate, endDate, page, size, sortBy, asc);
    }

    @GetMapping("/available-count")
    public ResponseEntity<Long> getAvailableUnitsCount(
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate) {
        long count = unitService.getAvailableUnitsCount(startDate, endDate);
        return ResponseEntity.ok(count);
    }
}
