package com.fintracker_backend.fintracker.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

import com.fintracker_backend.fintracker.dto.TransactionRequestDTO;
import com.fintracker_backend.fintracker.entity.Transaction;
import com.fintracker_backend.fintracker.service.TransactionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // ➕ Add Transaction
    @PostMapping
    public ResponseEntity<String> addTransaction(
            @RequestBody TransactionRequestDTO request,
            Authentication authentication
    ) {
        String email = authentication.getName();

        transactionService.addTransaction(request, email);

        return ResponseEntity.ok("Transaction added successfully");
    }

    // 📄 Get All Transactions
    @GetMapping
    public ResponseEntity<List<Transaction>> getUserTransactions(
            Authentication authentication
    ) {
        String email = authentication.getName();

        return ResponseEntity.ok(
                transactionService.getUserTransactions(email)
        );
    }

    // 🏦 Get by Account
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Transaction>> getByAccount(
            @PathVariable Long accountId,
            Authentication authentication
    ) {
        String email = authentication.getName();

        return ResponseEntity.ok(
                transactionService.getTransactionsByAccount(accountId, email)
        );
    }

    // ❌ Delete Transaction
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTransaction(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();

        transactionService.deleteTransaction(id, email);

        return ResponseEntity.ok("Transaction deleted successfully");
    }
}