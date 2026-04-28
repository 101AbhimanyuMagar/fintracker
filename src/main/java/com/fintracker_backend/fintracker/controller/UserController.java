package com.fintracker_backend.fintracker.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import lombok.RequiredArgsConstructor;
import com.fintracker_backend.fintracker.entity.User;
import com.fintracker_backend.fintracker.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<User> getLoggedInUser(Authentication authentication) {

        String email = authentication.getName(); // ✅ comes from JWT

        return ResponseEntity.ok(
                userService.getUserByEmail(email)
        );
    }
}