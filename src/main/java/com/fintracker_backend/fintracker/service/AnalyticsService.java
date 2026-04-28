package com.fintracker_backend.fintracker.service;

import com.fintracker_backend.fintracker.dto.*;
import com.fintracker_backend.fintracker.entity.User;
import com.fintracker_backend.fintracker.exception.ResourceNotFoundException;
import com.fintracker_backend.fintracker.repository.TransactionRepository;
import com.fintracker_backend.fintracker.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    // ==============================
    // 📊 Monthly Expense Trend
    // ==============================
public ChartDTO getMonthlyChart(String email) {

    log.debug("Fetching monthly chart | user={}", email);

    User user = getUser(email);

    List<Object[]> data =
            transactionRepository.getMonthlyExpenseTrend(user.getId());

    log.info("Monthly chart data fetched | records={}", data.size());

    return mapToChart(data);
}

    // ==============================
    // 🧾 Category-wise Expense
    // ==============================
    public ChartDTO getCategoryChart(String email) {

    User user = getUser(email);

    List<Object[]> data =
            transactionRepository.getCategoryExpense(user.getId());

    List<String> labels = new ArrayList<>();
    List<BigDecimal> values = new ArrayList<>();

    for (Object[] row : data) {
        labels.add((String) row[0]);
        values.add((BigDecimal) row[1]);
    }

    return new ChartDTO(labels, values);
}
public ChartDTO getDailyChart(String email, int year, int month) {

    User user = getUser(email);

    List<Object[]> data =
            transactionRepository.getDailyExpense(user.getId(), year, month);

    return mapToChart(data);
}
public ChartDTO getTopCategories(String email, int year, int month) {

    User user = getUser(email);

    List<Object[]> data =
            transactionRepository.getTopCategories(user.getId(), year, month);

    return mapToChart(data);
}
private ChartDTO mapToChart(List<Object[]> data) {

    List<String> labels = new ArrayList<>();
    List<BigDecimal> values = new ArrayList<>();

    for (Object[] row : data) {
        labels.add(row[0].toString());
        values.add((BigDecimal) row[1]);
    }

    return new ChartDTO(labels, values);
}
public SummaryDTO getSummary(String email) {

    log.debug("Generating summary | user={}", email);

    IncomeExpenseDTO data = getIncomeVsExpense(email);

    BigDecimal income = data.getTotalIncome();
    BigDecimal expense = data.getTotalExpense();

    BigDecimal savings = income.subtract(expense);

    log.info("Summary calculated | income={} | expense={} | savings={}",
            income, expense, savings);

    return new SummaryDTO(income, expense, savings);
}
    // ==============================
    // 📈 Income vs Expense
    // ==============================
    public IncomeExpenseDTO getIncomeVsExpense(String email) {

        User user = getUser(email);

        List<Object[]> data =
                transactionRepository.getIncomeVsExpense(user.getId());

        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;

        for (Object[] row : data) {
            String type = row[0].toString();
            BigDecimal total = (BigDecimal) row[1];

            if ("INCOME".equals(type)) {
                income = total;
            } else {
                expense = total;
            }
        }

        return new IncomeExpenseDTO(income, expense);
    }

    // ==============================
    // 💰 Savings
    // ==============================
    public BigDecimal getSavings(String email) {

        IncomeExpenseDTO data = getIncomeVsExpense(email);

        return data.getTotalIncome().subtract(data.getTotalExpense());
    }

    // ==============================
    // 🔐 Helper
    // ==============================
private User getUser(String email) {

    return userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.error("User not found | email={}", email);
                return new ResourceNotFoundException("User not found");
            });
}
public ChartDTO getAccountWiseExpense(String email) {

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    List<Object[]> data =
            transactionRepository.getExpenseByAccount(user.getId());

    List<String> labels = new ArrayList<>();
    List<BigDecimal> values = new ArrayList<>();

    for (Object[] row : data) {
        labels.add((String) row[0]);
        values.add((BigDecimal) row[1]);
    }

    return new ChartDTO(labels, values);
}
    
}
