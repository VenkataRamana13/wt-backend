package com.wtplatform.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class StpTrendDTO {
    private String month;
    private BigDecimal amount;

    // Constructor specifically for JPA projection query
    public StpTrendDTO(String month, BigDecimal amount) {
        this.month = month.toString();
        this.amount = amount;
    }
} 