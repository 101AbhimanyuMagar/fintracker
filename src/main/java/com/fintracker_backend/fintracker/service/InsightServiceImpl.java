package com.fintracker_backend.fintracker.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.fintracker_backend.fintracker.dto.InsightDTO;
import com.fintracker_backend.fintracker.entity.User;
import com.fintracker_backend.fintracker.repository.TransactionRepository;
import com.fintracker_backend.fintracker.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsightServiceImpl implements InsightService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AIService aiService;

    @Override
    @Cacheable(value = "insights", key = "#email + '-' + T(java.time.LocalDate).now().toString().substring(0,7)")
    public List<InsightDTO> generateInsights(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found | email={}", email);
                    return new RuntimeException("User not found");
                });

        String currentMonth = getCurrentMonth();

        BigDecimal expense = transactionRepository.getTotalExpenseByMonth(user.getId(), currentMonth);
        BigDecimal income  = transactionRepository.getTotalIncomeByMonth(user.getId(), currentMonth);

        if (expense == null) expense = BigDecimal.ZERO;
        if (income  == null) income  = BigDecimal.ZERO;

        BigDecimal savings = income.subtract(expense);

        // ✅ Use existing getCategoryWiseExpense
        List<Object[]> categoryData = transactionRepository
                .getCategoryWiseExpense(user.getId(), currentMonth);

        StringBuilder categoryBreakdown = new StringBuilder();
        for (Object[] row : categoryData) {
            categoryBreakdown
                .append("  - ")
                .append(row[0])
                .append(": ₹")
                .append(row[1])
                .append("\n");
        }

        String prompt = """
                You are a personal financial advisor. Analyze the user's finances and give exactly 3 short, actionable insights.
                Format each insight as a single sentence starting with a number (1., 2., 3.).
                Do not add any extra text, headers, or explanation outside the 3 lines.

                Current Month : %s
                Total Income  : ₹%s
                Total Expense : ₹%s
                Savings       : ₹%s

                Expense Breakdown by Category:
                %s
                """.formatted(
                        currentMonth,
                        income,
                        expense,
                        savings,
                        categoryBreakdown.isEmpty() ? "  No category data available." : categoryBreakdown
                );

        String aiResponse = aiService.getAIResponse(prompt);

        // Parse lines starting with 1. 2. 3.
        List<InsightDTO> insights = Arrays.stream(aiResponse.split("\n"))
                .map(String::trim)
                .filter(line -> line.matches("^[1-3]\\..*"))
                .map(InsightDTO::new)
                .collect(Collectors.toList());

        // Fallback if Gemini didn't follow format
        if (insights.isEmpty()) {
            insights = Arrays.stream(aiResponse.split("\n"))
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .map(InsightDTO::new)
                    .collect(Collectors.toList());
        }

        return insights;
    }

    private String getCurrentMonth() {
        LocalDate now = LocalDate.now();
        return now.getYear() + "-" + String.format("%02d", now.getMonthValue());
    }
}