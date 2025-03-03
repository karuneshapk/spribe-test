package com.spribe.bookingsystem.payload.request.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UserDto(
    @NotBlank(message = "Name is required")
    String name,

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    String email
) {}
