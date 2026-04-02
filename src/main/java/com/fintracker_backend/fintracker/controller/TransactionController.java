package com.fintracker_backend.fintracker.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
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
            @RequestParam Long userId // 🔥 TEMP (later JWT)
    ) {
        transactionService.addTransaction(request, userId);
        return ResponseEntity.ok("Transaction added successfully");
    }

    // 📄 Get All Transactions
    @GetMapping
    public ResponseEntity<List<Transaction>> getUserTransactions(
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(
                transactionService.getUserTransactions(userId)
        );
    }

    // 🏦 Get by Account
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Transaction>> getByAccount(
            @PathVariable Long accountId
    ) {
        return ResponseEntity.ok(
                transactionService.getTransactionsByAccount(accountId)
        );
    }

    // ❌ Delete Transaction
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTransaction(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        transactionService.deleteTransaction(id, userId);
        return ResponseEntity.ok("Transaction deleted successfully");
    }
}
