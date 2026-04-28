package com.fintracker_backend.fintracker.service;

import java.math.BigDecimal;
import java.util.List;

import com.fintracker_backend.fintracker.dto.AlertDTO;
import com.fintracker_backend.fintracker.dto.BudgetRequestDTO;
import com.fintracker_backend.fintracker.dto.BudgetResponseDTO;
import com.fintracker_backend.fintracker.entity.Budget;

public interface BudgetService {

    Budget setBudget(BudgetRequestDTO request, String email);

    Budget getBudget(String email, Long categoryId, String period);

    List<BudgetResponseDTO> getAllBudgets(String email);

    boolean checkBudgetExceeded(String email, Long categoryId, BigDecimal amount);

    List<AlertDTO> getBudgetAlerts(String email);
}