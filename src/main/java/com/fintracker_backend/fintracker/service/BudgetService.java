package com.fintracker_backend.fintracker.service;

import java.math.BigDecimal;

import com.fintracker_backend.fintracker.dto.BudgetRequestDTO;
import com.fintracker_backend.fintracker.entity.Budget;

public interface BudgetService {

    Budget setBudget(BudgetRequestDTO request, Long userId);

    Budget getBudget(Long userId, Long categoryId, String period);

    boolean checkBudgetExceeded(Long userId, Long categoryId, BigDecimal amount);
}