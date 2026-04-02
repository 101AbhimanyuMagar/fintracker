package com.fintracker_backend.fintracker.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import com.fintracker_backend.fintracker.dto.BudgetRequestDTO;
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
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(
                budgetService.setBudget(request, userId)
        );
    }

    @GetMapping
    public ResponseEntity<Budget> getBudget(
            @RequestParam Long userId,
            @RequestParam Long categoryId,
            @RequestParam String period
    ) {
        return ResponseEntity.ok(
                budgetService.getBudget(userId, categoryId, period)
        );
    }
}