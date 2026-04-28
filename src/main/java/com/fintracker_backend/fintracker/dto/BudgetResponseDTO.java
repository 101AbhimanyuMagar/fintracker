package com.fintracker_backend.fintracker.dto;

import java.math.BigDecimal;

import lombok.*;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BudgetResponseDTO {

    private Long id;
    private String categoryName;
    private BigDecimal limitAmount;
    private String period;

    private BigDecimal spentAmount;   // 🔥 calculated
}
