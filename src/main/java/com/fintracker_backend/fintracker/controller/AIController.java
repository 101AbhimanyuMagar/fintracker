package com.fintracker_backend.fintracker.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.fintracker_backend.fintracker.entity.User;
import com.fintracker_backend.fintracker.repository.TransactionRepository;
import com.fintracker_backend.fintracker.repository.UserRepository;
import com.fintracker_backend.fintracker.service.AIService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    // ✅ Single combined endpoint — replaces 4 separate calls
    @Cacheable(
  value = "dashboardAI",
  key = "#auth.name + '-' + T(java.time.LocalDate).now().toString().substring(0,7)"
)
@GetMapping("/dashboard-ai")
public ResponseEntity<Map<String, Object>> dashboardAI(Authentication auth) {

        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        String currentMonth = getCurrentMonth();

        BigDecimal income  = orZero(transactionRepository.getTotalIncomeByMonth(user.getId(), currentMonth));
        BigDecimal expense = orZero(transactionRepository.getTotalExpenseByMonth(user.getId(), currentMonth));
        BigDecimal savings = income.subtract(expense);

        List<Object[]> cats = transactionRepository.getCategoryWiseExpense(user.getId(), currentMonth);
        StringBuilder catStr = new StringBuilder();
        for (Object[] row : cats) {
            catStr.append(row[0]).append(": ₹").append(row[1]).append("\n");
        }

        List<Object[]> trend = transactionRepository.getMonthlyExpenseTrend(user.getId());
        StringBuilder trendStr = new StringBuilder();
        for (Object[] row : trend) {
            trendStr.append(row[0]).append(": ₹").append(row[1]).append("\n");
        }

        String prompt = """
                You are a personal financial advisor AI. Analyze the user's finances and respond in this EXACT format with these exact section headers:

                INSIGHTS:
                1. <insight one>
                2. <insight two>
                3. <insight three>

                PREDICTION:
                PREDICTION: ₹<amount>
                REASON: <one sentence>

                ALERTS:
                ⚠️ <alert one>
                ⚠️ <alert two>

                BUDGETS:
                <Category>: ₹<amount>
                <Category>: ₹<amount>

                User Data:
                Month: %s
                Income: ₹%s
                Expense: ₹%s
                Savings: ₹%s

                Category Breakdown:
                %s

                Monthly Trend:
                %s
                """.formatted(
                        currentMonth, income, expense, savings,
                        catStr.isEmpty() ? "No data" : catStr,
                        trendStr.isEmpty() ? "No data" : trendStr
                );

        String aiResponse = aiService.getAIResponse(prompt);

        Map<String, Object> result = new HashMap<>();
        result.put("insights",   extractSection(aiResponse, "INSIGHTS:",   "PREDICTION:"));
        result.put("prediction", extractSection(aiResponse, "PREDICTION:", "ALERTS:"));
        result.put("alerts",     extractSection(aiResponse, "ALERTS:",     "BUDGETS:"));
        result.put("budgets",    extractSection(aiResponse, "BUDGETS:",    null));

        return ResponseEntity.ok(result);
    }

    // 💬 Chat — kept separate, only called on user action (not on page load)
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(
            @RequestBody Map<String, String> body,
            Authentication auth) {

        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        String currentMonth = getCurrentMonth();

        BigDecimal income  = orZero(transactionRepository.getTotalIncomeByMonth(user.getId(), currentMonth));
        BigDecimal expense = orZero(transactionRepository.getTotalExpenseByMonth(user.getId(), currentMonth));

        String context = "Income: ₹%s, Expense: ₹%s, Savings: ₹%s this month."
                .formatted(income, expense, income.subtract(expense));

        String reply = aiService.chatWithAI(body.get("message"), context);
        return ResponseEntity.ok(Map.of("reply", reply));
    }

    private String extractSection(String text, String startMarker, String endMarker) {
        int start = text.indexOf(startMarker);
        if (start == -1) return "";
        start += startMarker.length();
        int end = endMarker != null ? text.indexOf(endMarker, start) : text.length();
        if (end == -1) end = text.length();
        return text.substring(start, end).trim();
    }

    private String getCurrentMonth() {
        LocalDate now = LocalDate.now();
        return now.getYear() + "-" + String.format("%02d", now.getMonthValue());
    }

    private BigDecimal orZero(BigDecimal val) {
        return val == null ? BigDecimal.ZERO : val;
    }
}