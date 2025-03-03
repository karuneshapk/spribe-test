package com.spribe.bookingsystem.controller;

import com.spribe.bookingsystem.payload.response.data.EventData;
import com.spribe.bookingsystem.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Endpoints for managing unit bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(
        summary = "Book a unit",
        description = "Allows a user to book a unit for a specific date range.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Booking successful",
                content = @Content(schema = @Schema(implementation = EventData.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "409", description = "Unit already booked for the given dates")
        }
    )
    public EventData bookUnit(
        @Parameter(description = "User ID making the booking", example = "1")
        @RequestParam int userId,

        @Parameter(description = "Unit ID to be booked", example = "101")
        @RequestParam int unitId,

        @Parameter(description = "Start date of the booking", example = "2025-03-05")
        @RequestParam LocalDate startDate,

        @Parameter(description = "End date of the booking", example = "2025-03-10")
        @RequestParam LocalDate endDate
    ) {
        return bookingService.bookUnit(userId, unitId, startDate, endDate);
    }
}
