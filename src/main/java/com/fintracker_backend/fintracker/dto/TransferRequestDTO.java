package com.fintracker_backend.fintracker.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data 
public class TransferRequestDTO {
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
    private String note;
}
