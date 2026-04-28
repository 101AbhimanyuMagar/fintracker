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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;

    // ================================
    // ➕ ADD TRANSACTION
    // ================================
 @Override
@Transactional
public void addTransaction(TransactionRequestDTO request, String email) {

    log.info("Add transaction started | user={} | accountId={} | amount={} | type={}",
            email, request.getAccountId(), request.getAmount(), request.getType());

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.error("User not found | email={}", email);
                return new ResourceNotFoundException("User not found");
            });

    Account account = accountRepository.findById(request.getAccountId())
            .filter(acc -> acc.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> {
                log.warn("Unauthorized account access | accountId={} | user={}",
                        request.getAccountId(), email);
                return new AccessDeniedException("Account not found or unauthorized");
            });

    Category category = categoryRepository.findById(request.getCategoryId())
            .filter(cat -> cat.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> {
                log.warn("Unauthorized category access | categoryId={} | user={}",
                        request.getCategoryId(), email);
                return new AccessDeniedException("Category not found or unauthorized");
            });

    // 💰 Balance update
    if (request.getType() == TransactionType.EXPENSE) {

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            log.warn("Insufficient balance | accountId={} | balance={} | requested={}",
                    account.getId(), account.getBalance(), request.getAmount());
            throw new InsufficientBalanceException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));

    } else {
        account.setBalance(account.getBalance().add(request.getAmount()));
    }

    accountRepository.save(account);

    String txnRef = "TXN-" + System.currentTimeMillis();

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

    log.info("Transaction successful | txnRef={} | accountId={} | amount={}",
            txnRef, account.getId(), request.getAmount());

    if (request.getType() == TransactionType.EXPENSE) {
        checkBudget(user.getId(), category.getId());
    }
}
    // ================================
    // 📄 GET USER TRANSACTIONS
    // ================================
@Override
public List<Transaction> getUserTransactions(String email) {

    log.debug("Fetching transactions | user={}", email);

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.error("User not found | email={}", email);
                return new ResourceNotFoundException("User not found");
            });

    List<Transaction> txns = transactionRepository.findByUserId(user.getId());

    log.info("Transactions fetched | count={} | user={}", txns.size(), email);

    return txns;
}

    // ================================
    // 🏦 GET BY ACCOUNT
    // ================================
    @Override
    public List<Transaction> getTransactionsByAccount(Long accountId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 🔐 Ensure account belongs to user
        accountRepository.findById(accountId)
                .filter(acc -> acc.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new AccessDeniedException("Unauthorized"));

        return transactionRepository.findByAccountId(accountId);
    }

    // ================================
    // ❌ DELETE TRANSACTION
    // ================================
@Override
@Transactional
public void deleteTransaction(Long id, String email) {

    log.info("Delete transaction requested | txnId={} | user={}", id, email);

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.error("User not found | email={}", email);
                return new ResourceNotFoundException("User not found");
            });

    Transaction txn = transactionRepository.findById(id)
            .orElseThrow(() -> {
                log.error("Transaction not found | txnId={}", id);
                return new ResourceNotFoundException("Transaction not found");
            });

    if (!txn.getUser().getId().equals(user.getId())) {
        log.warn("Unauthorized delete attempt | txnId={} | user={}", id, email);
        throw new AccessDeniedException("Unauthorized access");
    }

    Account account = txn.getAccount();

    if (txn.getType() == TransactionType.EXPENSE) {
        account.setBalance(account.getBalance().add(txn.getAmount()));
    } else {
        account.setBalance(account.getBalance().subtract(txn.getAmount()));
    }

    accountRepository.save(account);
    transactionRepository.delete(txn);

    log.info("Transaction deleted | txnId={} | balanceUpdatedAccountId={}",
            id, account.getId());
}

    // ================================
    // 🔥 BUDGET LOGIC
    // ================================
    private void checkBudget(Long userId, Long categoryId) {

    String period = getCurrentPeriod();

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
            log.warn("Budget exceeded | userId={} | categoryId={} | spent={} | limit={}",
                    userId, categoryId, totalSpent, budget.getLimitAmount());
        }
    }
}

    private String getCurrentPeriod() {
        LocalDate now = LocalDate.now();
        return now.getYear() + "-" + String.format("%02d", now.getMonthValue());
    }
}