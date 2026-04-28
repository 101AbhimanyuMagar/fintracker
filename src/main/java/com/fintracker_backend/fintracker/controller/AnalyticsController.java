package com.fintracker_backend.fintracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.fintracker_backend.fintracker.dto.ChartDTO;
import com.fintracker_backend.fintracker.service.AnalyticsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/monthly")
    public ResponseEntity<?> monthly(Authentication auth) {
        return ResponseEntity.ok(
                analyticsService.getMonthlyChart(auth.getName())
        );
    }

    @GetMapping("/category")
    public ResponseEntity<?> category(Authentication auth) {
        return ResponseEntity.ok(
                analyticsService.getCategoryChart(auth.getName())
        );
    }
@GetMapping("/daily")
public ResponseEntity<ChartDTO> getDaily(
        @RequestParam Integer year,
        @RequestParam Integer month,
        Authentication auth) {

    return ResponseEntity.ok(
        analyticsService.getDailyChart(auth.getName(), year, month)
    );
}

@GetMapping("/top-categories")
public ResponseEntity<ChartDTO> getTopCategories(
        @RequestParam Integer year,
        @RequestParam Integer month,
        Authentication auth) {

    return ResponseEntity.ok(
        analyticsService.getTopCategories(auth.getName(), year, month)
    );
}
    @GetMapping("/summary")
    public ResponseEntity<?> summary(Authentication auth) {
        return ResponseEntity.ok(
                analyticsService.getSummary(auth.getName())
        );
    }

    @GetMapping("/account-wise")
public ResponseEntity<?> accountWise(Authentication auth) {
    return ResponseEntity.ok(
            analyticsService.getAccountWiseExpense(auth.getName())
    );
}
}