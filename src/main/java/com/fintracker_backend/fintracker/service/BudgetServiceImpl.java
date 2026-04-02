package com.fintracker_backend.fintracker.service;

import java.math.BigDecimal;
import java.time.LocalDate;


import org.springframework.stereotype.Service;

import com.fintracker_backend.fintracker.dto.BudgetRequestDTO;
import com.fintracker_backend.fintracker.entity.Budget;
import com.fintracker_backend.fintracker.entity.Category;
import com.fintracker_backend.fintracker.entity.CategoryType;
import com.fintracker_backend.fintracker.entity.User;
import com.fintracker_backend.fintracker.exception.BadRequestException;
import com.fintracker_backend.fintracker.exception.ResourceNotFoundException;
import com.fintracker_backend.fintracker.repository.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    @Override
public Budget setBudget(BudgetRequestDTO request, Long userId) {

    if (request.getLimitAmount() == null || request.getLimitAmount().compareTo(BigDecimal.ZERO) <= 0) {
        throw new BadRequestException("Budget amount must be greater than zero");
    }

    if (request.getPeriod() == null || !request.getPeriod().matches("\\d{4}-\\d{2}")) {
        throw new BadRequestException("Invalid period format. Expected YYYY-MM");
    }

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Category category = categoryRepository.findById(request.getCategoryId())
            .filter(cat -> cat.getUser().getId().equals(userId)) // 🔥 SECURITY FIX
            .orElseThrow(() -> new ResourceNotFoundException("Category not found or unauthorized"));

    // 🔥 Business Rule
    if (category.getType() != CategoryType.EXPENSE) {
        throw new BadRequestException("Budget can only be set for EXPENSE categories");
    }

    Budget budget = budgetRepository
            .findByUserIdAndCategoryIdAndPeriod(
                    userId,
                    request.getCategoryId(),
                    request.getPeriod()
            )
            .orElse(Budget.builder()
                    .user(user)
                    .category(category)
                    .build());

    budget.setLimitAmount(request.getLimitAmount());
    budget.setPeriod(request.getPeriod());

    return budgetRepository.save(budget);
}

    @Override
public Budget getBudget(Long userId, Long categoryId, String period) {

    if (period == null || !period.matches("\\d{4}-\\d{2}")) {
        throw new BadRequestException("Invalid period format");
    }

    return budgetRepository
            .findByUserIdAndCategoryIdAndPeriod(userId, categoryId, period)
            .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
}

    @Override
public boolean checkBudgetExceeded(Long userId, Long categoryId, BigDecimal amount) {

    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
        throw new BadRequestException("Amount must be greater than zero");
    }

    String period = getCurrentPeriod();

    Budget budget = budgetRepository
            .findByUserIdAndCategoryIdAndPeriod(userId, categoryId, period)
            .orElse(null);

    if (budget == null) return false;

    BigDecimal totalSpent =
            transactionRepository.getTotalExpenseByCategoryAndPeriod(
                    userId, categoryId, period);

    if (totalSpent == null) totalSpent = BigDecimal.ZERO;

    BigDecimal newTotal = totalSpent.add(amount);

    return newTotal.compareTo(budget.getLimitAmount()) > 0;
}

    private String getCurrentPeriod() {
        LocalDate now = LocalDate.now();
        return now.getYear() + "-" + String.format("%02d", now.getMonthValue());
    }
}
