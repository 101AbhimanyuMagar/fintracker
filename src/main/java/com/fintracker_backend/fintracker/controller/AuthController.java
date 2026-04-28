package com.fintracker_backend.fintracker.controller;

import com.fintracker_backend.fintracker.dto.*;
import com.fintracker_backend.fintracker.security.JwtUtil;
import com.fintracker_backend.fintracker.service.UserService;

import lombok.RequiredArgsConstructor;

import com.fintracker_backend.fintracker.security.CustomUserDetails;
import com.fintracker_backend.fintracker.security.CustomUserDetailsService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;

import org.springframework.web.bind.annotation.*;
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {


    private final AuthenticationManager authenticationManager;


    private final CustomUserDetailsService userDetailsService;


    private final JwtUtil jwtUtil;


    private final UserService userService;

    // ✅ REGISTER
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody UserRegisterDTO request) {

        UserResponseDTO response = userService.registerUser(request);

        return ResponseEntity.status(201).body(response);
    }

    // ✅ LOGIN
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody UserLoginDTO request) {

        // 🔐 Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 🔍 Load user
        CustomUserDetails userDetails =
                (CustomUserDetails) userDetailsService.loadUserByUsername(request.getEmail());

        // 🔑 Generate JWT
        String token = jwtUtil.generateToken(userDetails);

        // 👤 Convert to UserResponseDTO (you must implement this)
        UserResponseDTO user = userService.getUserResponseByEmail(request.getEmail());

        // 📦 Return response
        return ResponseEntity.ok(
                AuthResponseDTO.builder()
                        .token(token)
                        .user(user)
                        .build()
        );
    }
}