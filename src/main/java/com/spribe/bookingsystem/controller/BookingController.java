package com.spribe.bookingsystem.controller;

import com.spribe.bookingsystem.entity.EventEntity;
import com.spribe.bookingsystem.service.BookingService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService eventService;

    @PostMapping
    public ResponseEntity<EventEntity> bookUnit(@RequestParam int userId, @RequestParam int unitId, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(eventService.bookUnit(userId, unitId, startDate, endDate));
    }

}
