package com.fintracker_backend.fintracker.dto;

import java.math.BigDecimal;

import com.fintracker_backend.fintracker.entity.PaymentMethod;
import com.fintracker_backend.fintracker.entity.TransactionType;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionRequestDTO {

    private Long accountId;

    private Long categoryId;

    private BigDecimal amount;

    private TransactionType type;

    private PaymentMethod paymentMethod;

    private String note;

    private LocalDateTime transactionDate;
}