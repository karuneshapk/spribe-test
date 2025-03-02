package com.spribe.bookingsystem.controller;

import com.spribe.bookingsystem.entity.AccommodationType;
import com.spribe.bookingsystem.entity.UnitEntity;
import com.spribe.bookingsystem.payload.request.dto.UnitDto;
import com.spribe.bookingsystem.payload.response.SearchUnitResponse;
import com.spribe.bookingsystem.service.UnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
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
@Tag(name = "Units", description = "Endpoints for managing booking units")
public class UnitController {
    private final UnitService unitService;

    @PostMapping
    @Operation(
        summary = "Add a new unit",
        description = "Allows a user to create a new unit with details like number of rooms, type, floor, cost, and description.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Unit successfully created",
                content = @Content(schema = @Schema(implementation = UnitEntity.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
        }
    )
    public ResponseEntity<UnitEntity> addUnit(@RequestBody UnitDto unitDto) {
        return ResponseEntity.ok(unitService.addUnit(unitDto));
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search for available units",
        description = "Filters available units based on various criteria like type, rooms, floor, cost, and date range.",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of available units",
                content = @Content(schema = @Schema(implementation = SearchUnitResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters")
        }
    )
    public SearchUnitResponse searchUnits(
        @Parameter(description = "Type of accommodation (HOME, FLAT, APARTMENTS)", example = "HOME")
        @RequestParam(required = false) AccommodationType type,

        @Parameter(description = "Number of rooms in the unit", example = "2")
        @RequestParam(required = false) Integer numRooms,

        @Parameter(description = "Floor number where the unit is located", example = "3")
        @RequestParam(required = false) Integer floor,

        @Parameter(description = "Minimum cost of the unit", example = "100.00")
        @RequestParam(required = false) BigDecimal minCost,

        @Parameter(description = "Maximum cost of the unit", example = "500.00")
        @RequestParam(required = false) BigDecimal maxCost,

        @Parameter(description = "Start date for booking availability", example = "2025-03-02")
        @RequestParam(required = false) LocalDate startDate,

        @Parameter(description = "End date for booking availability", example = "2025-03-10")
        @RequestParam(required = false) LocalDate endDate,

        @Parameter(description = "Page number for pagination", example = "0")
        @RequestParam(defaultValue = "0", required = false) int page,

        @Parameter(description = "Number of results per page", example = "10")
        @RequestParam(defaultValue = "10", required = false) int size,

        @Parameter(description = "Sorting field (e.g., type, totalCost)", example = "totalCost")
        @RequestParam(defaultValue = "type", required = false) String sortBy,

        @Parameter(description = "Sorting order: true for ascending, false for descending", example = "true")
        @RequestParam boolean asc) {

        return unitService.searchUnits(type, numRooms, floor, minCost, maxCost, startDate, endDate, page, size, sortBy, asc);
    }

    @GetMapping("/available-count")
    @Operation(
        summary = "Get count of available units",
        description = "Returns the number of units available for booking within a given date range.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Available units count"),
            @ApiResponse(responseCode = "400", description = "Invalid date parameters")
        }
    )
    public ResponseEntity<Long> getAvailableUnitsCount(
        @Parameter(description = "Start date for availability check", example = "2025-03-01")
        @RequestParam(required = false) LocalDate startDate,

        @Parameter(description = "End date for availability check", example = "2025-03-15")
        @RequestParam(required = false) LocalDate endDate) {

        long count = unitService.getAvailableUnitsCount(startDate, endDate);
        return ResponseEntity.ok(count);
    }
}
