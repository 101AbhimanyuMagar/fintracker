package com.fintracker_backend.fintracker.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fintracker_backend.fintracker.dto.AlertDTO;
import com.fintracker_backend.fintracker.dto.BudgetRequestDTO;
import com.fintracker_backend.fintracker.dto.BudgetResponseDTO;
import com.fintracker_backend.fintracker.entity.Budget;
import com.fintracker_backend.fintracker.entity.Category;
import com.fintracker_backend.fintracker.entity.CategoryType;
import com.fintracker_backend.fintracker.entity.User;
import com.fintracker_backend.fintracker.exception.BadRequestException;
import com.fintracker_backend.fintracker.exception.ResourceNotFoundException;
import com.fintracker_backend.fintracker.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    @Override
public Budget setBudget(BudgetRequestDTO request, String email) {

    log.info("Setting budget | user={} | categoryId={} | period={}",
            email, request.getCategoryId(), request.getPeriod());

    validateBudgetRequest(request);

    User user = getUserByEmail(email);
    Category category = getValidCategory(request.getCategoryId(), user);

    Budget budget = budgetRepository
            .findByUserIdAndCategoryIdAndPeriod(
                    user.getId(),
                    category.getId(),
                    request.getPeriod()
            )
            .orElse(Budget.builder()
                    .user(user)
                    .category(category)
                    .build());

    budget.setLimitAmount(request.getLimitAmount());
    budget.setPeriod(request.getPeriod());

    Budget saved = budgetRepository.save(budget);

    log.info("Budget saved | budgetId={} | amount={}",
            saved.getId(), saved.getLimitAmount());

    return saved;
}

    @Override
    public Budget getBudget(String email, Long categoryId, String period) {

        validatePeriod(period);

        User user = getUserByEmail(email);

        return budgetRepository
                .findByUserIdAndCategoryIdAndPeriod(user.getId(), categoryId, period)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
    }
@Override
public List<BudgetResponseDTO> getAllBudgets(String email) {

    log.debug("Fetching all budgets | user={}", email);

    User user = getUserByEmail(email);

    List<Budget> budgets = budgetRepository.findByUserId(user.getId());

    log.info("Budgets fetched | count={}", budgets.size());

    return budgets.stream().map(budget -> {

        BigDecimal spent = transactionRepository
                .getTotalExpenseByCategoryAndPeriod(
                        user.getId(),
                        budget.getCategory().getId(),
                        budget.getPeriod()
                );

        if (spent == null) spent = BigDecimal.ZERO;

        return BudgetResponseDTO.builder()
                .id(budget.getId())
                .categoryName(budget.getCategory().getName())
                .limitAmount(budget.getLimitAmount())
                .period(budget.getPeriod())
                .spentAmount(spent)
                .build();

    }).toList();
}
@Override
public boolean checkBudgetExceeded(String email, Long categoryId, BigDecimal amount) {

    log.debug("Checking budget exceed | user={} | categoryId={} | amount={}",
            email, categoryId, amount);

    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
        log.warn("Invalid amount for budget check | amount={}", amount);
        throw new BadRequestException("Amount must be greater than zero");
    }

    User user = getUserByEmail(email);
    String period = getCurrentPeriod();

    Budget budget = budgetRepository
            .findByUserIdAndCategoryIdAndPeriod(user.getId(), categoryId, period)
            .orElse(null);

    if (budget == null) {
        log.info("No budget set | user={} | categoryId={}", email, categoryId);
        return false;
    }

    BigDecimal totalSpent = transactionRepository
            .getTotalExpenseByCategoryAndPeriod(user.getId(), categoryId, period);

    if (totalSpent == null) totalSpent = BigDecimal.ZERO;

    boolean exceeded = totalSpent.add(amount)
            .compareTo(budget.getLimitAmount()) > 0;

    if (exceeded) {
        log.warn("Budget exceeded | user={} | categoryId={} | limit={} | spent={}",
                email, categoryId, budget.getLimitAmount(), totalSpent);
    }

    return exceeded;
}

    // =========================
    // 🔥 PRIVATE HELPER METHODS
    // =========================

private User getUserByEmail(String email) {

    return userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.error("User not found | email={}", email);
                return new ResourceNotFoundException("User not found");
            });
}

    private Category getValidCategory(Long categoryId, User user) {

        return categoryRepository.findById(categoryId)
                .filter(cat -> cat.getUser().getId().equals(user.getId()))
                .filter(cat -> cat.getType() == CategoryType.EXPENSE)
                .orElseThrow(() -> new BadRequestException("Invalid category or not allowed"));
    }
 @Override
public List<AlertDTO> getBudgetAlerts(String email) {

    log.debug("Generating budget alerts | user={}", email);

    User user = getUserByEmail(email);

    List<Budget> budgets = budgetRepository.findByUserId(user.getId());

    List<AlertDTO> alerts = new ArrayList<>();

    for (Budget budget : budgets) {

        BigDecimal spent = transactionRepository
                .getTotalExpenseByCategoryAndPeriod(
                        user.getId(),
                        budget.getCategory().getId(),
                        budget.getPeriod()
                );

        if (spent == null) spent = BigDecimal.ZERO;

        BigDecimal percentage = spent
                .multiply(BigDecimal.valueOf(100))
                .divide(budget.getLimitAmount(), 2, RoundingMode.HALF_UP);

        if (percentage.compareTo(BigDecimal.valueOf(100)) > 0) {

            log.warn("Budget exceeded alert | category={} | percentage={}",
                    budget.getCategory().getName(), percentage);

            alerts.add(new AlertDTO(
                    "EXCEEDED",
                    "🚨 Budget exceeded for " + budget.getCategory().getName(),
                    budget.getCategory().getName(),
                    percentage
            ));

        } else if (percentage.compareTo(BigDecimal.valueOf(80)) > 0) {

            log.info("Budget warning alert | category={} | percentage={}",
                    budget.getCategory().getName(), percentage);

            alerts.add(new AlertDTO(
                    "WARNING",
                    "⚠️ You used " + percentage + "% of " + budget.getCategory().getName(),
                    budget.getCategory().getName(),
                    percentage
            ));
        }
    }

    log.info("Budget alerts generated | count={}", alerts.size());

    return alerts;
}
    private void validateBudgetRequest(BudgetRequestDTO request) {

        if (request.getCategoryId() == null) {
            throw new BadRequestException("Category ID is required");
        }

        if (request.getLimitAmount() == null ||
                request.getLimitAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Budget amount must be greater than zero");
        }

        validatePeriod(request.getPeriod());
    }

    private void validatePeriod(String period) {
        if (period == null || !period.matches("\\d{4}-\\d{2}")) {
            throw new BadRequestException("Invalid period format. Expected YYYY-MM");
        }
    }

    private String getCurrentPeriod() {
        LocalDate now = LocalDate.now();
        return now.getYear() + "-" + String.format("%02d", now.getMonthValue());
    }
}
