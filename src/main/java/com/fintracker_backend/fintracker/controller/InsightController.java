package com.fintracker_backend.fintracker.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.fintracker_backend.fintracker.dto.InsightDTO;
import com.fintracker_backend.fintracker.service.InsightService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightController {

    private final InsightService insightService;

    @GetMapping
    public ResponseEntity<List<InsightDTO>> getInsights(Authentication auth) {

        return ResponseEntity.ok(
                insightService.generateInsights(auth.getName())
        );
    }
}