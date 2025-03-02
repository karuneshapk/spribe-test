package com.spribe.bookingsystem.controller;

import com.spribe.bookingsystem.entity.AccommodationType;
import com.spribe.bookingsystem.entity.UnitEntity;
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
    public ResponseEntity<List<UnitEntity>> searchUnits(
        @RequestParam AccommodationType type,
        @RequestParam int numRooms,
        @RequestParam int floor,
        @RequestParam BigDecimal minCost,
        @RequestParam BigDecimal maxCost,
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate,
        @RequestParam int page,
        @RequestParam int size,
        @RequestParam String sortBy,
        @RequestParam boolean asc) {
        return ResponseEntity.ok(unitService.searchUnits(type, numRooms, floor, minCost, maxCost, startDate, endDate, page, size, sortBy, asc).getContent());
    }

    @GetMapping("/available-count")
    public ResponseEntity<Long> getAvailableUnitsCount() {
        return ResponseEntity.ok(unitService.getAvailableUnits());
    }
}
