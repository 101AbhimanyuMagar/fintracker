package com.fintracker_backend.fintracker.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class BudgetRequestDTO {
    private Long categoryId;
    private BigDecimal limitAmount;
    private String period; // "2026-04"
}
