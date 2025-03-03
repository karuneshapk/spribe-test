package com.spribe.bookingsystem.controller;

import com.spribe.bookingsystem.entity.UserEntity;
import com.spribe.bookingsystem.payload.request.dto.UserDto;
import com.spribe.bookingsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for managing users")
public class UserController {
    private final UserService userService;

    @PostMapping
    @Operation(
        summary = "Create a new user",
        description = "Creates a new user with the provided name and email.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User successfully created",
                content = @Content(schema = @Schema(implementation = UserEntity.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "User with email already exists")
        }
    )
    public ResponseEntity<UserEntity> createUser(
        @Valid @RequestBody UserDto userDto
    ) {
        UserEntity createdUser = userService.createUser(userDto);
        return ResponseEntity.ok(createdUser);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get user by ID",
        description = "Fetches a user by their ID.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User found",
                content = @Content(schema = @Schema(implementation = UserEntity.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    public ResponseEntity<UserEntity> getUserById(
        @Parameter(description = "ID of the user", example = "1")
        @PathVariable Integer id
    ) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping
    @Operation(
        summary = "Get all users",
        description = "Retrieves a list of all registered users.",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of users",
                content = @Content(schema = @Schema(implementation = UserEntity.class))),
            @ApiResponse(responseCode = "204", description = "No users found")
        }
    )
    public ResponseEntity<List<UserEntity>> getUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
