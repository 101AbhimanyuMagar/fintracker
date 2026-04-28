package com.fintracker_backend.fintracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CategoryExpenseDTO {
    private String categoryName;
    private BigDecimal total;
}