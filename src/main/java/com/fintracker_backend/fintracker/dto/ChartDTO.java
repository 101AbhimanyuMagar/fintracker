package com.fintracker_backend.fintracker.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChartDTO {
    private List<String> labels;
    private List<BigDecimal> data;
}
