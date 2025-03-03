package com.spribe.bookingsystem.controller;

import com.spribe.bookingsystem.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Endpoints for managing payments")
public class PaymentController {
    private final PaymentService paymentService;

    @PutMapping("/{paymentId}/confirm")
    @Operation(
        summary = "Confirm a payment",
        description = "Confirms a pending payment, marking it as PAID.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Payment successfully confirmed",
                content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payment ID or already confirmed"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
        }
    )
    public ResponseEntity<String> confirmPayment(
        @Parameter(description = "ID of the payment to confirm", example = "101")
        @PathVariable int paymentId
    ) {
        paymentService.processPayment(paymentId, true);
        return ResponseEntity.ok("Payment confirmed");
    }

    @PutMapping("/{paymentId}/cancel")
    @Operation(
        summary = "Cancel a payment",
        description = "Cancels a pending payment, marking it as FAILED.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Payment successfully canceled",
                content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payment ID or already processed"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
        }
    )
    public ResponseEntity<String> cancelPayment(
        @Parameter(description = "ID of the payment to cancel", example = "102")
        @PathVariable int paymentId
    ) {
        paymentService.processPayment(paymentId, false);
        return ResponseEntity.ok("Payment canceled");
    }
}
