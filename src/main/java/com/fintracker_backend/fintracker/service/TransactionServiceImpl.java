package com.fintracker_backend.fintracker.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fintracker_backend.fintracker.dto.*;
import com.fintracker_backend.fintracker.entity.*;
import com.fintracker_backend.fintracker.exception.AccessDeniedException;
import com.fintracker_backend.fintracker.exception.InsufficientBalanceException;
import com.fintracker_backend.fintracker.exception.ResourceNotFoundException;
import com.fintracker_backend.fintracker.repository.*;

import jakarta.transaction.Transactional;
import lombok.*;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;

    @Override
    @Transactional
    public void addTransaction(TransactionRequestDTO request, Long userId) {

        // 1. Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Fetch account (SECURE)
        Account account = accountRepository.findById(request.getAccountId())
                .filter(acc -> acc.getUser().getId().equals(userId))
                .orElseThrow(() -> new AccessDeniedException("Account not found or unauthorized"));

        // 3. Fetch category (SECURE)
        Category category = categoryRepository.findById(request.getCategoryId())
                .filter(cat -> cat.getUser().getId().equals(userId))
                .orElseThrow(() -> new AccessDeniedException("Category not found or unauthorized"));

        // 4. Update balance 💰
        if (request.getType() == TransactionType.EXPENSE) {

            if (account.getBalance().compareTo(request.getAmount()) < 0) {
    throw new InsufficientBalanceException("Insufficient balance");
}

            account.setBalance(account.getBalance().subtract(request.getAmount()));

        } else {
            account.setBalance(account.getBalance().add(request.getAmount()));
        }

        accountRepository.save(account);

        // 5. Generate transaction reference 🔥
        String txnRef = "TXN-" + System.currentTimeMillis();

        // 6. Create transaction
        Transaction transaction = Transaction.builder()
                .user(user)
                .account(account)
                .category(category)
                .amount(request.getAmount())
                .type(request.getType())
                .paymentMethod(request.getPaymentMethod())
                .status(TransactionStatus.SUCCESS)
                .transactionRef(txnRef)
                .note(request.getNote())
                .createdAt(
                        request.getTransactionDate() != null
                                ? request.getTransactionDate()
                                : LocalDateTime.now()
                )
                .build();

        transactionRepository.save(transaction);

        // 7. Budget validation (ONLY for EXPENSE)
        if (request.getType() == TransactionType.EXPENSE) {
            checkBudget(userId, category.getId());
        }
    }

    // ================================
    // 📄 GET USER TRANSACTIONS
    // ================================
    @Override
    public List<Transaction> getUserTransactions(Long userId) {
        return transactionRepository.findByUserId(userId);
    }

    // ================================
    // 🏦 GET BY ACCOUNT
    // ================================
    @Override
    public List<Transaction> getTransactionsByAccount(Long accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    // ================================
    // ❌ DELETE TRANSACTION
    // ================================
    @Override
    @Transactional
    public void deleteTransaction(Long id, Long userId) {

        Transaction txn = transactionRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        // Security check
        if (!txn.getUser().getId().equals(userId)) {
    throw new AccessDeniedException("Unauthorized access");
}

        Account account = txn.getAccount();

        // Reverse balance
        if (txn.getType() == TransactionType.EXPENSE) {
            account.setBalance(account.getBalance().add(txn.getAmount()));
        } else {
            account.setBalance(account.getBalance().subtract(txn.getAmount()));
        }

        accountRepository.save(account);
        transactionRepository.delete(txn);
    }

    // ================================
    // 🔥 BUDGET LOGIC
    // ================================
    private void checkBudget(Long userId, Long categoryId) {

    String period = getCurrentPeriod(); // 🔥 central logic

    Optional<Budget> budgetOpt =
            budgetRepository.findByUserIdAndCategoryIdAndPeriod(
                    userId, categoryId, period);

    if (budgetOpt.isPresent()) {

        Budget budget = budgetOpt.get();

        BigDecimal totalSpent =
                transactionRepository.getTotalExpenseByCategoryAndPeriod(
                        userId, categoryId, period);

        if (totalSpent == null) totalSpent = BigDecimal.ZERO;

        if (totalSpent.compareTo(budget.getLimitAmount()) > 0) {
            System.out.println("⚠️ Budget exceeded for category!");
        }
    }
}
private String getCurrentPeriod() {
    LocalDate now = LocalDate.now();
    return now.getYear() + "-" + String.format("%02d", now.getMonthValue());
}
}