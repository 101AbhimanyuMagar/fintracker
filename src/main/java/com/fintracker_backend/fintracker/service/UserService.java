package com.fintracker_backend.fintracker.service;

import com.fintracker_backend.fintracker.dto.UserRegisterDTO;
import com.fintracker_backend.fintracker.dto.UserResponseDTO;
import com.fintracker_backend.fintracker.entity.User;

public interface UserService {
    User getUserById(Long id);
    User getUserByEmail(String email);

    UserResponseDTO getUserResponseByEmail(String email);

    UserResponseDTO registerUser(UserRegisterDTO request);
}
