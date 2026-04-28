package com.fintracker_backend.fintracker.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SummaryDTO {
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal savings;
}