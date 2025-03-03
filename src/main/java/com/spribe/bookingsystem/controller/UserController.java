package com.spribe.bookingsystem.controller;

import com.spribe.bookingsystem.entity.UserEntity;
import com.spribe.bookingsystem.payload.request.dto.UserDto;
import com.spribe.bookingsystem.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserEntity> createUser(@Valid @RequestBody UserDto userDto) {
        UserEntity createdUser = userService.createUser(userDto);
        return ResponseEntity.ok(createdUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserEntity> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping
    public ResponseEntity<List<UserEntity>> getUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

}
