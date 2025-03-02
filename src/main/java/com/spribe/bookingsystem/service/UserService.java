package com.spribe.bookingsystem.service;

import com.spribe.bookingsystem.entity.UserEntity;
import com.spribe.bookingsystem.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Optional<UserEntity> getUserById(int userId) {
        return userRepository.findById(userId);
    }
}
