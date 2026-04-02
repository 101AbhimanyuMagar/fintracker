package com.fintracker_backend.fintracker.dto;

import java.math.BigDecimal;

import com.fintracker_backend.fintracker.entity.AccountType;

import lombok.Data;

@Data
public class AccountRequestDTO {
    private String name;
    private AccountType type;
    private BigDecimal initialBalance;
}
