package com.fintracker_backend.fintracker.service;

import java.util.List;

import com.fintracker_backend.fintracker.dto.TransactionRequestDTO;
import com.fintracker_backend.fintracker.entity.Transaction;

public interface TransactionService {
    void addTransaction(TransactionRequestDTO request, Long userId);
    List<Transaction> getUserTransactions(Long userId);
    List<Transaction> getTransactionsByAccount(Long accountId);
    void deleteTransaction(Long id, Long userId);
}
