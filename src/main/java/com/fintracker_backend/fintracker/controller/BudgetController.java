package com.fintracker_backend.fintracker.controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import lombok.RequiredArgsConstructor;
import com.fintracker_backend.fintracker.dto.BudgetRequestDTO;
import com.fintracker_backend.fintracker.dto.BudgetResponseDTO;
import com.fintracker_backend.fintracker.entity.Budget;
import com.fintracker_backend.fintracker.service.BudgetService;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor

public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<Budget> setBudget(
            @RequestBody BudgetRequestDTO request,
            Authentication authentication
    ) {
        String username = authentication.getName(); // ✅ from JWT

        return ResponseEntity.ok(
                budgetService.setBudget(request, username)
        );
    }
    @GetMapping("/alerts")
public ResponseEntity<?> getAlerts(Authentication auth) {
    return ResponseEntity.ok(
            budgetService.getBudgetAlerts(auth.getName())
    );
}
@GetMapping("/all")
public ResponseEntity<List<BudgetResponseDTO>> getAllBudgets(Authentication authentication) {

    String email = authentication.getName();

    return ResponseEntity.ok(
            budgetService.getAllBudgets(email)
    );
}
    @GetMapping
    public ResponseEntity<Budget> getBudget(
            @RequestParam Long categoryId,
            @RequestParam String period,
            Authentication authentication
    ) {
        String username = authentication.getName();

        return ResponseEntity.ok(
                budgetService.getBudget(username, categoryId, period)
        );
    }
}