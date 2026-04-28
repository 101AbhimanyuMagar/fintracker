package com.fintracker_backend.fintracker.service;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fintracker_backend.fintracker.dto.UserRegisterDTO;
import com.fintracker_backend.fintracker.dto.UserResponseDTO;
import com.fintracker_backend.fintracker.entity.Role;
import com.fintracker_backend.fintracker.entity.User;
import com.fintracker_backend.fintracker.exception.BadRequestException;
import com.fintracker_backend.fintracker.exception.ResourceNotFoundException;
import com.fintracker_backend.fintracker.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
private final PasswordEncoder passwordEncoder;
@Override
public User getUserById(Long userId) {

    log.debug("Fetching user by id | userId={}", userId);

    return userRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("User not found | userId={}", userId);
                return new ResourceNotFoundException("User not found with id: " + userId);
            });
}

@Override
public UserResponseDTO getUserResponseByEmail(String email) {

    log.debug("Fetching user response | email={}", email);

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.error("User not found | email={}", email);
                return new ResourceNotFoundException("User not found");
            });

    return UserResponseDTO.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .build();
}
 // ✅ REGISTER LOGIC
@Override
public UserResponseDTO registerUser(UserRegisterDTO request) {

    log.info("User registration attempt | email={}", request.getEmail());

    userRepository.findByEmail(request.getEmail())
            .ifPresent(u -> {
                log.warn("Duplicate email registration attempt | email={}", request.getEmail());
                throw new BadRequestException("Email already exists");
            });

    User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(Role.CUSTOMER)
            .build();

    userRepository.save(user);

    log.info("User registered successfully | userId={} | email={}",
            user.getId(), user.getEmail());

    return UserResponseDTO.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .build();
}
 @Override
public User getUserByEmail(String email) {

    log.debug("Fetching user by email | email={}", email);

    return userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.error("User not found | email={}", email);
                return new ResourceNotFoundException("User not found with email: " + email);
            });
}
}
