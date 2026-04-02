package com.fintracker_backend.fintracker.service;

import com.fintracker_backend.fintracker.entity.User;

public interface UserService {
    User getUserById(Long id);
}
