package com.fintracker_backend.fintracker.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlertDTO {
    private String type;        // WARNING / EXCEEDED / SAFE
    private String message;
    private String category;
    private BigDecimal percentage;
}