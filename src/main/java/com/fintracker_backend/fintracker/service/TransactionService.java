package com.fintracker_backend.fintracker.service;

import java.util.List;

import com.fintracker_backend.fintracker.dto.TransactionRequestDTO;
import com.fintracker_backend.fintracker.entity.Transaction;

public interface TransactionService {
    void addTransaction(TransactionRequestDTO request, String email);
    List<Transaction> getUserTransactions(String email);
    List<Transaction> getTransactionsByAccount(Long accountId, String email);
    void deleteTransaction(Long id, String email);
}
