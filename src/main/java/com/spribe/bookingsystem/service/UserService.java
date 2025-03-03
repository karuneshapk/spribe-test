package com.spribe.bookingsystem.service;

import com.spribe.bookingsystem.entity.UserEntity;
import com.spribe.bookingsystem.exception.UserNotFoundException;
import com.spribe.bookingsystem.payload.request.dto.UserDto;
import com.spribe.bookingsystem.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserEntity getUserById(int userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Transactional
    public UserEntity createUser(UserDto userDto) {
        UserEntity user = new UserEntity();
        user.setName(userDto.name());
        user.setEmail(userDto.email());
        return userRepository.save(user);
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

}
